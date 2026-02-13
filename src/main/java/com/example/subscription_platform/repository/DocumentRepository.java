package com.example.subscription_platform.repository;

import com.example.subscription_platform.model.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Document entity.
 * Provides MongoDB CRUD operations for file metadata.
 */
@Repository
public interface DocumentRepository extends MongoRepository<Document, String> {

    /**
     * Find all documents by user ID.
     * 
     * @param userId User ID
     * @return List of user's documents
     */
    List<Document> findByUserId(String userId);

    /**
     * Find documents by subscription ID.
     * Used to find invoices/receipts for a specific subscription.
     * 
     * @param subscriptionId Subscription ID
     * @return List of documents for the subscription
     */
    List<Document> findBySubscriptionId(String subscriptionId);

    /**
     * Find documents by user ID and subscription ID.
     * 
     * @param userId         User ID
     * @param subscriptionId Subscription ID
     * @return List of matching documents
     */
    List<Document> findByUserIdAndSubscriptionId(String userId, String subscriptionId);

    /**
     * Find documents by user ID, entity type, and entity ID.
     * Generic method for finding documents associated with any entity.
     * 
     * @param userId     User ID
     * @param entityType Entity type (INVOICE, RECEIPT, etc.)
     * @param entityId   Entity ID
     * @return List of matching documents
     */
    List<Document> findByUserIdAndEntityTypeAndEntityId(String userId, String entityType, String entityId);
}
