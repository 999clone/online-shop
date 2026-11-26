package org.example.service;

import org.example.model.entity.Order;
import org.example.model.entity.Review;
import org.example.model.entity.User;
import org.example.model.enums.OrderStatus;
import org.example.model.exception.BusinessRuleException;
import org.example.model.exception.NotFoundException;
import org.example.model.exception.ValidationException;
import org.example.model.value.OrderItem;
import org.example.repository.OrderRepository;
import org.example.repository.ProductRepository;
import org.example.repository.ReviewRepository;
import org.example.service.rating.ProductRatingProvider;

import java.util.List;

/**
 * Service managing reviews and ratings (User Stories 30, 31, 32).
 * Also implements {@link ProductRatingProvider} to provide rating metrics without coupling.
 */
public class ReviewService implements ProductRatingProvider {
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserService userService;

    public ReviewService(ReviewRepository reviewRepository,
                         OrderRepository orderRepository,
                         ProductRepository productRepository,
                         UserService userService) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userService = userService;
    }

    /**
     * Checks whether a customer has purchased a product in a finalized (PAID, PROCESSING, SHIPPED, DELIVERED) order.
     */
    public boolean hasCustomerPurchasedProduct(String userId, String productId) {
        for (Order order : orderRepository.findAll()) {
            if (order.getUserId().equals(userId)) {
                OrderStatus s = order.getStatus();
                if (s == OrderStatus.PAID || s == OrderStatus.PROCESSING ||
                        s == OrderStatus.SHIPPED || s == OrderStatus.DELIVERED) {
                    for (OrderItem item : order.getItems()) {
                        if (item.getProductId().equals(productId)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * User Story 30 & 31: Add review and rating.
     */
    public Review addReview(String userId, String productId, String comment, int rating) {
        ensureProductExists(productId);
        User user = userService.getProfile(userId);

        if (!hasCustomerPurchasedProduct(userId, productId)) {
            throw new BusinessRuleException("ثبت نظر یا امتیاز تنها برای کاربرانی که این محصول را خریداری کرده‌اند امکان‌پذیر است.");
        }
        if (comment == null || comment.isBlank()) {
            throw new ValidationException("متن نظر نباید خالی باشد.");
        }

        Review review = new Review(productId, userId, user.getFullName(), comment, rating);
        reviewRepository.save(review);
        return review;
    }

    /**
     * User Story 31: Submit rating only (optional comment).
     */
    public Review submitRatingOnly(String userId, String productId, int rating) {
        ensureProductExists(productId);
        User user = userService.getProfile(userId);

        if (!hasCustomerPurchasedProduct(userId, productId)) {
            throw new BusinessRuleException("امتیاز دادن به محصول تنها برای خریداران آن کالا مجاز است.");
        }

        Review review = new Review(productId, userId, user.getFullName(), "", rating);
        reviewRepository.save(review);
        return review;
    }

    /**
     * User Story 32: View reviews for a product. Sensitive user personal details are masked.
     */
    public List<Review> getProductReviews(String productId) {
        ensureProductExists(productId);
        return reviewRepository.findByProductId(productId);
    }

    @Override
    public double getAverageRating(String productId) {
        List<Review> productReviews = reviewRepository.findByProductId(productId);
        if (productReviews.isEmpty()) {
            return 0.0;
        }
        double sum = 0;
        for (Review r : productReviews) {
            sum += r.getRating();
        }
        return Math.round((sum / productReviews.size()) * 10.0) / 10.0;
    }

    @Override
    public int getReviewCount(String productId) {
        return reviewRepository.findByProductId(productId).size();
    }

    private void ensureProductExists(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new ValidationException("شناسه محصول الزامی است.");
        }
        if (productRepository.findById(productId).isEmpty()) {
            throw new NotFoundException("محصول مورد نظر با شناسه '" + productId + "' در سیستم وجود ندارد.");
        }
    }
}
