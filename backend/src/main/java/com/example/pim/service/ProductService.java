package com.example.pim.service;

import com.example.pim.domain.Attribute;
import com.example.pim.domain.Product;
import com.example.pim.domain.ProductAttributeValue;
import com.example.pim.domain.ProductFamily;
import com.example.pim.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;
    private final CompletenessScoreService completenessScoreService;
    private final ProductFamilyService productFamilyService;
    private final ProductAttributeValueService productAttributeValueService;

    @Autowired
    public ProductService(ProductRepository productRepository, AuditLogService auditLogService, CompletenessScoreService completenessScoreService, ProductFamilyService productFamilyService, ProductAttributeValueService productAttributeValueService) {
        this.productRepository = productRepository;
        this.auditLogService = auditLogService;
        this.completenessScoreService = completenessScoreService;
        this.productFamilyService = productFamilyService;
        this.productAttributeValueService = productAttributeValueService;
    }

    @Transactional
    public Product createProduct(Product product) {
        if (productRepository.existsBySku(product.getSku())) {
            throw new IllegalArgumentException("SKU '" + product.getSku() + "' already exists");
        }

        // Validate against ProductFamily attributes
        if (product.getProductFamily() != null && product.getProductFamily().getId() != null) {
            ProductFamily productFamily = productFamilyService.getProductFamilyById(product.getProductFamily().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product family not found"));
            product.setProductFamily(productFamily);

            Set<Attribute> requiredAttributes = productFamily.getAttributes();
            List<ProductAttributeValue> providedAttributeValues = product.getAttributeValues();

            Set<Attribute> providedAttributes = providedAttributeValues.stream()
                    .map(ProductAttributeValue::getAttribute)
                    .collect(Collectors.toSet());

            for (Attribute requiredAttribute : requiredAttributes) {
                if (!providedAttributes.contains(requiredAttribute)) {
                    throw new IllegalArgumentException("Missing required attribute: " + requiredAttribute.getName());
                }
            }
        }

        // First, save the product to get an ID
        Product createdProduct = productRepository.save(product);

        // Associate attribute values with the created product and save them
        if (product.getAttributeValues() != null) {
            for (ProductAttributeValue pav : product.getAttributeValues()) {
                pav.setProduct(createdProduct);
                productAttributeValueService.saveProductAttributeValue(createdProduct, pav.getAttribute().getId(), pav.getValue());
            }
        }

        // Recalculate completeness score after saving attributes
        createdProduct.setCompletenessScore(completenessScoreService.calculateCompletenessScore(createdProduct));
        Product updatedProduct = productRepository.save(createdProduct);

        auditLogService.log("CREATE", "Product", updatedProduct.getId(), "system"); // TODO: Replace "system" with actual username
        return updatedProduct;
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional
    public Product updateProductStatus(Long productId, String newStatus) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if ("published".equalsIgnoreCase(newStatus)) {
            if (product.getProductFamily() != null) {
                int threshold = product.getProductFamily().getCompletenessThreshold();
                if (product.getCompletenessScore() < threshold) {
                    throw new IllegalArgumentException("Product completeness score (" + product.getCompletenessScore() + "%) is below the required threshold (" + threshold + "%) for publishing.");
                }
            } else {
                // Default behavior if no product family is assigned
                if (product.getCompletenessScore() < 100) {
                    throw new IllegalArgumentException("Product must be 100% complete to be published without a product family.");
                }
            }
        }

        product.setStatus(newStatus);
        Product updatedProduct = productRepository.save(product);
        auditLogService.log("UPDATE_STATUS", "Product", updatedProduct.getId(), "system");
        return updatedProduct;
    }
}