package com.example.pim.service;

import com.example.pim.domain.Product;
import com.example.pim.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class WorkflowService {

    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;

    // Define valid workflow transitions
    private static final List<String> VALID_WORKFLOW_STATUSES = Arrays.asList("not_started", "in_review", "approved", "rejected");

    @Autowired
    public WorkflowService(ProductRepository productRepository, AuditLogService auditLogService) {
        this.productRepository = productRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Product updateWorkflowStatus(Long productId, String newWorkflowStatus, String username) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!VALID_WORKFLOW_STATUSES.contains(newWorkflowStatus)) {
            throw new IllegalArgumentException("Invalid workflow status: " + newWorkflowStatus);
        }

        // TODO: Implement role-based permissions for state transitions

        String oldWorkflowStatus = product.getWorkflowStatus();
        product.setWorkflowStatus(newWorkflowStatus);
        Product updatedProduct = productRepository.save(product);

        auditLogService.log("UPDATE_WORKFLOW_STATUS", "Product", updatedProduct.getId(), username);
        // TODO: Implement notifications
        return updatedProduct;
    }
}
