/* HR Portal — Enhanced Script
   Features: Real-time clock, Nav active state, Scroll animations,
   Search/filter, Policy accordion, Toast notifications
*/

(function () {
  'use strict';

  /* ── Real-time Clock ─────────────────────────────────── */
  function updateClock() {
    const el = document.getElementById('hr-clock');
    if (!el) return;
    const now = new Date();
    el.textContent = now.toLocaleTimeString('en-IN', {
      hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: true
    }) + '  ·  ' + now.toLocaleDateString('en-IN', {
      weekday: 'short', day: 'numeric', month: 'short', year: 'numeric'
    });
  }
  updateClock();
  setInterval(updateClock, 1000);

  /* ── Active Nav Link ─────────────────────────────────── */
  const path = window.location.pathname;
  document.querySelectorAll('nav a').forEach(function (link) {
    const href = link.getAttribute('href');
    if (href === path || (href === '/' && path === '/')) {
      link.classList.add('active');
    } else if (href !== '/' && path.startsWith(href)) {
      link.classList.add('active');
    }
  });

  /* ── Scroll Reveal Animation ─────────────────────────── */
  const observer = new IntersectionObserver(function (entries) {
    entries.forEach(function (entry) {
      if (entry.isIntersecting) {
        entry.target.classList.add('visible');
        observer.unobserve(entry.target);
      }
    });
  }, { threshold: 0.08, rootMargin: '0px 0px -40px 0px' });

  document.querySelectorAll('.scroll-reveal').forEach(function (el) {
    observer.observe(el);
  });

  /* ── Policy Accordion ────────────────────────────────── */
  document.querySelectorAll('.policy-header').forEach(function (header) {
    header.addEventListener('click', function () {
      const card = this.closest('.policy-card');
      const isOpen = card.classList.contains('open');
      // Close all
      document.querySelectorAll('.policy-card').forEach(function (c) {
        c.classList.remove('open');
      });
      // Open clicked (unless it was already open)
      if (!isOpen) {
        card.classList.add('open');
      }
    });
  });

  /* ── News Search/Filter ──────────────────────────────── */
  const newsSearch = document.getElementById('news-search');
  if (newsSearch) {
    newsSearch.addEventListener('input', function () {
      const q = this.value.toLowerCase().trim();
      const items = document.querySelectorAll('.news-card');
      let count = 0;
      items.forEach(function (item) {
        const text = item.textContent.toLowerCase();
        const match = !q || text.includes(q);
        item.style.display = match ? '' : 'none';
        if (match) count++;
      });
      const empty = document.getElementById('news-empty');
      if (empty) empty.style.display = count === 0 ? 'block' : 'none';
    });
  }

  /* ── Policy Search/Filter ────────────────────────────── */
  const policySearch = document.getElementById('policy-search');
  if (policySearch) {
    policySearch.addEventListener('input', function () {
      const q = this.value.toLowerCase().trim();
      const items = document.querySelectorAll('.policy-card');
      let count = 0;
      items.forEach(function (item) {
        const text = item.textContent.toLowerCase();
        const match = !q || text.includes(q);
        item.style.display = match ? '' : 'none';
        if (match) count++;
      });
      const empty = document.getElementById('policy-empty');
      if (empty) empty.style.display = count === 0 ? 'block' : 'none';
    });
  }

  /* ── Toast Notification ──────────────────────────────── */
  function showToast(message, duration) {
    duration = duration || 4000;
    let container = document.querySelector('.toast-container');
    if (!container) {
      container = document.createElement('div');
      container.className = 'toast-container';
      document.body.appendChild(container);
    }
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.innerHTML = '<div class="toast-dot"></div><span>' + message + '</span>';
    container.appendChild(toast);

    setTimeout(function () {
      toast.classList.add('hide');
      setTimeout(function () {
        if (toast.parentNode) toast.parentNode.removeChild(toast);
      }, 300);
    }, duration);
  }

  /* ── Status badge dynamic class ──────────────────────── */
  document.querySelectorAll('.status-badge').forEach(function (badge) {
    const text = badge.textContent.toLowerCase();
    if (text.includes('operational') || text.includes('up') || text.includes('running') || text.includes('healthy') || text.includes('online')) {
      badge.classList.add('up');
    } else if (text.includes('down') || text.includes('error') || text.includes('fail') || text.includes('offline') || text.includes('timeout')) {
      badge.classList.add('down');
    } else {
      badge.classList.add('warn');
    }
  });

  /* ── Page load toast ─────────────────────────────────── */
  window.addEventListener('load', function () {
    const pages = {
      '/':              'Welcome to the Operations Dashboard 👋',
      '/services':      'Service Statuses loaded ✓',
      '/dashboard':     'Metrics Dashboard loaded ✓',
      '/maintenance':   'Maintenance Schedules loaded ✓',
      '/incidents':     'Incident Feed loaded ✓',
      '/contact':       'Support Contacts loaded ✓'
    };
    const msg = pages[path] || 'Page loaded ✓';
    setTimeout(function () { showToast(msg); }, 600);
  });

  /* ── Stagger animation delays ────────────────────────── */
  document.querySelectorAll('.card').forEach(function (card, i) {
    card.style.animationDelay = (i * 0.06) + 's';
  });
  document.querySelectorAll('.news-card, .announcement-card, .policy-card, .incident-panel').forEach(function (card, i) {
    card.style.animationDelay = (i * 0.08) + 's';
  });

  console.log('%cOperations Dashboard v2.0 loaded ✓', 'color:#3b82f6;font-weight:bold;font-size:14px;');
})();