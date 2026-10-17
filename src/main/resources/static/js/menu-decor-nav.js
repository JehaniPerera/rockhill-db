// Navigation wiring for the customer pages: header links, step bar, Back/Next buttons,
// and the "Select this ..." buttons on /menus and /packages.
(function () {
    const KEY = 'mdBookingId';

    function readStored() { try { return sessionStorage.getItem(KEY); } catch (e) { return null; } }
    function store(id) { try { sessionStorage.setItem(KEY, id); } catch (e) { /* ignore */ } }
    function clearStored() { try { sessionStorage.removeItem(KEY); } catch (e) { /* ignore */ } }

    // A booking is known from /booking/{id}/..., from ?booking={id}, or from this tab's memory.
    // ?booking=clear forgets it.
    function findBookingId() {
        const inPath = location.pathname.match(/^\/booking\/(\d+)/);
        if (inPath) { store(inPath[1]); return inPath[1]; }
        const q = new URLSearchParams(location.search).get('booking');
        if (q === 'clear') { clearStored(); return null; }
        if (q && /^\d+$/.test(q)) { store(q); return q; }
        return readStored();
    }

    async function loadBooking(id) {
        if (!id) return null;
        try {
            const res = await fetch('/api/bookings/' + id);
            if (!res.ok) { clearStored(); return null; }
            return await res.json();
        } catch (e) {
            return null;
        }
    }

    // Wedding: hide Menus. Other events: hide Packages. The visible link goes to this booking's page.
    function adaptHeader(booking) {
        const menus = document.getElementById('nav-menus');
        const packages = document.getElementById('nav-packages');
        if (!booking || !menus || !packages) return;
        if (booking.eventType === 'WEDDING') {
            menus.style.display = 'none';
            packages.href = '/booking/' + booking.id + '/select/package';
        } else {
            packages.style.display = 'none';
            menus.href = '/booking/' + booking.id + '/select/menu';
        }
    }

    function buildSteps(booking) {
        const id = booking.id;
        const first = booking.eventType === 'WEDDING'
            ? { key: 'package', label: 'Package' }
            : { key: 'menu', label: 'Menu' };
        return [
            { path: '/booking/' + id + '/select/' + first.key, label: '1. ' + first.label },
            { path: '/booking/' + id + '/select/addons', label: '2. Add-ons' },
            { path: '/booking/' + id + '/select/decor', label: '3. Decor' },
            { path: '/booking/' + id + '/summary', label: '4. Summary' }
        ];
    }

    function addStepBar(booking) {
        const onSelectionPage =
            /^\/booking\/\d+\/(select\/(package|menu|addons|decor)|summary)\/?$/.test(location.pathname);
        const main = document.querySelector('main.container');
        if (!onSelectionPage || !main) return;

        const steps = buildSteps(booking);
        const here = location.pathname.replace(/\/$/, '');
        const current = steps.findIndex(s => s.path === here);

        const bar = document.createElement('div');
        bar.className = 'step-bar';
        steps.forEach((s, i) => {
            if (i === current) {
                const span = document.createElement('span');
                span.className = 'current';
                span.textContent = s.label;
                bar.appendChild(span);
            } else {
                const a = document.createElement('a');
                a.href = s.path;
                a.textContent = s.label;
                bar.appendChild(a);
            }
        });
        main.insertBefore(bar, main.firstChild);

        if (current >= 0) {
            const buttons = document.createElement('div');
            buttons.className = 'step-buttons';
            if (current > 0) {
                const back = document.createElement('a');
                back.className = 'btn btn-secondary';
                back.href = steps[current - 1].path;
                back.textContent = '← Back';
                buttons.appendChild(back);
            } else {
                buttons.appendChild(document.createElement('span'));
            }
            if (current < steps.length - 1) {
                const next = document.createElement('a');
                next.className = 'btn';
                next.href = steps[current + 1].path;
                next.textContent = 'Next →';
                buttons.appendChild(next);
            }
            main.appendChild(buttons);
        }
    }

    // The "Select this menu / package" buttons on /menus and /packages.
    function wireSelectButtons(booking) {
        document.querySelectorAll('button.btn-select[data-kind]').forEach(btn => {
            const note = btn.nextElementSibling;
            const kind = btn.dataset.kind;
            let text = '';
            if (!booking) {
                text = 'Open this page from your booking to select.';
            } else if ((booking.eventType === 'WEDDING') !== (kind === 'package')) {
                text = kind === 'menu' ? 'Menus are for events that are not weddings.' : 'Packages are for weddings.';
            } else if (booking.status !== 'PENDING' || booking.submitted) {
                text = 'This booking can no longer be edited.';
            }
            if (text) {
                if (note) note.textContent = text;
                btn.disabled = true;
                return;
            }

            btn.disabled = false;
            if (note) note.textContent = 'Booking #' + booking.id;
            btn.addEventListener('click', async () => {
                btn.disabled = true;
                try {
                    const res = await fetch('/api/bookings/' + booking.id + '/' + kind + '/' + btn.dataset.id,
                        { method: 'PUT' });
                    if (!res.ok) {
                        let body = null;
                        try { body = await res.json(); } catch (e) { /* no JSON body */ }
                        throw new Error((body && body.error) || ('Request failed (' + res.status + ')'));
                    }
                    location.href = '/booking/' + booking.id + '/select/addons';
                } catch (e) {
                    if (note) note.textContent = e.message;
                    btn.disabled = false;
                }
            });
        });
    }

    document.addEventListener('DOMContentLoaded', async () => {
        if (location.pathname.startsWith('/admin')) return;   // admin pages are not affected
        const booking = await loadBooking(findBookingId());
        adaptHeader(booking);
        if (booking) addStepBar(booking);
        wireSelectButtons(booking);
    });
})();