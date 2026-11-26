package org.example.repository.inmemory;

import org.example.model.entity.Cart;
import org.example.repository.CartRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory implementation of {@link CartRepository}.
 */
public class InMemoryCartRepository implements CartRepository {
    private final Map<String, Cart> carts = new ConcurrentHashMap<>();

    @Override
    public Optional<Cart> findByUserId(String userId) {
        if (userId == null) return Optional.empty();
        return Optional.ofNullable(carts.get(userId));
    }

    @Override
    public Cart getOrCreate(String userId) {
        return carts.computeIfAbsent(userId, Cart::new);
    }

    @Override
    public void save(Cart cart) {
        carts.put(cart.getUserId(), cart);
    }
}
