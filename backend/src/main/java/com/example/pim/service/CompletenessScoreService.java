package com.example.pim.service;

import com.example.pim.domain.Attribute;
import com.example.pim.domain.Product;
import com.example.pim.domain.ProductAttributeValue;
import com.example.pim.domain.ProductFamily;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CompletenessScoreService {

    public int calculateCompletenessScore(Product product) {
        try {
            int score = 0;
            int totalFields = 4; // sku, name, description, status
            int completedFields = 0;

            if (product.getSku() != null && !product.getSku().isEmpty()) {
                completedFields++;
            }
            if (product.getName() != null && !product.getName().isEmpty()) {
                completedFields++;
            }
            if (product.getDescription() != null && !product.getDescription().isEmpty()) {
                completedFields++;
            }
            if (product.getStatus() != null && !product.getStatus().isEmpty()) {
                completedFields++;
            }

            // Consider attributes from ProductFamily for completeness score
            if (product.getProductFamily() != null) {
                ProductFamily productFamily = product.getProductFamily();
                Set<Attribute> requiredAttributes = productFamily.getAttributes();
                totalFields += requiredAttributes.size();

                if (product.getAttributeValues() != null) {
                    List<ProductAttributeValue> providedAttributeValues = product.getAttributeValues();
                    Set<Attribute> providedAttributes = providedAttributeValues.stream()
                            .map(ProductAttributeValue::getAttribute)
                            .collect(Collectors.toSet());

                    for (Attribute requiredAttribute : requiredAttributes) {
                        if (providedAttributes.contains(requiredAttribute)) {
                            // Further check if the value is not empty
                            providedAttributeValues.stream()
                                    .filter(pav -> pav.getAttribute().equals(requiredAttribute) && pav.getValue() != null && !pav.getValue().isEmpty())
                                    .findFirst()
                                    .ifPresent(pav -> completedFields++);
                        }
                    }
                }
            }

            if (totalFields > 0) {
                score = (int) (((double) completedFields / totalFields) * 100);
            }

            return score;
        } catch (Exception e) {
            System.err.println("Error calculating completeness score for product ID " + product.getId() + ": " + e.getMessage());
            // Log the exception for debugging purposes
            // Return a default score (e.g., 0) to indicate failure without crashing
            return 0;
        }
    }
}



