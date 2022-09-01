package com.example.pim.service;

import com.example.pim.domain.*;
import com.example.pim.repository.AttributeRepository;
import com.example.pim.repository.ProductAttributeValueRepository;
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
    private final AttributeRepository attributeRepository;
    private final ProductAttributeValueRepository productAttributeValueRepository;


    @Autowired
    public ProductService(ProductRepository productRepository, AuditLogService auditLogService, CompletenessScoreService completenessScoreService, ProductFamilyService productFamilyService, ProductAttributeValueService productAttributeValueService, AttributeRepository attributeRepository, ProductAttributeValueRepository productAttributeValueRepository) {
        this.productRepository = productRepository;
        this.auditLogService = auditLogService;
        this.completenessScoreService = completenessScoreService;
        this.productFamilyService = productFamilyService;
        this.productAttributeValueService = productAttributeValueService;
        this.attributeRepository = attributeRepository;
        this.productAttributeValueRepository = productAttributeValueRepository;
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
            // Check completeness threshold
            if (product.getProductFamily() != null) {
                int threshold = product.getProductFamily().getCompletenessThreshold();
                if (product.getCompletenessScore() < threshold) {
                    throw new IllegalArgumentException("Product completeness score (" + product.getCompletenessScore() + "%) is below the required threshold (" + threshold + "%) for publishing.");
                }
            }
            else {
                // Default behavior if no product family is assigned
                if (product.getCompletenessScore() < 100) {
                    throw new IllegalArgumentException("Product must be 100% complete to be published without a product family.");
                }
            }

            // Check if variants are complete
            if (product.getVariants() != null && !product.getVariants().isEmpty()) {
                List<Attribute> variantAttributes = attributeRepository.findAll().stream()
                        .filter(Attribute::isVariantAttribute)
                        .toList();
                for (ProductVariant variant : product.getVariants()) {
                    for (Attribute variantAttr : variantAttributes) {
                        if (!variant.getAttributes().containsKey(variantAttr.getName()) || variant.getAttributes().get(variantAttr.getName()).isEmpty()) {
                            throw new IllegalArgumentException("Cannot publish product: Variant with SKU '" + variant.getSku() + "' is incomplete. Missing value for attribute '" + variantAttr.getName() + "'.");
                        }
                    }
                }
            }
        }

        product.setStatus(newStatus);
        Product updatedProduct = productRepository.save(product);
        auditLogService.log("UPDATE_STATUS", "Product", updatedProduct.getId(), "system");
        return updatedProduct;
    }

    @Transactional
    public Product changeProductFamily(Long productId, Long newFamilyId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        ProductFamily newFamily = productFamilyService.getProductFamilyById(newFamilyId)
                .orElseThrow(() -> new IllegalArgumentException("Product family not found"));

        // Validate attribute compatibility
        Set<Attribute> newFamilyAttributes = newFamily.getAttributes();
        List<ProductAttributeValue> currentAttributeValues = product.getAttributeValues();

        // Remove attribute values that are not in the new family
        List<ProductAttributeValue> orphanedValues = currentAttributeValues.stream()
                .filter(pav -> !newFamilyAttributes.contains(pav.getAttribute()))
                .collect(Collectors.toList());
        productAttributeValueRepository.deleteAll(orphanedValues);
        currentAttributeValues.removeAll(orphanedValues);


        product.setProductFamily(newFamily);
        // Recalculate completeness score
        product.setCompletenessScore(completenessScoreService.calculateCompletenessScore(product));
        Product updatedProduct = productRepository.save(product);
        auditLogService.log("CHANGE_FAMILY", "Product", updatedProduct.getId(), "system");
        return updatedProduct;
    }


    public List<Product> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(query, query);
    }
}