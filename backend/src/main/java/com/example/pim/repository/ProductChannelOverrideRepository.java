package com.example.pim.repository;

import com.example.pim.domain.ProductChannelOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductChannelOverrideRepository extends JpaRepository<ProductChannelOverride, Long> {
    List<ProductChannelOverride> findByProductIdAndChannelId(Long productId, Long channelId);
}
