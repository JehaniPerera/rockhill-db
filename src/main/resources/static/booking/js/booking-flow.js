function initBooking(cfg) {
    const $ = id => document.getElementById(id);
    let bookingId = null;

    // Function 2's JSON uses addonId / decorId / packageId / menuId as the id field.
    const idOf = i => [i.addonId, i.decorId, i.packageId, i.menuId, i.id].find(v => v !== undefined && v !== null);
    const money = n => 'Rs. ' + Number(n).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

    function label(i) {
        const t = i.category ? i.category + ' - ' + i.name : i.name;
        if (i.pricePerGuest != null) return t + ' - ' + money(i.pricePerGuest) + ' per guest';
        if (i.price != null) {
            const free = cfg.wedding && (i.category !== undefined || i.includedInPackage);
            return t + (free ? ' - included in package'
                : ' - ' + money(i.price) + (i.unitType ? ' (' + i.unitType + ')' : ''));
        }
        return t;
    }

    $('eventDate').min = new Date(Date.now() + 86400000).toISOString().slice(0, 10);
    $('eventDate').onchange = () => { if (!bookingId) $('step2').classList.add('hidden'); };

    async function loadOptions(url) {
        try { const r = await api(url); return Array.isArray(r) ? r : null; } catch (e) { return null; }
    }

    // ---------- package / menu list (needed in step 2) ----------
    async function fillSelection() {
        const sel = $('selection');
        const items = await loadOptions(cfg.optionsUrl);
        if (items && items.length) {
            sel.innerHTML = '<option value="">-- choose --</option>';
            items.forEach(i => {
                const o = document.createElement('option');
                o.value = idOf(i); o.textContent = label(i);
                sel.appendChild(o);
            });
        } else {
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
            bookingId = b.id;
            ['eventDate', 'checkBtn', 'name', 'phone', 'email', 'guests', 'selection', 'createBtn']
                .forEach(id => $(id).disabled = true);
            showMsg(msg, 'Booking request #' + bookingId + ' started. You can now add extras below.', 'success');
            $('step3').classList.remove('hidden');
            fillChecks('addons', cfg.addonsUrl, true);
            fillChecks('decor', cfg.decorUrl, false);
        } catch (e) { showMsg(msg, e.message, 'error'); }
    };

    // ---------- Step 3: add-ons and decor (Function 2 endpoints) ----------
    async function fillChecks(containerId, url, withQty) {
        const box = $(containerId);
        const items = await loadOptions(url);
        box.innerHTML = '';
        if (items && items.length) {
            items.forEach(i => {
                const l = document.createElement('label');
                const c = document.createElement('input');
                c.type = 'checkbox'; c.value = idOf(i);
                const t = document.createElement('span'); t.textContent = label(i);
                l.append(c, t);
                if (withQty) {
                    const q = document.createElement('input');
                    q.type = 'number'; q.min = 1; q.placeholder = 'auto'; q.title = 'Quantity (leave empty for automatic)';
                    l.appendChild(q);
                }
                box.appendChild(l);
            });
        } else if (!items) {
            const inp = document.createElement('input');
            inp.type = 'text'; inp.className = 'ids-input';
            inp.placeholder = 'Optional: IDs separated by commas';
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
                .map(id => ({ id: id, quantity: null }));
        }
        return [...box.querySelectorAll('label')]
            .filter(l => l.querySelector('input[type=checkbox]').checked)
            .map(l => {
                const q = l.querySelector('input[type=number]');
                return { id: parseInt(l.querySelector('input[type=checkbox]').value, 10),
                    quantity: q && q.value ? parseInt(q.value, 10) : null };
            });
    }

    // Makes the server's selections equal to what is ticked: remove the unticked, add/update the ticked.
    async function syncSelections(kind, wanted) {
        const base = '/api/bookings/' + bookingId + '/selections/' + kind;
        const existing = await api(base);
        const wantedIds = new Set(wanted.map(w => w.id));
        for (const e of existing) {
            if (!wantedIds.has(e.itemId)) await api(base + '/' + e.itemId, { method: 'DELETE' });
        }
        for (const w of wanted) {
            const q = (kind === 'addons' && w.quantity) ? '?quantity=' + w.quantity : '';
            await api(base + '/' + w.id + q, { method: 'POST' });
        }
    }

    $('saveBtn').onclick = async () => {
        const msg = $('msg3'); clearMsg(msg);
        try {
            await syncSelections('addons', checkedItems('addons'));
            await syncSelections('decor', checkedItems('decor'));
            showMsg(msg, 'Extras saved.', 'success');
            await showSummary();
        } catch (e) { showMsg(msg, e.message, 'error'); }
    };

    // ---------- Step 4: summary with price estimate + submit ----------
    async function showSummary() {
        const base = '/api/bookings/' + bookingId + '/selections';
        const b = await api('/api/bookings/' + bookingId);
        const addons = await api(base + '/addons');
        const decor = await api(base + '/decor');
        const fmt = list => list.length
            ? list.map(x => x.name + ' x ' + x.quantity + ' = ' + money(x.lineTotal)).join('; ') : 'none';
        const rows = [
            ['Reference', '#' + b.id], ['Customer', b.customerName], ['Event type', b.eventType],
            ['Date', b.eventDate], ['Guests', b.guestCount],
            [cfg.selectionLabel + ' ID', b.packageId || b.menuId],
            ['Add-ons', fmt(addons)], ['Decor', fmt(decor)]
        ];
        try {
            const e = await api(base + '/estimate');
            rows.push(['Base price', money(e.basePrice)]);
            if (e.lowGuestSurcharge > 0) rows.push(['Low-guest charge', money(e.lowGuestSurcharge)]);
            rows.push(['Add-ons total', money(e.addOnTotal)], ['Decor total', money(e.decorTotal)],
                ['Estimated total', money(e.total)]);
        } catch (err) {
            rows.push(['Estimated total', 'Not available: ' + err.message]);
        }
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