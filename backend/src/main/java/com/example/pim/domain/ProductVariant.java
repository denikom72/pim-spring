package com.example.pim.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

@Entity
@Data
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotBlank(message = "SKU is mandatory")
    @Size(max = 64, message = "SKU must be max 64 chars")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "SKU must be alphanumeric, no spaces")
    private String sku;

    @NotBlank(message = "Name is mandatory")
    private String name;

    @ElementCollection
    @CollectionTable(name = "product_variant_attributes", joinColumns = @JoinColumn(name = "product_variant_id"))
    @MapKeyColumn(name = "attribute_name")
    @Column(name = "attribute_value")
    private Map<String, String> attributes;
}
