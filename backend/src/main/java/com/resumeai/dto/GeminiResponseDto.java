package com.resumeai.dto;

import java.util.List;
import java.util.Map;

public class GeminiResponseDto {
    private String overallAssessment;
    private String jobMatchExplanation;
    private List<Map<String, String>> strengths;
    private List<Map<String, String>> weaknesses;
    private List<Map<String, String>> missingKeywords;
    private List<Map<String, String>> semanticMatches;
    private List<Map<String, String>> improvements;
    private List<Map<String, String>> bulletImprovements;
    private List<Map<String, String>> resumeFormatSuggestions;
    private List<Map<String, String>> hrQuestions;
    private Map<String, String> summaryImprovement;
    private List<String> finalRecommendations;

    // Getters and Setters
    public String getOverallAssessment() { return overallAssessment; }
    public void setOverallAssessment(String overallAssessment) { this.overallAssessment = overallAssessment; }

    public String getJobMatchExplanation() { return jobMatchExplanation; }
    public void setJobMatchExplanation(String jobMatchExplanation) { this.jobMatchExplanation = jobMatchExplanation; }

    public List<Map<String, String>> getStrengths() { return strengths; }
    public void setStrengths(List<Map<String, String>> strengths) { this.strengths = strengths; }

    public List<Map<String, String>> getWeaknesses() { return weaknesses; }
    public void setWeaknesses(List<Map<String, String>> weaknesses) { this.weaknesses = weaknesses; }

    public List<Map<String, String>> getMissingKeywords() { return missingKeywords; }
    public void setMissingKeywords(List<Map<String, String>> missingKeywords) { this.missingKeywords = missingKeywords; }

    public List<Map<String, String>> getSemanticMatches() { return semanticMatches; }
    public void setSemanticMatches(List<Map<String, String>> semanticMatches) { this.semanticMatches = semanticMatches; }

    public List<Map<String, String>> getImprovements() { return improvements; }
    public void setImprovements(List<Map<String, String>> improvements) { this.improvements = improvements; }

    public List<Map<String, String>> getBulletImprovements() { return bulletImprovements; }
    public void setBulletImprovements(List<Map<String, String>> bulletImprovements) { this.bulletImprovements = bulletImprovements; }

    public List<Map<String, String>> getResumeFormatSuggestions() { return resumeFormatSuggestions; }
    public void setResumeFormatSuggestions(List<Map<String, String>> resumeFormatSuggestions) { this.resumeFormatSuggestions = resumeFormatSuggestions; }

    public List<Map<String, String>> getHrQuestions() { return hrQuestions; }
    public void setHrQuestions(List<Map<String, String>> hrQuestions) { this.hrQuestions = hrQuestions; }

    public Map<String, String> getSummaryImprovement() { return summaryImprovement; }
    public void setSummaryImprovement(Map<String, String> summaryImprovement) { this.summaryImprovement = summaryImprovement; }

    public List<String> getFinalRecommendations() { return finalRecommendations; }
    public void setFinalRecommendations(List<String> finalRecommendations) { this.finalRecommendations = finalRecommendations; }
}
