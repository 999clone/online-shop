package org.example.model.entity;

import org.example.model.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a review and rating submitted by a customer for a purchased product (US 30, 31, 32).
 */
public class Review {
    public static final int MAX_COMMENT_LENGTH = 500;
    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;

    private final String id;
    private final String productId;
    private final String userId;
    private final String userDisplayName;
    private final String comment;
    private final int rating;
    private final LocalDateTime createdAt;

    public Review(String id, String productId, String userId, String userDisplayName,
                  String comment, int rating, LocalDateTime createdAt) {
        validate(productId, userId, comment, rating);
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id;
        this.productId = productId;
        this.userId = userId;
        this.userDisplayName = (userDisplayName == null || userDisplayName.isBlank()) ? "کاربر ناشناس" : userDisplayName;
        this.comment = (comment != null) ? comment.trim() : "";
        this.rating = rating;
        this.createdAt = (createdAt == null) ? LocalDateTime.now() : createdAt;
    }

    public Review(String productId, String userId, String userDisplayName, String comment, int rating) {
        this(UUID.randomUUID().toString(), productId, userId, userDisplayName, comment, rating, LocalDateTime.now());
    }

    private void validate(String productId, String userId, String comment, int rating) {
        if (productId == null || productId.isBlank()) {
            throw new ValidationException("شناسه محصول در ثبت نظر الزامی است.");
        }
        if (userId == null || userId.isBlank()) {
            throw new ValidationException("شناسه کاربر در ثبت نظر الزامی است.");
        }
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new ValidationException("امتیاز فقط می‌تواند بین ۱ تا ۵ باشد.");
        }
        if (comment != null) {
            if (comment.trim().length() > MAX_COMMENT_LENGTH) {
                throw new ValidationException("طول متن نظر نمی‌تواند بیش از " + MAX_COMMENT_LENGTH + " کاراکتر باشد.");
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    /**
     * Returns masked user display name to protect privacy according to User Story 32.
     */
    public String getMaskedDisplayName() {
        if (userDisplayName == null || userDisplayName.length() <= 2) {
            return "ک***";
        }
        return userDisplayName.charAt(0) + "***" + userDisplayName.charAt(userDisplayName.length() - 1);
    }

    public String getComment() {
        return comment;
    }

    public int getRating() {
        return rating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return Objects.equals(id, review.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "[" + rating + "/5 ستاره] " + getMaskedDisplayName() + ": " + (comment.isEmpty() ? "(بدون متن)" : comment);
    }
}
