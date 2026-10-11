const $ = s => document.querySelector(s);
const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));

function toast(msg, ok = true) {
  const t = $('#toast'); t.textContent = msg; t.className = 'show ' + (ok ? 'ok' : 'err');
  clearTimeout(toast.t); toast.t = setTimeout(() => t.className = '', 3500);
}
async function api(path, method = 'GET', body) {
  const res = await fetch('/api' + path, {
    method, headers: body ? { 'Content-Type': 'application/json' } : {},
    body: body ? JSON.stringify(body) : undefined
  });
  if (res.status === 204) return null;
  const data = await res.json().catch(() => null);
  if (!res.ok) {
    const fe = data && data.fieldErrors ? ' (' + Object.values(data.fieldErrors).join(', ') + ')' : '';
    throw new Error(((data && data.message) || 'Error ' + res.status) + fe);
  }
  return data;
}
const run = fn => async (...a) => { try { await fn(...a); } catch (e) { toast(e.message, false); } };

// ---------- tabs ----------
document.querySelectorAll('.tab').forEach(b => b.onclick = () => {
  document.querySelectorAll('.tab').forEach(x => x.classList.toggle('active', x === b));
  ['staff', 'avail', 'assign'].forEach(t => $('#tab-' + t).hidden = t !== b.dataset.tab);
  if (b.dataset.tab === 'assign') run(loadBookings)();
  if (b.dataset.tab === 'avail') run(loadDates)();
});

// ---------- 1. staff ----------
let staff = [];
async function loadStaff() {
  staff = await api('/staff');
  $('#st-list').innerHTML = staff.map(s => `<tr><td>${s.staffId}</td><td>${esc(s.name)}</td><td>${esc(s.role)}</td>
    <td>${esc(s.type)}</td><td>${esc(s.contact)}</td>
    <td><button class="sm" data-edit="${s.staffId}">Edit</button> <button class="sm danger" data-del="${s.staffId}">Delete</button></td></tr>`).join('')
    || '<tr><td colspan="6" class="muted">No staff yet</td></tr>';
  const keep = $('#av-staff').value;
  $('#av-staff').innerHTML = staff.map(s => `<option value="${s.staffId}">${esc(s.name)} (${esc(s.role)})</option>`).join('');
  if (keep) $('#av-staff').value = keep;
}
function resetStaffForm() {
  $('#staff-form').reset(); $('#st-id').value = ''; $('#st-save').textContent = 'Add'; $('#st-cancel').hidden = true;
}
$('#staff-form').onsubmit = run(async e => {
  e.preventDefault();
  const id = $('#st-id').value;
  const body = { name: $('#st-name').value, role: $('#st-role').value, type: $('#st-type').value, contact: $('#st-contact').value };
  await (id ? api('/staff/' + id, 'PUT', body) : api('/staff', 'POST', body));
  toast(id ? 'Staff updated' : 'Staff added'); resetStaffForm(); await loadStaff();
});
$('#st-cancel').onclick = resetStaffForm;
$('#st-list').onclick = run(async e => {
  const edit = e.target.dataset.edit, del = e.target.dataset.del;
  if (edit) {
    const s = staff.find(x => x.staffId == edit);
    $('#st-id').value = s.staffId; $('#st-name').value = s.name; $('#st-role').value = s.role;
    $('#st-type').value = s.type; $('#st-contact').value = s.contact || '';
    $('#st-save').textContent = 'Update'; $('#st-cancel').hidden = false; scrollTo(0, 0);
  } else if (del && confirm('Delete this staff/vendor?')) {
    await api('/staff/' + del, 'DELETE'); toast('Deleted'); await loadStaff();
  }
});

// ---------- 2. availability ----------
async function loadDates() {
  const id = $('#av-staff').value;
  if (!id) { $('#av-list').innerHTML = ''; return; }
  const d = await api(`/staff/${id}/unavailable-dates`);
  $('#av-list').innerHTML = d.map(x => `<tr><td>${x.date}</td><td>${esc(x.reason)}</td>
    <td><button class="sm ghost" data-undo="${x.date}">Make available again</button></td></tr>`).join('')
    || '<tr><td colspan="3" class="muted">No unavailable dates</td></tr>';
}
$('#av-staff').onchange = run(loadDates);
$('#av-form').onsubmit = run(async e => {
  e.preventDefault();
  await api(`/staff/${$('#av-staff').value}/unavailable-dates`, 'POST', { date: $('#av-date').value, reason: $('#av-reason').value });
  toast('Marked unavailable'); $('#av-reason').value = ''; await loadDates();
});
$('#av-list').onclick = run(async e => {
  if (!e.target.dataset.undo) return;
  await api(`/staff/${$('#av-staff').value}/unavailable-dates/${e.target.dataset.undo}`, 'DELETE');
  toast('Available again'); await loadDates();
});

// ---------- 3. assignments ----------
let avail = [];
async function loadBookings() {
  const keep = $('#as-booking').value;
  const b = await api('/bookings');
  $('#as-booking').innerHTML = b.map(x => `<option value="${x.bookingId}">#${x.bookingId} · ${x.eventDate} · ${x.status}</option>`).join('');
  if (keep) $('#as-booking').value = keep;
}
async function loadAssign() {
  const id = $('#as-booking').value;
  if (!id) return;
  const role = encodeURIComponent($('#as-role').value.trim());
  const [assigned, a] = await Promise.all([
    api(`/bookings/${id}/assignments`),
    api(`/bookings/${id}/assignments/available-staff?role=${role}`)]);
  avail = a;
  const opts = a.map(s => `<option value="${s.staffId}">${esc(s.name)} (${esc(s.role)})</option>`).join('');
  $('#as-assigned').innerHTML = assigned.map(x => `<tr><td>${esc(x.staff.name)}</td><td>${esc(x.staff.role)}</td>
    <td>${esc(x.staff.type)}</td><td>${x.assignedDate}</td>
    <td><select>${opts}</select> <button class="sm" data-replace="${x.staff.staffId}">Replace</button></td></tr>`).join('')
    || '<tr><td colspan="5" class="muted">Nobody assigned yet</td></tr>';
  $('#as-avail').innerHTML = a.map(s => `<label><input type="checkbox" value="${s.staffId}">
    ${esc(s.name)} <span class="muted">${esc(s.role)} · ${esc(s.type)}</span></label>`).join('')
    || '<span class="muted">No staff available on this date</span>';
}
$('#as-load').onclick = run(loadAssign);
$('#as-booking').onchange = run(loadAssign);
$('#as-assign').onclick = run(async () => {
  const ids = [...document.querySelectorAll('#as-avail input:checked')].map(c => +c.value);
  if (!ids.length) return toast('Select at least one staff/vendor', false);
  await api(`/bookings/${$('#as-booking').value}/assignments`, 'POST', { staffIds: ids });
  toast('Assigned (staff notified)'); await loadAssign();
});
$('#as-assigned').onclick = run(async e => {
  const old = e.target.dataset.replace;
  if (!old) return;
  const sel = e.target.parentElement.querySelector('select');
  if (!sel.value) return toast('No replacement available', false);
  await api(`/bookings/${$('#as-booking').value}/assignments/${old}`, 'PUT', { newStaffId: +sel.value });
  toast('Reassigned'); await loadAssign();
});

run(loadStaff)();
