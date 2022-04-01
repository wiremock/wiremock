const tabNav = document.querySelectorAll(".code-tabs-main-wrapper ul li");
const snippetTab = document.querySelectorAll("[data-snippettab]");

// console.log(snippetTab);

function removeActiveSnippetTab() {
    tabNav.forEach((singletab) => {
        singletab.classList.remove("active-tab-example");
    });
}

function removeActiveSnippetTabDiv() {
    snippetTab.forEach((snippetTabDiv) => {
        snippetTabDiv.classList.remove("activeCodeSnippet");
    });
}

tabNav.forEach((tab) => {
    tab.addEventListener("click", () => {
        removeActiveSnippetTab();
        const tabName = tab.innerHTML.toLowerCase();
        removeActiveSnippetTabDiv();
        snippetTab.forEach((singleSnippetTab) => {
            if (tabName === singleSnippetTab.dataset.snippettab) {
                singleSnippetTab.classList.add("activeCodeSnippet");
            }
            // console.log(singleSnippetTab.dataset.snippettab);
        });

        tab.classList.add("active-tab-example");
    });
});
