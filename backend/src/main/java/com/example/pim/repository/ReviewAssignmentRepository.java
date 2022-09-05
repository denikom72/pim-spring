package com.example.pim.repository;

import com.example.pim.domain.Product;
import com.example.pim.domain.ReviewAssignment;
import com.example.pim.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewAssignmentRepository extends JpaRepository<ReviewAssignment, Long> {
    List<ReviewAssignment> findByProductId(Long productId);
    Optional<ReviewAssignment> findByProductAndReviewer(Product product, User reviewer);
    long countByProductIdAndIsApprovedFalse(Long productId);
}
