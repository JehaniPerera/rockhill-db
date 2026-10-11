// booking-flow.js - used by wedding-booking.html and event-booking.html
// initBookingPage('WEDDING') or initBookingPage('OTHER')
function initBookingPage(type) {
    const isWedding = type === 'WEDDING';
    const $ = id => document.getElementById(id);
    let booking = null;           // the Pending draft once it is created
    let selectionId = null;       // chosen package / menu id
    let selectionName = '';       // its name, for the summary
    const selectedDecor = {};     // category -> decorId (one item per singular category)

    $('eventDate').min = tomorrow();

    // ---------- load lists from Function 2 ----------
    async function loadOptions() {
        try {
            const list = await api(isWedding ? '/api/packages' : '/api/menus');
            const box = $('selectionList');
            box.innerHTML = '';
            list.forEach(item => {
                const id = isWedding ? item.packageId : item.menuId;
                const lab = document.createElement('label');
                lab.className = 'option';
                const r = document.createElement('input');
                r.type = 'radio'; r.name = 'selection'; r.value = id;
                r.addEventListener('change', () => onSelectionChange(id, item.name));
                lab.appendChild(r);
                lab.appendChild(document.createTextNode(item.name));
                const small = document.createElement('small');
                small.textContent = isWedding ? (item.description || '') : (money(item.pricePerGuest) + ' per guest');
                lab.appendChild(small);
                box.appendChild(lab);
            });
            const addons = await api('/api/addons?eventType=' + (isWedding ? 'Wedding' : 'Other'));
            renderAddons(addons);
            const decor = await api('/api/decor-options');
            renderDecor(decor);
        } catch (e) { showMsg('msg', 'Could not load options: ' + e.message); }
    }

    function renderAddons(items) {
        const box = $('addonList');
        box.innerHTML = '';
        if (!items.length) { box.textContent = 'None available.'; return; }
        items.forEach(a => {
            const lab = document.createElement('label');
            lab.className = 'option';
            const c = document.createElement('input');
            c.type = 'checkbox'; c.disabled = true; c.dataset.locked = '1';
            c.addEventListener('change', () => toggle(c, 'addons', a.addonId));
            lab.appendChild(c);
            lab.appendChild(document.createTextNode(a.name));
            const small = document.createElement('small');
            const included = isWedding && a.includedInPackage;
            small.textContent = (included ? 'Included in the package' : money(a.price)) + (a.unitType ? ' / ' + a.unitType : '');
            lab.appendChild(small);
            box.appendChild(lab);
        });
    }

    // Singular categories (Poruwa, Settee Back, ...): pick one. Table decoration: tick it.
    function renderDecor(items) {
        const box = $('decorList');
        box.innerHTML = '';
        if (!items.length) { box.textContent = 'None available.'; return; }
        const groups = {};
        items.forEach(d => { (groups[d.category] = groups[d.category] || []).push(d); });
        Object.keys(groups).forEach(cat => {
            const h = document.createElement('h3');
            h.textContent = cat;
            h.style.margin = '14px 0 4px';
            box.appendChild(h);
            const singular = !!groups[cat][0].isSingular;
            groups[cat].forEach(d => {
                const lab = document.createElement('label');
                lab.className = 'option';
                const c = document.createElement('input');
                c.type = singular ? 'radio' : 'checkbox';
                c.name = 'decor-' + cat; c.disabled = true; c.dataset.locked = '1';
                c.addEventListener('change', () => {
                    if (singular) chooseSingularDecor(cat, d.decorId, c);
                    else toggle(c, 'decor', d.decorId);
                });
                lab.appendChild(c);
                lab.appendChild(document.createTextNode(d.name));
                const small = document.createElement('small');
                small.textContent = isWedding ? 'Included in the package' : money(d.price);
                lab.appendChild(small);
                box.appendChild(lab);
            });
            if (singular) {                       // allow "no choice" for optional singular decor
                const none = document.createElement('label');
                none.className = 'option';
                const c = document.createElement('input');
                c.type = 'radio'; c.name = 'decor-' + cat; c.disabled = true; c.dataset.locked = '1'; c.checked = true;
                c.addEventListener('change', () => clearSingularDecor(cat, c));
                none.appendChild(c);
                none.appendChild(document.createTextNode('None'));
                box.appendChild(none);
            }
        });
    }

    // ---------- step 1: availability ----------
    function showSuggestions(list) {
        const box = $('suggestions');
        box.innerHTML = '';
        if (!list || !list.length) return;
        box.appendChild(document.createTextNode('Nearest open dates: '));
        list.forEach(d => {
            const b = document.createElement('button');
            b.type = 'button'; b.className = 'btn small secondary'; b.textContent = d;
            b.addEventListener('click', () => { $('eventDate').value = d; box.innerHTML = ''; clearMsg('msg'); });
            box.appendChild(b);
        });
    }

    async function checkAvailability() {
        const date = $('eventDate').value;
        if (!date) { showMsg('msg', 'Please pick an event date.'); return false; }
        try {
            const r = await api('/api/bookings/availability?date=' + date);
            if (r.available) { $('suggestions').innerHTML = ''; showMsg('msg', date + ' is available.', 'success'); return true; }
            showMsg('msg', 'Date not available.');
            showSuggestions(r.suggestions);
            return false;
        } catch (e) { showMsg('msg', e.message); return false; }
    }
    $('checkBtn').addEventListener('click', async () => { clearMsg('msg'); $('suggestions').innerHTML = ''; await checkAvailability(); });

    // ---------- step 2: create the Pending draft ----------
    async function onSelectionChange(id, name) {
        selectionId = id; selectionName = name;
        if (!booking) return;                       // draft not created yet
        try {                                       // draft exists -> change package / menu
            booking = await api('/api/bookings/' + booking.id + (isWedding ? '/package/' : '/menu/') + id, { method: 'PUT' });
            refreshSummary();
        } catch (e) { showMsg('msg', e.message); }
    }

    $('holdBtn').addEventListener('click', async () => {
        clearMsg('msg'); $('suggestions').innerHTML = '';
        const body = {
            customerName: $('customerName').value.trim(),
            customerPhone: $('customerPhone').value.trim(),
            customerEmail: $('customerEmail').value.trim(),
            eventDate: $('eventDate').value,
            eventType: type,
            guestCount: parseInt($('guestCount').value, 10),
            selectionId: selectionId
        };
        if (!isWedding && $('eventDescription')) body.eventDescription = $('eventDescription').value;
        if (!body.customerName || !body.customerPhone || !body.customerEmail) { showMsg('msg', 'Please fill in your name, phone and email.'); return; }
        if (!body.eventDate) { showMsg('msg', 'Please pick an event date.'); return; }
        if (!body.guestCount || body.guestCount < 1) { showMsg('msg', 'Please enter the number of guests.'); return; }
        if (isWedding && (body.guestCount < 100 || body.guestCount > 300)) { showMsg('msg', 'Wedding guest count must be between 100 and 300. Please correct it or contact the administrator.'); return; }
        if (!selectionId) { showMsg('msg', isWedding ? 'Please choose a package. A package is required for weddings.' : 'Please choose a menu. A menu is required for events.'); return; }
        try {
            booking = await api(isWedding ? '/api/bookings/wedding' : '/api/bookings/event', { method: 'POST', body: JSON.stringify(body) });
            ['customerName', 'customerPhone', 'customerEmail', 'eventDate', 'guestCount', 'eventDescription'].forEach(id => { if ($(id)) $(id).disabled = true; });
            $('checkBtn').disabled = true;
            $('holdBtn').classList.add('hidden');
            document.querySelectorAll('#extras input').forEach(c => c.disabled = false);
            $('extras').classList.remove('hidden');
            $('submitBtn').classList.remove('hidden');
            $('discardBtn').classList.remove('hidden');
            showMsg('msg', 'Your date is held (booking #' + booking.id + '). Choose extras, then press "Submit request". Unsubmitted drafts expire after 24 hours.', 'success');
            refreshSummary();
        } catch (e) {
            showMsg('msg', e.message);
            if (/not available|just taken/i.test(e.message)) {     // show nearest open dates
                try { const r = await api('/api/bookings/availability?date=' + body.eventDate); showSuggestions(r.suggestions); } catch (x) { /* ignore */ }
            }
        }
    });

    // ---------- step 3: add-ons / decor ----------
    async function toggle(c, kind, id) {
        clearMsg('msg');
        try {
            await api('/api/bookings/' + booking.id + '/selections/' + kind + '/' + id, { method: c.checked ? 'POST' : 'DELETE' });
            refreshSummary();
        } catch (e) { c.checked = !c.checked; showMsg('msg', e.message); }
    }

    async function chooseSingularDecor(cat, id, input) {
        clearMsg('msg');
        try {
            await api('/api/bookings/' + booking.id + '/selections/decor/' + id, { method: 'POST' });  // server replaces the old one in this category
            selectedDecor[cat] = id;
            refreshSummary();
        } catch (e) { showMsg('msg', e.message); input.checked = false; }
    }

    async function clearSingularDecor(cat, input) {
        clearMsg('msg');
        const old = selectedDecor[cat];
        if (!old) return;
        try {
            await api('/api/bookings/' + booking.id + '/selections/decor/' + old, { method: 'DELETE' });
            delete selectedDecor[cat];
            refreshSummary();
        } catch (e) { showMsg('msg', e.message); }
    }

    // ---------- summary (scenario 2 step 7 / scenario 3 step 6) ----------
    async function refreshSummary() {
        try {
            const base = '/api/bookings/' + booking.id + '/selections/';
            const [e, addons, decor] = await Promise.all([api(base + 'estimate'), api(base + 'addons'), api(base + 'decor')]);
            const t = $('estimate');
            t.innerHTML = '';
            const row = (a, b, bold) => {
                const tr = document.createElement('tr');
                if (bold) tr.className = 'total';
                tr.appendChild(cell(a)); tr.appendChild(cell(b)); t.appendChild(tr);
            };
            row(isWedding ? 'Package' : 'Menu', selectionName);
            row('Date / guests', $('eventDate').value + ' / ' + $('guestCount').value);
            row('Base price', money(e.basePrice));
            if (e.lowGuestSurcharge) row('Low guest count charge', money(e.lowGuestSurcharge));
            addons.forEach(l => row('Add-on: ' + l.name + ' x ' + l.quantity, l.lineTotal ? money(l.lineTotal) : 'Included'));
            decor.forEach(l => row('Decor: ' + l.name + ' x ' + l.quantity, l.lineTotal ? money(l.lineTotal) : 'Included'));
            row('Estimated total', money(e.total), true);
            $('estimateCard').classList.remove('hidden');
        } catch (e) { showMsg('msg', e.message); }
    }

    // ---------- step 4: submit or discard ----------
    $('submitBtn').addEventListener('click', async () => {
        clearMsg('msg');
        try {
            booking = await api('/api/bookings/' + booking.id + '/submit', { method: 'POST' });
            $('submitBtn').disabled = true; $('discardBtn').disabled = true;
            document.querySelectorAll('#extras input').forEach(c => c.disabled = true);
            showMsg('msg', 'Request submitted! Your booking reference is #' + booking.id + '. The hall team will review it. Keep this number and your phone number to check it in "My bookings".', 'success');
        } catch (e) { showMsg('msg', e.message); }
    });

    $('discardBtn').addEventListener('click', async () => {
        if (!confirm('Discard this draft and release the date?')) return;
        try {
            await api('/api/bookings/' + booking.id + '/cancel', { method: 'POST', body: JSON.stringify({ reason: 'Customer discarded draft' }) });
            location.reload();
        } catch (e) { showMsg('msg', e.message); }
    });

    loadOptions();
}