package com.example.pim.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "SKU is mandatory")
    @Size(max = 64, message = "SKU must be max 64 chars")
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "SKU must be alphanumeric, no spaces")
    private String sku;

    @NotBlank(message = "Name is mandatory")
    private String name;

    private String description;

    private String status = "draft";
}
