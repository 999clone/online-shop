package org.example.model.entity;

import org.example.model.exception.BusinessRuleException;
import org.example.model.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a store product according to User Stories 8-12, 28, 29, 33-35.
 */
public class Product {
    private final String id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private String categoryId;
    private String brand;
    private final List<String> images = new ArrayList<>();
    private boolean active;
    private final LocalDateTime createdAt;

    public Product(String id, String name, String description, double price, int stock,
                   String categoryId, String brand, List<String> images, boolean active, LocalDateTime createdAt) {
        validate(name, price, stock, categoryId);
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id;
        this.name = name.trim();
        this.description = (description == null) ? "" : description.trim();
        this.price = price;
        this.stock = stock;
        this.categoryId = categoryId.trim();
        this.brand = (brand == null || brand.isBlank()) ? "متفرقه" : brand.trim();
        if (images != null) {
            this.images.addAll(images);
        }
        this.active = active;
        this.createdAt = (createdAt == null) ? LocalDateTime.now() : createdAt;
    }

    public Product(String id, String name, String description, double price, int stock, String categoryId, String brand) {
        this(id, name, description, price, stock, categoryId, brand, null, true, LocalDateTime.now());
    }

    public Product(String name, String description, double price, int stock, String categoryId, String brand) {
        this(UUID.randomUUID().toString(), name, description, price, stock, categoryId, brand, null, true, LocalDateTime.now());
    }

    private void validate(String name, double price, int stock, String categoryId) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("نام محصول نباید خالی باشد.");
        }
        if (price < 0) {
            throw new ValidationException("قیمت محصول نباید منفی باشد.");
        }
        if (stock < 0) {
            throw new ValidationException("موجودی محصول نباید منفی باشد.");
        }
        if (categoryId == null || categoryId.isBlank()) {
            throw new ValidationException("محصول باید دارای دسته‌بندی باشد.");
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getBrand() {
        return brand;
    }

    public List<String> getImages() {
        return Collections.unmodifiableList(images);
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void updateDetails(String name, String description, double price, int stock, String categoryId, String brand) {
        validate(name, price, stock, categoryId);
        this.name = name.trim();
        this.description = (description == null) ? "" : description.trim();
        this.price = price;
        this.stock = stock;
        this.categoryId = categoryId.trim();
        this.brand = (brand == null || brand.isBlank()) ? "متفرقه" : brand.trim();
    }

    public void increaseStock(int delta) {
        if (delta <= 0) {
            throw new ValidationException("مقدار افزایش موجودی باید بزرگتر از صفر باشد.");
        }
        this.stock += delta;
    }

    public void decreaseStock(int delta) {
        if (delta <= 0) {
            throw new ValidationException("مقدار کاهش موجودی باید بزرگتر از صفر باشد.");
        }
        if (this.stock < delta) {
            throw new BusinessRuleException("موجودی انبار کمتر از مقدار درخواستی است و موجودی نمی‌تواند منفی شود.");
        }
        this.stock -= delta;
    }

    public void setStock(int newStock) {
        if (newStock < 0) {
            throw new ValidationException("موجودی محصول نباید عدد منفی باشد.");
        }
        this.stock = newStock;
    }

    public void setPrice(double newPrice) {
        if (newPrice < 0) {
            throw new ValidationException("قیمت نباید منفی باشد.");
        }
        this.price = newPrice;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public void addImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isBlank()) {
            images.add(imageUrl.trim());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (" + brand + ") - قیمت: " + price + " تومان | موجودی: " + stock + (active ? "" : " [غیرفعال]");
    }
}
