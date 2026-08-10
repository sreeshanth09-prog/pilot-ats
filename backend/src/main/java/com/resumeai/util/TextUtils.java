package com.resumeai.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TextUtils {
    
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "the", "and", "for", "with", "from", "this", "that", "etc", "a", "an", "of", "in", "to", "is", "on", "it", "as", "by", "or"
    ));

    public static String normalize(String text) {
        if (text == null) return "";
        return text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
    }

    public static List<String> extractKeywords(String text) {
        String normalized = normalize(text);
        return Arrays.stream(normalized.split(" "))
                     .filter(word -> word.length() > 2) // Ignore very short words
                     .filter(word -> !STOP_WORDS.contains(word))
                     .distinct()
                     .collect(Collectors.toList());
    }

    public static int countWords(String text) {
        if (text == null || text.trim().isEmpty()) return 0;
        return text.trim().split("\\s+").length;
    }
}
