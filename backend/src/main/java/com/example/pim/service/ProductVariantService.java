package com.example.pim.service;

import com.example.pim.domain.Attribute;
import com.example.pim.domain.Product;
import com.example.pim.domain.ProductVariant;
import com.example.pim.repository.AttributeRepository;
import com.example.pim.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final AttributeRepository attributeRepository;
    private final AuditLogService auditLogService;
    private final AtomicInteger skuCounter = new AtomicInteger(1); // For basic sequential SKU generation

    @Autowired
    public ProductVariantService(ProductVariantRepository productVariantRepository, AttributeRepository attributeRepository, AuditLogService auditLogService) {
        this.productVariantRepository = productVariantRepository;
        this.attributeRepository = attributeRepository;
        this.auditLogService = auditLogService;
    }

    public ProductVariant createProductVariant(Product product, ProductVariant productVariant) {
        if (productVariant.getSku() == null || productVariant.getSku().isEmpty()) {
            // Auto-generate SKU if not provided
            String baseSku = product.getSku();
            String generatedSku = baseSku + "-VAR-" + skuCounter.getAndIncrement();
            productVariant.setSku(generatedSku);
        }

        if (productVariantRepository.existsBySku(productVariant.getSku())) {
            throw new IllegalArgumentException("SKU '" + productVariant.getSku() + "' already exists");
        }

        // Validate differentiating attributes (US-102: Prevents variant creation without differentiating attributes)
        List<Attribute> variantAttributes = attributeRepository.findAll().stream()
                .filter(Attribute::isVariantAttribute)
                .toList();

        Set<String> providedVariantAttributeNames = productVariant.getAttributes().keySet().stream()
                .filter(attrName -> variantAttributes.stream().anyMatch(va -> va.getName().equals(attrName)))
                .collect(Collectors.toSet());

        if (providedVariantAttributeNames.isEmpty() && !variantAttributes.isEmpty()) {
            throw new IllegalArgumentException("Product variant must have at least one differentiating attribute.");
        }

        // Validate variant combination uniqueness (US-102)
        List<ProductVariant> existingVariants = productVariantRepository.findByProductId(product.getId());
        for (ProductVariant existingVariant : existingVariants) {
            if (areAttributeMapsEqual(existingVariant.getAttributes(), productVariant.getAttributes(), variantAttributes)) {
                throw new IllegalArgumentException("A product variant with the same combination of differentiating attributes already exists.");
            }
        }

        productVariant.setProduct(product);
        ProductVariant createdProductVariant = productVariantRepository.save(productVariant);
        auditLogService.log("CREATE_VARIANT", "ProductVariant", createdProductVariant.getId(), "system"); // TODO: Replace "system" with actual username
        return createdProductVariant;
    }

    private boolean areAttributeMapsEqual(Map<String, String> map1, Map<String, String> map2, List<Attribute> variantAttributes) {
        if (map1 == null || map2 == null) {
            return map1 == map2;
        }

        for (Attribute attr : variantAttributes) {
            String attrName = attr.getName();
            String value1 = map1.get(attrName);
            String value2 = map2.get(attrName);

            if (value1 == null && value2 == null) {
                continue;
            }
            if (value1 == null || value2 == null || !value1.equals(value2)) {
                return false;
            }
        }
        return true;
    }
}

