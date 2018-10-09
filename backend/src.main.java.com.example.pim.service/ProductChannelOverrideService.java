package com.example.pim.service;

import com.example.pim.domain.Channel;
import com.example.pim.domain.Product;
import com.example.pim.domain.ProductChannelOverride;
import com.example.pim.repository.ChannelRepository;
import com.example.pim.repository.ProductChannelOverrideRepository;
import com.example.pim.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductChannelOverrideService {

    private final ProductChannelOverrideRepository overrideRepository;
    private final ProductRepository productRepository;
    private final ChannelRepository channelRepository;
    private final AuditLogService auditLogService;

    @Autowired
    public ProductChannelOverrideService(ProductChannelOverrideRepository overrideRepository, ProductRepository productRepository, ChannelRepository channelRepository, AuditLogService auditLogService) {
        this.overrideRepository = overrideRepository;
        this.productRepository = productRepository;
        this.channelRepository = channelRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public ProductChannelOverride saveOverride(Long productId, Long channelId, String attributeName, String overrideValue) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found"));

        ProductChannelOverride override = overrideRepository.findByProductIdAndChannelId(productId, channelId).stream()
                .filter(o -> o.getAttributeName().equals(attributeName))
                .findFirst()
                .orElse(new ProductChannelOverride());

        override.setProduct(product);
        override.setChannel(channel);
        override.setAttributeName(attributeName);
        override.setOverrideValue(overrideValue);

        ProductChannelOverride savedOverride = overrideRepository.save(override);
        auditLogService.log("SAVE_OVERRIDE", "ProductChannelOverride", savedOverride.getId(), "system");
        return savedOverride;
    }

    public List<ProductChannelOverride> getOverridesForProductAndChannel(Long productId, Long channelId) {
        return overrideRepository.findByProductIdAndChannelId(productId, channelId);
    }
}
