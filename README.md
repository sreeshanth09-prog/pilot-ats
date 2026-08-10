# Pilot — AI-Powered ATS Resume Analyzer

**Pilot** is an AI-powered Resume Analyzer and ATS Scorer designed to help candidates optimize their resumes against specific job descriptions. It combines a deterministic Java ATS scoring engine with Google's Gemini LLM to provide deep semantic insights and actionable improvements.

## 1. Project Overview
Pilot processes PDF, DOCX, and TXT resumes completely in-memory, evaluating them for keyword matches, structural completeness, and readability. It then sends the extracted data to Google's Gemini API for semantic evaluation. The application is completely stateless (no database) and designed with privacy and security in mind.

## 2. Architecture
The architecture follows a classic Spring Boot layered design serving a static HTML/JS frontend:
- **Frontend (`frontend/`)**: HTML5, CSS3, Vanilla JS, and minimal Bootstrap. Served by Spring Boot and interacts directly with the backend via REST API.
- **Backend (`backend/src/main/java/com/resumeai`)**: Spring Boot Java backend.
  - **Controllers**: Exposes `POST /api/resume/analyze`, `POST /api/resume/report/pdf`, `POST /api/resume/report/word`.
  - **Services**: Handles validation, parsing, deterministic scoring, AI orchestration, and report generation.
  - **External API (GeminiService)**: Calls the Gemini API securely without exposing the API key to the client.

## 3. Technologies
- **Backend**: Java 17, Spring Boot 3.2.x, Maven
- **Document Parsing**: Apache PDFBox (PDF), Apache POI (DOCX)
- **AI Integration**: Google Gemini API (via standard REST)
- **Frontend**: HTML5, CSS3, Vanilla JavaScript, Bootstrap 5 (CDN)
- **Environment**: Dotenv-java for local `.env` configuration

## 4. Folder Structure
```
ATS Resume/
├── README.md
├── frontend/
│   ├── index.html
│   ├── results.html
│   ├── css/
│   │   ├── style.css
│   │   └── responsive.css
│   └── js/
│       ├── app.js
│       ├── upload.js
│       ├── validation.js
│       └── results.js
└── backend/
    ├── pom.xml
    ├── .env                  ← Local only, never committed
    └── src/
        └── main/
            ├── java/com/resumeai/
            │   ├── controller/
            │   ├── service/
            │   ├── model/
            │   ├── dto/
            │   ├── exception/
            │   └── util/
            └── resources/
                └── application.properties
```

## 5. Prerequisites

### Java 17
Ensure you have Java 17 or higher installed.
- **Mac (Homebrew)**: `brew install openjdk@17`
- **Windows/Linux**: Download from [Adoptium (Eclipse Temurin)](https://adoptium.net/)

### Maven
- **Mac (Homebrew)**: `brew install maven`
- **Windows/Linux**: Download from [Apache Maven Project](https://maven.apache.org/)

## 6. Configuration — Gemini API Key

1. Create a file named `.env` inside the `backend/` directory:
   ```
   backend/.env
   ```
2. Add your Gemini API key:
   ```env
   GEMINI_API_KEY=your_actual_api_key_here
   ```

> **Note:** `.env` is listed in `.gitignore` and will never be committed to the repository.

You can obtain a free Gemini API key at [Google AI Studio](https://aistudio.google.com/app/apikey).

## 7. How to Run
Navigate to the `backend/` directory in your terminal:
```bash
cd backend
mvn spring-boot:run
```
The application (frontend + API) will be accessible at:

**[http://localhost:8080](http://localhost:8080)**

## 8. API Endpoints

### `POST /api/resume/analyze`
Analyzes a resume and returns ATS scores + AI insights.
- **Content-Type**: `multipart/form-data`
- **Parameters**:
  - `resume` *(File — PDF, DOCX, or TXT, required)*
  - `jobDescription` *(String, optional)*
  - `companyResumeFormat` *(String, optional)*
- **Response**: JSON with ATS score breakdown, matched/missing keywords, detected skills, format suggestions, HR questions, and AI semantic analysis.

### `POST /api/resume/report/pdf`
Generates a downloadable PDF report from a previous analysis result.

### `POST /api/resume/report/word`
Generates a downloadable Word (.docx) report from a previous analysis result.

## 9. ATS Scoring Methodology
The authoritative numerical score (out of 100) is calculated natively in Java without AI hallucinations:

| Category | Max Points |
|---|---|
| Keyword Match | 25 |
| Skills Match | 20 |
| Job Description Match | 15 |
| Experience Relevance | 15 |
| ATS Compatibility | 10 |
| Resume Structure | 10 |
| Formatting Quality | 5 |

## 10. Gemini AI Integration
Gemini acts as an advisor, not the judge. The `GeminiService` constructs a strict prompt incorporating:
- The deterministic Java ATS score (authoritative — Gemini cannot override it)
- The raw extracted resume text
- The target job description

Gemini returns structured JSON with semantic matches, bullet improvements (before/after), missing keywords, and overall recommendations. The backend **sanitizes AI output** (hallucination prevention) to ensure the AI cannot claim skills are missing if the deterministic engine already found them.

## 11. Privacy & No-Database Architecture
Pilot is completely stateless:
- Files are parsed entirely in-memory using `InputStream` — never written to disk.
- Resume text and API responses are returned to the client and immediately discarded by the server.
- No user accounts, database tables, sessions, or permanent file storage are used.

## 12. Limitations
- **PDF Extraction**: Complex multi-column PDFs may produce jumbled text via PDFBox, which can affect deterministic scoring. Use single-column, text-based PDFs for best results.
- **Rate Limits**: The free tier of the Gemini API has request limits. If the API is unavailable or times out, Pilot gracefully degrades and displays only the deterministic Java ATS score.

## 13. License
MIT License — built and designed by **Sreeshanth**.
