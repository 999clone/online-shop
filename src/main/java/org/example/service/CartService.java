package org.example.service;

import org.example.model.entity.Cart;
import org.example.model.entity.Product;
import org.example.model.exception.NotFoundException;
import org.example.model.exception.ValidationException;
import org.example.repository.CartRepository;
import org.example.repository.ProductRepository;

/**
 * Service managing customer shopping cart operations (User Stories 15 to 19).
 */
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    /**
     * User Story 16: Retrieve cart for authenticated user. Total price is system-computed.
     */
    public Cart getOrCreateCart(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ValidationException("شناسه کاربر برای دسترسی به سبد خرید الزامی است.");
        }
        return cartRepository.getOrCreate(userId);
    }

    /**
     * User Story 15: Add product to cart.
     */
    public void addToCart(String userId, String productId, int quantity) {
        Cart cart = getOrCreateCart(userId);
        Product product = findProduct(productId);
        cart.addItem(product, quantity);
        cartRepository.save(cart);
    }

    /**
     * User Story 17: Update quantity of a product in the cart.
     */
    public void updateItemQuantity(String userId, String productId, int newQuantity) {
        Cart cart = getOrCreateCart(userId);
        Product product = findProduct(productId);
        cart.updateQuantity(productId, newQuantity, product.getStock());
        cartRepository.save(cart);
    }

    /**
     * User Story 18: Remove a product from the cart.
     */
    public void removeFromCart(String userId, String productId) {
        Cart cart = getOrCreateCart(userId);
        cart.removeItem(productId);
        cartRepository.save(cart);
    }

    /**
     * User Story 19: Clear all items from the cart. Count and total become zero.
     */
    public void clearCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        cart.clear();
        cartRepository.save(cart);
    }

    private Product findProduct(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new ValidationException("شناسه محصول الزامی است.");
        }
        return productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("محصول مورد نظر با شناسه '" + productId + "' در سیستم وجود ندارد."));
    }
}
