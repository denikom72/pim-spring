package com.example.pim.repository;

import com.example.pim.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameAndParent(String name, Category parent);
    List<Category> findByParentIsNull(); // For top-level categories
    boolean existsByParentAndName(Category parent, String name);
    boolean existsBySlug(String slug);
}
