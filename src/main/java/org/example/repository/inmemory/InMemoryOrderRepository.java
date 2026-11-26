package org.example.repository.inmemory;

import org.example.model.entity.Order;
import org.example.repository.OrderRepository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory implementation of {@link OrderRepository}.
 */
public class InMemoryOrderRepository implements OrderRepository {
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    @Override
    public Optional<Order> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(orders.get(id));
    }

    @Override
    public Collection<Order> findAll() {
        return orders.values();
    }

    @Override
    public void save(Order order) {
        orders.put(order.getId(), order);
    }
}
