package com.example.pim.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Entity
@Data
public class ProductFamily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product family code is mandatory")
    @Column(unique = true)
    @Size(max = 64, message = "Product family code must be max 64 chars")
    private String code;

    @NotBlank(message = "Product family name is mandatory")
    @Size(max = 255, message = "Product family name must be max 255 chars")
    private String name;

    @ManyToMany
    @JoinTable(
            name = "product_family_attribute",
            joinColumns = @JoinColumn(name = "product_family_id"),
            inverseJoinColumns = @JoinColumn(name = "attribute_id"))
    private Set<Attribute> attributes;

    @Min(value = 0, message = "Completeness threshold must be between 0 and 100")
    @Max(value = 100, message = "Completeness threshold must be between 0 and 100")
    private int completenessThreshold = 80; // Default threshold

    private String skuGenerationPattern; // e.g., "{parent_sku}-{attribute_code}"
}


