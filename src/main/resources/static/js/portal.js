(function () {
  var CUSTOMER_ONLY = /^\/?(menus|packages|index\.html|customer\.html|booking\/(?!booking-admin))/i;
  var ADMIN_ONLY = /^\/?(admin|booking\/booking-admin|css\/admin\.html)/i;

  function pathOf(a) {
    var h = a.getAttribute('href') || '';
    try { h = new URL(h, location.origin).pathname; } catch (e) {}
    return h.replace(/^\//, '');
  }

  function adaptHeader() {
    var onAdminPage = location.pathname.indexOf('/admin') === 0;
    var brand = document.querySelector('.site-header .brand');
    if (brand) {
      brand.textContent = 'Rockhill Reception Hall & Caterers';
      brand.setAttribute('href', onAdminPage ? '/admin/index.html' : '/index.html');
    }
    if (onAdminPage) {
      document.querySelectorAll('.site-header nav a').forEach(function (a) {
        a.style.display = 'none';
      });
    }
  }

  adaptHeader();

  function init(user) {
    var admin = user.role === 'ADMIN';
    document.querySelectorAll('a[href]').forEach(function (a) {
      var p = pathOf(a);
      if (admin && CUSTOMER_ONLY.test(p)) a.style.display = 'none';
      if (!admin && ADMIN_ONLY.test(p)) a.style.display = 'none';
    });

    var bar = document.createElement('div');
    bar.style.cssText = 'position:fixed;right:12px;bottom:12px;z-index:9999;background:#1a1a1a;color:#fff;' +
      'padding:8px 14px;border-radius:20px;font:13px system-ui,Arial,sans-serif;border:1px solid #b8922e;box-shadow:0 2px 8px #0004;';
    bar.className = 'portal-bar';
    var ps = document.createElement('style');
    ps.textContent = '@media print{.portal-bar{display:none!important}}';
    document.head.appendChild(ps);
    var name = document.createElement('span');
    name.textContent = user.fullName + ' (' + (admin ? 'Admin' : 'Customer') + ')';
    bar.appendChild(name);
    [['Home', admin ? '/admin/index.html' : '/index.html'], ['Sign out', '/logout']].forEach(function (l) {
      var a = document.createElement('a');
      a.href = l[1];
      a.textContent = l[0];
      a.style.cssText = 'color:#fff;margin-left:12px;text-decoration:underline;';
      bar.appendChild(a);
    });
    document.body.appendChild(bar);
  }

  fetch('/api/auth/me', { credentials: 'same-origin' })
    .then(function (r) {
      if (r.status === 401) { location.href = '/login.html'; return null; }
      return r.ok ? r.json() : null;
    })
    .then(function (u) { if (u) init(u); })
    .catch(function () {});
})();
