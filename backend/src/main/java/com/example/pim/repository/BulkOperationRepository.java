package com.example.pim.repository;

import com.example.pim.domain.BulkOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BulkOperationRepository extends JpaRepository<BulkOperation, Long> {
}
