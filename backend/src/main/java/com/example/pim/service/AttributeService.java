package com.example.pim.service;

import com.example.pim.domain.Attribute;
import com.example.pim.repository.AttributeRepository;
import com.example.pim.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        validateAttribute(attribute);
        Attribute createdAttribute = attributeRepository.save(attribute);
        auditLogService.log("CREATE", "Attribute", createdAttribute.getId(), "system"); // TODO: Replace "system" with actual username
        return createdAttribute;
    }

    @Transactional
    public Attribute updateAttribute(Long id, Attribute attributeDetails) {
        Attribute attribute = attributeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Attribute not found"));

        // Prevent changing the code
        if (!attribute.getCode().equals(attributeDetails.getCode())) {
            throw new IllegalArgumentException("Attribute code cannot be changed.");
        }

        attribute.setName(attributeDetails.getName());
        attribute.setType(attributeDetails.getType());
        attribute.setValidationRegex(attributeDetails.getValidationRegex());
        attribute.setDefaultValue(attributeDetails.getDefaultValue());
        attribute.setVariantAttribute(attributeDetails.isVariantAttribute());

        validateAttribute(attribute);
        Attribute updatedAttribute = attributeRepository.save(attribute);
        auditLogService.log("UPDATE", "Attribute", updatedAttribute.getId(), "system");
        return updatedAttribute;
    }


    private void validateAttribute(Attribute attribute) {
        // Validate regex pattern
        if (attribute.getValidationRegex() != null && !attribute.getValidationRegex().isEmpty()) {
            try {
                Pattern pattern = Pattern.compile(attribute.getValidationRegex());
                // Validate default value against regex
                if (attribute.getDefaultValue() != null && !attribute.getDefaultValue().isEmpty()) {
                    if (!pattern.matcher(attribute.getDefaultValue()).matches()) {
                        throw new IllegalArgumentException("Default value does not match the validation regex.");
                    }
                }
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException("Invalid regex pattern in validation rule: " + e.getMessage());
            }
        }
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