package com.example.pim.repository;

import com.example.pim.domain.ProductFamily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductFamilyRepository extends JpaRepository<ProductFamily, Long> {
    Optional<ProductFamily> findByCode(String code);
}
