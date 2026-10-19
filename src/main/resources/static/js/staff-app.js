const $ = id => document.getElementById(id);
const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
let staffList = [], bookings = [], editingId = null;

function toast(msg, ok = true) {
  const t = $('toast'); t.textContent = msg; t.className = ok ? 'ok' : 'err'; t.style.display = 'block';
  clearTimeout(toast.h); toast.h = setTimeout(() => t.style.display = 'none', 3500);
}

async function api(path, opts = {}) {
  const res = await fetch('/api' + path, {
    headers: {'Content-Type': 'application/json'}, ...opts,
    body: opts.body ? JSON.stringify(opts.body) : undefined
  });
  if (res.status === 204) return null;
  let data = null;
  try { data = await res.json(); } catch (e) {}
  if (!res.ok) {
    const fe = data && data.fieldErrors ? ' ' + Object.values(data.fieldErrors).join(', ') : '';
    throw new Error((data && data.message ? data.message : 'Request failed (' + res.status + ')') + fe);
  }
  return data;
}
const run = async fn => { try { await fn(); } catch (e) { toast(e.message, false); } };

/* ---------- tabs ---------- */
document.querySelectorAll('#tabs button').forEach(b => b.onclick = () => {
  document.querySelectorAll('#tabs button').forEach(x => x.classList.toggle('on', x === b));
  document.querySelectorAll('section').forEach(s => s.classList.toggle('on', s.id === b.dataset.t));
  if (b.dataset.t === 'avail') loadAvail();
  if (b.dataset.t === 'assign') loadAssignments();
  if (b.dataset.t === 'damage') loadDamage();
});

/* ---------- staff ---------- */
async function loadStaff() {
  staffList = await api('/staff');
  const rows = staffList.map(s => `<tr>
    <td>${esc(s.name)}</td><td>${esc(s.role)}</td><td><span class="tag">${esc(s.type)}</span></td><td>${esc(s.contact)}</td>
    <td style="text-align:right"><button class="b sec sm" onclick="editStaff(${s.staffId})">Edit</button>
    <button class="b del" onclick="delStaff(${s.staffId})">Delete</button></td></tr>`).join('');
  $('staffTable').innerHTML = staffList.length
    ? `<table><tr><th>Name</th><th>Role</th><th>Type</th><th>Contact</th><th></th></tr>${rows}</table>`
    : '<div class="empty">No staff yet.</div>';
  const opts = staffList.map(s => `<option value="${s.staffId}">${esc(s.name)} (${esc(s.role)})</option>`).join('');
  const keep = $('aStaff').value; $('aStaff').innerHTML = opts; if (keep) $('aStaff').value = keep;
}
function resetStaffForm() {
  editingId = null; $('sName').value = $('sRole').value = $('sContact').value = ''; $('sType').value = 'Internal';
  $('staffFormTitle').textContent = 'Add staff / vendor'; $('sCancel').style.display = 'none';
}
window.editStaff = id => {
  const s = staffList.find(x => Number(x.staffId) === Number(id));
  if (!s) { toast('Staff record not found - refresh the page (Ctrl+F5)', false); return; }
  editingId = Number(id);
  $('sName').value = s.name; $('sRole').value = s.role; $('sType').value = s.type; $('sContact').value = s.contact || '';
  $('staffFormTitle').textContent = 'Edit staff #' + id; $('sCancel').style.display = ''; scrollTo(0, 0);
};
window.delStaff = id => run(async () => {
  if (!Number.isFinite(Number(id))) throw new Error('Invalid staff id - refresh the page (Ctrl+F5)');
  if (!confirm('Delete this staff member?')) return;
  await api('/staff/' + id, {method: 'DELETE'}); toast('Deleted'); await loadStaff();
});
$('sCancel').onclick = resetStaffForm;
$('sSave').onclick = () => run(async () => {
  const body = {name: $('sName').value.trim(), role: $('sRole').value.trim(), type: $('sType').value, contact: $('sContact').value.trim() || null};
  if (!body.name) throw new Error('Name is required');
  if (!body.role) throw new Error('Role is required');
  if (editingId) await api('/staff/' + editingId, {method: 'PUT', body}); else await api('/staff', {method: 'POST', body});
  toast('Saved'); resetStaffForm(); await loadStaff();
});

/* ---------- availability ---------- */
async function loadAvail() {
  const id = $('aStaff').value;
  if (!id) { $('availTable').innerHTML = '<div class="empty">Add a staff member first.</div>'; return; }
  const list = await api(`/staff/${id}/unavailable-dates`);
  $('availTable').innerHTML = list.length
    ? `<table><tr><th>Date</th><th>Reason</th><th></th></tr>${list.map(a => `<tr><td>${esc(a.date)}</td><td>${esc(a.reason)}</td>
       <td style="text-align:right"><button class="b del" onclick="unblock('${a.date}')">Make available</button></td></tr>`).join('')}</table>`
    : '<div class="empty">No blocked dates.</div>';
}
$('aStaff').onchange = () => run(loadAvail);
window.unblock = date => run(async () => {
  await api(`/staff/${$('aStaff').value}/unavailable-dates/${date}`, {method: 'DELETE'}); toast('Date freed'); await loadAvail();
});
$('aSave').onclick = () => run(async () => {
  if (!$('aStaff').value) throw new Error('Select a staff member');
  if (!$('aDate').value) throw new Error('Pick a date');
  await api(`/staff/${$('aStaff').value}/unavailable-dates`, {method: 'POST', body: {date: $('aDate').value, reason: $('aReason').value.trim() || null}});
  toast('Marked unavailable'); $('aReason').value = ''; await loadAvail();
});

/* ---------- bookings (shared) ---------- */
async function loadBookings() {
  bookings = await api('/staff/bookings');
  const opts = bookings.length
    ? bookings.map(b => `<option value="${b.bookingId}">#${b.bookingId} - ${esc(b.eventDate)} (${esc(b.status)})</option>`).join('')
    : '<option value="">No bookings found</option>';
  const k1 = $('bSel').value, k2 = $('dSel').value;
  $('bSel').innerHTML = opts; $('dSel').innerHTML = opts;
  if (k1) $('bSel').value = k1; if (k2) $('dSel').value = k2;
}

/* ---------- assignments ---------- */
async function loadAssignments() {
  const id = $('bSel').value;
  $('availList').innerHTML = ''; $('doAssign').style.display = 'none';
  if (!id) { $('assignTable').innerHTML = '<div class="empty">No booking selected.</div>'; $('bInfo').textContent = ''; return; }
  const b = bookings.find(x => String(x.bookingId) === id);
  $('bInfo').innerHTML = `Event date: <b>${esc(b.eventDate)}</b> &nbsp; <span class="tag ${esc(b.status)}">${esc(b.status)}</span>`;
  const list = await api(`/bookings/${id}/assignments`);
  $('assignTable').innerHTML = list.length
    ? `<table><tr><th>Name</th><th>Role</th><th>Type</th><th>Assigned date</th><th>Reassign to</th></tr>${list.map(a => `<tr>
       <td>${esc(a.staff.name)}</td><td>${esc(a.staff.role)}</td><td><span class="tag">${esc(a.staff.type)}</span></td><td>${esc(a.assignedDate)}</td>
       <td><select id="re${a.staff.staffId}" style="padding:5px">${staffList.filter(s => s.staffId !== a.staff.staffId).map(s => `<option value="${s.staffId}">${esc(s.name)} (${esc(s.role)})</option>`).join('')}</select>
       <button class="b sec sm" onclick="reassign(${a.staff.staffId})">Go</button></td></tr>`).join('')}</table>`
    : '<div class="empty">Nobody assigned yet.</div>';
}
$('bSel').onchange = () => run(loadAssignments);
$('loadAvail').onclick = () => run(async () => {
  const id = $('bSel').value; if (!id) throw new Error('Select a booking');
  const role = $('roleFilter').value.trim();
  const list = await api(`/bookings/${id}/assignments/available-staff` + (role ? '?role=' + encodeURIComponent(role) : ''));
  $('availList').innerHTML = list.length
    ? list.map(s => `<label><input type="checkbox" value="${s.staffId}"> ${esc(s.name)} - ${esc(s.role)} (${esc(s.type)})</label>`).join('')
    : '<div class="empty">No available staff for that date.</div>';
  $('doAssign').style.display = list.length ? '' : 'none';
});
$('doAssign').onclick = () => run(async () => {
  const ids = [...document.querySelectorAll('#availList input:checked')].map(c => Number(c.value));
  if (!ids.length) throw new Error('Tick at least one person');
  await api(`/bookings/${$('bSel').value}/assignments`, {method: 'POST', body: {staffIds: ids}});
  toast('Assigned'); await loadAssignments();
});
window.reassign = oldId => run(async () => {
  const newId = Number($('re' + oldId).value); if (!newId) throw new Error('Pick a replacement');
  await api(`/bookings/${$('bSel').value}/assignments/${oldId}`, {method: 'PUT', body: {newStaffId: newId}});
  toast('Reassigned'); await loadAssignments();
});

/* ---------- damage reports ---------- */
async function loadDamage() {
  const list = await api('/staff/damage-reports');
  $('damageTable').innerHTML = list.length
    ? `<table><tr><th>Booking</th><th>Damaged</th><th>Notes</th><th>Reported at</th></tr>${list.map(r => `<tr><td>#${r.bookingId}</td>
       <td><span class="tag ${r.damaged ? 'Cancelled' : 'Confirmed'}">${r.damaged ? 'Yes' : 'No'}</span></td><td>${esc(r.notes)}</td><td>${esc((r.reportedAt || '').replace('T', ' ').slice(0, 16))}</td></tr>`).join('')}</table>`
    : '<div class="empty">No reports yet.</div>';
}
$('dSave').onclick = () => run(async () => {
  const id = $('dSel').value; if (!id) throw new Error('Select a booking');
  const body = {damaged: $('dDamaged').value === 'true', notes: $('dNotes').value.trim() || null};
  let exists = true;
  try { await api(`/staff/bookings/${id}/damage-report`); } catch (e) { exists = false; }
  await api(`/staff/bookings/${id}/damage-report`, {method: exists ? 'PUT' : 'POST', body});
  toast(exists ? 'Report updated' : 'Report saved'); await loadDamage();
});

/* ---------- init ---------- */
run(async () => { await loadStaff(); await loadBookings(); });
