// common.js - shared helpers for all booking pages

// Call the REST API. Returns parsed JSON (or null if no body).
// Throws an Error whose message is the server's message.
async function api(url, options) {
    options = options || {};
    const headers = { 'Content-Type': 'application/json' };
    const res = await fetch(url, Object.assign({}, options, { headers: headers }));
    let data = null;
    const text = await res.text();
    if (text) {
        try { data = JSON.parse(text); } catch (e) { data = text; }
    }
    if (!res.ok) {
        let msg = 'Request failed (' + res.status + ')';
        if (data && typeof data === 'object') {
            msg = data.error || data.message || msg;
        } else if (typeof data === 'string' && data) {
            msg = data;
        }
        throw new Error(msg);
    }
    return data;
}

function el(target) {
    return typeof target === 'string' ? document.getElementById(target) : target;
}

// Show a message in a box. type: 'error' (default) or 'success'
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

// Plain table cell
function cell(text) {
    const td = document.createElement('td');
    td.textContent = (text === null || text === undefined) ? '' : text;
    return td;
}

// Table cell with a coloured status badge
function statusCell(status) {
    const td = document.createElement('td');
    const span = document.createElement('span');
    span.className = 'badge ' + String(status || '').toLowerCase();
    span.textContent = status || '';
    td.appendChild(span);
    return td;
}

// Button for table rows
function actionButton(label, onClick, cls) {
    if (typeof onClick !== 'function' && typeof label === 'function') {
        // tolerate (onClick, label) order
        const t = label; label = onClick; onClick = t;
    }
    const b = document.createElement('button');
    b.type = 'button';
    b.textContent = label;
    b.className = 'btn small ' + (cls || '');
    b.addEventListener('click', onClick);
    return b;
}