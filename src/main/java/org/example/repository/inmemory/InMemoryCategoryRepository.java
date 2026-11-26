package org.example.repository.inmemory;

import org.example.model.entity.Category;
import org.example.repository.CategoryRepository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory implementation of {@link CategoryRepository}.
 */
public class InMemoryCategoryRepository implements CategoryRepository {
    private final Map<String, Category> categories = new ConcurrentHashMap<>();

    @Override
    public Optional<Category> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(categories.get(id));
    }

    @Override
    public Collection<Category> findAll() {
        return categories.values();
    }

    @Override
    public boolean existsById(String id) {
        return id != null && categories.containsKey(id);
    }

    @Override
    public void save(Category category) {
        categories.put(category.getId(), category);
    }
}
