const hamburgerMenuIcon = document.getElementById("hamburgerMenuIcon");
const closeMenuIcon = document.getElementById("closeMenuIcon");
const overlayMenuClick = document.getElementById("overlay-menu");
const overlayMenu = document.getElementsByClassName("overlay-menu");
const mobileMenuWrapper = document.getElementsByClassName(
    "mobile-menu-wrapper"
);

closeMenuIcon.addEventListener("click", () => {
    closeMenu();
});

overlayMenuClick.addEventListener("click", () => {
    closeMenu();
});

hamburgerMenuIcon.addEventListener("click", () => {
    overlayMenu[0].classList.add("active-overlay");
    mobileMenuWrapper[0].classList.add("active-menu");
});

function closeMenu() {
    overlayMenu[0].classList.remove("active-overlay");
    mobileMenuWrapper[0].classList.remove("active-menu");
}
