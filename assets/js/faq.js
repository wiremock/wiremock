var faqList = document.querySelector('.faq-list');

if (faqList) {
    document.querySelectorAll('.faq-list > dt').forEach(function (dd) {
        dd.classList.add('faq-list--collapsed');
    });

    document.querySelector('.faq-list').addEventListener('click', function (event) {
        if (event.target.tagName === 'DT') {
            var dd = event.target;

            if (dd.classList.contains('faq-list--expanded')) {
                dd.classList.remove('faq-list--expanded');
                dd.classList.add('faq-list--collapsed');
            } else {
                dd.classList.add('faq-list--expanded');
                dd.classList.remove('faq-list--collapsed');
            }
        }
    });
}
