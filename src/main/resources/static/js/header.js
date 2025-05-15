document.addEventListener('DOMContentLoaded', function () {
    // Mobile menu toggle
    const menuToggle = document.querySelector('.mobile-menu-toggle');
    const mobileNav = document.querySelector('.mobile-nav');
    const closeBtn = document.querySelector('.mobile-close-btn');
    const body = document.body;

    // Open mobile menu
    if (menuToggle) {
        menuToggle.addEventListener('click', function () {
            mobileNav.classList.add('active');
            body.style.overflow = 'hidden'; // Prevent scrolling
        });
    }

    // Close mobile menu
    if (closeBtn) {
        closeBtn.addEventListener('click', function () {
            mobileNav.classList.remove('active');
            body.style.overflow = '';
        });
    }

    // Close menu when clicking outside
    document.addEventListener('click', function (event) {
        if (mobileNav.classList.contains('active') &&
            !mobileNav.contains(event.target) &&
            !menuToggle.contains(event.target)) {
            mobileNav.classList.remove('active');
            body.style.overflow = '';
        }
    });

    // Toggle mobile submenu
    const mobileDropdowns = document.querySelectorAll('.mobile-dropdown');
    mobileDropdowns.forEach(dropdown => {
        const link = dropdown.querySelector('a');
        link.addEventListener('click', function (e) {
            e.preventDefault();
            dropdown.classList.toggle('active');
        });
    });

    // Sticky header effect
    const header = document.querySelector('.main-header');
    let lastScrollTop = 0;

    window.addEventListener('scroll', function () {
        let scrollTop = window.pageYOffset || document.documentElement.scrollTop;

        // Add shadow when scrolling down
        if (scrollTop > 10) {
            header.classList.add('scrolled');
        } else {
            header.classList.remove('scrolled');
        }

        lastScrollTop = scrollTop;
    });
});