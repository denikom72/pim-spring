package com.example.pim.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Channel code is mandatory")
    @Column(unique = true)
    @Size(max = 64, message = "Channel code must be max 64 chars")
    private String code;

    @NotBlank(message = "Channel name is mandatory")
    @Size(max = 255, message = "Channel name must be max 255 chars")
    private String name;

    private String description;

    private boolean isActive = false;
}
