<div align="center">

<img src="frontend/favicon.png" alt="Pilot Logo" width="120" />

# Pilot — AI-Powered ATS Resume Analyzer

**Analyze your resume against any job description, score it with a real ATS engine, and get AI-driven feedback — completely free, no account required.**

[![Live Demo](https://img.shields.io/badge/Live%20Demo-Vercel-black?style=for-the-badge&logo=vercel)](https://pilot-ats.vercel.app)
[![Backend](https://img.shields.io/badge/Backend-Railway-blueviolet?style=for-the-badge&logo=railway)](https://pilot-ats-production-1859.up.railway.app)
[![Java](https://img.shields.io/badge/Java%2017-Spring%20Boot%203.2-orange?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

</div>

---

## What is Pilot?

Pilot is a full-stack Resume Analyzer that combines a **deterministic Java ATS scoring engine** with **Google Gemini AI** to give candidates honest, data-backed feedback on their resumes.

- 📄 Upload a PDF, DOCX, or TXT resume
- 🎯 Paste a job description to compare keyword alignment
- 🏢 Optionally paste the company's required resume format
- 📊 Get a scored breakdown, keyword gaps, bullet rewrites, HR questions, and improvement cards
- 📥 Download the full report as PDF or Word

> **Free · No account · No database · No data stored**

---

## Architecture

```
┌──────────────────────────────────────────────────────┐
│                  User's Browser                      │
│  index.html · results.html · CSS · Vanilla JS        │
│         Deployed on ► Vercel (CDN)                   │
└────────────────────────┬─────────────────────────────┘
                         │ HTTPS (POST multipart/form-data)
┌────────────────────────▼─────────────────────────────┐
│              Spring Boot Backend (Java 17)            │
│   POST /api/resume/analyze                           │
│   POST /api/resume/report/pdf                        │
│   POST /api/resume/report/word                       │
│         Deployed on ► Railway                        │
└───────────┬─────────────────────────┬────────────────┘
            │ Parse & Score           │ Semantic Review
  ┌─────────▼──────────┐   ┌──────────▼──────────────┐
  │  Deterministic ATS │   │  Google Gemini API       │
  │  Engine (Java)     │   │  gemini-2.0-flash        │
  │  PDFBox · POI      │   │  (fallback-safe)         │
  └────────────────────┘   └──────────────────────────┘
```

**Key design decisions:**
- Frontend and backend are deployed independently (Vercel + Railway)
- The server is **fully stateless** — no database, no disk writes, no sessions
- Gemini acts as an *advisor*; the Java engine is the *judge* (score cannot be overridden by AI)
- If Gemini is unavailable or rate-limited, the app **gracefully degrades** to deterministic results

---

## Recent Updates

- **RAG-based Resume Analysis:** Uploaded resumes are now vectorized and stored in a local JSON vector store using Gemini's `text-embedding-004` model. This enables Retrieval-Augmented Generation (RAG) for more context-aware analysis and semantic search across parsed resumes.
- **Premium Glassmorphism UI:** Completely redesigned the frontend with a modern, premium "glassmorphism" aesthetic featuring dynamic mesh background gradients, frosted glass containers, and improved typography (Outfit + Inter).

## Features

| Feature | Details |
|---|---|
| **ATS Score (out of 100)** | Deterministic Java engine — no AI hallucinations |
| **Score Breakdown** | 7 weighted categories (keyword match, skills, experience, structure, etc.) |
| **Keyword Analysis** | Matched keywords + missing keywords highlighted as chips |
| **Semantic Matches** | AI maps job requirements → resume evidence |
| **Strengths & Weaknesses** | AI-identified resume strengths and gaps |
| **Bullet Improvements** | Before/After rewrites of weak resume bullets |
| **Company Format Fit** | Compares resume structure against company's format template |
| **Likely HR Questions** | Generated from your resume + job description |
| **Final Recommendations** | Prioritised action list |
| **PDF & Word Reports** | Full report download — works on mobile and desktop |

---

## ATS Scoring Breakdown

The score (out of 100) is calculated entirely in Java — deterministic, reproducible, and hallucination-free:

| Category | Max Points | What It Measures |
|---|---|---|
| Keyword Match | 25 | How many job description keywords appear in the resume |
| Skills Match | 20 | Detected technical/soft skills vs. required skills |
| Job Description Match | 15 | Semantic alignment with the job description |
| Experience Relevance | 15 | Depth and relevance of work experience |
| ATS Compatibility | 10 | File format, section headers, readability |
| Resume Structure | 10 | Presence of expected sections (Summary, Skills, Experience, Education) |
| Formatting Quality | 5 | Bullet usage, length, and layout readability |

---

## Tech Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Core language |
| Spring Boot | 3.2.x | Web framework + REST API |
| Apache PDFBox | 2.0.29 | PDF parsing |
| Apache POI | 5.2.5 | DOCX parsing |
| Google Gemini API | gemini-2.0-flash | Semantic AI analysis |
| Dotenv-java | 3.0.0 | Local `.env` config loading |

### Frontend
| Technology | Purpose |
|---|---|
| HTML5 + Vanilla JS | Structure and logic |
| CSS3 (custom) | Design system with GPU-composited animations |
| Bootstrap 5 (CDN) | Layout grid only |
| Inter (Google Fonts) | Typography |

### Deployment
| Layer | Platform |
|---|---|
| Frontend | Vercel (CDN, auto-deploy on push) |
| Backend | Railway (Spring Boot JAR) |

---

## Project Structure

```
ATS Resume/
├── README.md
├── frontend/
│   ├── index.html              ← Upload form + loading screen
│   ├── results.html            ← Analysis results page
│   ├── favicon.png             ← Browser tab logo
│   ├── css/
│   │   ├── style.css           ← Design system, GPU-optimised animations
│   │   └── responsive.css      ← Mobile-first responsive layout
│   └── js/
│       ├── app.js              ← App initialisation
│       ├── upload.js           ← Drag-and-drop file handling
│       ├── validation.js       ← Form validation + API call with retry logic
│       └── results.js          ← Results rendering + report downloads
└── backend/
    ├── pom.xml
    ├── .env                    ← Local only — never committed
    └── src/main/
        ├── java/com/resumeai/
        │   ├── controller/     ← REST endpoints
        │   ├── service/        ← Business logic (parsing, scoring, AI, reports)
        │   ├── model/          ← Domain models
        │   ├── dto/            ← Data transfer objects
        │   ├── exception/      ← Global error handling
        │   └── util/           ← Keyword matcher, text utilities
        └── resources/
            └── application.properties
```

---

## Local Development

### Prerequisites

- **Java 17+** — [Adoptium](https://adoptium.net/) or `brew install openjdk@17`
- **Maven 3.8+** — [Apache Maven](https://maven.apache.org/) or `brew install maven`
- **Gemini API Key** — [Get a free key at Google AI Studio](https://aistudio.google.com/app/apikey)

### Setup

**1. Clone the repository**
```bash
git clone https://github.com/sreeshanth09-prog/pilot-ats.git
cd pilot-ats
```

**2. Create the local environment file**
```bash
echo "GEMINI_API_KEY=your_actual_key_here" > backend/.env
```

> `.env` is in `.gitignore` and will never be committed.

**3. Run the backend**
```bash
cd backend
mvn spring-boot:run
```

**4. Open the app**

The frontend is bundled into the Spring Boot JAR at build time — no separate server needed:
```
http://localhost:8080
```

---

## API Reference

### `POST /api/resume/analyze`

Analyzes a resume and returns a full ATS score + AI insights.

| Parameter | Type | Required | Description |
|---|---|---|---|
| `resume` | File (multipart) | ✅ Yes | PDF, DOCX, or TXT — max 10 MB |
| `jobDescription` | String | ❌ No | Full job description for keyword comparison |
| `companyResumeFormat` | String | ❌ No | Company's required resume format/template |

**Response** (JSON):
```json
{
  "atsScore": 74,
  "status": "Good Match",
  "scoreBreakdown": { "keywordMatch": 18, "skillsMatch": 16, "..." : "..." },
  "matchedKeywords": ["Java", "Spring Boot"],
  "missingKeywords": ["Kubernetes", "CI/CD"],
  "detectedSkills": ["Java", "REST API"],
  "aiAnalysis": {
    "overallAssessment": "...",
    "strengths": [...],
    "weaknesses": [...],
    "improvements": [...],
    "bulletImprovements": [...],
    "hrQuestions": [...],
    "finalRecommendations": [...]
  }
}
```

### `POST /api/resume/report/pdf`
Generates a downloadable PDF report.
- **Body**: JSON (same shape as `/analyze` response)
- **Returns**: `application/pdf`

### `POST /api/resume/report/word`
Generates a downloadable Word (.docx) report.
- **Body**: JSON (same shape as `/analyze` response)
- **Returns**: `application/vnd.openxmlformats-officedocument.wordprocessingml.document`

---

## Reliability & Load Handling

Pilot is designed to be stable under concurrent usage.

### Frontend resilience (`validation.js`)
- **Retry with exponential back-off** — Up to 3 automatic retries (2s → 4s → 8s) on network errors and 5xx responses
- **60-second AbortController timeout** per attempt — prevents the browser hanging indefinitely
- **Button disabled during request** — prevents double-submits
- **User-friendly error messages** — distinct copy for timeouts, rate-limits (429), server overload (502/503/504), and network failures

### Concurrent user behaviour
| Scenario | Expected Result |
|---|---|
| 10 users simultaneously | 8–10 succeed fully; 0–2 may fall back to deterministic results if Gemini rate-limits |
| Gemini API hangs | Request times out cleanly; user receives a clear error message |
| User double-clicks Analyze | Second click blocked by disabled button state |
| Network drops mid-request | Frontend auto-retries up to 3 times with back-off |

---

## Mobile Support

The app is fully responsive across all screen sizes (320px to 1440px+):

- **Upload area** — fluid padding using `clamp()`, comfortable on all phone sizes
- **Analyze button** — `min-height: 48px` touch target
- **Keyword chips** — flex-wrap containers, no overflow on narrow screens
- **File name display** — `text-overflow: ellipsis` for long filenames
- **Animations** — GPU-composited via `will-change` and `translateZ(0)` — no layout repaints
- **Report downloads** — iOS Safari uses `window.location.href` (native Save/Share sheet); desktop uses standard anchor download

---

## Privacy & Data Policy

| What | Policy |
|---|---|
| Resume files | Parsed in-memory — **never written to disk** |
| Resume text | Sent to Gemini API under your key; not stored by Pilot |
| Analysis results | Stored in `sessionStorage` (browser only); cleared when tab closes |
| User accounts | None |
| Database | None |
| Analytics / tracking | None |

---

## Limitations

- **Complex PDFs** — Multi-column or image-based PDFs may produce jumbled text via PDFBox. Use single-column, text-based PDFs for best results.
- **Gemini Free Tier** — 15 requests/minute. Under heavy concurrent load, some users may fall back to deterministic-only results. Upgrade to a paid Gemini key to eliminate this limit.
- **Report downloads on iOS** — Files open via the browser's native Share sheet (Save to Files). This is an iOS Safari restriction, not a bug.

---

## License

MIT License — Built and designed by **Sreeshanth**.
