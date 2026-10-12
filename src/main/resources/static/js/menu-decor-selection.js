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
        editable = booking.status === 'PENDING' && !booking.submitted;        addons = await MDSel.api('/api/addons?eventType=' + encodeURIComponent(booking.eventType));
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

async function initDecorPage(bookingId) {
    const base = '/api/bookings/' + bookingId;
    const groups = document.getElementById('decor-groups');
    let booking, options, selected;
    let editable = true;

    async function loadAll() {
        booking = await MDSel.api(base);
        editable = booking.status === 'PENDING' && !booking.submitted;
        options = await MDSel.api('/api/decor-options');
        selected = await MDSel.api(base + '/selections/decor');
        const est = await MDSel.api(base + '/selections/estimate');
        document.getElementById('est-decor').textContent = MDSel.money(est.decorTotal);
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

    function removeButton(decorId, name) {
        const btn = MDSel.el('button', 'Remove', 'btn-danger');
        btn.type = 'button';
        btn.disabled = !editable;
        btn.addEventListener('click', () => {
            act(() => MDSel.api(base + '/selections/decor/' + decorId, { method: 'DELETE' }), name + ' removed.');
        });
        return btn;
    }

    function render() {
        groups.innerHTML = '';
        const wedding = booking.eventType === 'WEDDING';
        const categories = [];
        options.forEach(o => { if (!categories.includes(o.category)) categories.push(o.category); });

        categories.forEach(cat => {
            const items = options.filter(o => o.category === cat);
            const singular = items.some(o => o.isSingular);
            const group = MDSel.el('div', undefined, 'choice-group');
            group.appendChild(MDSel.el('h3', cat));
            if (singular) group.appendChild(MDSel.el('div', 'Choose one', 'pending-note'));

            items.forEach(o => {
                const line = selected.find(s => s.itemId === o.decorId);
                const row = MDSel.el('div', undefined, 'choice-row');

                const label = document.createElement('label');
                const input = document.createElement('input');
                input.type = singular ? 'radio' : 'checkbox';
                input.name = 'decor-' + cat;
                input.checked = !!line;
                input.disabled = !editable;
                input.addEventListener('change', () => {
                    if (input.checked) {
                        act(() => MDSel.api(base + '/selections/decor/' + o.decorId, { method: 'POST' }), o.name + ' chosen.');
                    } else {
                        act(() => MDSel.api(base + '/selections/decor/' + o.decorId, { method: 'DELETE' }), o.name + ' removed.');
                    }
                });
                label.appendChild(input);
                label.appendChild(document.createTextNode(' ' + o.name));
                row.appendChild(label);

                row.appendChild(MDSel.el('span', wedding ? 'Included' : MDSel.money(o.price)));
                row.appendChild(MDSel.el('span',
                    line ? 'Quantity ' + line.quantity + ' - ' + MDSel.money(line.lineTotal) : ''));
                group.appendChild(row);
            });

            const chosen = items.map(o => selected.find(s => s.itemId === o.decorId)).find(Boolean);
            if (singular && chosen) {
                const clear = MDSel.el('button', 'Clear choice', 'btn-secondary');
                clear.type = 'button';
                clear.disabled = !editable;
                clear.addEventListener('click', () => {
                    act(() => MDSel.api(base + '/selections/decor/' + chosen.itemId, { method: 'DELETE' }),
                        chosen.name + ' removed.');
                });
                group.appendChild(clear);
            }
            groups.appendChild(group);
        });

        // Chosen decor that was disabled later: not in the catalog list, but still on the booking.
        const orphans = selected.filter(s => !options.some(o => o.decorId === s.itemId));
        if (orphans.length > 0) {
            const group = MDSel.el('div', undefined, 'choice-group');
            group.appendChild(MDSel.el('h3', 'No longer available'));
            orphans.forEach(s => {
                const row = MDSel.el('div', undefined, 'choice-row');
                row.appendChild(MDSel.el('span', s.name));
                row.appendChild(MDSel.el('span', 'Quantity ' + s.quantity + ' - ' + MDSel.money(s.lineTotal)));
                row.appendChild(removeButton(s.itemId, s.name));
                group.appendChild(row);
            });
            groups.appendChild(group);
        }
    }

    try { await loadAll(); } catch (e) { MDSel.message(e.message, 'error'); }
}

async function initSummaryPage(bookingId) {
    const base = '/api/bookings/' + bookingId;

    function fillLines(tbodyId, lines) {
        const tbody = document.getElementById(tbodyId);
        tbody.innerHTML = '';
        if (lines.length === 0) {
            const tr = document.createElement('tr');
            const td = MDSel.el('td', 'Nothing chosen');
            td.colSpan = 4;
            tr.appendChild(td);
            tbody.appendChild(tr);
            return;
        }
        lines.forEach(l => {
            const tr = document.createElement('tr');
            tr.appendChild(MDSel.el('td', l.name));
            tr.appendChild(MDSel.el('td', String(l.quantity)));
            tr.appendChild(MDSel.el('td', MDSel.money(l.chargedAmount)));
            tr.appendChild(MDSel.el('td', MDSel.money(l.lineTotal)));
            tbody.appendChild(tr);
        });
    }

    function addEstimateRow(tbody, label, value, bold) {
        const tr = document.createElement('tr');
        tr.appendChild(MDSel.el('td', label, bold ? 'price' : undefined));
        tr.appendChild(MDSel.el('td', MDSel.money(value), bold ? 'price' : undefined));
        tbody.appendChild(tr);
    }

    async function loadAll() {
        const booking = await MDSel.api(base);
        const editable = booking.status === 'PENDING' && !booking.submitted;
        const wedding = booking.eventType === 'WEDDING';

        let choice = 'Not chosen yet';
        const choiceId = wedding ? booking.packageId : booking.menuId;
        if (choiceId) {
            try {
                const item = await MDSel.api((wedding ? '/api/packages/' : '/api/menus/') + choiceId);
                choice = item.name;
            } catch (e) {
                choice = '#' + choiceId;
            }
        }

        const addons = await MDSel.api(base + '/selections/addons');
        const decor = await MDSel.api(base + '/selections/decor');
        const est = await MDSel.api(base + '/selections/estimate');

        const details = document.getElementById('sum-details');
        details.innerHTML = '';
        const lines = [
            ['Booking', '#' + booking.id],
            ['Customer', booking.customerName],
            ['Event date', booking.eventDate],
            ['Event type', booking.eventType],
            ['Guests', booking.guestCount],
            [wedding ? 'Package' : 'Menu', choice],
            ['Status', booking.submitted ? 'Submitted - waiting for the hall administrator' : booking.status]
        ];
        lines.forEach(pair => {
            const div = MDSel.el('div', undefined, 'detail-line');
            div.appendChild(MDSel.el('strong', pair[0] + ': '));
            div.appendChild(document.createTextNode(String(pair[1])));
            details.appendChild(div);
        });

        fillLines('sum-addons', addons);
        fillLines('sum-decor', decor);

        const tbody = document.getElementById('sum-estimate');
        tbody.innerHTML = '';
        addEstimateRow(tbody, wedding ? 'Package price' : 'Menu price', est.basePrice, false);
        if (est.lowGuestSurcharge > 0) {
            addEstimateRow(tbody, 'Low guest charge', est.lowGuestSurcharge, false);
        }
        addEstimateRow(tbody, 'Add-ons', est.addOnTotal, false);
        addEstimateRow(tbody, 'Decor', est.decorTotal, false);
        addEstimateRow(tbody, 'Estimated total', est.total, true);

        const btn = document.getElementById('submit-btn');
        btn.disabled = !editable;
        btn.onclick = async () => {
            if (!confirm('Submit this booking request? You cannot change it afterwards.')) return;
            try {
                await MDSel.api(base + '/submit', { method: 'POST' });
                MDSel.message('Your booking request was submitted.', 'success');
            } catch (e) {
                MDSel.message(e.message, 'error');
            }
            try { await loadAll(); } catch (e) { MDSel.message(e.message, 'error'); }
        };
    }

    try { await loadAll(); } catch (e) { MDSel.message(e.message, 'error'); }
}

async function initChoicePage(bookingId, kind) {
    const isPackage = kind === 'package';
    const base = '/api/bookings/' + bookingId;
    const cards = document.getElementById('choice-cards');
    let booking, items;
    let tiers = {};
    let editable = true;

    async function loadAll() {
        booking = await MDSel.api(base);
        editable = booking.status === 'PENDING' && !booking.submitted;
        const wedding = booking.eventType === 'WEDDING';
        document.getElementById('booking-info').textContent =
            'Booking #' + booking.id + ' - ' + booking.eventType + ' - ' + booking.guestCount + ' guests' +
            (editable ? '' : ' (this booking can no longer be edited)');

        // Packages are for weddings only, menus for other events only.
        if (wedding !== isPackage) {
            cards.innerHTML = '';
            const p = MDSel.el('p', isPackage
                ? 'Packages are for weddings. This booking is not a wedding, so please choose a menu: '
                : 'Menus are for events that are not weddings. This booking is a wedding, so please choose a package: ');
            const a = MDSel.el('a', 'Go to the ' + (isPackage ? 'menu' : 'package') + ' page');
            a.href = '/booking/' + bookingId + '/select/' + (isPackage ? 'menu' : 'package');
            p.appendChild(a);
            cards.appendChild(p);
            return;
        }

        items = await MDSel.api(isPackage ? '/api/packages' : '/api/menus');
        tiers = {};
        if (isPackage) {
            await Promise.all(items.map(async p => {
                tiers[p.packageId] = await MDSel.api('/api/package-pricing-tiers/package/' + p.packageId);
            }));
        }

        const chosenId = isPackage ? booking.packageId : booking.menuId;
        let note = '';
        if (chosenId && !items.some(i => (isPackage ? i.packageId : i.menuId) === chosenId)) {
            try {
                const old = await MDSel.api((isPackage ? '/api/packages/' : '/api/menus/') + chosenId);
                note = 'Your current ' + kind + ' (' + old.name + ') is no longer available. Please choose another.';
            } catch (e) {
                note = 'Your current ' + kind + ' is no longer available. Please choose another.';
            }
        }
        document.getElementById('choice-note').textContent = note;

        try {
            const est = await MDSel.api(base + '/selections/estimate');
            document.getElementById('est-base').textContent = MDSel.money(est.basePrice);
            document.getElementById('est-total').textContent = MDSel.money(est.total);
            const row = document.getElementById('est-surcharge-row');
            row.style.display = est.lowGuestSurcharge > 0 ? 'block' : 'none';
            document.getElementById('est-surcharge').textContent = MDSel.money(est.lowGuestSurcharge);
        } catch (e) {
            document.getElementById('est-base').textContent = '-';
            document.getElementById('est-total').textContent = '-';
        }

        render(chosenId);
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

    function render(chosenId) {
        cards.innerHTML = '';
        items.forEach(item => {
            const id = isPackage ? item.packageId : item.menuId;
            const isChosen = chosenId === id;
            const card = MDSel.el('div', undefined, 'card' + (isChosen ? ' chosen' : ''));
            card.appendChild(MDSel.el('h3', item.name));

            let hasPrice = true;
            if (isPackage) {
                card.appendChild(MDSel.el('p', item.description || ''));
                const tier = (tiers[id] || []).find(t =>
                    booking.guestCount >= t.minGuests && booking.guestCount <= t.maxGuests);
                if (tier) {
                    card.appendChild(MDSel.el('p', MDSel.money(tier.totalPrice), 'price'));
                    card.appendChild(MDSel.el('div',
                        'For ' + tier.minGuests + ' - ' + tier.maxGuests + ' guests', 'pending-note'));
                } else {
                    hasPrice = false;
                    card.appendChild(MDSel.el('div',
                        'No price bracket for ' + booking.guestCount + ' guests', 'pending-note'));
                }
            } else {
                card.appendChild(MDSel.el('p', MDSel.money(item.pricePerGuest) + ' per guest', 'price'));
            }

            // RESERVED: dishes section goes here later. Not built yet.
            card.appendChild(MDSel.el('div', undefined, 'card-dishes'));

            const btn = MDSel.el('button', isChosen ? 'Chosen' : 'Choose this ' + kind, 'btn-select');
            btn.type = 'button';
            btn.disabled = !editable || isChosen || !hasPrice;
            btn.addEventListener('click', () => {
                act(() => MDSel.api(base + '/' + kind + '/' + id, { method: 'PUT' }), item.name + ' chosen.');
            });
            card.appendChild(btn);
            cards.appendChild(card);
        });
    }

    try { await loadAll(); } catch (e) { MDSel.message(e.message, 'error'); }
}

document.addEventListener('DOMContentLoaded', () => {
    const addonsPage = document.getElementById('addons-page');
    if (addonsPage) initAddOnsPage(addonsPage.dataset.bookingId);

    const decorPage = document.getElementById('decor-page');
    if (decorPage) initDecorPage(decorPage.dataset.bookingId);

    const summaryPage = document.getElementById('summary-page');
    if (summaryPage) initSummaryPage(summaryPage.dataset.bookingId);

    const choicePage = document.getElementById('choice-page');
    if (choicePage) initChoicePage(choicePage.dataset.bookingId, choicePage.dataset.kind);
});