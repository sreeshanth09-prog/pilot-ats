package com.resumeai.service;

import com.resumeai.model.ATSResult;
import com.resumeai.model.ResumeData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ATSScoringService {

    public ATSResult calculateScore(ResumeData resumeData, List<String> requiredKeywords, List<String> matchedKeywords, List<String> detectedSkills) {
        ATSResult result = new ATSResult();
        
        // 1. Keyword Match (25 max)
        int kwScore = 0;
        if (requiredKeywords.isEmpty()) {
            kwScore = 25; // default if no JD
        } else {
            double ratio = (double) matchedKeywords.size() / requiredKeywords.size();
            kwScore = (int) Math.min(25, ratio * 25);
        }
        result.setKeywordMatch(kwScore);

        // 2. Skills Match (20 max)
        int skillScore = Math.min(20, detectedSkills.size() * 2); // 2 points per detected skill up to 20
        result.setSkillsMatch(skillScore);

        // 3. Job Description Match (15 max)
        int jdScore = requiredKeywords.isEmpty() ? 15 : (int) Math.min(15, ((double)matchedKeywords.size() / requiredKeywords.size()) * 15);
        result.setJobDescriptionMatch(jdScore);

        // 4. Experience Relevance (15 max)
        int expScore = resumeData.getExtractedSections().containsKey("EXPERIENCE") ? 15 : 5;
        result.setExperienceRelevance(expScore);

        // 5. ATS Compatibility (10 max)
        int atsScore = 10;
        if (resumeData.getWordCount() < 100) atsScore -= 5;
        if (resumeData.getWordCount() > 2000) atsScore -= 3;
        result.setAtsCompatibility(atsScore);

        // 6. Resume Structure (10 max)
        int structureScore = 0;
        if (resumeData.getExtractedSections().containsKey("EDUCATION")) structureScore += 3;
        if (resumeData.getExtractedSections().containsKey("EXPERIENCE")) structureScore += 4;
        if (resumeData.getExtractedSections().containsKey("SKILLS")) structureScore += 3;
        result.setResumeStructure(structureScore);

        // 7. Formatting Quality (5 max)
        int formattingScore = 5; // Assuming text extraction worked well, it's basically readable.
        if (resumeData.getRawText() == null || resumeData.getRawText().trim().isEmpty()) {
            formattingScore = 0;
        }
        result.setFormattingQuality(formattingScore);

        return result;
    }
}
