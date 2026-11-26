package org.example;

import org.example.model.entity.*;
import org.example.model.enums.OrderStatus;
import org.example.model.enums.PaymentStatus;
import org.example.model.exception.BusinessRuleException;
import org.example.model.exception.ValidationException;
import org.example.model.value.Address;
import org.example.model.value.PaymentTransaction;
import org.example.repository.*;
import org.example.repository.inmemory.*;
import org.example.service.*;
import org.example.service.payment.SimulatedPaymentGateway;
import org.example.service.security.Sha256PasswordHasher;
import org.example.service.sorting.ProductSortStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests covering User Stories 15 to 27, 30 to 32, and 36
 * (Cart, Checkout, Payment, Order History, Reviews & Ratings, and Admin Order Management).
 */
public class CartOrderPaymentReviewTest {

    private UserService userService;
    private ProductService productService;
    private CartService cartService;
    private OrderService orderService;
    private PaymentService paymentService;
    private ReviewService reviewService;

    private static final String CUST_ID = "user-cust";

    @BeforeEach
    void setUp() {
        var passwordHasher = new Sha256PasswordHasher();
        UserRepository userRepo = new InMemoryUserRepository();
        CategoryRepository catRepo = new InMemoryCategoryRepository();
        ProductRepository prodRepo = new InMemoryProductRepository();
        CartRepository cartRepo = new InMemoryCartRepository();
        OrderRepository orderRepo = new InMemoryOrderRepository();
        ReviewRepository reviewRepo = new InMemoryReviewRepository();

        new SampleDataSeeder(catRepo, userRepo, prodRepo, orderRepo, reviewRepo, passwordHasher).seed();

        userService = new UserService(userRepo, passwordHasher);
        reviewService = new ReviewService(reviewRepo, orderRepo, prodRepo, userService);
        productService = new ProductService(prodRepo, catRepo, reviewService, ProductSortStrategyFactory.createDefault(reviewService));
        cartService = new CartService(cartRepo, prodRepo);
        orderService = new OrderService(orderRepo, cartService, prodRepo, userService);
        paymentService = new PaymentService(orderService, prodRepo, new SimulatedPaymentGateway());
    }

    @Test
    @DisplayName("US 15-19: عملیات جامع سبد خرید (افزودن، مشاهده، تغییر تعداد، حذف، خالی کردن)")
    void testCartLifecycle() {
        // US 15: Add to cart
        cartService.addToCart(CUST_ID, "prod-1", 2);
        Cart cart = cartService.getOrCreateCart(CUST_ID);
        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getItem("prod-1").getQuantity());

        // Add again: quantity should increase
        cartService.addToCart(CUST_ID, "prod-1", 1);
        assertEquals(3, cart.getItem("prod-1").getQuantity());

        // US 16: System-calculated total price
        double expectedTotal = productService.getProductById("prod-1").getPrice() * 3;
        assertEquals(expectedTotal, cart.getTotalPrice());

        // US 17: Update quantity
        cartService.updateItemQuantity(CUST_ID, "prod-1", 1);
        assertEquals(1, cart.getItem("prod-1").getQuantity());

        // Exceeding stock throws exception
        assertThrows(BusinessRuleException.class, () ->
                cartService.updateItemQuantity(CUST_ID, "prod-1", 9999)
        );

        // US 18: Remove item
        cartService.removeFromCart(CUST_ID, "prod-1");
        assertTrue(cart.isEmpty());

        // US 19: Clear cart
        cartService.addToCart(CUST_ID, "prod-2", 1);
        cartService.addToCart(CUST_ID, "prod-3", 1);
        assertFalse(cart.isEmpty());
        cartService.clearCart(CUST_ID);
        assertTrue(cart.isEmpty());
        assertEquals(0, cart.getTotalPrice());
    }

    @Test
    @DisplayName("US 20-22: ثبت سفارش از سبد خرید، اعتبارسنجی آدرس و محاسبه سیستمی مبلغ")
    void testCheckoutProcess() {
        cartService.addToCart(CUST_ID, "prod-5", 2);
        User cust = userService.getProfile(CUST_ID);
        Address addr = cust.getDefaultAddress();
        assertNotNull(addr);

        // Start checkout
        Order order = orderService.checkout(CUST_ID, addr.getId());
        assertNotNull(order);
        assertEquals(OrderStatus.PENDING_PAYMENT, order.getStatus());
        assertEquals(1, order.getItems().size());
        assertEquals(productService.getProductById("prod-5").getPrice() * 2, order.getTotalAmount());

        // Cart must be cleared after checkout
        Cart cart = cartService.getOrCreateCart(CUST_ID);
        assertTrue(cart.isEmpty());

        // Checkout with empty cart must fail
        assertThrows(BusinessRuleException.class, () ->
                orderService.checkout(CUST_ID, addr.getId())
        );
    }

    @Test
    @DisplayName("US 23 & 25: پرداخت موفق سفارش، کسر موجودی و جلوگیری از پرداخت دوباره")
    void testSuccessfulPayment() {
        Product p = productService.getProductById("prod-5");
        int stockBefore = p.getStock();

        cartService.addToCart(CUST_ID, p.getId(), 2);
        Address addr = userService.getProfile(CUST_ID).getDefaultAddress();
        Order order = orderService.checkout(CUST_ID, addr.getId());

        // US 23: Successful Payment
        PaymentTransaction trx = paymentService.processPayment(CUST_ID, order.getId(), true);
        assertEquals(PaymentStatus.SUCCESS, trx.getStatus());
        assertEquals(OrderStatus.PAID, order.getStatus());

        // Stock decreased
        assertEquals(stockBefore - 2, p.getStock());

        // US 23: Cannot pay again
        assertThrows(BusinessRuleException.class, () ->
                paymentService.processPayment(CUST_ID, order.getId(), true)
        );
    }

    @Test
    @DisplayName("US 24: پرداخت ناموفق و امکان پرداخت مجدد")
    void testFailedPaymentAndRetry() {
        cartService.addToCart(CUST_ID, "prod-6", 1);
        Address addr = userService.getProfile(CUST_ID).getDefaultAddress();
        Order order = orderService.checkout(CUST_ID, addr.getId());

        // Simulate failure
        PaymentTransaction trx = paymentService.processPayment(CUST_ID, order.getId(), false);
        assertEquals(PaymentStatus.FAILED, trx.getStatus());
        assertEquals(OrderStatus.FAILED_PAYMENT, order.getStatus());

        // Can be retried
        assertTrue(order.canBePaid());

        // Retry with success
        PaymentTransaction retryTrx = paymentService.processPayment(CUST_ID, order.getId(), true);
        assertEquals(PaymentStatus.SUCCESS, retryTrx.getStatus());
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    @DisplayName("US 26, 27, 36: تاریخچه سفارش مشتری، جزئیات سفارش و مدیریت سفارش‌ها توسط ادمین")
    void testOrderHistoryAndAdminOversight() {
        // Customer history
        List<Order> custOrders = orderService.getCustomerOrders(CUST_ID);
        assertFalse(custOrders.isEmpty());

        // Order details
        Order firstOrder = custOrders.get(0);
        Order details = orderService.getOrderDetails(CUST_ID, firstOrder.getId());
        assertEquals(firstOrder.getId(), details.getId());

        // Unauthorized access to other's order
        assertThrows(BusinessRuleException.class, () ->
                orderService.getOrderDetails("user-other", firstOrder.getId())
        );

        // Admin view all orders & filter (US 36)
        List<Order> allOrders = orderService.getAllOrdersForAdmin(null);
        assertFalse(allOrders.isEmpty());

        List<Order> deliveredOrders = orderService.getAllOrdersForAdmin(OrderStatus.DELIVERED);
        assertFalse(deliveredOrders.isEmpty());
        for (Order o : deliveredOrders) {
            assertEquals(OrderStatus.DELIVERED, o.getStatus());
            // Sensitive transaction info is masked
            if (o.getPaymentTransaction() != null) {
                assertTrue(o.getPaymentTransaction().toMaskedString().contains("****"));
            }
        }
    }

    @Test
    @DisplayName("US 30, 31, 32: ثبت نظر و امتیاز فقط توسط خریدار و محافظت از هویت")
    void testReviewsAndRatings() {
        // prod-5 was purchased by CUST_ID in seed data -> eligible
        Review r = reviewService.addReview(CUST_ID, "prod-5", "محصول بسیار کاربردی و عالی بود.", 5);
        assertNotNull(r.getId());
        assertEquals(5, r.getRating());

        // User who has NOT purchased prod-1 should be rejected (US 30)
        assertThrows(BusinessRuleException.class, () ->
                reviewService.addReview(CUST_ID, "prod-1", "من نخریدم ولی نظر میدم!", 4)
        );

        // Rating out of 1..5 range should be rejected (US 31)
        assertThrows(ValidationException.class, () ->
                reviewService.addReview(CUST_ID, "prod-5", "امتیاز خارج از بازه", 6)
        );

        // View reviews (US 32): masked display name
        List<Review> reviews = reviewService.getProductReviews("prod-5");
        assertFalse(reviews.isEmpty());
        for (Review rev : reviews) {
            assertTrue(rev.getMaskedDisplayName().contains("***"), "نام کاربر باید ماسک شده باشد");
        }
    }
}
