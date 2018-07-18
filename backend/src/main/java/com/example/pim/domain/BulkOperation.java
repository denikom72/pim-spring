package com.example.pim.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class BulkOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String operationType; // e.g., "PRODUCT_UPDATE", "PRODUCT_DELETE"

    private String status; // e.g., "PENDING", "IN_PROGRESS", "COMPLETED", "FAILED", "ROLLING_BACK", "ROLLED_BACK"

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String initiatedBy; // Username

    private int totalRecords;

    private int processedRecords;

    private int failedRecords;

    @Lob
    private String errorDetails; // Store details of partial or full failures

    @Lob
    private String rollbackDetails; // Details about rollback if applicable
}
