// Shared helpers and the add-ons page logic for the customer selection pages.
// Prices always come from the API; nothing is calculated or hard-coded here.
const MDSel = (() => {
    function el(tag, text, cls) {
        const e = document.createElement(tag);
        if (text !== undefined) e.textContent = text;
        if (cls) e.className = cls;
        return e;
    }

    function money(n) {
        return 'Rs. ' + Number(n).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }

    function message(text, kind) {
        const box = document.getElementById('msg');
        if (!box) return;
        box.className = text ? (kind === 'error' ? 'sel-error' : 'sel-success') : '';
        box.textContent = text || '';
    }

    async function api(url, options) {
        const res = await fetch(url, options);
        if (res.status === 204) return null;
        let body = null;
        try { body = await res.json(); } catch (e) { /* no JSON body */ }
        if (!res.ok) {
            const err = new Error((body && body.error) || ('Request failed (' + res.status + ')'));
            err.status = res.status;
            throw err;
        }
        return body;
    }

    return { el, money, message, api };
})();

async function initAddOnsPage(bookingId) {
    const base = '/api/bookings/' + bookingId;
    const rows = document.getElementById('addon-rows');
    let booking, addons, selected;
    let editable = true;

    async function loadAll() {
        booking = await MDSel.api(base);
        editable = booking.status === 'PENDING';
        addons = await MDSel.api('/api/addons?eventType=' + encodeURIComponent(booking.eventType));
        selected = await MDSel.api(base + '/selections/addons');
        const est = await MDSel.api(base + '/selections/estimate');
        document.getElementById('est-addons').textContent = MDSel.money(est.addOnTotal);
        document.getElementById('est-total').textContent = MDSel.money(est.total);
        document.getElementById('booking-info').textContent =
            'Booking #' + booking.id + ' - ' + booking.eventType + ' - ' + booking.guestCount + ' guests' +
            (editable ? '' : ' (this booking can no longer be edited)');
        render();
    }

    async function act(fn, okText) {
        try {
            await fn();
            MDSel.message(okText, 'success');
        } catch (e) {
            MDSel.message(e.message, 'error');
            if (e.status === 409) { editable = false; }
        }
        try { await loadAll(); } catch (e) { MDSel.message(e.message, 'error'); }
    }

    function render() {
        rows.innerHTML = '';
        const wedding = booking.eventType === 'WEDDING';
        addons.forEach(a => {
            const line = selected.find(s => s.itemId === a.addonId);
            const tr = document.createElement('tr');

            tr.appendChild(MDSel.el('td', a.name));

            const priceTd = MDSel.el('td');
            if (wedding && a.includedInPackage) {
                priceTd.appendChild(MDSel.el('span', MDSel.money(0)));
                priceTd.appendChild(MDSel.el('div', 'Included in your package', 'included-note'));
            } else {
                priceTd.textContent = MDSel.money(a.price) + ' ' + a.unitType;
            }
            tr.appendChild(priceTd);

            const qtyTd = MDSel.el('td');
            const input = document.createElement('input');
            input.type = 'number';
            input.min = '1';
            input.className = 'qty';
            input.value = line ? line.quantity : (a.unitType === 'per guest' ? booking.guestCount : 1);
            input.disabled = !editable;
            qtyTd.appendChild(input);
            tr.appendChild(qtyTd);

            tr.appendChild(MDSel.el('td', line ? MDSel.money(line.lineTotal) : '-'));

            const actionTd = MDSel.el('td');
            const save = MDSel.el('button', line ? 'Update' : 'Add');
            save.type = 'button';
            save.disabled = !editable;
            save.addEventListener('click', () => {
                const q = parseInt(input.value, 10);
                const url = base + '/selections/addons/' + a.addonId + (q ? '?quantity=' + q : '');
                act(() => MDSel.api(url, { method: 'POST' }), a.name + ' saved.');
            });
            actionTd.appendChild(save);

            if (line) {
                const remove = MDSel.el('button', 'Remove', 'btn-danger');
                remove.type = 'button';
                remove.disabled = !editable;
                remove.style.marginLeft = '6px';
                remove.addEventListener('click', () => {
                    act(() => MDSel.api(base + '/selections/addons/' + a.addonId, { method: 'DELETE' }),
                        a.name + ' removed.');
                });
                actionTd.appendChild(remove);
            }
            tr.appendChild(actionTd);
            rows.appendChild(tr);
        });

        // Selected add-ons that were disabled later: not in the catalog list, but still on the booking.
        selected.filter(s => !addons.some(a => a.addonId === s.itemId)).forEach(s => {
            const tr = document.createElement('tr');
            const nameTd = MDSel.el('td', s.name);
            nameTd.appendChild(MDSel.el('div', 'No longer available - you can remove it', 'pending-note'));
            tr.appendChild(nameTd);
            tr.appendChild(MDSel.el('td', MDSel.money(s.chargedAmount)));
            tr.appendChild(MDSel.el('td', String(s.quantity)));
            tr.appendChild(MDSel.el('td', MDSel.money(s.lineTotal)));
            const actionTd = MDSel.el('td');
            const remove = MDSel.el('button', 'Remove', 'btn-danger');
            remove.type = 'button';
            remove.disabled = !editable;
            remove.addEventListener('click', () => {
                act(() => MDSel.api(base + '/selections/addons/' + s.itemId, { method: 'DELETE' }),
                    s.name + ' removed.');
            });
            actionTd.appendChild(remove);
            tr.appendChild(actionTd);
            rows.appendChild(tr);
        });
    }

    try { await loadAll(); } catch (e) { MDSel.message(e.message, 'error'); }
}

document.addEventListener('DOMContentLoaded', () => {
    const addonsPage = document.getElementById('addons-page');
    if (addonsPage) initAddOnsPage(addonsPage.dataset.bookingId);
});