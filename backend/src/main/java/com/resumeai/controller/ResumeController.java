package com.resumeai.controller;

import com.resumeai.dto.ResumeAnalysisResponse;
import com.resumeai.service.ReportExportService;
import com.resumeai.service.ResumeAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.resumeai.service.RagService;
import com.resumeai.model.ResumeData;
import com.resumeai.service.ResumeParserService;
import java.util.Map;
import java.util.HashMap;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @Autowired
    private ResumeAnalysisService analysisService;

    @Autowired
    private RagService ragService;

    @Autowired
    private ResumeParserService parserService;

    @Autowired
    private ReportExportService reportExportService;

    @PostMapping("/analyze")
    public ResponseEntity<ResumeAnalysisResponse> analyzeResume(
            @RequestParam("resume") MultipartFile resume,
            @RequestParam(value = "jobDescription", required = false) String jobDescription,
            @RequestParam(value = "companyResumeFormat", required = false) String companyResumeFormat) throws Exception {
        
        String jd = jobDescription != null ? jobDescription : "";
        String format = companyResumeFormat != null ? companyResumeFormat : "";
        ResumeAnalysisResponse response = analysisService.analyze(resume, jd, format);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/report/pdf")
    public ResponseEntity<byte[]> downloadPdfReport(@RequestBody ResumeAnalysisResponse response) throws Exception {
        byte[] report = reportExportService.generatePdfReport(response);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume-ats-report.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(report);
    }

    @PostMapping("/report/word")
    public ResponseEntity<byte[]> downloadWordReport(@RequestBody ResumeAnalysisResponse response) throws Exception {
        byte[] report = reportExportService.generateWordReport(response);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume-ats-report.docx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(report);
    }
    @PostMapping("/upload-to-db")
    public ResponseEntity<Map<String, String>> uploadToVectorStore(@RequestParam("resume") MultipartFile resume) throws Exception {
        // Parse the resume
        ResumeData resumeData = parserService.parseResume(resume);
        
        // Index into Vector Store
        ragService.indexResume(resumeData, resume.getOriginalFilename());
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Resume uploaded and indexed successfully into the vector store.");
        response.put("filename", resume.getOriginalFilename());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search-db")
    public ResponseEntity<Map<String, Object>> searchVectorStore(@RequestParam("jobDescription") String jobDescription,
                                                                 @RequestParam(value = "topK", defaultValue = "3") int topK) {
        
        Map<String, Object> response = ragService.searchResumes(jobDescription, topK);
        return ResponseEntity.ok(response);
    }
}
