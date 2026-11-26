package org.example.repository.inmemory;

import org.example.model.entity.Product;
import org.example.repository.ProductRepository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory implementation of {@link ProductRepository}.
 */
public class InMemoryProductRepository implements ProductRepository {
    private final Map<String, Product> products = new ConcurrentHashMap<>();

    @Override
    public Optional<Product> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(products.get(id));
    }

    @Override
    public Collection<Product> findAll() {
        return products.values();
    }

    @Override
    public void save(Product product) {
        products.put(product.getId(), product);
    }
}
