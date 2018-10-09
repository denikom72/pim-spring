package com.example.pim.controller;

import com.example.pim.domain.ProductFamily;
import com.example.pim.service.ProductFamilyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/product-families")
public class ProductFamilyController {

    private final ProductFamilyService productFamilyService;

    @Autowired
    public ProductFamilyController(ProductFamilyService productFamilyService) {
        this.productFamilyService = productFamilyService;
    }

    @PostMapping
    public ResponseEntity<ProductFamily> createProductFamily(@Valid @RequestBody ProductFamily productFamily) {
        try {
            ProductFamily createdProductFamily = productFamilyService.createProductFamily(productFamily);
            return new ResponseEntity<>(createdProductFamily, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductFamily> getProductFamilyById(@PathVariable Long id) {
        return productFamilyService.getProductFamilyById(id)
                .map(productFamily -> new ResponseEntity<>(productFamily, HttpStatus.OK))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product family not found"));
    }

    @GetMapping
    public ResponseEntity<List<ProductFamily>> getAllProductFamilies() {
        List<ProductFamily> productFamilies = productFamilyService.getAllProductFamilies();
        return new ResponseEntity<>(productFamilies, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductFamily(@PathVariable Long id) {
        try {
            productFamilyService.deleteProductFamily(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}

