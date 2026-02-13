package com.example.subscription_platform.service;

import com.example.subscription_platform.model.Document;
import com.example.subscription_platform.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * Service for file upload and management.
 * Handles invoice and document uploads.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final DocumentRepository documentRepository;
    private final UserService userService;

    // Upload directory (configurable via application.yml)
    private final String uploadDir = "uploads/documents/";

    /**
     * Upload a file and create document metadata.
     * 
     * @param file       Multipart file
     * @param entityType Type of entity (INVOICE, RECEIPT, etc.)
     * @param entityId   Related entity ID
     * @return Created document
     */
    public Document uploadFile(MultipartFile file, String entityType, String entityId) throws IOException {
        String userId = userService.getCurrentUser().getId();

        // Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("Cannot upload empty file");
        }

        // Get original filename
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // Generate unique filename
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

        // Create upload directory if not exists
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save file to disk
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Create document metadata
        Document document = Document.builder()
                .userId(userId)
                .fileName(originalFilename)
                .filePath(filePath.toString())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .entityType(entityType)
                .entityId(entityId)
                .build();

        documentRepository.save(document);

        log.info("File uploaded successfully: {} for user: {}", originalFilename, userId);
        return document;
    }

    /**
     * Get document by ID.
     * 
     * @param documentId Document ID
     * @return Document metadata
     */
    public Document getDocument(String documentId) {
        String userId = userService.getCurrentUser().getId();

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Verify ownership
        if (!document.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        return document;
    }

    /**
     * Get all documents for current user.
     * 
     * @return List of documents
     */
    public List<Document> getUserDocuments() {
        String userId = userService.getCurrentUser().getId();
        return documentRepository.findByUserId(userId);
    }

    /**
     * Get documents by entity (e.g., all invoices for a subscription).
     * 
     * @param entityType Entity type
     * @param entityId   Entity ID
     * @return List of documents
     */
    public List<Document> getDocumentsByEntity(String entityType, String entityId) {
        String userId = userService.getCurrentUser().getId();
        return documentRepository.findByUserIdAndEntityTypeAndEntityId(userId, entityType, entityId);
    }

    /**
     * Delete document.
     * 
     * @param documentId Document ID
     */
    public void deleteDocument(String documentId) throws IOException {
        String userId = userService.getCurrentUser().getId();

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        // Verify ownership
        if (!document.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        // Delete file from disk
        Path filePath = Paths.get(document.getFilePath());
        Files.deleteIfExists(filePath);

        // Delete metadata
        documentRepository.delete(document);

        log.info("Document deleted: {} for user: {}", document.getFileName(), userId);
    }

    /**
     * Get file extension from filename.
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }
}
