// app.js - Global utilities and UI handling

document.addEventListener('DOMContentLoaded', () => {
    // Smooth scrolling for anchor links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth'
                });
            }
        });
    });

    setupCharacterCounter('job-description', 'char-count');
    setupCharacterCounter('company-format', 'format-char-count');
});

function setupCharacterCounter(textareaId, counterId) {
    const textarea = document.getElementById(textareaId);
    const charCount = document.getElementById(counterId);

    if (textarea && charCount) {
        textarea.addEventListener('input', () => {
            const length = textarea.value.length;
            charCount.textContent = `${length} characters`;
            if (length > 0) {
                charCount.classList.remove('text-muted');
                charCount.classList.add('text-primary');
            } else {
                charCount.classList.add('text-muted');
                charCount.classList.remove('text-primary');
            }
        });
    }
}
