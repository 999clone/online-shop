package org.example.repository.inmemory;

import org.example.model.entity.User;
import org.example.repository.UserRepository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory implementation of {@link UserRepository}.
 */
public class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> users = new ConcurrentHashMap<>();

    @Override
    public Optional<User> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> findByContact(String contact) {
        if (contact == null || contact.isBlank()) return Optional.empty();
        String trimmed = contact.trim();
        return users.values().stream()
                .filter(u -> trimmed.equalsIgnoreCase(u.getEmail()) || trimmed.equals(u.getPhoneNumber()))
                .findFirst();
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public void save(User user) {
        users.put(user.getId(), user);
    }
}
