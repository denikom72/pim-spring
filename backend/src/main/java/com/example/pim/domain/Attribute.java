package com.example.pim.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Data
public class Attribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Attribute code is mandatory")
    @Column(unique = true)
    private String code;
    private String name;
    private String type; // e.g., TEXT, NUMBER, COLOR, SIZE
    private String validationRegex; // Regex pattern for validation
    private boolean variantAttribute; // true if this attribute can differentiate variants
}


