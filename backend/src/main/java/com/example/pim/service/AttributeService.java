package com.example.pim.service;

import com.example.pim.domain.Attribute;
import com.example.pim.repository.AttributeRepository;
import com.example.pim.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Service
public class AttributeService {

    private final AttributeRepository attributeRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AuditLogService auditLogService;

    @Autowired
    public AttributeService(AttributeRepository attributeRepository, ProductVariantRepository productVariantRepository, AuditLogService auditLogService) {
        this.attributeRepository = attributeRepository;
        this.productVariantRepository = productVariantRepository;
        this.auditLogService = auditLogService;
    }

    public Attribute createAttribute(Attribute attribute) {
        if (attributeRepository.findByCode(attribute.getCode()).isPresent()) {
            throw new IllegalArgumentException("Attribute with code '" + attribute.getCode() + "' already exists.");
        }
        if (attribute.getValidationRegex() != null && !attribute.getValidationRegex().isEmpty()) {
            try {
                Pattern.compile(attribute.getValidationRegex());
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException("Invalid regex pattern in validation rule: " + e.getMessage());
            }
        }
        Attribute createdAttribute = attributeRepository.save(attribute);
        auditLogService.log("CREATE", "Attribute", createdAttribute.getId(), "system"); // TODO: Replace "system" with actual username
        return createdAttribute;
    }

    public Optional<Attribute> getAttributeById(Long id) {
        return attributeRepository.findById(id);
    }

    public List<Attribute> getAllAttributes() {
        return attributeRepository.findAll();
    }

    public void deleteAttribute(Long id) {
        Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attribute not found"));

        // Check if the attribute is used in any product variants
        boolean isAttributeUsed = productVariantRepository.findAll().stream()
                .anyMatch(variant -> variant.getAttributes().containsKey(attribute.getName()));

        if (isAttributeUsed) {
            throw new IllegalArgumentException("Cannot delete attribute '" + attribute.getName() + "' as it is used in one or more product variants.");
        }

        attributeRepository.delete(attribute);
        auditLogService.log("DELETE", "Attribute", id, "system"); // TODO: Replace "system" with actual username
    }
}