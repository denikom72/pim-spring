package com.example.pim.controller;

import com.example.pim.domain.Media;
import com.example.pim.domain.Product;
import com.example.pim.service.MediaService;
import com.example.pim.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/media")
public class MediaController {

    private final MediaService mediaService;
    private final ProductService productService;

    @Autowired
    public MediaController(MediaService mediaService, ProductService productService) {
        this.mediaService = mediaService;
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Media> uploadMedia(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file) {
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        try {
            Media savedMedia = mediaService.saveMedia(product, file);
            return new ResponseEntity<>(savedMedia, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Media>> getMediaForProduct(@PathVariable Long productId) {
        List<Media> media = mediaService.getMediaForProduct(productId);
        return new ResponseEntity<>(media, HttpStatus.OK);
    }
}
