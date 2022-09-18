package com.example.pim.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Data
public class ExportTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Template name is mandatory")
    private String name;

    @NotBlank(message = "Template syntax is mandatory")
    @Lob
    private String templateSyntax; // e.g., Freemarker, Velocity, or a custom format

    @NotBlank(message = "Output format is mandatory")
    private String outputFormat; // e.g., CSV, XML, JSON

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    // Could also include mapping configurations here
}

