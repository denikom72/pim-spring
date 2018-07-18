package com.example.pim.repository;

import com.example.pim.domain.Category;
import com.example.pim.domain.Product;
import com.example.pim.domain.ProductFamily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySku(String sku);
    long countByCategoriesContaining(Category category);
    long countByProductFamily(ProductFamily productFamily);
    List<Product> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(String name, String sku);
}



