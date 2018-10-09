package com.example.pim.repository;

import com.example.pim.domain.ProductAttributeValue;
import com.example.pim.domain.Product;
import com.example.pim.domain.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductAttributeValueRepository extends JpaRepository<ProductAttributeValue, Long> {
    Optional<ProductAttributeValue> findByProductAndAttribute(Product product, Attribute attribute);
    List<ProductAttributeValue> findByProductId(Long productId);
}
