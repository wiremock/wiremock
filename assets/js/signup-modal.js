
const signupModalOverlay = document.getElementsByClassName('signup-modal-overlay');
const signupModal = document.getElementsByClassName('signup-modal');
const scheduleCallNavBTN = document.getElementsByClassName('schedule-demo-nav');
const scheduleCallMobileNav = document.getElementsByClassName('schedule-demo-mobile-nav');
const closeIconModal = document.getElementsByClassName('close-modal-icon');

function activateSignupModalForm() {
    signupModalOverlay[0].classList.add('activeModalForm');
    signupModal[0].classList.add('activeModalForm');
}
function deactivateSignupModalForm() {
    signupModalOverlay[0].classList.remove('activeModalForm');
    signupModal[0].classList.remove('activeModalForm');
}

signupModalOverlay[0].addEventListener('click', () => {
    deactivateSignupModalForm();
})
scheduleCallNavBTN[0].addEventListener('click', () => {
    ga('send', 'event', 'homepage_masthead', 'schedule_demo_clicked', 'Schedule demo button clicked', 1);
    activateSignupModalForm();
})
scheduleCallMobileNav[0].addEventListener('click', () => {
    ga('send', 'event', 'homepage_mobile_masthead', 'schedule_demo_clicked', 'Schedule demo button clicked', 1);
    closeMenu();
    activateSignupModalForm();
})
closeIconModal[0].addEventListener('click', () => {
    deactivateSignupModalForm();
})

const params = new Proxy(new URLSearchParams(window.location.search), {
    get: (searchParams, prop) => searchParams.get(prop),
});
let scheduleACall = params.scheduleACall;

if (scheduleACall) {
    const scrollToFold = document.getElementById('a-studio-enterprise');
    scrollToFold.click();
    setTimeout(() => {
        activateSignupModalForm();
    },1000)

}