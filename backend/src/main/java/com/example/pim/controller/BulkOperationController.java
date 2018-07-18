package com.example.pim.controller;

import com.example.pim.domain.BulkOperation;
import com.example.pim.service.BulkOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bulk-operations")
public class BulkOperationController {

    private final BulkOperationService bulkOperationService;

    @Autowired
    public BulkOperationController(BulkOperationService bulkOperationService) {
        this.bulkOperationService = bulkOperationService;
    }

    @PostMapping("/products/update")
    public ResponseEntity<BulkOperation> initiateProductBulkUpdate(@RequestBody List<Map<String, Object>> productUpdates) {
        String username = "system"; // TODO: Get username from security context
        try {
            BulkOperation operation = bulkOperationService.initiateBulkUpdate(productUpdates, username);
            return new ResponseEntity<>(operation, HttpStatus.ACCEPTED); // 202 Accepted
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BulkOperation> getBulkOperationStatus(@PathVariable Long id) {
        return bulkOperationService.getBulkOperationStatus(id)
                .map(operation -> new ResponseEntity<>(operation, HttpStatus.OK))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bulk operation not found"));
    }
}
