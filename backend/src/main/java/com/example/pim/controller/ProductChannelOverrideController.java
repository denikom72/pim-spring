package com.example.pim.controller;

import com.example.pim.domain.ProductChannelOverride;
import com.example.pim.service.ProductChannelOverrideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products/{productId}/channels/{channelId}/overrides")
public class ProductChannelOverrideController {

    private final ProductChannelOverrideService overrideService;

    @Autowired
    public ProductChannelOverrideController(ProductChannelOverrideService overrideService) {
        this.overrideService = overrideService;
    }

    @PostMapping
    public ResponseEntity<ProductChannelOverride> saveOverride(
            @PathVariable Long productId,
            @PathVariable Long channelId,
            @RequestBody Map<String, String> payload) {
        String attributeName = payload.get("attributeName");
        String overrideValue = payload.get("overrideValue");

        if (attributeName == null || attributeName.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "attributeName is mandatory.");
        }

        try {
            ProductChannelOverride savedOverride = overrideService.saveOverride(productId, channelId, attributeName, overrideValue);
            return new ResponseEntity<>(savedOverride, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ProductChannelOverride>> getOverrides(
            @PathVariable Long productId,
            @PathVariable Long channelId) {
        List<ProductChannelOverride> overrides = overrideService.getOverridesForProductAndChannel(productId, channelId);
        return new ResponseEntity<>(overrides, HttpStatus.OK);
    }
}
