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

        // Disable button to prevent double-submits
        analyzeBtn.disabled = true;
        analyzeBtn.textContent = 'Analyzing…';

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
            const data = await fetchWithRetry(getApiUrl(), {
                method: 'POST',
                body: formData
            });

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

            // Re-enable button
            analyzeBtn.disabled = false;
            analyzeBtn.textContent = 'Analyze Resume';
        }
    });

    /**
     * Fetch with exponential back-off retry.
     * Retries up to maxRetries times on network errors and 5xx responses.
     * Each attempt has a hard 60-second AbortController timeout.
     */
    async function fetchWithRetry(url, options, maxRetries = 3) {
        const TIMEOUT_MS = 60_000;
        const BASE_DELAY_MS = 2_000;

        let lastError;

        for (let attempt = 1; attempt <= maxRetries; attempt++) {
            const controller = new AbortController();
            const timer = setTimeout(() => controller.abort(), TIMEOUT_MS);

            try {
                const response = await fetch(url, {
                    ...options,
                    signal: controller.signal
                });

                clearTimeout(timer);

                // Handle rate-limit separately — no point retrying immediately
                if (response.status === 429) {
                    throw new Error(
                        'The server is busy with too many requests. Please wait a moment and try again.'
                    );
                }

                let data;
                try {
                    data = await response.json();
                } catch (_) {
                    throw new Error(
                        response.ok
                            ? 'The server returned an unexpected response. Please try again.'
                            : buildStatusError(response.status)
                    );
                }

                if (!response.ok) {
                    // 5xx — retriable
                    if (response.status >= 500 && attempt < maxRetries) {
                        lastError = new Error(data.message || buildStatusError(response.status));
                        await delay(BASE_DELAY_MS * Math.pow(2, attempt - 1));
                        continue;
                    }
                    throw new Error(data.message || buildStatusError(response.status));
                }

                return data;

            } catch (err) {
                clearTimeout(timer);

                // Timeout (AbortError)
                if (err.name === 'AbortError') {
                    lastError = new Error(
                        attempt < maxRetries
                            ? `Request timed out (attempt ${attempt}/${maxRetries}). Retrying…`
                            : 'The request timed out. The server may be under heavy load — please try again in a moment.'
                    );
                    if (attempt < maxRetries) {
                        await delay(BASE_DELAY_MS * Math.pow(2, attempt - 1));
                        continue;
                    }
                    throw lastError;
                }

                // Network / CORS / other fetch errors
                if (err.message.includes('fetch') || err.message.includes('network') ||
                    err.message.toLowerCase().includes('failed to fetch')) {
                    lastError = new Error(
                        attempt < maxRetries
                            ? `Connection failed (attempt ${attempt}/${maxRetries}). Retrying…`
                            : 'Could not connect to the server. Please check your internet connection and try again.'
                    );
                    if (attempt < maxRetries) {
                        await delay(BASE_DELAY_MS * Math.pow(2, attempt - 1));
                        continue;
                    }
                    throw lastError;
                }

                // Non-retriable errors (validation, 429, etc.) — throw immediately
                throw err;
            }
        }

        throw lastError || new Error('Analysis failed after multiple attempts. Please try again.');
    }

    function buildStatusError(status) {
        if (status === 503 || status === 502) {
            return 'The server is temporarily unavailable (high load). Please try again in a few seconds.';
        }
        if (status === 504) {
            return 'The server took too long to respond. Please try again.';
        }
        return `Server error (${status}). Please try again.`;
    }

    function delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

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

        return 'https://pilot-ats-production-1859.up.railway.app/api/resume/analyze';
    }
});
