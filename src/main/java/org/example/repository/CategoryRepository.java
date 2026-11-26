package org.example.repository;

import org.example.model.entity.Category;

import java.util.Collection;
import java.util.Optional;

/**
 * Abstraction for category persistence operations (DIP & ISP).
 */
public interface CategoryRepository {
    Optional<Category> findById(String id);
    Collection<Category> findAll();
    boolean existsById(String id);
    void save(Category category);
}
