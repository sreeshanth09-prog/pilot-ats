package com.resumeai.service;

import com.resumeai.dto.GeminiResponseDto;
import com.resumeai.dto.ResumeAnalysisResponse;
import com.resumeai.model.ATSResult;
import com.resumeai.model.ResumeData;
import com.resumeai.util.KeywordMatcher;
import com.resumeai.util.TextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class ResumeAnalysisService {

    @Autowired
    private FileValidationService validationService;

    @Autowired
    private ResumeParserService parserService;

    @Autowired
    private ATSScoringService scoringService;

    @Autowired
    private GeminiService geminiService;

    public ResumeAnalysisResponse analyze(MultipartFile resume, String jobDescription, String companyResumeFormat) throws Exception {
        
        // 1. Validate
        validationService.validate(resume);
        
        // 2. Parse
        ResumeData resumeData = parserService.parseResume(resume);
        
        // 3. Keyword / Skill Matching
        List<String> requiredKeywords = TextUtils.extractKeywords(jobDescription);
        List<String> matchedKeywords = KeywordMatcher.findMatchedKeywords(requiredKeywords, resumeData.getRawText());
        List<String> missingKeywords = KeywordMatcher.findMissingKeywords(requiredKeywords, resumeData.getRawText());
        List<String> detectedSkills = KeywordMatcher.detectSkills(resumeData.getRawText());
        
        // 4. Score
        ATSResult atsResult = scoringService.calculateScore(resumeData, requiredKeywords, matchedKeywords, detectedSkills);
        int totalScore = atsResult.getKeywordMatch() + atsResult.getSkillsMatch() + atsResult.getJobDescriptionMatch() +
                         atsResult.getExperienceRelevance() + atsResult.getAtsCompatibility() + atsResult.getResumeStructure() +
                         atsResult.getFormattingQuality();
                         
        // 5. Construct Response
        ResumeAnalysisResponse response = new ResumeAnalysisResponse();
        response.setAtsScore(totalScore);
        response.setStatus(totalScore >= 80 ? "Excellent Match" : (totalScore >= 60 ? "Good Match" : "Needs Improvement"));
        response.setScoreBreakdown(atsResult);
        response.setMatchedKeywords(matchedKeywords);
        response.setMissingKeywords(missingKeywords);
        response.setDetectedSkills(detectedSkills);
        List<Map<String, String>> deterministicFormatSuggestions = buildFormatSuggestions(resumeData, companyResumeFormat);
        List<Map<String, String>> deterministicHrQuestions = buildHrQuestions(resumeData, detectedSkills, matchedKeywords, missingKeywords, jobDescription);
        response.setResumeFormatSuggestions(deterministicFormatSuggestions);
        response.setHrQuestions(deterministicHrQuestions);
        
        // 6. Gemini Semantic Analysis
        GeminiResponseDto aiResponse = geminiService.analyzeResume(resumeData, jobDescription, companyResumeFormat, atsResult);
        
        // 7. Hallucination Prevention (Sanitize AI Response against deterministic data)
        if (aiResponse != null) {
            sanitizeAiResponse(aiResponse, detectedSkills, resumeData.getRawText());
            if (aiResponse.getResumeFormatSuggestions() != null && !aiResponse.getResumeFormatSuggestions().isEmpty()) {
                response.setResumeFormatSuggestions(mergeGuidance(aiResponse.getResumeFormatSuggestions(), deterministicFormatSuggestions, "section"));
            }
            if (aiResponse.getHrQuestions() != null && !aiResponse.getHrQuestions().isEmpty()) {
                response.setHrQuestions(mergeGuidance(aiResponse.getHrQuestions(), deterministicHrQuestions, "question"));
            }
        }
        
        response.setAiAnalysis(aiResponse);
        
        return response;
    }

    private void sanitizeAiResponse(GeminiResponseDto aiResponse, List<String> detectedSkills, String rawText) {
        // Prevent Gemini from asserting a skill is missing if we definitely found it deterministically
        if (aiResponse.getMissingKeywords() != null) {
            aiResponse.getMissingKeywords().removeIf(kw -> {
                String k = kw.get("keyword");
                return k != null && detectedSkills.stream().anyMatch(s -> s.equalsIgnoreCase(k));
            });
        }
    }

    private List<Map<String, String>> buildFormatSuggestions(ResumeData resumeData, String companyResumeFormat) {
        if (companyResumeFormat == null || companyResumeFormat.trim().isEmpty()) {
            return List.of(
                    Map.of(
                            "section", "Company Format",
                            "currentIssue", "No hiring-company resume format was provided.",
                            "recommendation", "Paste the company's required section order, template notes, or sample resume format before analyzing."
                    )
            );
        }

        List<String> requestedSections = TextUtils.extractKeywords(companyResumeFormat);
        List<Map<String, String>> suggestions = new java.util.ArrayList<>();

        addSectionSuggestion(suggestions, resumeData, requestedSections, "SUMMARY", "Add or adjust your profile summary to match the company's preferred opening section.");
        addSectionSuggestion(suggestions, resumeData, requestedSections, "SKILLS", "Group skills using the company's labels, then keep only skills you can defend in interview.");
        addSectionSuggestion(suggestions, resumeData, requestedSections, "EXPERIENCE", "Reorder experience bullets so the most relevant responsibilities appear first.");
        addSectionSuggestion(suggestions, resumeData, requestedSections, "PROJECTS", "Add project entries only when the company format asks for projects or technical work samples.");
        addSectionSuggestion(suggestions, resumeData, requestedSections, "EDUCATION", "Keep education in the requested position and use the company's preferred naming.");

        if (suggestions.isEmpty()) {
            suggestions.add(Map.of(
                    "section", "Overall Format",
                    "currentIssue", "Your resume includes the common sections needed for the provided company format.",
                    "recommendation", "Keep the existing content, then adjust section order, headings, and bullet length to match the company template exactly."
            ));
        }

        return suggestions;
    }

    private void addSectionSuggestion(List<Map<String, String>> suggestions, ResumeData resumeData, List<String> requestedSections, String section, String recommendation) {
        boolean requested = requestedSections.stream().anyMatch(keyword -> section.toLowerCase().contains(keyword) || keyword.contains(section.toLowerCase()));
        boolean present = resumeData.getExtractedSections().containsKey(section);

        if (requested && !present) {
            suggestions.add(Map.of(
                    "section", section,
                    "currentIssue", "The company format appears to expect this section, but it was not clearly detected in the resume.",
                    "recommendation", recommendation
            ));
        } else if (requested) {
            suggestions.add(Map.of(
                    "section", section,
                    "currentIssue", "This section exists and should be aligned to the company's expected wording/order.",
                    "recommendation", recommendation
            ));
        }
    }

    private List<Map<String, String>> buildHrQuestions(ResumeData resumeData, List<String> detectedSkills, List<String> matchedKeywords, List<String> missingKeywords, String jobDescription) {
        List<Map<String, String>> questions = new java.util.ArrayList<>();

        questions.add(Map.of(
                "question", "Walk me through your resume and the work you are most proud of.",
                "whyAsked", "HR commonly starts here to check communication, ownership, and clarity.",
                "answerFocus", "Give a short timeline, highlight your strongest role or project, and connect it to the target job."
        ));

        if (!detectedSkills.isEmpty()) {
            String skill = detectedSkills.get(0);
            questions.add(Map.of(
                    "question", "You mentioned " + skill + " in your resume. Can you explain where you used it and what result it produced?",
                    "whyAsked", "Recruiters check whether listed skills are backed by real project or work evidence.",
                    "answerFocus", "Name the project, your responsibility, the challenge, and a measurable or concrete outcome."
            ));
        }

        if (!matchedKeywords.isEmpty()) {
            String keyword = matchedKeywords.get(0);
            questions.add(Map.of(
                    "question", "This role needs " + keyword + ". What experience do you have related to it?",
                    "whyAsked", "The question connects the job description directly to your resume.",
                    "answerFocus", "Use one resume example and show how it maps to the company's requirement."
            ));
        }

        if (!missingKeywords.isEmpty()) {
            String keyword = missingKeywords.get(0);
            questions.add(Map.of(
                    "question", "The job description mentions " + keyword + ". Have you worked with it or something similar?",
                    "whyAsked", "HR may probe gaps between the resume and the job description.",
                    "answerFocus", "Be honest. Explain related experience, learning progress, or how you would ramp up."
            ));
        }

        if (resumeData.getExtractedSections().containsKey("PROJECTS")) {
            questions.add(Map.of(
                    "question", "Which project from your resume best shows your problem-solving ability?",
                    "whyAsked", "Project questions reveal ownership, decision-making, and practical skill depth.",
                    "answerFocus", "Use the STAR method: situation, task, action, result."
            ));
        }

        if (resumeData.getExtractedSections().containsKey("EXPERIENCE")) {
            questions.add(Map.of(
                    "question", "Tell me about a difficult task in your previous experience and how you handled it.",
                    "whyAsked", "HR looks for attitude, teamwork, and accountability.",
                    "answerFocus", "Choose a real challenge, explain your action, and end with what improved."
            ));
        }

        questions.add(Map.of(
                "question", "Why are you interested in this company and this role?",
                "whyAsked", "HR checks motivation and whether you understand the role.",
                "answerFocus", "Connect the company's work, the job description, and two strengths from your resume."
        ));

        return questions;
    }

    private List<Map<String, String>> mergeGuidance(List<Map<String, String>> primary, List<Map<String, String>> fallback, String uniqueKey) {
        List<Map<String, String>> merged = new java.util.ArrayList<>();
        java.util.Set<String> seen = new java.util.HashSet<>();

        addUniqueGuidance(merged, seen, primary, uniqueKey);
        addUniqueGuidance(merged, seen, fallback, uniqueKey);

        return merged;
    }

    private void addUniqueGuidance(List<Map<String, String>> merged, java.util.Set<String> seen, List<Map<String, String>> source, String uniqueKey) {
        for (Map<String, String> item : source) {
            String key = item.getOrDefault(uniqueKey, item.toString()).toLowerCase();
            if (seen.add(key)) {
                merged.add(item);
            }
        }
    }
}
