package com.example.pim.service;

import com.example.pim.domain.BulkOperation;
import com.example.pim.domain.Product;
import com.example.pim.repository.BulkOperationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class BulkOperationService {

    private final BulkOperationRepository bulkOperationRepository;
    private final AuditLogService auditLogService;
    private final ProductService productService; // To perform actual product updates
    private final ExecutorService executorService = Executors.newFixedThreadPool(5); // TODO: Configure thread pool size
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    public BulkOperationService(BulkOperationRepository bulkOperationRepository, AuditLogService auditLogService, ProductService productService) {
        this.bulkOperationRepository = bulkOperationRepository;
        this.auditLogService = auditLogService;
        this.productService = productService;
    }

    @Transactional
    public BulkOperation initiateBulkCreate(List<Map<String, Object>> productData, String initiatedBy) {
        BulkOperation operation = new BulkOperation();
        operation.setOperationType("PRODUCT_CREATE");
        operation.setStatus("PENDING");
        operation.setStartTime(LocalDateTime.now());
        operation.setInitiatedBy(initiatedBy);
        operation.setTotalRecords(productData.size());
        operation.setProcessedRecords(0);
        operation.setFailedRecords(0);
        BulkOperation savedOperation = bulkOperationRepository.save(operation);

        auditLogService.log("BULK_CREATE_INITIATED", "BulkOperation", savedOperation.getId(), initiatedBy);
        executorService.submit(() -> executeBulkCreate(savedOperation.getId(), productData));
        return savedOperation;
    }


    @Transactional
    public BulkOperation initiateBulkUpdate(List<Map<String, Object>> productUpdates, String initiatedBy) {
        BulkOperation operation = new BulkOperation();
        operation.setOperationType("PRODUCT_UPDATE");
        operation.setStatus("PENDING");
        operation.setStartTime(LocalDateTime.now());
        operation.setInitiatedBy(initiatedBy);
        operation.setTotalRecords(productUpdates.size());
        operation.setProcessedRecords(0);
        operation.setFailedRecords(0);
        BulkOperation savedOperation = bulkOperationRepository.save(operation);

        auditLogService.log("BULK_UPDATE_INITIATED", "BulkOperation", savedOperation.getId(), initiatedBy);

        // Execute the bulk operation in a separate thread
        executorService.submit(() -> executeBulkUpdate(savedOperation.getId(), productUpdates));

        return savedOperation;
    }

    private void executeBulkCreate(Long operationId, List<Map<String, Object>> productData) {
        BulkOperation operation = bulkOperationRepository.findById(operationId).orElse(null);
        if (operation == null) return;

        operation.setStatus("IN_PROGRESS");
        bulkOperationRepository.save(operation);

        int processed = 0;
        int failed = 0;
        StringBuilder errorDetails = new StringBuilder();

        for (Map<String, Object> data : productData) {
            try {
                Product product = objectMapper.convertValue(data, Product.class);
                productService.createProduct(product); // This is transactional
                processed++;
            } catch (Exception e) {
                failed++;
                errorDetails.append("Record failed: ").append(data).append(" - Error: ").append(e.getMessage()).append("\n");
            }
            operation.setProcessedRecords(processed);
            operation.setFailedRecords(failed);
            bulkOperationRepository.save(operation); // Save progress
        }

        operation.setEndTime(LocalDateTime.now());
        if (failed == 0) {
            operation.setStatus("COMPLETED");
        } else if (processed == 0) {
            operation.setStatus("FAILED");
            operation.setErrorDetails(errorDetails.toString());
        } else {
            operation.setStatus("COMPLETED_WITH_ERRORS");
            operation.setErrorDetails(errorDetails.toString());
        }
        bulkOperationRepository.save(operation);
        auditLogService.log("BULK_CREATE_COMPLETED", "BulkOperation", operation.getId(), operation.getInitiatedBy());
    }


    private void executeBulkUpdate(Long operationId, List<Map<String, Object>> productUpdates) {
        BulkOperation operation = bulkOperationRepository.findById(operationId).orElse(null);
        if (operation == null) return; // Should not happen

        operation.setStatus("IN_PROGRESS");
        bulkOperationRepository.save(operation);

        int processed = 0;
        int failed = 0;
        StringBuilder errorDetails = new StringBuilder();

        for (Map<String, Object> update : productUpdates) {
            try {
                Long productId = ((Number) update.get("id")).longValue(); // Assuming ID is present for update
                String newName = (String) update.get("name"); // Example update field
                // TODO: Implement actual product update logic using productService
                // For now, just simulate success
                processed++;
            } catch (Exception e) {
                failed++;
                errorDetails.append("Record failed: ").append(update).append(" - Error: ").append(e.getMessage()).append("\n");
            }
            operation.setProcessedRecords(processed);
            operation.setFailedRecords(failed);
            bulkOperationRepository.save(operation);
        }

        operation.setEndTime(LocalDateTime.now());
        if (failed == 0) {
            operation.setStatus("COMPLETED");
        } else if (processed == 0) {
            operation.setStatus("FAILED");
            operation.setErrorDetails(errorDetails.toString());
        } else {
            operation.setStatus("COMPLETED_WITH_ERRORS"); // Partial failure
            operation.setErrorDetails(errorDetails.toString());
        }
        bulkOperationRepository.save(operation);
        auditLogService.log("BULK_UPDATE_COMPLETED", "BulkOperation", operation.getId(), operation.getInitiatedBy());
    }

    public Optional<BulkOperation> getBulkOperationStatus(Long id) {
        return bulkOperationRepository.findById(id);
    }

    // TODO: Implement rollback logic
    // TODO: Implement bulk operation size limits
    // TODO: Implement user permissions validation
}
