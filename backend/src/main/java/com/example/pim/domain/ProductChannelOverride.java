package com.example.pim.domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "channel_id", "attribute_name"}))
public class ProductChannelOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @Column(name = "attribute_name", nullable = false)
    private String attributeName; // e.g., "name", "description"

    @Lob
    private String overrideValue;
}
