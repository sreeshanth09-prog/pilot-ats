// results.js - Render analysis data on the results page

document.addEventListener('DOMContentLoaded', () => {
    // Only run on results page
    if (!document.getElementById('overall-score')) return;

    // Read data from sessionStorage
    const storedData = sessionStorage.getItem('resumeAnalysisResult');
    if (!storedData) {
        window.location.href = 'index.html'; // Redirect if no data
        return;
    }

    let apiData;
    try {
        apiData = JSON.parse(storedData);
    } catch (e) {
        window.location.href = 'index.html';
        return;
    }

    // Map API data
    const scoreBreakdown = apiData.scoreBreakdown || {};
    const formattedData = {
        overallScore: apiData.atsScore || 0,
        status: apiData.status || 'Analysis Complete',
        breakdown: [
            { category: "Keyword Match", score: scoreBreakdown.keywordMatch || 0, max: 25 },
            { category: "Skills Match", score: scoreBreakdown.skillsMatch || 0, max: 20 },
            { category: "Job Alignment", score: scoreBreakdown.jobDescriptionMatch || 0, max: 15 },
            { category: "Experience", score: scoreBreakdown.experienceRelevance || 0, max: 15 },
            { category: "ATS Compatibility", score: scoreBreakdown.atsCompatibility || 0, max: 10 },
            { category: "Resume Structure", score: scoreBreakdown.resumeStructure || 0, max: 10 },
            { category: "Formatting Quality", score: scoreBreakdown.formattingQuality || 0, max: 5 }
        ],
        matchedKeywords: apiData.matchedKeywords || [],
        missingKeywords: apiData.missingKeywords || [],
        detectedSkills: apiData.detectedSkills || [],
        resumeFormatSuggestions: apiData.resumeFormatSuggestions || [],
        hrQuestions: apiData.hrQuestions || [],
        aiAnalysis: apiData.aiAnalysis || null
    };

    renderResults(formattedData);
    setupReportDownloads(formattedData);

    function renderResults(data) {
        // 1. Overall Score
        document.getElementById('overall-score').textContent = data.overallScore;
        document.getElementById('score-status').textContent = data.status;

        // 2. Score Breakdown
        const breakdownContainer = document.getElementById('score-breakdown-container');
        data.breakdown.forEach(item => {
            const percentage = item.max > 0 ? Math.round((item.score / item.max) * 100) : 0;
            const html = `
                <div class="col-md-6">
                    <div class="d-flex justify-content-between mb-1">
                        <span class="fw-medium text-sm">${item.category}</span>
                        <span class="fw-bold text-primary">${item.score}/${item.max}</span>
                    </div>
                    <div class="progress">
                        <div class="progress-bar" role="progressbar" style="width: ${percentage}%" aria-valuenow="${percentage}" aria-valuemin="0" aria-valuemax="100"></div>
                    </div>
                </div>
            `;
            breakdownContainer.insertAdjacentHTML('beforeend', html);
        });

        // If external review is unavailable, show banner and exit early
        const verdictContainer = document.getElementById('ai-verdict-container');
        const verdictText = document.getElementById('ai-verdict-text');
        
        if (!data.aiAnalysis) {
            verdictContainer.style.display = 'block';
            verdictContainer.classList.remove('bg-light', 'border');
            verdictContainer.classList.add('alert', 'alert-warning');
            verdictText.textContent = "Detailed review is temporarily unavailable. Your ATS results are still available below.";
            renderDeterministicResults(data);
            return;
        }

        // 3. Review summary
        verdictContainer.style.display = 'block';
        verdictText.textContent = cleanText(data.aiAnalysis.overallAssessment || data.aiAnalysis.jobMatchExplanation || "Review completed.");

        // 4. Strengths & Weaknesses
        const strengthsList = document.getElementById('strengths-list');
        (data.aiAnalysis.strengths || []).forEach(s => {
            strengthsList.insertAdjacentHTML('beforeend', `<li><strong>${escapeHtml(s.title || 'Strength')}:</strong> ${escapeHtml(s.explanation || s.evidence || '')}</li>`);
        });

        const weaknessesList = document.getElementById('weaknesses-list');
        (data.aiAnalysis.weaknesses || []).forEach(w => {
            weaknessesList.insertAdjacentHTML('beforeend', `<li><strong>${escapeHtml(w.title || 'Weakness')}:</strong> ${escapeHtml(w.explanation || w.evidence || '')}</li>`);
        });

        // 5. Semantic Matches
        const semanticContainer = document.getElementById('semantic-matches-container');
        (data.aiAnalysis.semanticMatches || []).forEach(m => {
            semanticContainer.insertAdjacentHTML('beforeend', `<div class="mb-2"><span class="badge bg-success me-2">Match</span><span class="text-sm">${escapeHtml(m.jobRequirement || '')} -> ${escapeHtml(m.resumeEvidence || '')}</span></div>`);
        });
        if (!data.aiAnalysis.semanticMatches || data.aiAnalysis.semanticMatches.length === 0) {
            semanticContainer.innerHTML = '<span class="text-muted text-sm">No strong semantic matches found.</span>';
        }

        // 6. Missing Keywords
        const keywordsContainer = document.getElementById('missing-keywords-container');
        (data.aiAnalysis.missingKeywords || []).forEach(k => {
            keywordsContainer.insertAdjacentHTML('beforeend', `<span class="keyword-chip missing" title="${escapeHtml(k.reason || '')}">${escapeHtml(k.keyword || '')}</span>`);
        });
        if (!data.aiAnalysis.missingKeywords || data.aiAnalysis.missingKeywords.length === 0) {
            keywordsContainer.innerHTML = '<span class="text-muted text-sm">No critical keywords missing.</span>';
        }

        // 7. Improvements (Section-based)
        const improvementsContainer = document.getElementById('improvements-container');
        (data.aiAnalysis.improvements || []).forEach(imp => {
            const html = `
                <div class="improvement-card">
                    <h6 class="text-primary mb-3">Section: ${escapeHtml(imp.section || 'Resume')}</h6>
                    <div class="row">
                        <div class="col-md-6 mb-3 mb-md-0">
                            <div class="improvement-label label-problem">Problem</div>
                            <p class="mb-3 text-sm">${escapeHtml(imp.problem || '')}</p>
                            <div class="text-muted text-xs">Why: ${escapeHtml(imp.whyItMatters || '')}</div>
                        </div>
                        <div class="col-md-6">
                            <div class="improvement-label label-suggestion">Recommendation</div>
                            <p class="mb-0 text-sm">${escapeHtml(imp.recommendation || '')}</p>
                        </div>
                    </div>
                </div>
            `;
            improvementsContainer.insertAdjacentHTML('beforeend', html);
        });

        // 8. Bullet Improvements
        const bulletContainer = document.getElementById('bullet-improvements-container');
        (data.aiAnalysis.bulletImprovements || []).forEach(b => {
            const html = `
                <div class="improvement-card">
                    <div class="row">
                        <div class="col-md-6 mb-3 mb-md-0">
                            <div class="improvement-label label-current">Before</div>
                            <p class="mb-2 text-sm">"${escapeHtml(b.original || '')}"</p>
                        </div>
                        <div class="col-md-6">
                            <div class="improvement-label label-suggestion">After</div>
                            <p class="mb-2 text-sm">"${escapeHtml(b.improved || '')}"</p>
                        </div>
                    </div>
                    <div class="text-muted text-xs mt-2 border-top pt-2"><strong>Why:</strong> ${escapeHtml(b.reason || '')}</div>
                </div>
            `;
            bulletContainer.insertAdjacentHTML('beforeend', html);
        });

        // 9. Final Recommendations
        renderFormatSuggestions(data.aiAnalysis.resumeFormatSuggestions || data.resumeFormatSuggestions);
        renderHrQuestions(data.aiAnalysis.hrQuestions || data.hrQuestions);

        const finalContainer = document.getElementById('final-recommendations-container');
        (data.aiAnalysis.finalRecommendations || []).forEach(rec => {
            finalContainer.insertAdjacentHTML('beforeend', `<li class="mb-2 text-sm">${escapeHtml(rec)}</li>`);
        });
    }

    function renderDeterministicResults(data) {
        const strengthsList = document.getElementById('strengths-list');
        if (data.detectedSkills.length > 0) {
            strengthsList.insertAdjacentHTML('beforeend', `<li><strong>Detected Skills:</strong> ${escapeHtml(data.detectedSkills.join(', '))}</li>`);
        } else {
            strengthsList.insertAdjacentHTML('beforeend', '<li><strong>Readable Resume:</strong> Resume text was parsed successfully for ATS scoring.</li>');
        }

        const weaknessesList = document.getElementById('weaknesses-list');
        if (data.missingKeywords.length > 0) {
            weaknessesList.insertAdjacentHTML('beforeend', `<li><strong>Keyword Gaps:</strong> ${escapeHtml(data.missingKeywords.slice(0, 12).join(', '))}</li>`);
        } else {
            weaknessesList.insertAdjacentHTML('beforeend', '<li><strong>Keyword Coverage:</strong> No critical deterministic keyword gaps were found.</li>');
        }

        const semanticContainer = document.getElementById('semantic-matches-container');
        if (data.matchedKeywords.length > 0) {
            semanticContainer.innerHTML = data.matchedKeywords.slice(0, 12)
                .map(keyword => `<span class="keyword-chip">${escapeHtml(keyword)}</span>`)
                .join('');
        } else {
            semanticContainer.innerHTML = '<span class="text-muted text-sm">Add a job description to compare keyword alignment.</span>';
        }

        const keywordsContainer = document.getElementById('missing-keywords-container');
        if (data.missingKeywords.length > 0) {
            keywordsContainer.innerHTML = data.missingKeywords.slice(0, 20)
                .map(keyword => `<span class="keyword-chip missing">${escapeHtml(keyword)}</span>`)
                .join('');
        } else {
            keywordsContainer.innerHTML = '<span class="text-muted text-sm">No critical keywords missing.</span>';
        }

        document.getElementById('improvements-container').innerHTML =
            '<p class="text-muted text-sm">Configure GEMINI_API_KEY to receive detailed section improvements.</p>';
        document.getElementById('bullet-improvements-container').innerHTML =
            '<p class="text-muted text-sm">Configure GEMINI_API_KEY to receive detailed bullet rewrites.</p>';
        renderFormatSuggestions(data.resumeFormatSuggestions);
        renderHrQuestions(data.hrQuestions);
        document.getElementById('final-recommendations-container').innerHTML =
            '<li class="mb-2 text-sm">Review missing keywords and add only the ones that honestly reflect your experience.</li>';
    }

    function renderFormatSuggestions(suggestions) {
        const container = document.getElementById('format-suggestions-container');
        if (!container) return;

        if (!suggestions || suggestions.length === 0) {
            container.innerHTML = '<p class="text-muted text-sm">Paste the company resume format before analyzing to get format-specific guidance.</p>';
            return;
        }

        container.innerHTML = suggestions.map(item => `
            <div class="improvement-card">
                <h6 class="text-primary mb-3">Section: ${escapeHtml(item.section || 'Resume Format')}</h6>
                <div class="row">
                    <div class="col-md-6 mb-3 mb-md-0">
                        <div class="improvement-label label-problem">Current Fit</div>
                        <p class="mb-0 text-sm">${escapeHtml(item.currentIssue || item.problem || 'Review this section against the company format.')}</p>
                    </div>
                    <div class="col-md-6">
                        <div class="improvement-label label-suggestion">Change</div>
                        <p class="mb-0 text-sm">${escapeHtml(item.recommendation || 'Align heading, order, and bullet style to the company template.')}</p>
                    </div>
                </div>
            </div>
        `).join('');
    }

    function renderHrQuestions(questions) {
        const container = document.getElementById('hr-questions-container');
        if (!container) return;

        if (!questions || questions.length === 0) {
            container.innerHTML = '<p class="text-muted text-sm">Upload a detailed resume to generate likely HR questions.</p>';
            return;
        }

        container.innerHTML = questions.map(item => `
            <div class="improvement-card">
                <h6 class="mb-3">${escapeHtml(item.question || 'Tell me about yourself.')}</h6>
                <div class="row">
                    <div class="col-md-6 mb-3 mb-md-0">
                        <div class="improvement-label label-current">Why HR May Ask</div>
                        <p class="mb-0 text-sm">${escapeHtml(item.whyAsked || 'To understand your fit for the role.')}</p>
                    </div>
                    <div class="col-md-6">
                        <div class="improvement-label label-suggestion">Answer Focus</div>
                        <p class="mb-0 text-sm">${escapeHtml(item.answerFocus || 'Use a specific resume example and keep it concise.')}</p>
                    </div>
                </div>
            </div>
        `).join('');
    }

    function escapeHtml(value) {
        return cleanText(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    function cleanText(value) {
        return String(value || '').replace(/\bAI\b/gi, 'Automated');
    }

    function setupReportDownloads(data) {
        const pdfButton = document.getElementById('download-pdf-btn');
        const wordButton = document.getElementById('download-word-btn');

        if (pdfButton) {
            pdfButton.addEventListener('click', async () => {
                await handleDownload(pdfButton, '/api/resume/report/pdf', 'resume-ats-report.pdf', 'application/pdf', 'PDF');
            });
        }

        if (wordButton) {
            wordButton.addEventListener('click', async () => {
                await handleDownload(wordButton, '/api/resume/report/word', 'resume-ats-report.docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'Word');
            });
        }
    }

    async function handleDownload(btn, path, fileName, mimeType, label) {
        const originalText = btn.textContent;
        btn.disabled = true;
        btn.textContent = `Generating ${label}…`;

        try {
            await downloadReport(path, fileName, mimeType);
        } catch (err) {
            showDownloadError(`Could not generate ${label} report. Please try again.`);
        } finally {
            btn.disabled = false;
            btn.textContent = originalText;
        }
    }

    function showDownloadError(msg) {
        // Reuse or create a small error banner near the download buttons
        let banner = document.getElementById('download-error-msg');
        if (!banner) {
            banner = document.createElement('p');
            banner.id = 'download-error-msg';
            banner.style.cssText = 'color:#EF4444;font-size:0.85rem;margin-top:0.5rem;text-align:center;';
            const btnRow = document.querySelector('.d-flex.flex-wrap.justify-content-center.gap-2');
            if (btnRow) btnRow.insertAdjacentElement('afterend', banner);
        }
        banner.textContent = msg;
        setTimeout(() => { banner.textContent = ''; }, 5000);
    }

    async function downloadReport(path, fileName, mimeType) {
        const response = await fetch(getReportUrl(path), {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: storedData
        });

        if (!response.ok) {
            throw new Error('Unable to generate report.');
        }

        const blob = await response.blob();
        downloadBlob(blob, fileName, mimeType);
    }

    function getReportUrl(path) {
        if (window.location.protocol === 'file:') {
            return `http://localhost:8080${path}`;
        }
        // Always use the full Railway URL — frontend is on Vercel (different origin)
        return `https://pilot-ats-production-1859.up.railway.app${path}`;
    }

    function downloadBlob(blob, fileName, mimeType) {
        const typedBlob = blob.type ? blob : new Blob([blob], { type: mimeType });
        const url = URL.createObjectURL(typedBlob);

        // iOS Safari silently ignores programmatic link.click() for downloads.
        // Android Chrome supports it but benefits from the same approach.
        const isMobile = /iPhone|iPad|iPod|Android/i.test(navigator.userAgent);

        if (isMobile) {
            // Open blob URL in the same tab — browser will prompt Save / Share sheet
            window.location.href = url;
            // Delay revoke so the browser has time to start the download
            setTimeout(() => URL.revokeObjectURL(url), 5000);
        } else {
            const link = document.createElement('a');
            link.href = url;
            link.download = fileName;
            document.body.appendChild(link);
            link.click();
            link.remove();
            // Small delay before revoke so the download starts cleanly
            setTimeout(() => URL.revokeObjectURL(url), 1000);
        }
    }
});
