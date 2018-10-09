package com.example.pim.service;

import com.example.pim.domain.Attribute;
import com.example.pim.domain.Product;
import com.example.pim.domain.ProductAttributeValue;
import com.example.pim.domain.ProductFamily;
import com.example.pim.repository.AttributeRepository;
import com.example.pim.repository.ProductAttributeValueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Service
public class ProductAttributeValueService {

    private final ProductAttributeValueRepository productAttributeValueRepository;
    private final AttributeRepository attributeRepository;
    private final AuditLogService auditLogService;

    @Autowired
    public ProductAttributeValueService(ProductAttributeValueRepository productAttributeValueRepository, AttributeRepository attributeRepository, AuditLogService auditLogService) {
        this.productAttributeValueRepository = productAttributeValueRepository;
        this.attributeRepository = attributeRepository;
        this.auditLogService = auditLogService;
    }

    public ProductAttributeValue saveProductAttributeValue(Product product, Long attributeId, String value) {
        Attribute attribute = attributeRepository.findById(attributeId)
                .orElseThrow(() -> new IllegalArgumentException("Attribute not found"));

        // Validate value against attribute's regex if present
        if (attribute.getValidationRegex() != null && !attribute.getValidationRegex().isEmpty()) {
            try {
                if (!Pattern.compile(attribute.getValidationRegex()).matcher(value).matches()) {
                    throw new IllegalArgumentException("Value '" + value + "' does not match regex for attribute '" + attribute.getName() + "'.");
                }
            } catch (PatternSyntaxException e) {
                // This should ideally be caught during attribute creation, but good to have a fallback
                throw new IllegalArgumentException("Invalid regex pattern for attribute '" + attribute.getName() + "'.");
            }
        }

        Optional<ProductAttributeValue> existingValue = productAttributeValueRepository.findByProductAndAttribute(product, attribute);
        ProductAttributeValue productAttributeValue;

        if (existingValue.isPresent()) {
            productAttributeValue = existingValue.get();
            productAttributeValue.setValue(value);
            auditLogService.log("UPDATE_ATTRIBUTE_VALUE", "ProductAttributeValue", productAttributeValue.getId(), "system");
        } else {
            productAttributeValue = new ProductAttributeValue();
            productAttributeValue.setProduct(product);
            productAttributeValue.setAttribute(attribute);
            productAttributeValue.setValue(value);
            auditLogService.log("CREATE_ATTRIBUTE_VALUE", "ProductAttributeValue", null, "system"); // ID will be generated on save
        }
        return productAttributeValueRepository.save(productAttributeValue);
    }

    public List<ProductAttributeValue> getAttributeValuesForProduct(Long productId) {
        return productAttributeValueRepository.findByProductId(productId);
    }
}
