package com.example.pim.service;

import com.example.pim.domain.Product;
import com.example.pim.domain.Role;
import com.example.pim.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WorkflowService {

    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final ReviewAssignmentService reviewAssignmentService;

    // Define valid workflow transitions and required roles
    private static final List<String> VALID_WORKFLOW_STATUSES = Arrays.asList("not_started", "in_review", "approved", "rejected");

    @Autowired
    public WorkflowService(ProductRepository productRepository, AuditLogService auditLogService, NotificationService notificationService, ReviewAssignmentService reviewAssignmentService) {
        this.productRepository = productRepository;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
        this.reviewAssignmentService = reviewAssignmentService;
    }

    @Transactional
    public Product updateWorkflowStatus(Long productId, String newWorkflowStatus, String username) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!VALID_WORKFLOW_STATUSES.contains(newWorkflowStatus)) {
            throw new IllegalArgumentException("Invalid workflow status: " + newWorkflowStatus);
        }

        // Role-based permissions for state transitions
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Set<String> userRoles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(s -> s.replace("ROLE_", ""))
                .collect(Collectors.toSet());

        String currentStatus = product.getWorkflowStatus();

        // Example permission logic (can be made more sophisticated)
        if ("in_review".equals(newWorkflowStatus)) {
            if (!userRoles.contains(Role.EDITOR.name()) && !userRoles.contains(Role.ADMIN.name())) {
                throw new SecurityException("Unauthorized: Only EDITOR or ADMIN can send products for review.");
            }
        } else if ("approved".equals(newWorkflowStatus)) {
            if (!userRoles.contains(Role.REVIEWER.name()) && !userRoles.contains(Role.ADMIN.name())) {
                throw new SecurityException("Unauthorized: Only REVIEWER or ADMIN can approve products.");
            }
            if (!"in_review".equals(currentStatus)) {
                throw new IllegalArgumentException("Product must be in 'in_review' status to be approved.");
            }
            // Validate reviewer assignments and approvals
            long pendingApprovals = reviewAssignmentService.countPendingApprovals(productId);
            if (pendingApprovals > 0) {
                throw new IllegalArgumentException("Cannot approve product: " + pendingApprovals + " pending review(s) remaining.");
            }
        } else if ("rejected".equals(newWorkflowStatus)) {
            if (!userRoles.contains(Role.REVIEWER.name()) && !userRoles.contains(Role.ADMIN.name())) {
                throw new SecurityException("Unauthorized: Only REVIEWER or ADMIN can reject products.");
            }
            if (!"in_review".equals(currentStatus)) {
                throw new IllegalArgumentException("Product must be in 'in_review' status to be rejected.");
            }
        }

        String oldWorkflowStatus = product.getWorkflowStatus();
        product.setWorkflowStatus(newWorkflowStatus);
        Product updatedProduct = productRepository.save(product);

        auditLogService.log("UPDATE_WORKFLOW_STATUS", "Product", updatedProduct.getId(), username);
        try {
            notificationService.sendNotification(username, "Product Workflow Update: " + updatedProduct.getName(),
                    "Product '" + updatedProduct.getName() + "' workflow status changed from '" + oldWorkflowStatus + "' to '" + newWorkflowStatus + "' by " + username + ".");
        } catch (Exception e) {
            System.err.println("Failed to send notification for product ID " + updatedProduct.getId() + ": " + e.getMessage());
            // Log the exception, but don't rethrow to avoid disrupting the main workflow
        }
        return updatedProduct;
    }
}



