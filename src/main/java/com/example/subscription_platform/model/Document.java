package com.example.subscription_platform.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * Document entity for storing file metadata (invoices, receipts, etc.).
 * Supports the file upload functionality requirement.
 * Actual files stored in filesystem or cloud storage (S3).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@org.springframework.data.mongodb.core.mapping.Document(collection = "documents")
public class Document {

    @Id
    private String id;

    @NotBlank(message = "User ID is required")
    @Indexed
    private String userId;

    @Indexed
    private String subscriptionId;

    @NotBlank(message = "File name is required")
    private String fileName;

    @NotBlank(message = "File type is required")
    private String fileType; // pdf, jpeg, png, etc.

    @NotBlank(message = "File path is required")
    private String filePath; // Local path or S3 URL

    private Long fileSize; // Size in bytes

    private String entityType; // Type of associated entity

    private String entityId; // ID of associated entity

    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
