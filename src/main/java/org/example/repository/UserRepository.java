package org.example.repository;

import org.example.model.entity.User;

import java.util.Collection;
import java.util.Optional;

/**
 * Abstraction for user persistence operations (DIP & ISP).
 */
public interface UserRepository {
    Optional<User> findById(String id);
    Optional<User> findByContact(String contact);
    Collection<User> findAll();
    void save(User user);
}
