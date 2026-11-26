package org.example.service.security;

/**
 * Abstraction for password hashing and verification (SRP & DIP).
 */
public interface PasswordHasher {
    String hash(String rawPassword);
    boolean matches(String rawPassword, String hashedPassword);
}
