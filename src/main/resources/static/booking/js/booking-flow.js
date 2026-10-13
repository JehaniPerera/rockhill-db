// booking-flow.js - Function 1 intake step, used by wedding-booking.html and event-booking.html
//   initBookingPage('WEDDING')  -> wedding-booking.html
//   initBookingPage('OTHER')    -> event-booking.html
// Collects the customer's details, the date and the guest count, checks that the date is free,
// creates the Pending draft (this holds the date) and hands the customer over to Function 2,
// which is where the package / menu, add-ons, decor and the final summary + Submit button live.
//   Wedding      -> /booking/{id}/select/package
//   Other events -> /booking/{id}/select/menu
function initBookingPage(type) {
    const isWedding = type === 'WEDDING';
    const $ = id => document.getElementById(id);

    $('eventDate').min = tomorrow();

    if (!isWedding) {   // event page only: the free-text box is needed just for "Other"
        const onTypeChange = () => $('descWrap').classList.toggle('hidden', $('eventType').value !== 'OTHER');
        $('eventType').addEventListener('change', onTypeChange);
        onTypeChange();
    }

    // ---------- availability (Scenario 1) ----------
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
            const r = await api('/api/bookings/availability?date=' + encodeURIComponent(date));
            if (r.available) { $('suggestions').innerHTML = ''; showMsg('msg', date + ' is available.', 'success'); return true; }
            showMsg('msg', 'Sorry, ' + date + ' is not available.');
            showSuggestions(r.suggestions);
            return false;
        } catch (e) { showMsg('msg', e.message); return false; }
    }
    $('checkBtn').addEventListener('click', async () => { clearMsg('msg'); $('suggestions').innerHTML = ''; await checkAvailability(); });

    // ---------- Continue: validate -> create the Pending draft -> go to Function 2 ----------
    $('continueBtn').addEventListener('click', async () => {
        clearMsg('msg'); $('suggestions').innerHTML = '';
        const eventType = isWedding ? 'WEDDING' : $('eventType').value;
        if (!isWedding && !eventType) { showMsg('msg', 'Please choose the type of event.'); return; }
        const body = {
            customerName: $('customerName').value.trim(),
            customerPhone: $('customerPhone').value.trim(),
            customerEmail: $('customerEmail').value.trim(),
            eventDate: $('eventDate').value,
            eventType: eventType,
            guestCount: parseInt($('guestCount').value, 10)
            // no selectionId: Function 2 chooses the package / menu next
        };
        if (!isWedding) {
            const sel = $('eventType');
            body.eventDescription = eventType === 'OTHER'
                ? $('eventDescription').value.trim()
                : sel.options[sel.selectedIndex].text;
        }
        const problem = validateBooking(body, isWedding);
        if (problem) { showMsg('msg', problem); return; }

        $('continueBtn').disabled = true;
        try {
            const booking = await api(isWedding ? '/api/bookings/wedding' : '/api/bookings/event',
                { method: 'POST', body: JSON.stringify(body) });
            // Function 2 takes over from here.
            location.href = '/booking/' + booking.id + '/select/' + (isWedding ? 'package' : 'menu');
        } catch (e) {
            $('continueBtn').disabled = false;
            showMsg('msg', e.message);
            if (/not available|just taken/i.test(e.message)) {
                try { const r = await api('/api/bookings/availability?date=' + encodeURIComponent(body.eventDate)); showSuggestions(r.suggestions); } catch (x) { /* ignore */ }
            }
        }
    });
}
