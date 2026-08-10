package com.resumeai.model;

import java.util.List;
import java.util.Map;

public class ResumeData {
    private String rawText;
    private Map<String, String> extractedSections; // e.g., "EXPERIENCE" -> "content..."
    private int wordCount;

    public ResumeData(String rawText, Map<String, String> extractedSections, int wordCount) {
        this.rawText = rawText;
        this.extractedSections = extractedSections;
        this.wordCount = wordCount;
    }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
    public Map<String, String> getExtractedSections() { return extractedSections; }
    public void setExtractedSections(Map<String, String> extractedSections) { this.extractedSections = extractedSections; }
    public int getWordCount() { return wordCount; }
    public void setWordCount(int wordCount) { this.wordCount = wordCount; }
}
