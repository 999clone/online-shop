package org.example.model.value;

import org.example.model.exception.ValidationException;
import java.util.Objects;

/**
 * Value Object representing an immutable line item snapshot inside a finalized order.
 */
public class OrderItem {
    private final String productId;
    private final String productName;
    private final double unitPrice;
    private final int quantity;
    private final double subtotal;

    public OrderItem(String productId, String productName, double unitPrice, int quantity) {
        if (productId == null || productId.isBlank()) {
            throw new ValidationException("شناسه محصول در سفارش نامعتبر است.");
        }
        if (productName == null || productName.isBlank()) {
            throw new ValidationException("نام محصول در سفارش نامعتبر است.");
        }
        if (unitPrice < 0) {
            throw new ValidationException("قیمت واحد نمی‌تواند منفی باشد.");
        }
        if (quantity < 1) {
            throw new ValidationException("تعداد باید حداقل ۱ باشد.");
        }
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subtotal = unitPrice * quantity;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getSubtotal() {
        return subtotal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return Double.compare(orderItem.unitPrice, unitPrice) == 0 &&
                quantity == orderItem.quantity &&
                Double.compare(orderItem.subtotal, subtotal) == 0 &&
                Objects.equals(productId, orderItem.productId) &&
                Objects.equals(productName, orderItem.productName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, productName, unitPrice, quantity, subtotal);
    }

    @Override
    public String toString() {
        return productName + " x " + quantity + " (قیمت واحد: " + unitPrice + " -> " + subtotal + ")";
    }
}
