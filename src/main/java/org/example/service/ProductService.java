package org.example.service;

import org.example.model.entity.Product;
import org.example.model.enums.ProductSortOption;
import org.example.model.exception.NotFoundException;
import org.example.model.exception.ValidationException;
import org.example.repository.CategoryRepository;
import org.example.repository.ProductRepository;
import org.example.service.rating.ProductRatingProvider;
import org.example.service.sorting.ProductSortStrategyFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing product catalog, searching, filtering, sorting, and administrative product lifecycle
 * (User Stories 8, 9, 10, 11, 12, 14, 33, 34, 35).
 */
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRatingProvider ratingProvider;
    private final ProductSortStrategyFactory sortStrategyFactory;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          ProductRatingProvider ratingProvider,
                          ProductSortStrategyFactory sortStrategyFactory) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.ratingProvider = ratingProvider;
        this.sortStrategyFactory = sortStrategyFactory;
    }

    /**
     * User Story 8: View all active products.
     */
    public List<Product> getActiveProducts() {
        return productRepository.findAll().stream()
                .filter(Product::isActive)
                .filter(p -> p.getPrice() >= 0 && p.getStock() >= 0)
                .collect(Collectors.toList());
    }

    /**
     * User Story 9: View product details.
     */
    public Product getProductById(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new ValidationException("شناسه محصول الزامی است.");
        }
        return productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("محصول مورد نظر با شناسه '" + productId + "' در سیستم وجود ندارد."));
    }

    /**
     * User Story 10: Search products. Max query length 100 characters.
     */
    public List<Product> searchProducts(String query) {
        if (query == null || query.isBlank()) {
            return getActiveProducts();
        }
        if (query.trim().length() > 100) {
            throw new ValidationException("عبارت جستجو نباید بیش از ۱۰۰ کاراکتر باشد.");
        }

        String lowerQuery = query.trim().toLowerCase();
        return getActiveProducts().stream()
                .filter(p -> p.getName().toLowerCase().contains(lowerQuery) ||
                        p.getDescription().toLowerCase().contains(lowerQuery) ||
                        p.getBrand().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }

    /**
     * User Story 11: Filter products by category, min/max price, brand, rating, and in-stock.
     */
    public List<Product> filterProducts(String categoryId, Double minPrice, Double maxPrice,
                                        String brand, Integer minRating, Boolean inStockOnly) {
        if (minPrice != null && minPrice < 0) {
            throw new ValidationException("حداقل قیمت نباید منفی باشد.");
        }
        if (maxPrice != null && maxPrice < 0) {
            throw new ValidationException("حداکثر قیمت نباید منفی باشد.");
        }
        if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
            throw new ValidationException("حداقل قیمت نباید بیشتر از حداکثر قیمت باشد.");
        }
        if (minRating != null && (minRating < 1 || minRating > 5)) {
            throw new ValidationException("امتیاز فیلتر باید بین ۱ تا ۵ باشد.");
        }
        if (categoryId != null && !categoryId.isBlank() && !categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("دسته‌بندی مشخص‌شده وجود ندارد.");
        }

        return getActiveProducts().stream()
                .filter(p -> categoryId == null || categoryId.isBlank() || p.getCategoryId().equals(categoryId))
                .filter(p -> minPrice == null || p.getPrice() >= minPrice)
                .filter(p -> maxPrice == null || p.getPrice() <= maxPrice)
                .filter(p -> brand == null || brand.isBlank() || p.getBrand().equalsIgnoreCase(brand.trim()))
                .filter(p -> inStockOnly == null || !inStockOnly || p.getStock() > 0)
                .filter(p -> minRating == null || ratingProvider.getAverageRating(p.getId()) >= minRating)
                .collect(Collectors.toList());
    }

    /**
     * User Story 12: Sort products using OCP-compliant strategy lookup.
     */
    public List<Product> sortProducts(List<Product> products, ProductSortOption sortOption) {
        return sortStrategyFactory.getStrategy(sortOption).sort(products);
    }

    /**
     * User Story 14: View products of a category.
     */
    public List<Product> getProductsByCategory(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            throw new ValidationException("شناسه دسته‌بندی الزامی است.");
        }
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("دسته‌بندی مورد نظر با شناسه '" + categoryId + "' یافت نشد.");
        }
        return getActiveProducts().stream()
                .filter(p -> p.getCategoryId().equals(categoryId))
                .collect(Collectors.toList());
    }

    /**
     * User Story 33: Create product (Admin only).
     */
    public Product createProduct(String name, String description, double price, int stock,
                                 String categoryId, String brand) {
        if (categoryId == null || categoryId.isBlank() || !categoryRepository.existsById(categoryId)) {
            throw new ValidationException("دسته‌بندی مشخص‌شده برای محصول وجود ندارد یا نامعتبر است.");
        }
        Product newProduct = new Product(name, description, price, stock, categoryId, brand);
        productRepository.save(newProduct);
        return newProduct;
    }

    /**
     * User Story 34: Edit product (Admin only).
     */
    public Product updateProduct(String productId, String name, String description, double price,
                                 int stock, String categoryId, String brand) {
        Product product = getProductById(productId);
        if (categoryId == null || categoryId.isBlank() || !categoryRepository.existsById(categoryId)) {
            throw new ValidationException("دسته‌بندی مشخص‌شده برای ویرایش محصول وجود ندارد.");
        }
        product.updateDetails(name, description, price, stock, categoryId, brand);
        productRepository.save(product);
        return product;
    }

    /**
     * User Story 35: Deactivate product (Admin only).
     */
    public void deactivateProduct(String productId) {
        Product product = getProductById(productId);
        product.deactivate();
        productRepository.save(product);
    }

    public void activateProduct(String productId) {
        Product product = getProductById(productId);
        product.activate();
        productRepository.save(product);
    }

    public double getAverageRating(String productId) {
        return ratingProvider.getAverageRating(productId);
    }

    public int getReviewCount(String productId) {
        return ratingProvider.getReviewCount(productId);
    }

    public List<Product> getAllProductsForAdmin() {
        return new ArrayList<>(productRepository.findAll());
    }
}
