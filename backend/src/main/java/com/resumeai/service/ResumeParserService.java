package com.resumeai.service;

import com.resumeai.model.ResumeData;
import com.resumeai.util.TextUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeParserService {

    public ResumeData parseResume(MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        String filename = originalFilename == null ? "" : originalFilename.toLowerCase();
        String rawText = "";

        if (filename.endsWith(".pdf")) {
            rawText = parsePdf(file.getInputStream());
        } else if (filename.endsWith(".docx")) {
            rawText = parseDocx(file.getInputStream());
        } else if (filename.endsWith(".txt")) {
            rawText = parseTxt(file.getInputStream());
        }

        Map<String, String> sections = extractSections(rawText);
        int wordCount = TextUtils.countWords(rawText);

        return new ResumeData(rawText, sections, wordCount);
    }

    private String parsePdf(InputStream is) throws Exception {
        try (PDDocument document = PDDocument.load(is)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String parseDocx(InputStream is) throws Exception {
        try (XWPFDocument document = new XWPFDocument(is);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String parseTxt(InputStream is) throws Exception {
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private Map<String, String> extractSections(String text) {
        Map<String, String> sections = new HashMap<>();
        // Basic heuristic: look for common section headers followed by newlines.
        // This is a simple regex approach for deterministic extraction.
        String[] possibleHeaders = {
            "CONTACT", "SUMMARY", "OBJECTIVE", "EDUCATION", "EXPERIENCE", 
            "WORK EXPERIENCE", "PROFESSIONAL EXPERIENCE", "SKILLS", "PROJECTS", 
            "CERTIFICATIONS", "ACHIEVEMENTS", "LANGUAGES", "INTERESTS"
        };
        
        String lines[] = text.split("\\r?\\n");
        String currentSection = "UNKNOWN";
        StringBuilder sectionContent = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim().toUpperCase();
            boolean isHeader = false;
            for (String header : possibleHeaders) {
                if (trimmed.equals(header) || trimmed.equals(header + ":")) {
                    // Normalize variants to canonical names
                    if (header.contains("EXPERIENCE")) header = "EXPERIENCE";
                    if (header.equals("WORK EXPERIENCE")) header = "EXPERIENCE";
                    
                    if (sectionContent.length() > 0) {
                        sections.put(currentSection, sectionContent.toString());
                        sectionContent = new StringBuilder();
                    }
                    currentSection = header;
                    isHeader = true;
                    break;
                }
            }
            if (!isHeader) {
                sectionContent.append(line).append("\n");
            }
        }
        if (sectionContent.length() > 0) {
            sections.put(currentSection, sectionContent.toString());
        }
        
        return sections;
    }
}
