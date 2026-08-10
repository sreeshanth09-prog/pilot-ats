package com.resumeai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeai.dto.GeminiResponseDto;
import com.resumeai.model.ATSResult;
import com.resumeai.model.ResumeData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiResponseDto analyzeResume(ResumeData resumeData, String jobDescription, String companyResumeFormat, ATSResult atsResult) {
        if (apiKey == null || apiKey.equals("missing_key") || apiKey.isEmpty()) {
            return null;
        }

        try {
            String prompt = buildPrompt(resumeData, jobDescription, companyResumeFormat, atsResult);
            Map<String, Object> requestBody = buildRequestBody(prompt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-goog-api-key", apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Call Gemini API
            Map<String, Object> response = restTemplate.postForObject(apiUrl, request, Map.class);
            return parseResponse(response);

        } catch (Exception e) {
            System.err.println("Gemini API Error: " + e.getMessage());
            return null; // Return null to indicate fallback to deterministic results
        }
    }

    private String buildPrompt(ResumeData resumeData, String jobDescription, String companyResumeFormat, ATSResult atsResult) {
        return "You are an expert Applicant Tracking System evaluator, professional resume reviewer, and technical recruiter.\n\n" +
               "Analyze the candidate's resume against the target job description and the hiring company's resume format instructions.\n\n" +
               "CRITICAL RULES:\n" +
               "1. Never invent candidate experience, employers, projects, certifications, technologies, achievements, or numerical metrics.\n" +
               "2. Never claim a skill exists if it is not supported by the resume.\n" +
               "3. Do not recommend adding a skill unless the job description requires it AND the candidate appears to have related evidence.\n" +
               "4. Preserve factual accuracy. Improve wording without changing factual meaning.\n" +
               "5. Do not change dates, companies, job titles, or qualifications.\n" +
               "6. If a company resume format is supplied, recommend section order, heading changes, and content placement according to that format.\n" +
               "7. Generate realistic HR interview questions based only on the resume and target job description.\n" +
               "8. Return valid JSON only, without any markdown blocks like ```json.\n\n" +
               "The deterministic ATS score supplied by the application is authoritative. Do not replace it. Score: " + 
               (atsResult.getKeywordMatch() + atsResult.getSkillsMatch() + atsResult.getJobDescriptionMatch() + 
                atsResult.getExperienceRelevance() + atsResult.getAtsCompatibility() + atsResult.getResumeStructure() + 
                atsResult.getFormattingQuality()) + "/100\n\n" +
               "SCHEMA:\n" +
               "{\n" +
               "  \"overallAssessment\": \"...\",\n" +
               "  \"jobMatchExplanation\": \"...\",\n" +
               "  \"strengths\": [ {\"title\": \"...\", \"explanation\": \"...\", \"evidence\": \"...\"} ],\n" +
               "  \"weaknesses\": [ {\"title\": \"...\", \"explanation\": \"...\", \"evidence\": \"...\"} ],\n" +
               "  \"missingKeywords\": [ {\"keyword\": \"...\", \"importance\": \"HIGH\", \"reason\": \"...\"} ],\n" +
               "  \"semanticMatches\": [ {\"jobRequirement\": \"...\", \"resumeEvidence\": \"...\", \"matchLevel\": \"STRONG\"} ],\n" +
               "  \"improvements\": [ {\"section\": \"...\", \"problem\": \"...\", \"whyItMatters\": \"...\", \"recommendation\": \"...\"} ],\n" +
               "  \"bulletImprovements\": [ {\"original\": \"...\", \"improved\": \"...\", \"reason\": \"...\"} ],\n" +
               "  \"resumeFormatSuggestions\": [ {\"section\": \"...\", \"currentIssue\": \"...\", \"recommendation\": \"...\"} ],\n" +
               "  \"hrQuestions\": [ {\"question\": \"...\", \"whyAsked\": \"...\", \"answerFocus\": \"...\"} ],\n" +
               "  \"summaryImprovement\": {\"original\": \"...\", \"improved\": \"...\", \"reason\": \"...\"},\n" +
               "  \"finalRecommendations\": [\"...\"]\n" +
               "}\n\n" +
               "JOB DESCRIPTION:\n" + (jobDescription == null || jobDescription.trim().isEmpty() ? "None provided." : jobDescription) + "\n\n" +
               "HIRING COMPANY RESUME FORMAT:\n" + (companyResumeFormat == null || companyResumeFormat.trim().isEmpty() ? "None provided." : companyResumeFormat) + "\n\n" +
               "RESUME EXTRACTED TEXT:\n" + resumeData.getRawText();
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> content = new HashMap<>();
        Map<String, Object> parts = new HashMap<>();
        parts.put("text", prompt);
        content.put("parts", List.of(parts));

        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("response_mime_type", "application/json");

        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(content));
        body.put("generationConfig", generationConfig);

        return body;
    }

    private GeminiResponseDto parseResponse(Map<String, Object> apiResponse) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) apiResponse.get("candidates");
            if (candidates == null || candidates.isEmpty()) return null;

            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            String rawText = (String) parts.get(0).get("text");

            // Sanitize JSON
            String jsonStr = rawText.trim();
            if (jsonStr.startsWith("```json")) {
                jsonStr = jsonStr.substring(7);
            } else if (jsonStr.startsWith("```")) {
                jsonStr = jsonStr.substring(3);
            }
            if (jsonStr.endsWith("```")) {
                jsonStr = jsonStr.substring(0, jsonStr.length() - 3);
            }

            return objectMapper.readValue(jsonStr.trim(), GeminiResponseDto.class);
        } catch (Exception e) {
            System.err.println("Failed to parse Gemini JSON: " + e.getMessage());
            return null;
        }
    }
}
