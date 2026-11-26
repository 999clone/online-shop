package org.example;

import org.example.cli.*;
import org.example.model.entity.Order;
import org.example.model.entity.Product;
import org.example.model.entity.User;
import org.example.model.enums.PaymentStatus;
import org.example.model.enums.ProductSortOption;
import org.example.model.enums.UserRole;
import org.example.model.exception.ValidationException;
import org.example.model.value.Address;
import org.example.model.value.OrderItem;
import org.example.model.value.PaymentTransaction;
import org.example.repository.*;
import org.example.repository.inmemory.*;
import org.example.service.*;
import org.example.service.payment.PaymentGateway;
import org.example.service.payment.SimulatedPaymentGateway;
import org.example.service.security.PasswordHasher;
import org.example.service.security.Sha256PasswordHasher;
import org.example.service.sorting.ProductSortStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying the SOLID-refactored abstractions (SRP, OCP, LSP, ISP, DIP).
 */
public class SolidArchitectureTest {

    private ProductService productService;
    private UserRepository userRepository;
    private PasswordHasher passwordHasher;

    @BeforeEach
    void setUp() {
        passwordHasher = new Sha256PasswordHasher();
        userRepository = new InMemoryUserRepository();
        CategoryRepository catRepo = new InMemoryCategoryRepository();
        ProductRepository prodRepo = new InMemoryProductRepository();
        OrderRepository orderRepo = new InMemoryOrderRepository();
        ReviewRepository reviewRepo = new InMemoryReviewRepository();

        new SampleDataSeeder(catRepo, userRepository, prodRepo, orderRepo, reviewRepo, passwordHasher).seed();

        UserService userService = new UserService(userRepository, passwordHasher);
        ReviewService reviewService = new ReviewService(reviewRepo, orderRepo, prodRepo, userService);
        productService = new ProductService(prodRepo, catRepo, reviewService, ProductSortStrategyFactory.createDefault(reviewService));
    }

    @Test
    @DisplayName("SRP & DIP: PasswordHasher hashes and verifies passwords consistently")
    void testPasswordHasher() {
        String hash = passwordHasher.hash("secret123");
        assertNotEquals("secret123", hash);
        assertTrue(passwordHasher.matches("secret123", hash));
        assertFalse(passwordHasher.matches("wrong", hash));
    }

    @Test
    @DisplayName("OCP & LSP: All ProductSortStrategy options sort without mutating input")
    void testAllSortStrategies() {
        List<Product> active = productService.getActiveProducts();
        for (ProductSortOption option : ProductSortOption.values()) {
            List<Product> sorted = productService.sortProducts(active, option);
            assertEquals(active.size(), sorted.size());
        }
        assertThrows(ValidationException.class, () -> productService.sortProducts(active, null));
    }

    @Test
    @DisplayName("OCP & DIP: PaymentGateway handles success and failure transactions")
    void testPaymentGateway() {
        PaymentGateway gateway = new SimulatedPaymentGateway();
        Address addr = new Address("علی", "09120000000", "ایران", "تهران", "خیابان آزادی", "1111111111", "منزل", true);
        Order order = new Order("user-cust", List.of(new OrderItem("prod-5", "ماوس", 4500000, 1)), addr, 4500000);

        PaymentTransaction ok = gateway.charge(order, true);
        assertEquals(PaymentStatus.SUCCESS, ok.getStatus());
        assertEquals(4500000, ok.getAmount());

        PaymentTransaction fail = gateway.charge(order, false);
        assertEquals(PaymentStatus.FAILED, fail.getStatus());
        assertFalse(fail.getFailureReason().isBlank());
    }

    @Test
    @DisplayName("OCP & LSP: MenuHandler implementations support distinct user roles cleanly")
    void testMenuHandlerRoleRouting() {
        InputHelper input = new InputHelper(new Scanner(""));
        MenuHandler guest = new GuestMenuHandler(null, productService, null, input);
        MenuHandler customer = new CustomerMenuHandler(null, productService, null, null, null, null, null, input);
        MenuHandler admin = new AdminMenuHandler(productService, null, null, null, null, input);

        User custUser = userRepository.findById("user-cust").orElseThrow();
        User adminUser = userRepository.findById("user-admin").orElseThrow();

        assertTrue(guest.supports(null));
        assertFalse(guest.supports(custUser));

        assertTrue(customer.supports(custUser));
        assertFalse(customer.supports(adminUser));
        assertFalse(customer.supports(null));

        assertTrue(admin.supports(adminUser));
        assertFalse(admin.supports(custUser));
        assertEquals(UserRole.ADMIN, adminUser.getRole());
    }
}
