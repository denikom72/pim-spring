package com.example.pim.controller;

import com.example.pim.domain.Product;
import com.example.pim.domain.ProductVariant;
import com.example.pim.service.ProductService;
import com.example.pim.service.ProductVariantService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/products/{productId}/variants")
public class ProductVariantController {

    private final ProductVariantService productVariantService;
    private final ProductService productService;

    @Autowired
    public ProductVariantController(ProductVariantService productVariantService, ProductService productService) {
        this.productVariantService = productVariantService;
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductVariant> createProductVariant(
            @PathVariable Long productId,
            @Valid @RequestBody ProductVariant productVariant) {
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        ProductVariant createdVariant = productVariantService.createProductVariant(product, productVariant);
        return new ResponseEntity<>(createdVariant, HttpStatus.CREATED);
    }
}
