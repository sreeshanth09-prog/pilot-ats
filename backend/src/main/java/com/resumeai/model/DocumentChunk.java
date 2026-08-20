package com.resumeai.model;

import java.util.List;
import java.util.Map;

public class DocumentChunk {
    private String id;
    private String resumeId;
    private String text;
    private List<Double> embedding;
    private Map<String, String> metadata;

    public DocumentChunk() {}

    public DocumentChunk(String id, String resumeId, String text, List<Double> embedding, Map<String, String> metadata) {
        this.id = id;
        this.resumeId = resumeId;
        this.text = text;
        this.embedding = embedding;
        this.metadata = metadata;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getResumeId() { return resumeId; }
    public void setResumeId(String resumeId) { this.resumeId = resumeId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public List<Double> getEmbedding() { return embedding; }
    public void setEmbedding(List<Double> embedding) { this.embedding = embedding; }

    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }
}
