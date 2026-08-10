package com.resumeai.model;

public class ATSResult {
    private int keywordMatch;
    private int skillsMatch;
    private int jobDescriptionMatch;
    private int experienceRelevance;
    private int atsCompatibility;
    private int resumeStructure;
    private int formattingQuality;

    // Getters and Setters
    public int getKeywordMatch() { return keywordMatch; }
    public void setKeywordMatch(int keywordMatch) { this.keywordMatch = keywordMatch; }
    
    public int getSkillsMatch() { return skillsMatch; }
    public void setSkillsMatch(int skillsMatch) { this.skillsMatch = skillsMatch; }
    
    public int getJobDescriptionMatch() { return jobDescriptionMatch; }
    public void setJobDescriptionMatch(int jobDescriptionMatch) { this.jobDescriptionMatch = jobDescriptionMatch; }
    
    public int getExperienceRelevance() { return experienceRelevance; }
    public void setExperienceRelevance(int experienceRelevance) { this.experienceRelevance = experienceRelevance; }
    
    public int getAtsCompatibility() { return atsCompatibility; }
    public void setAtsCompatibility(int atsCompatibility) { this.atsCompatibility = atsCompatibility; }
    
    public int getResumeStructure() { return resumeStructure; }
    public void setResumeStructure(int resumeStructure) { this.resumeStructure = resumeStructure; }
    
    public int getFormattingQuality() { return formattingQuality; }
    public void setFormattingQuality(int formattingQuality) { this.formattingQuality = formattingQuality; }
}
