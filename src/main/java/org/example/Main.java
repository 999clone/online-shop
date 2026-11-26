package org.example;

import org.example.cli.*;
import org.example.repository.*;
import org.example.repository.inmemory.*;
import org.example.service.*;
import org.example.service.payment.PaymentGateway;
import org.example.service.payment.SimulatedPaymentGateway;
import org.example.service.security.PasswordHasher;
import org.example.service.security.Sha256PasswordHasher;
import org.example.service.sorting.ProductSortStrategyFactory;

import java.util.List;
import java.util.Scanner;

/**
 * Composition Root for the Online Shop Application.
 * Wires abstractions and concrete implementations according to Dependency Inversion Principle (DIP).
 */
public class Main {
    public static void main(String[] args) {
        // 1. Initialize Infrastructure & Security Abstractions
        PasswordHasher passwordHasher = new Sha256PasswordHasher();
        PaymentGateway paymentGateway = new SimulatedPaymentGateway();

        UserRepository userRepository = new InMemoryUserRepository();
        CategoryRepository categoryRepository = new InMemoryCategoryRepository();
        ProductRepository productRepository = new InMemoryProductRepository();
        CartRepository cartRepository = new InMemoryCartRepository();
        OrderRepository orderRepository = new InMemoryOrderRepository();
        ReviewRepository reviewRepository = new InMemoryReviewRepository();

        // Seed initial sample data
        new SampleDataSeeder(
                categoryRepository, userRepository, productRepository,
                orderRepository, reviewRepository, passwordHasher
        ).seed();

        // 2. Initialize Service Layer
        UserService userService = new UserService(userRepository, passwordHasher);
        CategoryService categoryService = new CategoryService(categoryRepository);
        ReviewService reviewService = new ReviewService(reviewRepository, orderRepository, productRepository, userService);
        ProductSortStrategyFactory sortStrategyFactory = ProductSortStrategyFactory.createDefault(reviewService);
        ProductService productService = new ProductService(productRepository, categoryRepository, reviewService, sortStrategyFactory);
        CartService cartService = new CartService(cartRepository, productRepository);
        OrderService orderService = new OrderService(orderRepository, cartService, productRepository, userService);
        PaymentService paymentService = new PaymentService(orderService, productRepository, paymentGateway);
        InventoryService inventoryService = new InventoryService(productRepository);

        // 3. Initialize Presentation Layer (CLI)
        InputHelper input = new InputHelper(new Scanner(System.in));
        CatalogBrowserView catalogView = new CatalogBrowserView(productService, categoryService, reviewService, input);
        List<MenuHandler> menuHandlers = List.of(
                new GuestMenuHandler(userService, productService, catalogView, input),
                new CustomerMenuHandler(userService, productService, cartService, orderService,
                        paymentService, reviewService, catalogView, input),
                new AdminMenuHandler(productService, categoryService, inventoryService,
                        orderService, catalogView, input)
        );

        ConsoleApp app = new ConsoleApp(menuHandlers);

        // 4. Run Application
        app.run();
    }
}
