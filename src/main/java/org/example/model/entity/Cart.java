package org.example.model.entity;

import org.example.model.exception.BusinessRuleException;
import org.example.model.exception.ValidationException;
import org.example.model.value.CartItem;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Entity / Aggregate root representing a user's shopping cart according to User Stories 15 to 19.
 */
public class Cart {
    private final String userId;
    private final Map<String, CartItem> items = new LinkedHashMap<>();

    public Cart(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ValidationException("شناسه کاربر برای سبد خرید الزامی است.");
        }
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public Collection<CartItem> getItems() {
        return Collections.unmodifiableCollection(items.values());
    }

    public CartItem getItem(String productId) {
        return items.get(productId);
    }

    public void addItem(Product product, int quantity) {
        if (product == null) {
            throw new ValidationException("محصول برای افزودن به سبد خرید یافت نشد.");
        }
        if (!product.isActive()) {
            throw new BusinessRuleException("محصول غیرفعال قابل خرید و افزودن به سبد نیست.");
        }
        if (product.getStock() <= 0) {
            throw new BusinessRuleException("محصول ناموجود است.");
        }
        if (quantity < 1) {
            throw new ValidationException("تعداد انتخابی باید حداقل ۱ باشد.");
        }

        CartItem existing = items.get(product.getId());
        int totalQuantity = (existing == null) ? quantity : existing.getQuantity() + quantity;

        if (totalQuantity > product.getStock()) {
            throw new BusinessRuleException("تعداد انتخاب‌شده (" + totalQuantity + ") بیشتر از موجودی انبار (" + product.getStock() + ") است.");
        }

        items.put(product.getId(), new CartItem(product.getId(), product.getName(), product.getPrice(), totalQuantity));
    }

    public void updateQuantity(String productId, int newQuantity, int availableStock) {
        CartItem existing = items.get(productId);
        if (existing == null) {
            throw new BusinessRuleException("محصول در سبد خرید وجود ندارد.");
        }
        if (newQuantity < 1) {
            throw new ValidationException("تعداد باید حداقل ۱ باشد.");
        }
        if (newQuantity > availableStock) {
            throw new BusinessRuleException("تعداد درخواستی (" + newQuantity + ") بیشتر از موجودی انبار (" + availableStock + ") است.");
        }
        items.put(productId, existing.withQuantity(newQuantity));
    }

    public void removeItem(String productId) {
        if (!items.containsKey(productId)) {
            throw new BusinessRuleException("محصول در سبد خرید یافت نشد.");
        }
        items.remove(productId);
    }

    public void clear() {
        items.clear();
    }

    public double getTotalPrice() {
        double sum = 0.0;
        for (CartItem item : items.values()) {
            sum += item.getSubtotal();
        }
        return sum;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getItemCount() {
        int count = 0;
        for (CartItem item : items.values()) {
            count += item.getQuantity();
        }
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cart cart = (Cart) o;
        return Objects.equals(userId, cart.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
