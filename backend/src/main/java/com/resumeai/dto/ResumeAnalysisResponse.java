package com.resumeai.dto;

import com.resumeai.model.ATSResult;
import java.util.List;
import java.util.Map;

public class ResumeAnalysisResponse {
    private int atsScore;
    private String status;
    private ATSResult scoreBreakdown;
    private List<String> matchedKeywords;
    private List<String> missingKeywords;
    private List<String> detectedSkills;
    private List<Map<String, String>> resumeFormatSuggestions;
    private List<Map<String, String>> hrQuestions;
    private GeminiResponseDto aiAnalysis;
    
    // Getters and Setters
    public int getAtsScore() { return atsScore; }
    public void setAtsScore(int atsScore) { this.atsScore = atsScore; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public ATSResult getScoreBreakdown() { return scoreBreakdown; }
    public void setScoreBreakdown(ATSResult scoreBreakdown) { this.scoreBreakdown = scoreBreakdown; }
    
    public List<String> getMatchedKeywords() { return matchedKeywords; }
    public void setMatchedKeywords(List<String> matchedKeywords) { this.matchedKeywords = matchedKeywords; }
    
    public List<String> getMissingKeywords() { return missingKeywords; }
    public void setMissingKeywords(List<String> missingKeywords) { this.missingKeywords = missingKeywords; }
    
    public List<String> getDetectedSkills() { return detectedSkills; }
    public void setDetectedSkills(List<String> detectedSkills) { this.detectedSkills = detectedSkills; }

    public List<Map<String, String>> getResumeFormatSuggestions() { return resumeFormatSuggestions; }
    public void setResumeFormatSuggestions(List<Map<String, String>> resumeFormatSuggestions) { this.resumeFormatSuggestions = resumeFormatSuggestions; }

    public List<Map<String, String>> getHrQuestions() { return hrQuestions; }
    public void setHrQuestions(List<Map<String, String>> hrQuestions) { this.hrQuestions = hrQuestions; }
    
    public GeminiResponseDto getAiAnalysis() { return aiAnalysis; }
    public void setAiAnalysis(GeminiResponseDto aiAnalysis) { this.aiAnalysis = aiAnalysis; }
}
