package com.example.subscription_platform.controller;

import com.example.subscription_platform.dto.common.ApiResponse;
import com.example.subscription_platform.model.Document;
import com.example.subscription_platform.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Controller for file upload and document management.
 * Requires authentication.
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Documents", description = "File upload and document management APIs")
public class DocumentController {

    private final FileUploadService fileUploadService;

    /**
     * Upload a document.
     * 
     * @param file       File to upload
     * @param entityType Entity type (INVOICE, RECEIPT, etc.)
     * @param entityId   Related entity ID
     * @return Uploaded document metadata
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload document", description = "Upload invoice, receipt, or other document")
    public ResponseEntity<ApiResponse<Document>> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") String entityId) throws IOException {
        Document document = fileUploadService.uploadFile(file, entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", document));
    }

    /**
     * Get all documents for current user.
     * 
     * @return List of documents
     */
    @GetMapping
    @Operation(summary = "Get user documents", description = "Get all documents for authenticated user")
    public ResponseEntity<ApiResponse<List<Document>>> getUserDocuments() {
        List<Document> documents = fileUploadService.getUserDocuments();
        return ResponseEntity.ok(ApiResponse.success(documents));
    }

    /**
     * Get documents by entity.
     * 
     * @param entityType Entity type
     * @param entityId   Entity ID
     * @return List of documents
     */
    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Get documents by entity", description = "Get all documents for a specific entity (e.g., subscription)")
    public ResponseEntity<ApiResponse<List<Document>>> getDocumentsByEntity(
            @PathVariable String entityType,
            @PathVariable String entityId) {
        List<Document> documents = fileUploadService.getDocumentsByEntity(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success(documents));
    }

    /**
     * Download a document.
     * 
     * @param documentId Document ID
     * @return File resource
     */
    @GetMapping("/download/{documentId}")
    @Operation(summary = "Download document", description = "Download document file")
    public ResponseEntity<Resource> downloadDocument(@PathVariable String documentId) throws IOException {
        Document document = fileUploadService.getDocument(documentId);

        Path filePath = Paths.get(document.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(document.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + document.getFileName() + "\"")
                    .body(resource);
        } else {
            throw new RuntimeException("File not found or not readable");
        }
    }

    /**
     * Delete a document.
     * 
     * @param documentId Document ID
     * @return Success message
     */
    @DeleteMapping("/{documentId}")
    @Operation(summary = "Delete document", description = "Delete document and file")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable String documentId) throws IOException {
        fileUploadService.deleteDocument(documentId);
        return ResponseEntity.ok(ApiResponse.success("Document deleted successfully", null));
    }
}
