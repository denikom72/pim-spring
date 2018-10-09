package com.example.pim.service;

import com.example.pim.domain.ProductFamily;
import com.example.pim.repository.ProductFamilyRepository;
import com.example.pim.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductFamilyService {

    private final ProductFamilyRepository productFamilyRepository;
    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;

    @Autowired
    public ProductFamilyService(ProductFamilyRepository productFamilyRepository, ProductRepository productRepository, AuditLogService auditLogService) {
        this.productFamilyRepository = productFamilyRepository;
        this.productRepository = productRepository;
        this.auditLogService = auditLogService;
    }

    public ProductFamily createProductFamily(ProductFamily productFamily) {
        if (productFamilyRepository.findByCode(productFamily.getCode()).isPresent()) {
            throw new IllegalArgumentException("Product family with code '" + productFamily.getCode() + "' already exists.");
        }
        ProductFamily createdProductFamily = productFamilyRepository.save(productFamily);
        auditLogService.log("CREATE", "ProductFamily", createdProductFamily.getId(), "system"); // TODO: Replace "system" with actual username
        return createdProductFamily;
    }

    public Optional<ProductFamily> getProductFamilyById(Long id) {
        return productFamilyRepository.findById(id);
    }

    public List<ProductFamily> getAllProductFamilies() {
        return productFamilyRepository.findAll();
    }

    public void deleteProductFamily(Long id) {
        ProductFamily productFamily = productFamilyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product family not found"));

        long productCount = productRepository.countByProductFamily(productFamily);
        if (productCount > 0) {
            throw new IllegalArgumentException("Cannot delete product family '" + productFamily.getName() + "' as it is assigned to " + productCount + " products.");
        }

        productFamilyRepository.delete(productFamily);
        auditLogService.log("DELETE", "ProductFamily", id, "system"); // TODO: Replace "system" with actual username
    }
}

