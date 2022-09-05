package com.example.pim.service;

import com.example.pim.domain.Product;
import com.example.pim.domain.ReviewAssignment;
import com.example.pim.domain.User;
import com.example.pim.repository.ProductRepository;
import com.example.pim.repository.ReviewAssignmentRepository;
import com.example.pim.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewAssignmentService {

    private final ReviewAssignmentRepository reviewAssignmentRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    @Autowired
    public ReviewAssignmentService(ReviewAssignmentRepository reviewAssignmentRepository, ProductRepository productRepository, UserRepository userRepository, AuditLogService auditLogService, NotificationService notificationService) {
        this.reviewAssignmentRepository = reviewAssignmentRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ReviewAssignment assignReviewer(Long productId, Long reviewerId, String assignedBy) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found"));

        if (reviewAssignmentRepository.findByProductAndReviewer(product, reviewer).isPresent()) {
            throw new IllegalArgumentException("Reviewer '" + reviewer.getUsername() + "' is already assigned to product '" + product.getName() + "'.");
        }

        ReviewAssignment assignment = new ReviewAssignment();
        assignment.setProduct(product);
        assignment.setReviewer(reviewer);
        assignment.setAssignmentDate(LocalDateTime.now());
        ReviewAssignment savedAssignment = reviewAssignmentRepository.save(assignment);

        auditLogService.log("ASSIGN_REVIEWER", "ReviewAssignment", savedAssignment.getId(), assignedBy);
        notificationService.sendNotification(reviewer.getUsername(), "New Product Review Assignment",
                "You have been assigned to review product '" + product.getName() + "'.");
        return savedAssignment;
    }

    @Transactional
    public ReviewAssignment updateReviewStatus(Long assignmentId, boolean isApproved, String comments, String reviewedBy) {
        ReviewAssignment assignment = reviewAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Review assignment not found"));

        if (!assignment.getReviewer().getUsername().equals(reviewedBy)) {
            throw new SecurityException("Unauthorized: Only the assigned reviewer can update this review.");
        }

        assignment.setApproved(isApproved);
        assignment.setComments(comments);
        assignment.setReviewDate(LocalDateTime.now());
        ReviewAssignment updatedAssignment = reviewAssignmentRepository.save(assignment);

        auditLogService.log(isApproved ? "REVIEW_APPROVED" : "REVIEW_REJECTED", "ReviewAssignment", updatedAssignment.getId(), reviewedBy);
        notificationService.sendNotification(assignment.getProduct().getSku(), "Product Review Update: " + assignment.getProduct().getName(),
                "Product '" + assignment.getProduct().getName() + "' has been " + (isApproved ? "approved" : "rejected") + " by " + reviewedBy + ". Comments: " + comments);
        return updatedAssignment;
    }

    public List<ReviewAssignment> getAssignmentsForProduct(Long productId) {
        return reviewAssignmentRepository.findByProductId(productId);
    }

    public long countPendingApprovals(Long productId) {
        return reviewAssignmentRepository.countByProductIdAndIsApprovedFalse(productId);
    }
}
