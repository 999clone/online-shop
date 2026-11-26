package org.example.service;

import org.example.model.entity.Product;
import org.example.model.exception.BusinessRuleException;
import org.example.model.exception.NotFoundException;
import org.example.model.exception.ValidationException;
import org.example.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Service managing warehouse inventory operations for administrators (User Stories 28 and 29).
 */
public class InventoryService {
    private final ProductRepository productRepository;

    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * User Story 28: View product inventory (Admin only).
     */
    public List<Product> getInventoryList() {
        return new ArrayList<>(productRepository.findAll());
    }

    /**
     * User Story 29: Change inventory (increase or decrease).
     * Prevents stock from becoming less than 0.
     */
    public Product adjustStock(String productId, int delta) {
        Product product = findProduct(productId);
        if (delta == 0) {
            throw new ValidationException("مقدار تغییر موجودی نمی‌تواند صفر باشد.");
        }

        if (delta > 0) {
            product.increaseStock(delta);
        } else {
            int decreaseAmount = Math.abs(delta);
            if (product.getStock() < decreaseAmount) {
                throw new BusinessRuleException("کاهش موجودی امکان‌پذیر نیست؛ موجودی فعلی (" +
                        product.getStock() + ") کمتر از مقدار درخواستی (" + decreaseAmount + ") است و موجودی منفی نمی‌شود.");
            }
            product.decreaseStock(decreaseAmount);
        }

        productRepository.save(product);
        return product;
    }

    /**
     * User Story 29: Direct stock set.
     */
    public Product setStock(String productId, int newStock) {
        if (newStock < 0) {
            throw new ValidationException("موجودی کالا نباید عدد منفی باشد.");
        }
        Product product = findProduct(productId);
        product.setStock(newStock);
        productRepository.save(product);
        return product;
    }

    private Product findProduct(String productId) {
        if (productId == null || productId.isBlank()) {
            throw new ValidationException("شناسه محصول الزامی است.");
        }
        return productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("محصول مورد نظر با شناسه '" + productId + "' در سیستم وجود ندارد."));
    }
}
