package org.example.repository.inmemory;

import org.example.model.entity.*;
import org.example.model.enums.OrderStatus;
import org.example.model.enums.UserRole;
import org.example.model.value.Address;
import org.example.model.value.OrderItem;
import org.example.model.value.PaymentTransaction;
import org.example.repository.*;
import org.example.service.security.PasswordHasher;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dedicated component for seeding initial sample data (SRP).
 */
public class SampleDataSeeder {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordHasher passwordHasher;

    public SampleDataSeeder(CategoryRepository categoryRepository,
                            UserRepository userRepository,
                            ProductRepository productRepository,
                            OrderRepository orderRepository,
                            ReviewRepository reviewRepository,
                            PasswordHasher passwordHasher) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.passwordHasher = passwordHasher;
    }

    public void seed() {
        Category cat1 = new Category("cat-1", "لپ‌تاپ و کامپیوتر", "انواع لپ‌تاپ‌های گیمینگ، اداری و شخصی", true);
        Category cat2 = new Category("cat-2", "موبایل و تبلت", "جدیدترین گوشی‌های هوشمند و تبلت‌ها", true);
        Category cat3 = new Category("cat-3", "لوازم جانبی", "کیبورد، ماوس، هندزفری و کابل", true);
        Category cat4 = new Category("cat-4", "کتاب و لوازم‌التحریر", "کتاب‌های مهندسی و هنری (دسته‌بندی خالی تستی)", true);
        categoryRepository.save(cat1);
        categoryRepository.save(cat2);
        categoryRepository.save(cat3);
        categoryRepository.save(cat4);

        User admin = new User("user-admin", "مدیر سیستم", "admin@shop.com", "09120000000",
                passwordHasher.hash("admin123"), UserRole.ADMIN, true, LocalDateTime.now());
        User customer = new User("user-cust", "سجاد سارو", "customer@shop.com", "09350000000",
                passwordHasher.hash("user123"), UserRole.CUSTOMER, true, LocalDateTime.now());

        Address defaultAddr = new Address("addr-1", "سجاد سارو", "09350000000", "ایران",
                "تهران", "خیابان حافظ، روبروی دانشگاه امیرکبیر، پلاک ۱", "1591634311", "منزل", true);
        customer.addAddress(defaultAddr);

        userRepository.save(admin);
        userRepository.save(customer);

        Product p1 = new Product("prod-1", "لپ‌تاپ ایسوس ZenBook 14", "لپ‌تاپ باریک با پردازنده Core i7 و حافظه 16GB",
                58000000, 10, cat1.getId(), "ASUS");
        Product p2 = new Product("prod-2", "مک‌بوک ایر M2", "مک‌بوک ایر با تراشه قدرتمند M2 و شارژدهی فوق‌العاده",
                69000000, 5, cat1.getId(), "Apple");
        Product p3 = new Product("prod-3", "گوشی موبایل گلکسی S24", "پرچمدار سامسونگ با هوش مصنوعی و دوربین 200 مگاپیکسل",
                52000000, 12, cat2.getId(), "Samsung");
        Product p4 = new Product("prod-4", "آیفون ۱۵ پرو ۲۵۶ گیگابایت", "بدنه تیتانیومی با چیپ A17 Pro و کیفیت خیره‌کننده",
                79000000, 7, cat2.getId(), "Apple");
        Product p5 = new Product("prod-5", "ماوس بی‌سیم لاجیتک MX Master 3S", "ارگونومیک با کلیدهای بی‌صدا و سنسور دقیق",
                4500000, 20, cat3.getId(), "Logitech");
        Product p6 = new Product("prod-6", "کیبورد مکانیکی ریزر BlackWidow", "سوییچ‌های مکانیکی سبز با نورپردازی RGB",
                6200000, 8, cat3.getId(), "Razer");

        productRepository.save(p1);
        productRepository.save(p2);
        productRepository.save(p3);
        productRepository.save(p4);
        productRepository.save(p5);
        productRepository.save(p6);

        List<OrderItem> orderItems = List.of(new OrderItem(p5.getId(), p5.getName(), p5.getPrice(), 1));
        Order pastOrder = new Order("ord-seed-1", customer.getId(), orderItems, defaultAddr,
                p5.getPrice(), OrderStatus.DELIVERED,
                PaymentTransaction.createSuccess(p5.getPrice(), "REF-PAST-12345"),
                LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(5));
        orderRepository.save(pastOrder);

        Review review1 = new Review("rev-1", p5.getId(), customer.getId(), customer.getFullName(),
                "کیفیت ساخت عالی است و دست هنگام کار طولانی خسته نمی‌شود.", 5, LocalDateTime.now().minusDays(3));
        reviewRepository.save(review1);
    }
}
