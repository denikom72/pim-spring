package com.example.pim.controller;

import com.example.pim.domain.Product;
import com.example.pim.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/products/{productId}/workflow")
public class WorkflowController {

    private final WorkflowService workflowService;

    @Autowired
    public WorkflowController(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @PutMapping
    public ResponseEntity<Product> updateWorkflowStatus(
            @PathVariable Long productId,
            @RequestBody Map<String, String> payload) {
        String newStatus = payload.get("status");
        String username = "system"; // TODO: Get username from security context

        if (newStatus == null || newStatus.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Workflow status is mandatory.");
        }

        try {
            Product updatedProduct = workflowService.updateWorkflowStatus(productId, newStatus, username);
            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
