package org.example.repository.inmemory;

import org.example.model.entity.Review;
import org.example.repository.ReviewRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory implementation of {@link ReviewRepository}.
 */
public class InMemoryReviewRepository implements ReviewRepository {
    private final Map<String, List<Review>> reviews = new ConcurrentHashMap<>();

    @Override
    public List<Review> findByProductId(String productId) {
        List<Review> list = reviews.get(productId);
        return (list == null) ? Collections.emptyList() : Collections.unmodifiableList(list);
    }

    @Override
    public void save(Review review) {
        reviews.computeIfAbsent(review.getProductId(), k -> new ArrayList<>()).add(review);
    }
}
