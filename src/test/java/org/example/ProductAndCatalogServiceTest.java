package org.example;

import org.example.model.entity.Category;
import org.example.model.entity.Product;
import org.example.model.enums.ProductSortOption;
import org.example.model.exception.BusinessRuleException;
import org.example.model.exception.NotFoundException;
import org.example.model.exception.ValidationException;
import org.example.repository.*;
import org.example.repository.inmemory.*;
import org.example.service.*;
import org.example.service.security.Sha256PasswordHasher;
import org.example.service.sorting.ProductSortStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests covering User Stories 8 to 14, 28, 29, 33 to 35
 * (Catalog, Search, Filter, Sort, Categories, Inventory, and Product Admin).
 */
public class ProductAndCatalogServiceTest {

    private ProductService productService;
    private CategoryService categoryService;
    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        var passwordHasher = new Sha256PasswordHasher();
        UserRepository userRepo = new InMemoryUserRepository();
        CategoryRepository catRepo = new InMemoryCategoryRepository();
        ProductRepository prodRepo = new InMemoryProductRepository();
        OrderRepository orderRepo = new InMemoryOrderRepository();
        ReviewRepository reviewRepo = new InMemoryReviewRepository();

        new SampleDataSeeder(catRepo, userRepo, prodRepo, orderRepo, reviewRepo, passwordHasher).seed();

        UserService userService = new UserService(userRepo, passwordHasher);
        ReviewService reviewService = new ReviewService(reviewRepo, orderRepo, prodRepo, userService);
        productService = new ProductService(prodRepo, catRepo, reviewService, ProductSortStrategyFactory.createDefault(reviewService));
        categoryService = new CategoryService(catRepo);
        inventoryService = new InventoryService(prodRepo);
    }

    @Test
    @DisplayName("US 8: مشاهده فهرست محصولات فعال و نامنفی بودن قیمت و موجودی")
    void testGetActiveProducts() {
        List<Product> products = productService.getActiveProducts();
        assertFalse(products.isEmpty());
        for (Product p : products) {
            assertTrue(p.isActive());
            assertTrue(p.getPrice() >= 0);
            assertTrue(p.getStock() >= 0);
        }
    }

    @Test
    @DisplayName("US 9: مشاهده جزئیات محصول و بررسی عدم وجود")
    void testGetProductDetails() {
        Product p = productService.getProductById("prod-1");
        assertNotNull(p);
        assertEquals("لپ‌تاپ ایسوس ZenBook 14", p.getName());

        assertThrows(NotFoundException.class, () ->
                productService.getProductById("non-existent-id")
        );
    }

    @Test
    @DisplayName("US 10: جستجوی محصول با محدودیت حداکثر ۱۰۰ کاراکتر")
    void testSearchProducts() {
        List<Product> results = productService.searchProducts("ایسوس");
        assertEquals(1, results.size());
        assertEquals("prod-1", results.get(0).getId());

        // Empty search returns all active
        assertEquals(productService.getActiveProducts().size(), productService.searchProducts("").size());

        // Over 100 characters must throw ValidationException
        String longQuery = "a".repeat(101);
        assertThrows(ValidationException.class, () ->
                productService.searchProducts(longQuery)
        );
    }

    @Test
    @DisplayName("US 11: فیلتر محصولات و اعتبارسنجی بازه‌های قیمت و امتیاز")
    void testFilterProducts() {
        // Valid filter
        List<Product> filtered = productService.filterProducts("cat-1", 50000000.0, 60000000.0, null, null, null);
        assertEquals(1, filtered.size());
        assertEquals("prod-1", filtered.get(0).getId());

        // Min price > Max price should throw ValidationException
        assertThrows(ValidationException.class, () ->
                productService.filterProducts(null, 100.0, 50.0, null, null, null)
        );

        // Negative price should throw ValidationException
        assertThrows(ValidationException.class, () ->
                productService.filterProducts(null, -10.0, 50.0, null, null, null)
        );

        // Rating out of 1..5 should throw ValidationException
        assertThrows(ValidationException.class, () ->
                productService.filterProducts(null, null, null, null, 6, null)
        );
    }

    @Test
    @DisplayName("US 12: مرتب‌سازی محصولات بر اساس ارزان‌ترین و گران‌ترین")
    void testSortProducts() {
        List<Product> active = productService.getActiveProducts();

        List<Product> cheapest = productService.sortProducts(active, ProductSortOption.CHEAPEST);
        assertTrue(cheapest.get(0).getPrice() <= cheapest.get(cheapest.size() - 1).getPrice());

        List<Product> mostExpensive = productService.sortProducts(active, ProductSortOption.MOST_EXPENSIVE);
        assertTrue(mostExpensive.get(0).getPrice() >= mostExpensive.get(mostExpensive.size() - 1).getPrice());
    }

    @Test
    @DisplayName("US 13 & 14: مشاهده دسته‌بندی‌ها و محصولات یک دسته‌بندی")
    void testCategoriesAndCategoryProducts() {
        List<Category> cats = categoryService.getActiveCategories();
        assertFalse(cats.isEmpty());

        List<Product> cat1Products = productService.getProductsByCategory("cat-1");
        assertFalse(cat1Products.isEmpty());

        // Empty category (cat-4)
        List<Product> emptyCatProducts = productService.getProductsByCategory("cat-4");
        assertTrue(emptyCatProducts.isEmpty());

        // Non-existent category
        assertThrows(NotFoundException.class, () ->
                productService.getProductsByCategory("invalid-cat")
        );
    }

    @Test
    @DisplayName("US 28 & 29: مشاهده موجودی انبار و تغییر موجودی بدون منفی شدن")
    void testInventoryManagement() {
        List<Product> inventory = inventoryService.getInventoryList();
        assertFalse(inventory.isEmpty());

        Product p = productService.getProductById("prod-5");
        int initialStock = p.getStock();

        // Increase stock
        inventoryService.adjustStock(p.getId(), 5);
        assertEquals(initialStock + 5, p.getStock());

        // Decrease stock
        inventoryService.adjustStock(p.getId(), -3);
        assertEquals(initialStock + 2, p.getStock());

        // Excessive decrease causing negative stock must fail
        assertThrows(BusinessRuleException.class, () ->
                inventoryService.adjustStock(p.getId(), -(p.getStock() + 10))
        );
    }

    @Test
    @DisplayName("US 33, 34, 35: ایجاد، ویرایش و غیرفعال‌سازی محصول توسط مدیر")
    void testProductAdminOperations() {
        // Create product
        Product newP = productService.createProduct("هدفون سونی WH-1000XM5", "هدفون با نویزکنسلینگ فوق‌العاده",
                18000000, 10, "cat-3", "Sony");
        assertNotNull(newP.getId());
        assertTrue(newP.isActive());

        // Edit product
        productService.updateProduct(newP.getId(), "هدفون سونی مدل XM5 ویرایش شده", "توضیح جدید",
                17500000, 15, "cat-3", "Sony");
        Product updated = productService.getProductById(newP.getId());
        assertEquals("هدفون سونی مدل XM5 ویرایش شده", updated.getName());
        assertEquals(17500000, updated.getPrice());

        // Deactivate product
        productService.deactivateProduct(newP.getId());
        assertFalse(updated.isActive());

        // Deactivated product should NOT appear in active products catalog
        List<Product> activeProducts = productService.getActiveProducts();
        assertFalse(activeProducts.contains(updated));
    }
}
