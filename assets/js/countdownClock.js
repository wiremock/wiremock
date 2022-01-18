const countDownClock = () => {
    const d = document;
    const daysElement = d.querySelector(".days");
    const hoursElement = d.querySelector(".hours");
    const minutesElement = d.querySelector(".minutes");
    const secondsElement = d.querySelector(".seconds");

    let countdown;
    timer();

    function timer() {
        countdown = setInterval(() => {
            const year = new Date().getFullYear();
            const difference = +new Date(`${year}/1/31`) - +new Date();

            if (difference <= 0) {
                clearInterval(countdown);
                return;
            }

            displayTimeLeft(difference);
        }, 1000);
    }

    function displayTimeLeft(difference) {
        daysElement.textContent = `${Math.floor(
            difference / (1000 * 60 * 60 * 24)
        )}`;
        hoursElement.textContent = `${Math.floor(
            (difference / (1000 * 60 * 60)) % 24
        )}`;
        minutesElement.textContent = `${Math.floor(
            (difference / 1000 / 60) % 60
        )}`;
        secondsElement.textContent = `${Math.floor((difference / 1000) % 60)}`;
    }
};
