package com.example.pim.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Category name is mandatory")
    @Size(max = 255, message = "Category name must be max 255 chars")
    private String name;

    @NotBlank(message = "Category slug is mandatory")
    @Size(max = 255, message = "Category slug must be max 255 chars")
    @Pattern(regexp = "^[a-z0-9-]*$", message = "Category slug must be lowercase alphanumeric and hyphens only")
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("name ASC") // Order by name for consistent hierarchy
    private List<Category> children;

    private int level; // To store the depth of the category
}
