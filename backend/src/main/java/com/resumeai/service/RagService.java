package com.resumeai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeai.model.DocumentChunk;
import com.resumeai.model.ResumeData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RagService {

    @Autowired
    private GeminiService geminiService;

    private List<DocumentChunk> vectorStore = new ArrayList<>();
    private final String STORE_PATH = "resumes_vector_store.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        File storeFile = new File(STORE_PATH);
        if (storeFile.exists()) {
            try {
                vectorStore = objectMapper.readValue(storeFile, new TypeReference<List<DocumentChunk>>() {});
                System.out.println("Loaded " + vectorStore.size() + " chunks from vector store.");
            } catch (IOException e) {
                System.err.println("Failed to load vector store: " + e.getMessage());
            }
        }
    }

    @PreDestroy
    public void saveStore() {
        try {
            objectMapper.writeValue(new File(STORE_PATH), vectorStore);
            System.out.println("Saved vector store to disk.");
        } catch (IOException e) {
            System.err.println("Failed to save vector store: " + e.getMessage());
        }
    }

    public void indexResume(ResumeData resumeData, String filename) {
        String fullText = resumeData.getRawText();
        List<String> chunks = chunkText(fullText, 500); // chunk size approx 500 chars

        String resumeId = UUID.randomUUID().toString();
        
        for (int i = 0; i < chunks.size(); i++) {
            String textChunk = chunks.get(i);
            List<Double> embedding = geminiService.getEmbedding(textChunk);
            
            if (embedding != null && !embedding.isEmpty()) {
                Map<String, String> metadata = new HashMap<>();
                metadata.put("filename", filename);
                metadata.put("chunkIndex", String.valueOf(i));
                metadata.put("uploadDate", new Date().toString());
                
                DocumentChunk chunk = new DocumentChunk(
                        UUID.randomUUID().toString(),
                        resumeId,
                        textChunk,
                        embedding,
                        metadata
                );
                vectorStore.add(chunk);
            }
        }
        
        saveStore(); // persist immediately
    }

    public Map<String, Object> searchResumes(String jobDescription, int topK) {
        List<Double> queryEmbedding = geminiService.getEmbedding(jobDescription);
        if (queryEmbedding == null || queryEmbedding.isEmpty()) {
            return Map.of("error", "Failed to generate embedding for job description");
        }

        // Calculate similarities
        Map<DocumentChunk, Double> scores = new HashMap<>();
        for (DocumentChunk chunk : vectorStore) {
            double similarity = cosineSimilarity(queryEmbedding, chunk.getEmbedding());
            scores.put(chunk, similarity);
        }

        // Sort by similarity descending
        List<DocumentChunk> topChunks = scores.entrySet().stream()
                .sorted(Map.Entry.<DocumentChunk, Double>comparingByValue().reversed())
                .limit(topK)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Group by resume
        Map<String, List<DocumentChunk>> resumeChunks = topChunks.stream()
                .collect(Collectors.groupingBy(DocumentChunk::getResumeId));

        // Use Gemini to generate an analysis based on these chunks
        String prompt = buildRagPrompt(jobDescription, resumeChunks);
        // Note: For simplicity we can use the same analyze endpoint or a new one
        return Map.of(
            "topCandidates", resumeChunks.keySet(),
            "contextChunks", topChunks.stream().map(DocumentChunk::getText).collect(Collectors.toList()),
            "message", "RAG search complete. Found " + resumeChunks.keySet().size() + " matching resumes."
        );
    }

    private String buildRagPrompt(String jobDescription, Map<String, List<DocumentChunk>> resumeChunks) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an expert recruiter. I have searched our candidate database using semantic search and retrieved the following snippets from various resumes.\n");
        sb.append("Please evaluate the top candidates against the Job Description.\n\n");
        sb.append("Job Description:\n").append(jobDescription).append("\n\n");
        
        for (Map.Entry<String, List<DocumentChunk>> entry : resumeChunks.entrySet()) {
            sb.append("Candidate Resume ID: ").append(entry.getKey()).append("\n");
            for (DocumentChunk chunk : entry.getValue()) {
                sb.append(" - ").append(chunk.getText()).append("\n");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }

    private List<String> chunkText(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        for (int i = 0; i < length; i += chunkSize) {
            chunks.add(text.substring(i, Math.min(length, i + chunkSize)));
        }
        return chunks;
    }

    private double cosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }
        if (normA == 0 || normB == 0) return 0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
