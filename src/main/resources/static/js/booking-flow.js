function initBooking(cfg) {
    const $ = id => document.getElementById(id);
    let bookingId = null;

    $('eventDate').min = new Date(Date.now() + 86400000).toISOString().slice(0, 10);
    $('eventDate').onchange = () => { if (!bookingId) $('step2').classList.add('hidden'); };

    async function loadOptions(url) {
        try { const r = await api(url); return Array.isArray(r) ? r : null; } catch (e) { return null; }
    }
    const label = i => (i.name || ('#' + i.id))
        + (i.priceNote ? ' - ' + i.priceNote : (i.price != null ? ' - Rs. ' + i.price : ''));

    // ---------- package / menu list (needed in step 2) ----------
    async function fillSelection() {
        const sel = $('selection');
        const items = await loadOptions(cfg.optionsUrl);
        if (items && items.length) {
            sel.innerHTML = '<option value="">-- choose --</option>';
            items.forEach(i => {
                const o = document.createElement('option');
                o.value = i.id; o.textContent = label(i);
                sel.appendChild(o);
            });
        } else {  // list not available -> let the user type the ID
            const inp = document.createElement('input');
            inp.type = 'number'; inp.id = 'selection';
            inp.placeholder = 'Enter ' + cfg.selectionLabel + ' ID';
            sel.replaceWith(inp);
        }
    }
    fillSelection();

    // ---------- Step 1: check availability ----------
    $('checkBtn').onclick = async () => {
        const msg = $('msg1'); clearMsg(msg);
        const date = $('eventDate').value;
        if (!date) return showMsg(msg, 'Please choose a date.', 'error');
        try {
            const r = await api('/api/bookings/availability?date=' + date);
            if (r.available) {
                showMsg(msg, date + ' is available.', 'success');
                $('step2').classList.remove('hidden');
            } else {
                showMsg(msg, date + ' is already booked. Please choose another date.', 'error');
                $('step2').classList.add('hidden');
            }
        } catch (e) { showMsg(msg, e.message, 'error'); }
    };

    // ---------- Step 2: details + package/menu -> creates the Pending booking ----------
    $('createBtn').onclick = async () => {
        const msg = $('msg2'); clearMsg(msg);
        const name = $('name').value.trim();
        const phone = $('phone').value.trim();
        const guests = parseInt($('guests').value, 10);
        const selection = parseInt($('selection').value, 10);
        if (!name || !phone) return showMsg(msg, 'Name and phone are required.', 'error');
        if (isNaN(guests) || guests < cfg.minGuests || guests > cfg.maxGuests)
            return showMsg(msg, cfg.guestMsg, 'error');
        if (isNaN(selection)) return showMsg(msg, 'Please choose a ' + cfg.selectionLabel + '.', 'error');
        const body = {
            customerName: name,
            customerPhone: phone,
            customerEmail: $('email').value.trim() || null,
            eventDate: $('eventDate').value,
            eventType: cfg.eventType,
            guestCount: guests,
            selectionId: selection
        };
        try {
            const b = await api(cfg.createUrl, { method: 'POST', body: JSON.stringify(body) });
            bookingId = b.booking_id;
            ['eventDate', 'checkBtn', 'name', 'phone', 'email', 'guests', 'selection', 'createBtn']
                .forEach(id => $(id).disabled = true);
            showMsg(msg, 'Booking request #' + bookingId + ' started. You can now add extras below.', 'success');
            $('step3').classList.remove('hidden');
            fillChecks('addons', cfg.addonsUrl, cfg.hideIncludedAddons);
            fillChecks('decor', cfg.decorUrl, false);
        } catch (e) { showMsg(msg, e.message, 'error'); }
    };

    // ---------- Step 3: add-ons and decor with quantities ----------
    async function fillChecks(containerId, url, hideIncluded) {
        const box = $(containerId);
        let items = await loadOptions(url);
        box.innerHTML = '';
        if (items && hideIncluded) items = items.filter(i => !i.includedInPackage);
        if (items && items.length) {
            items.forEach(i => {
                const l = document.createElement('label');
                const c = document.createElement('input');
                c.type = 'checkbox'; c.value = i.id;
                const t = document.createElement('span'); t.textContent = label(i);
                const q = document.createElement('input');
                q.type = 'number'; q.min = 1; q.value = 1; q.title = 'Quantity';
                l.append(c, t, q);
                box.appendChild(l);
            });
        } else if (!items) {  // list not available -> type IDs
            const inp = document.createElement('input');
            inp.type = 'text'; inp.className = 'ids-input';
            inp.placeholder = 'Optional: IDs separated by commas (quantity 1 each)';
            box.appendChild(inp);
        } else {
            box.textContent = 'Nothing to choose here.';
        }
    }

    function checkedItems(containerId) {
        const box = $(containerId);
        const txt = box.querySelector('.ids-input');
        if (txt) {
            return txt.value.split(',').map(s => parseInt(s.trim(), 10)).filter(n => !isNaN(n))
                .map(id => ({ id: id, quantity: 1 }));
        }
        return [...box.querySelectorAll('label')]
            .filter(l => l.querySelector('input[type=checkbox]').checked)
            .map(l => ({
                id: parseInt(l.querySelector('input[type=checkbox]').value, 10),
                quantity: Math.max(1, parseInt(l.querySelector('input[type=number]').value, 10) || 1)
            }));
    }

    $('saveBtn').onclick = async () => {
        const msg = $('msg3'); clearMsg(msg);
        try {
            await api('/api/bookings/' + bookingId + '/addons', { method: 'PUT', body: JSON.stringify(checkedItems('addons')) });
            await api('/api/bookings/' + bookingId + '/decor', { method: 'PUT', body: JSON.stringify(checkedItems('decor')) });
            showMsg(msg, 'Extras saved.', 'success');
            await showSummary();
        } catch (e) { showMsg(msg, e.message, 'error'); }
    };

    // ---------- Step 4: summary + submit ----------
    async function showSummary() {
        const b = await api('/api/bookings/' + bookingId);
        const fmt = list => list.length ? list.map(x => '#' + x.id + ' x ' + x.quantity).join(', ') : 'none';
        const rows = [
            ['Reference', '#' + b.booking_id], ['Customer', b.customerName], ['Event type', b.eventType],
            ['Date', b.eventDate], ['Guests', b.guestCount],
            [cfg.selectionLabel + ' ID', b.packageId || b.menuId],
            ['Add-ons', fmt(b.addOns)], ['Decor', fmt(b.decor)]
        ];
        const t = $('summary'); t.innerHTML = '';
        rows.forEach(([k, v]) => {
            const tr = document.createElement('tr');
            const th = document.createElement('th'); th.textContent = k;
            const td = document.createElement('td'); td.textContent = v;
            tr.append(th, td); t.appendChild(tr);
        });
        $('step4').classList.remove('hidden');
    }

    function lockAll() {
        document.querySelectorAll('#step3 button, #step4 button, #step3 input')
            .forEach(el => el.disabled = true);
    }

    $('submitBtn').onclick = async () => {
        const msg = $('msg4'); clearMsg(msg);
        try {
            await api('/api/bookings/' + bookingId + '/submit', { method: 'POST' });
            showMsg(msg, 'Your request #' + bookingId + ' has been submitted. The hall administrator will confirm or reject it. '
                + 'If it is confirmed, a Rs. 20,000 deposit is due within 7 days.', 'success');
            lockAll();
        } catch (e) { showMsg(msg, e.message, 'error'); }
    };

    document.querySelectorAll('.cancel-request').forEach(btn => {
        btn.onclick = async () => {
            if (!confirm('Cancel this booking request?')) return;
            try {
                await api('/api/bookings/' + bookingId + '/cancel',
                    { method: 'POST', body: JSON.stringify({ reason: 'Cancelled by customer before submission' }) });
                showMsg($('msg3'), 'Request cancelled. The date has been released.', 'info');
                showMsg($('msg4'), 'Request cancelled. The date has been released.', 'info');
                lockAll();
            } catch (e) { showMsg($('msg3'), e.message, 'error'); }
        };
    });
}