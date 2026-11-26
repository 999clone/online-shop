package org.example.repository;

import org.example.model.entity.Review;

import java.util.List;

/**
 * Abstraction for review persistence operations (DIP & ISP).
 */
public interface ReviewRepository {
    List<Review> findByProductId(String productId);
    void save(Review review);
}
