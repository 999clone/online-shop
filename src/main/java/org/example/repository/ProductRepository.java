package org.example.repository;

import org.example.model.entity.Product;

import java.util.Collection;
import java.util.Optional;

/**
 * Abstraction for product persistence operations (DIP & ISP).
 */
public interface ProductRepository {
    Optional<Product> findById(String id);
    Collection<Product> findAll();
    void save(Product product);
}
