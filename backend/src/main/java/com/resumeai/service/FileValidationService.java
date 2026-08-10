package com.resumeai.service;

import com.resumeai.exception.InvalidFileException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
public class FileValidationService {
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
    );

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is empty or not provided.");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException("File is too large. Maximum size is 10 MB.");
        }
        
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        
        boolean validExt = filename != null && (filename.toLowerCase().endsWith(".pdf") || 
                                                filename.toLowerCase().endsWith(".docx") || 
                                                filename.toLowerCase().endsWith(".txt"));

        if (!ALLOWED_CONTENT_TYPES.contains(contentType) && !validExt) {
            throw new InvalidFileException("Unsupported file type. Please upload PDF, DOCX or TXT.");
        }
    }
}
