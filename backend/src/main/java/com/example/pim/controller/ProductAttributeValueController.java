package com.example.pim.controller;

import com.example.pim.domain.Product;
import com.example.pim.domain.ProductAttributeValue;
import com.example.pim.service.ProductAttributeValueService;
import com.example.pim.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products/{productId}/attributes")
public class ProductAttributeValueController {

    private final ProductAttributeValueService productAttributeValueService;
    private final ProductService productService;

    @Autowired
    public ProductAttributeValueController(ProductAttributeValueService productAttributeValueService, ProductService productService) {
        this.productAttributeValueService = productAttributeValueService;
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductAttributeValue> saveProductAttributeValue(
            @PathVariable Long productId,
            @RequestBody Map<String, String> payload) {
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        Long attributeId = Long.parseLong(payload.get("attributeId"));
        String value = payload.get("value");

        try {
            ProductAttributeValue savedValue = productAttributeValueService.saveProductAttributeValue(product, attributeId, value);
            return new ResponseEntity<>(savedValue, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ProductAttributeValue>> getAttributeValuesForProduct(@PathVariable Long productId) {
        List<ProductAttributeValue> attributeValues = productAttributeValueService.getAttributeValuesForProduct(productId);
        return new ResponseEntity<>(attributeValues, HttpStatus.OK);
    }
}
