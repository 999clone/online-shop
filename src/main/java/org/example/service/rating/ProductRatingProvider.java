package org.example.service.rating;

/**
 * Segregated interface providing product rating metrics without exposing review management (ISP & DIP).
 */
public interface ProductRatingProvider {
    double getAverageRating(String productId);
    int getReviewCount(String productId);
}
