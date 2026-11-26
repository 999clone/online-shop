package org.example.cli;

import org.example.model.entity.Category;
import org.example.model.entity.Product;
import org.example.model.entity.Review;
import org.example.model.enums.ProductSortOption;
import org.example.service.CategoryService;
import org.example.service.ProductService;
import org.example.service.ReviewService;

import java.util.List;

/**
 * Shared catalog browsing, searching, filtering, and sorting view (SRP & DRY).
 */
public class CatalogBrowserView {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;
    private final InputHelper input;

    public CatalogBrowserView(ProductService productService,
                              CategoryService categoryService,
                              ReviewService reviewService,
                              InputHelper input) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.reviewService = reviewService;
        this.input = input;
    }

    public void showProductCatalog(List<Product> products) {
        System.out.println("\n-- فهرست محصولات فعال (US 8) --");
        if (products.isEmpty()) {
            System.out.println("هیچ محصول فعالی در فروشگاه یافت نشد.");
            return;
        }
        printProductTable(products);
    }

    public void showProductDetails() {
        System.out.println("\n-- مشاهده جزئیات محصول (US 9) --");
        String pid = input.readString("شناسه محصول (مثلاً prod-1)");
        Product p = productService.getProductById(pid);

        double avgRating = productService.getAverageRating(pid);
        int reviewCount = productService.getReviewCount(pid);

        System.out.println("--------------------------------------------------");
        System.out.println("شناسه: " + p.getId());
        System.out.println("نام: " + p.getName());
        System.out.println("برند: " + p.getBrand());
        System.out.println("توضیحات: " + p.getDescription());
        System.out.println("قیمت: " + String.format("%,.0f", p.getPrice()) + " تومان");
        System.out.println("موجودی در انبار: " + p.getStock() + " عدد");
        System.out.println("وضعیت: " + (p.isActive() ? "فعال و قابل خرید" : "غیرفعال"));
        System.out.println("میانگین امتیاز: " + (avgRating > 0 ? avgRating + "/5" : "هنوز امتیازی ثبت نشده") + " (" + reviewCount + " نظر)");
        System.out.println("--------------------------------------------------");

        List<Review> reviews = reviewService.getProductReviews(pid);
        if (!reviews.isEmpty()) {
            System.out.println("💬 نظرات خریداران (اطلاعات هویتی محافظت شده - US 32):");
            for (Review r : reviews) {
                System.out.println("  " + r);
            }
        }
    }

    public void searchProducts() {
        System.out.println("\n-- جستجوی محصولات (US 10) --");
        String q = input.readString("عبارت مورد نظر برای جستجو (حداکثر ۱۰۰ کاراکتر)");
        List<Product> results = productService.searchProducts(q);
        if (results.isEmpty()) {
            System.out.println("🔍 نتیجه‌ای برای عبارت '" + q + "' یافت نشد.");
        } else {
            System.out.println("✅ " + results.size() + " محصول مرتبط یافت شد:");
            printProductTable(results);
        }
    }

    public void filterProducts() {
        System.out.println("\n-- فیلتر پیشرفته محصولات (US 11) --");
        String categoryId = input.readOptionalString("شناسه دسته‌بندی (مثلاً cat-1)", null);
        Double minPrice = input.readOptionalDouble("حداقل قیمت", 0.0);
        Double maxPrice = input.readOptionalDouble("حداکثر قیمت", 0.0);
        String brand = input.readOptionalString("نام برند", null);
        Integer minRating = input.readOptionalInt("حداقل امتیاز (۱ تا ۵)", 1, 5);
        boolean inStockOnly = input.readBoolean("فقط کالاهای دارای موجودی انبار؟");

        List<Product> filtered = productService.filterProducts(categoryId, minPrice, maxPrice, brand, minRating, inStockOnly);
        if (filtered.isEmpty()) {
            System.out.println("محصولی با فیلترهای مشخص‌شده یافت نشد.");
        } else {
            System.out.println("✅ " + filtered.size() + " محصول منطبق بر فیلتر:");
            printProductTable(filtered);
        }
    }

    public void sortProducts() {
        System.out.println("\n-- مرتب‌سازی محصولات (US 12) --");
        System.out.println("1. ارزان‌ترین");
        System.out.println("2. گران‌ترین");
        System.out.println("3. جدیدترین");
        System.out.println("4. بیشترین امتیاز");
        System.out.println("5. محبوب‌ترین");

        int choice = input.readInt("معیار مرتب‌سازی", 1, 5);
        ProductSortOption opt = switch (choice) {
            case 1 -> ProductSortOption.CHEAPEST;
            case 2 -> ProductSortOption.MOST_EXPENSIVE;
            case 3 -> ProductSortOption.NEWEST;
            case 4 -> ProductSortOption.HIGHEST_RATING;
            case 5 -> ProductSortOption.POPULAR;
            default -> throw new IllegalStateException();
        };

        List<Product> sorted = productService.sortProducts(productService.getActiveProducts(), opt);
        System.out.println("✅ محصولات مرتب‌شده بر اساس [" + opt.getPersianLabel() + "]:");
        printProductTable(sorted);
    }

    public void showCategories() {
        System.out.println("\n-- فهرست دسته‌بندی‌های فعال (US 13) --");
        List<Category> cats = categoryService.getActiveCategories();
        for (Category c : cats) {
            System.out.println("• [" + c.getId() + "] " + c.getName() + " - " + c.getDescription());
        }
    }

    public void showProductsInCategory() {
        System.out.println("\n-- مشاهده محصولات یک دسته‌بندی (US 14) --");
        showCategories();
        String catId = input.readString("شناسه دسته‌بندی را وارد کنید");
        List<Product> prods = productService.getProductsByCategory(catId);
        if (prods.isEmpty()) {
            System.out.println("ℹ️ در این دسته‌بندی هیچ محصول فعالی یافت نشد.");
        } else {
            printProductTable(prods);
        }
    }

    public void printProductTable(List<Product> products) {
        System.out.println("---------------------------------------------------------------------------------------");
        System.out.printf("%-10s | %-28s | %-12s | %-15s | %-8s%n", "شناسه", "نام محصول", "برند", "قیمت (تومان)", "موجودی");
        System.out.println("---------------------------------------------------------------------------------------");
        for (Product p : products) {
            System.out.printf("%-10s | %-28s | %-12s | %-15s | %-8d%n",
                    p.getId(),
                    (p.getName().length() > 27 ? p.getName().substring(0, 24) + "..." : p.getName()),
                    p.getBrand(),
                    String.format("%,.0f", p.getPrice()),
                    p.getStock());
        }
        System.out.println("---------------------------------------------------------------------------------------");
    }
}
