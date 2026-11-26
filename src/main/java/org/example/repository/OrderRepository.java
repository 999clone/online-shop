package org.example.repository;

import org.example.model.entity.Order;

import java.util.Collection;
import java.util.Optional;

/**
 * Abstraction for order persistence operations (DIP & ISP).
 */
public interface OrderRepository {
    Optional<Order> findById(String id);
    Collection<Order> findAll();
    void save(Order order);
}
