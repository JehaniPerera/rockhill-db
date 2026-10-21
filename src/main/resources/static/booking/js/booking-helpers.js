// booking-helpers.js - shared helpers for all booking pages
async function api(url, options) {
    options = options || {};
    const res = await fetch(url, Object.assign({}, options, { headers: { 'Content-Type': 'application/json' } }));
    let data = null;
    const text = await res.text();
    if (text) { try { data = JSON.parse(text); } catch (e) { data = text; } }
    if (!res.ok) {
        let msg = 'Request failed (' + res.status + ')';
        if (data && typeof data === 'object') msg = data.error || data.message || msg;
        else if (typeof data === 'string' && data) msg = data;
        throw new Error(msg);
    }
    return data;
}
function el(target) { return typeof target === 'string' ? document.getElementById(target) : target; }
function showMsg(target, text, type) {
    const box = el(target);
    if (!box) { alert(text); return; }
    box.textContent = text;
    box.className = 'msg ' + (type || 'error');
    box.style.display = 'block';
}
function clearMsg(target) {
    const box = el(target);
    if (!box) return;
    box.textContent = '';
    box.style.display = 'none';
}
function money(n) { return 'Rs. ' + Number(n || 0).toLocaleString('en-LK', { maximumFractionDigits: 2 }); }
function cell(text) {
    const td = document.createElement('td');
    td.textContent = (text === null || text === undefined) ? '' : text;
    return td;
}
function statusCell(status) {
    const td = document.createElement('td');
    const span = document.createElement('span');
    span.className = 'badge ' + String(status || '').toLowerCase();
    span.textContent = status || '';
    td.appendChild(span);
    return td;
}
function actionButton(label, onClick, cls) {
    const b = document.createElement('button');
    b.type = 'button';
    b.textContent = label;
    b.className = 'btn small ' + (cls || '');
    b.addEventListener('click', onClick);
    return b;
}
// Reschedule helper (scenario 5): asks for a date, checks availability, asks for confirmation.
async function askNewDate() {
    const d = prompt('New event date (YYYY-MM-DD):');
    if (!d) return null;
    if (!/^\d{4}-\d{2}-\d{2}$/.test(d.trim())) throw new Error('Please type the date like 2026-12-25');
    const r = await api('/api/bookings/availability?date=' + encodeURIComponent(d.trim()));
    if (!r.available) {
        const s = (r.suggestions || []).join(', ');
        throw new Error('Date not available.' + (s ? ' Nearest open dates: ' + s : ''));
    }
    return confirm(d.trim() + ' is available. Change the booking to this date?') ? d.trim() : null;
}
// Checks every required booking field. Returns an error message, or null if all is fine.
function validateBooking(b, isWedding) {
    if (!b.customerName) return 'Please enter your name.';
    if (!/^(\d{10}|\+\d{9,14})$/.test(b.customerPhone))
        return 'Phone number must be 10 digits (like 0771234567) or start with + followed by the country code and number (like +94771234567).';
    if (!b.customerEmail) return 'Please enter your email.';
    if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(b.customerEmail))
        return 'Please enter a valid email address that contains @ (like name@example.com).';
    if (!isWedding && !b.eventDescription) return 'Please describe what kind of event it is.';
    if (!b.eventDate) return 'Please pick the event date.';
    if (!b.guestCount || b.guestCount < 1) return 'Please enter the number of guests.';
    if (isWedding && (b.guestCount < 100 || b.guestCount > 300))
        return 'Wedding guest count must be between 100 and 300. Please correct it or contact the administrator.';
    return null;
}
function tomorrow() {
    const d = new Date(); d.setDate(d.getDate() + 1);
    return d.toISOString().slice(0, 10);
}