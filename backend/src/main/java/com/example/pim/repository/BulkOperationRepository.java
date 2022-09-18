package com.example.pim.repository;

import com.example.pim.domain.BulkOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BulkOperationRepository extends JpaRepository<BulkOperation, Long> {
    List<BulkOperation> findByOperationTypeAndStatus(String operationType, String status);
    // TODO: Add a method to find bulk operations by channel if we link templates to channels
}

