package org.example.service;

import org.example.model.entity.Category;
import org.example.model.exception.NotFoundException;
import org.example.model.exception.ValidationException;
import org.example.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Service managing categories according to User Stories 13 and 14.
 */
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * User Story 13: View active categories with non-empty names.
     */
    public List<Category> getActiveCategories() {
        List<Category> result = new ArrayList<>();
        for (Category cat : categoryRepository.findAll()) {
            if (cat.isActive() && cat.getName() != null && !cat.getName().isBlank()) {
                result.add(cat);
            }
        }
        return result;
    }

    public Category getCategoryById(String categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("دسته‌بندی مورد نظر با شناسه '" + categoryId + "' یافت نشد."));
    }

    public Category createCategory(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("نام دسته‌بندی نمی‌تواند خالی باشد.");
        }
        Category category = new Category(name, description);
        categoryRepository.save(category);
        return category;
    }

    public boolean categoryExists(String categoryId) {
        return categoryRepository.existsById(categoryId);
    }
}
