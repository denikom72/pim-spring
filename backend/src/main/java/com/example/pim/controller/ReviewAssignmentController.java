package com.example.pim.controller;

import com.example.pim.domain.ReviewAssignment;
import com.example.pim.service.ReviewAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products/{productId}/review-assignments")
public class ReviewAssignmentController {

    private final ReviewAssignmentService reviewAssignmentService;

    @Autowired
    public ReviewAssignmentController(ReviewAssignmentService reviewAssignmentService) {
        this.reviewAssignmentService = reviewAssignmentService;
    }

    @PostMapping
    public ResponseEntity<ReviewAssignment> assignReviewer(
            @PathVariable Long productId,
            @RequestBody Map<String, Long> payload) {
        Long reviewerId = payload.get("reviewerId");
        if (reviewerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "reviewerId is mandatory.");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String assignedBy = authentication.getName();

        try {
            ReviewAssignment assignment = reviewAssignmentService.assignReviewer(productId, reviewerId, assignedBy);
            return new ResponseEntity<>(assignment, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{assignmentId}")
    public ResponseEntity<ReviewAssignment> updateReviewStatus(
            @PathVariable Long assignmentId,
            @RequestBody Map<String, Object> payload) {
        Boolean isApproved = (Boolean) payload.get("isApproved");
        String comments = (String) payload.get("comments");

        if (isApproved == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "isApproved is mandatory.");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String reviewedBy = authentication.getName();

        try {
            ReviewAssignment updatedAssignment = reviewAssignmentService.updateReviewStatus(assignmentId, isApproved, comments, reviewedBy);
            return new ResponseEntity<>(updatedAssignment, HttpStatus.OK);
        } catch (IllegalArgumentException | SecurityException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ReviewAssignment>> getAssignmentsForProduct(@PathVariable Long productId) {
        List<ReviewAssignment> assignments = reviewAssignmentService.getAssignmentsForProduct(productId);
        return new ResponseEntity<>(assignments, HttpStatus.OK);
    }
}
