/* app.js — Nexus Logistics
   GSAP 3.12.7 + ScrollTrigger for Thymeleaf MPA */

// Register plugin before DOMContentLoaded (GSAP is loaded synchronously)
if (typeof gsap !== 'undefined' && typeof ScrollTrigger !== 'undefined') {
    gsap.registerPlugin(ScrollTrigger);
}

// Accessibility guard
function shouldAnimate() {
    return typeof gsap !== 'undefined' &&
           !window.matchMedia('(prefers-reduced-motion: reduce)').matches;
}

// ─────────────────────────────────────────────────────────────
// GSAP ANIMATION MODULES
// ─────────────────────────────────────────────────────────────

function initPageEntrance() {
    // Navbar slides in from top
    gsap.from('.navbar', { y: -60, opacity: 0, duration: 0.5, ease: 'power2.out', clearProps: 'all' });

    // Page header
    const header = document.querySelector('.page-header, .hero');
    if (header) {
        gsap.from(header, { opacity: 0, y: 16, duration: 0.45, delay: 0.1, ease: 'power2.out', clearProps: 'all' });
    }

    // Stat cards stagger
    const statCards = document.querySelectorAll('.stat-card');
    if (statCards.length) {
        gsap.from(statCards, {
            opacity: 0, y: 28, scale: 0.97,
            stagger: 0.08, duration: 0.5, delay: 0.15,
            ease: 'power2.out', clearProps: 'all'
        });
    }

    // Content cards
    const cards = document.querySelectorAll('.card, .form-card, .auth-card, .card-elevated');
    if (cards.length) {
        gsap.from(cards, {
            opacity: 0, y: 18,
            stagger: 0.06, duration: 0.4, delay: 0.2,
            ease: 'power2.out', clearProps: 'all'
        });
    }
}

function initNavbarAnimation() {
    gsap.from('.navbar-brand', { opacity: 0, x: -20, duration: 0.55, ease: 'back.out(1.5)' });
    gsap.from('.navbar-nav li', {
        opacity: 0, y: -10,
        stagger: 0.05, duration: 0.35, delay: 0.2,
        ease: 'power1.out'
    });
}

function initHeroAnimation() {
    const eyebrow = document.querySelector('.hero-eyebrow');
    const title   = document.querySelector('.hero-title');
    const sub     = document.querySelector('.hero-subtitle');
    const actions = document.querySelector('.hero-actions');
    if (!title) return;

    const tl = gsap.timeline({ defaults: { ease: 'power2.out', clearProps: 'all' } });
    if (eyebrow) tl.from(eyebrow, { opacity: 0, y: 12, duration: 0.4 }, 0.05);
    tl.from(title,  { opacity: 0, y: 22, duration: 0.5 }, 0.15);
    if (sub)     tl.from(sub,     { opacity: 0, y: 14, duration: 0.4 }, 0.3);
    if (actions) tl.from(actions, { opacity: 0, y: 12, duration: 0.4 }, 0.42);

    // Feature cards stagger
    const featureCards = document.querySelectorAll('.feature-card');
    if (featureCards.length) {
        gsap.from(featureCards, {
            opacity: 0, y: 30,
            stagger: 0.1, duration: 0.5, delay: 0.5,
            ease: 'power2.out', clearProps: 'all'
        });
    }
}

function initTableAnimation() {
    const wrapper = document.querySelector('.table-wrapper');
    if (!wrapper) return;

    gsap.from(wrapper, { opacity: 0, y: 16, duration: 0.4, delay: 0.2, ease: 'power2.out', clearProps: 'all' });

    // Stagger table rows using ScrollTrigger.batch for performance
    const rows = wrapper.querySelectorAll('tbody tr');
    if (!rows.length) return;

    gsap.set(rows, { opacity: 0, x: -8 });

    ScrollTrigger.batch(rows, {
        start: 'top 92%',
        interval: 0.05,
        onEnter: batch => gsap.to(batch, {
            opacity: 1, x: 0,
            stagger: 0.04, duration: 0.35,
            ease: 'power1.out', overwrite: true
        }),
        once: true
    });

    // Refresh after images/layout settles
    window.addEventListener('load', () => ScrollTrigger.refresh());
}

function initCounterAnimations() {
    // Elements with data-count attribute get animated number count-up
    document.querySelectorAll('[data-count]').forEach(el => {
        const target = parseFloat(el.getAttribute('data-count'));
        if (isNaN(target)) return;
        const isFloat = String(target).includes('.');
        const proxy   = { val: 0 };

        gsap.to(proxy, {
            val: target,
            duration: 1.5,
            ease: 'power1.out',
            delay: 0.35,
            roundProps: isFloat ? undefined : 'val',
            onUpdate() {
                el.textContent = isFloat
                    ? proxy.val.toFixed(1)
                    : Math.round(proxy.val).toLocaleString();
            }
        });
    });
}

function initCardHovers() {
    document.querySelectorAll('.stat-card, .feature-card').forEach(card => {
        const tl = gsap.timeline({ paused: true })
            .to(card, { y: -4, duration: 0.22, ease: 'power2.out' });
        card.addEventListener('mouseenter', () => tl.play());
        card.addEventListener('mouseleave', () => tl.reverse());
    });
}

function initButtonHovers() {
    document.querySelectorAll('.btn-primary').forEach(btn => {
        const tl = gsap.timeline({ paused: true })
            .to(btn, { scale: 1.04, duration: 0.18, ease: 'power1.out' });
        btn.addEventListener('mouseenter', () => tl.play());
        btn.addEventListener('mouseleave', () => tl.reverse());
    });
}

function initTimelineAnimation() {
    const items = document.querySelectorAll('.timeline-item');
    if (!items.length) return;
    gsap.from(items, {
        opacity: 0, x: -20,
        stagger: 0.12, duration: 0.45, delay: 0.3,
        ease: 'power2.out', clearProps: 'all'
    });
}

function initPageTransition() {
    // Fade-exit before navigating away
    document.querySelectorAll('a[href]').forEach(link => {
        const href   = link.getAttribute('href');
        const target = link.getAttribute('target');
        if (!href || href.startsWith('#') || href.startsWith('http') ||
            href.startsWith('mailto') || target === '_blank' ||
            link.hasAttribute('download')) return;

        link.addEventListener('click', e => {
            e.preventDefault();
            gsap.to('main', {
                opacity: 0, y: -6, duration: 0.18, ease: 'power1.in',
                onComplete() { window.location.href = href; }
            });
        });
    });
}

// ─────────────────────────────────────────────────────────────
// MAIN — DOMContentLoaded
// ─────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {

    // Active nav link
    const path = window.location.pathname;
    document.querySelectorAll('.nav-link').forEach(link => {
        const href = link.getAttribute('href');
        if (!href) return;
        if (href === '/' && path === '/') link.classList.add('active');
        else if (href !== '/' && path.startsWith(href)) link.classList.add('active');
    });

    // Delete confirmations
    document.querySelectorAll('form.delete-form, form[action$="/delete"]').forEach(form => {
        form.addEventListener('submit', e => {
            if (!confirm('Delete this record? This cannot be undone.')) e.preventDefault();
        });
    });

    // Toast system
    window.showToast = function (message, type = 'info') {
        let container = document.querySelector('.toast-container');
        if (!container) {
            container = document.createElement('div');
            container.className = 'toast-container';
            document.body.appendChild(container);
        }
        const icons = { success: '✓', error: '✕', info: 'i', warning: '!' };
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerHTML = `
            <span style="font-weight:700;font-size:0.9rem;">${icons[type] || 'i'}</span>
            <span>${message}</span>
        `;
        container.appendChild(toast);

        if (typeof gsap !== 'undefined') {
            gsap.from(toast, { x: 80, opacity: 0, duration: 0.3, ease: 'power2.out' });
            gsap.to(toast, {
                x: 80, opacity: 0, duration: 0.3, ease: 'power2.in',
                delay: 4,
                onComplete: () => toast.remove()
            });
        } else {
            setTimeout(() => { toast.style.opacity = '0'; toast.style.transition = 'opacity .3s'; setTimeout(() => toast.remove(), 350); }, 4000);
        }
    };

    // Auto-toasts from URL params
    const p = new URLSearchParams(window.location.search);
    if (p.has('success')) showToast('Saved successfully.', 'success');
    if (p.has('updated')) showToast('Record updated.', 'success');
    if (p.has('deleted')) showToast('Record deleted.', 'success');
    if (p.has('error'))   showToast('Something went wrong. Please try again.', 'error');
    if (p.has('logout'))  showToast('You have been signed out.', 'info');

    // Read Thymeleaf flash messages rendered as hidden divs into toasts
    // These are set via RedirectAttributes.addFlashAttribute() in controllers
    const successEl = document.querySelector('[data-flash-success]');
    const infoEl    = document.querySelector('[data-flash-info]');
    const errorEl   = document.querySelector('[data-flash-error]');
    if (successEl) showToast(successEl.dataset.flashSuccess, 'success');
    if (infoEl)    showToast(infoEl.dataset.flashInfo,    'info');
    if (errorEl)   showToast(errorEl.dataset.flashError,  'error');

    // Run animations if allowed
    if (shouldAnimate()) {
        initNavbarAnimation();
        initPageEntrance();
        initHeroAnimation();
        initTableAnimation();
        initCounterAnimations();
        initCardHovers();
        initButtonHovers();
        initTimelineAnimation();
        initPageTransition();
    }

});