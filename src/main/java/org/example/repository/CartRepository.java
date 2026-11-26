package org.example.repository;

import org.example.model.entity.Cart;

import java.util.Optional;

/**
 * Abstraction for shopping cart persistence operations (DIP & ISP).
 */
public interface CartRepository {
    Optional<Cart> findByUserId(String userId);
    Cart getOrCreate(String userId);
    void save(Cart cart);
}
