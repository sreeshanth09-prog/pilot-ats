package com.resumeai.util;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class KeywordMatcher {
    
    // Configurable skill dictionary
    private static final List<String> SKILL_DICTIONARY = Arrays.asList(
        // Programming Languages
        "Java", "Python", "JavaScript", "TypeScript", "C++", "C#", "Ruby", "Go", "Rust", "PHP",
        // Frameworks
        "Spring Boot", "Spring", "React", "Angular", "Vue", "Node.js", "Django", "Flask", "Express",
        // Databases
        "MySQL", "PostgreSQL", "MongoDB", "Redis", "Oracle", "SQL Server", "Cassandra",
        // Cloud & DevOps
        "AWS", "Azure", "GCP", "Docker", "Kubernetes", "Jenkins", "Git", "GitHub", "GitLab", "CI/CD", "Terraform",
        // Architecture & Design
        "Microservices", "REST API", "GraphQL", "Agile", "Scrum",
        // Tools & Testing
        "JUnit", "Mockito", "Selenium", "Jest", "Maven", "Gradle"
    );

    public static List<String> detectSkills(String text) {
        String normalizedText = " " + TextUtils.normalize(text) + " ";
        return SKILL_DICTIONARY.stream()
                .filter(skill -> normalizedText.contains(" " + TextUtils.normalize(skill) + " "))
                .collect(Collectors.toList());
    }

    public static List<String> findMatchedKeywords(List<String> requiredKeywords, String resumeText) {
        String normalizedResume = " " + TextUtils.normalize(resumeText) + " ";
        return requiredKeywords.stream()
                .filter(kw -> normalizedResume.contains(" " + kw + " "))
                .collect(Collectors.toList());
    }

    public static List<String> findMissingKeywords(List<String> requiredKeywords, String resumeText) {
        String normalizedResume = " " + TextUtils.normalize(resumeText) + " ";
        return requiredKeywords.stream()
                .filter(kw -> !normalizedResume.contains(" " + kw + " "))
                .collect(Collectors.toList());
    }
}
