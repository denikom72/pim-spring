package com.example.pim.service;

import com.example.pim.domain.Category;
import com.example.pim.repository.CategoryRepository;
import com.example.pim.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final AuditLogService auditLogService;
    private static final int MAX_CATEGORY_DEPTH = 5;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository, AuditLogService auditLogService) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.auditLogService = auditLogService;
    }

    public Category createCategory(Category category) {
        // Validate duplicate category names per level
        if (category.getParent() != null && categoryRepository.existsByParentAndName(category.getParent(), category.getName())) {
            throw new IllegalArgumentException("Category with name '" + category.getName() + "' already exists under this parent.");
        } else if (category.getParent() == null && categoryRepository.findByNameAndParent(category.getName(), null).isPresent()) {
            throw new IllegalArgumentException("Root category with name '" + category.getName() + "' already exists.");
        }

        // Validate maximum category depth
        int level = (category.getParent() != null) ? category.getParent().getLevel() + 1 : 0;
        if (level > MAX_CATEGORY_DEPTH) {
            throw new IllegalArgumentException("Maximum category depth of " + MAX_CATEGORY_DEPTH + " exceeded.");
        }
        category.setLevel(level);

        // Validate unique slug
        if (categoryRepository.existsBySlug(category.getSlug())) {
            throw new IllegalArgumentException("Category slug '" + category.getSlug() + "' already exists.");
        }

        Category createdCategory = categoryRepository.save(category);
        auditLogService.log("CREATE", "Category", createdCategory.getId(), "system"); // TODO: Replace "system" with actual username
        return createdCategory;
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getRootCategories() {
        return categoryRepository.findByParentIsNull();
    }

    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        long productCount = productRepository.countByCategoriesContaining(category);
        if (productCount > 0) {
            throw new IllegalArgumentException("Cannot delete category with " + productCount + " published products.");
        }

        categoryRepository.delete(category);
        auditLogService.log("DELETE", "Category", id, "system"); // TODO: Replace "system" with actual username
    }
}

