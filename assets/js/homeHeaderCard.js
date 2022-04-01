const openSourceCard = document.querySelector("#openSourceCard");
const leftHeaderHP = document.querySelector(".home-header-leftHeaderHP");

const studioCard = document.querySelector("#studioCard");
const rightHeaderHP = document.querySelector(".home-header-rightHeaderHP");

openSourceCard.addEventListener("mouseover", () => {
    leftHeaderHP.classList.add("headerHoverCardOpenStuio");
    leftHeaderHP.classList.add("headerHoverCard");
});
openSourceCard.addEventListener("mouseout", () => {
    leftHeaderHP.classList.remove("headerHoverCard");
});

studioCard.addEventListener("mouseover", () => {
    rightHeaderHP.classList.add("headerHoverCardStudio");
    rightHeaderHP.classList.add("headerHoverCard");
});
studioCard.addEventListener("mouseout", () => {
    rightHeaderHP.classList.remove("headerHoverCard");
});
