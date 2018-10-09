package com.example.pim.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Data
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @NotBlank(message = "File path is mandatory")
    private String filePath;

    @NotBlank(message = "File type is mandatory")
    private String fileType; // e.g., "image/jpeg", "image/png"

    private String altText;

    private boolean isPrimary; // To mark the main image for a product
}
