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

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @Autowired
    private ResumeAnalysisService analysisService;

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
}
