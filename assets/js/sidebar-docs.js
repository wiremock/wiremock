var carets = document.getElementsByClassName("doc-category__title");

for (var i = 0; i < carets.length; i++) {
    carets[i].addEventListener("click", function () {
        li = this.parentElement;
        li.querySelector(".doc-category__sub-nav").classList.toggle(
            "doc-category__sub-nav--active"
        );
        this.classList.toggle("doc-category__caret-down");
    });
}
