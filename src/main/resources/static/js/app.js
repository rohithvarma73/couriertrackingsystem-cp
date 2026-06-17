document.addEventListener("DOMContentLoaded", function () {
    const links = document.querySelectorAll(".navbar-custom .nav-link");
    const current = window.location.pathname;
    links.forEach(link => {
        if (link.getAttribute("href") === current) {
            link.classList.add("active");
        }
    });
});