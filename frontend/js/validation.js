// validation.js - Form validation and loading simulation

document.addEventListener('DOMContentLoaded', () => {
    const analyzeBtn = document.getElementById('analyze-btn');
    const fileInput = document.getElementById('resume-upload');
    const fileError = document.getElementById('file-error');
    const uploadSection = document.getElementById('upload-section');
    const loadingScreen = document.getElementById('loading-screen');

    if (!analyzeBtn) return;

    analyzeBtn.addEventListener('click', async () => {
        // Validation
        if (!fileInput || !fileInput.files || fileInput.files.length === 0) {
            fileError.textContent = "Please upload a resume first.";
            fileError.style.display = 'block';
            return;
        }

        // Hide upload section, show loading
        uploadSection.style.display = 'none';
        loadingScreen.style.display = 'block';

        const formData = new FormData();
        formData.append('resume', fileInput.files[0]);
        
        const jd = document.getElementById('job-description').value;
        if (jd && jd.trim() !== '') {
            formData.append('jobDescription', jd);
        }

        const companyFormat = document.getElementById('company-format').value;
        if (companyFormat && companyFormat.trim() !== '') {
            formData.append('companyResumeFormat', companyFormat);
        }

        // Start visual loading
        const interval = simulateLoadingVisuals();

        try {
            const response = await fetch(getApiUrl(), {
                method: 'POST',
                body: formData
            });

            let data;
            try {
                data = await response.json();
            } catch (parseError) {
                throw new Error(
                    response.ok
                        ? 'The server returned an unexpected response. Please try again.'
                        : `Server error (${response.status}). Please try again.`
                );
            }

            if (!response.ok) {
                throw new Error(data.message || `Server error (${response.status}). Please try again.`);
            }
            
            // Store results and redirect
            sessionStorage.setItem('resumeAnalysisResult', JSON.stringify(data));
            
            // Wait for at least some visual progression
            setTimeout(() => {
                clearInterval(interval);
                window.location.href = 'results.html';
            }, 1500);

        } catch (error) {
            clearInterval(interval);
            fileError.textContent = error.message;
            fileError.style.display = 'block';
            uploadSection.style.display = 'block';
            loadingScreen.style.display = 'none';
        }
    });

    function simulateLoadingVisuals() {
        const steps = [
            document.getElementById('step-2'),
            document.getElementById('step-3'),
            document.getElementById('step-4'),
            document.getElementById('step-5'),
            document.getElementById('step-6')
        ];

        let currentStep = 0;

        return setInterval(() => {
            if (currentStep > 0 && currentStep <= steps.length) {
                // Mark previous as done
                steps[currentStep - 1].classList.remove('active');
                steps[currentStep - 1].classList.add('done');
                steps[currentStep - 1].textContent = '✓ ' + steps[currentStep - 1].textContent.substring(2);
            }

            if (currentStep < steps.length) {
                // Set current to active
                steps[currentStep].classList.add('active');
            }
            
            if (currentStep < steps.length) currentStep++;
        }, 800);
    }

    function getApiUrl() {
        if (window.location.protocol === 'file:') {
            return 'http://localhost:8080/api/resume/analyze';
        }

        return '/api/resume/analyze';
    }
});
