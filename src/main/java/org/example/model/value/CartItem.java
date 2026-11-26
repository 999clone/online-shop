package org.example.model.value;

import org.example.model.exception.ValidationException;
import java.util.Objects;

/**
 * Value Object representing a line item in a customer's shopping cart.
 */
public class CartItem {
    private final String productId;
    private final String productName;
    private final double unitPrice;
    private final int quantity;

    public CartItem(String productId, String productName, double unitPrice, int quantity) {
        if (productId == null || productId.isBlank()) {
            throw new ValidationException("شناسه محصول در آیتم سبد خرید نامعتبر است.");
        }
        if (productName == null || productName.isBlank()) {
            throw new ValidationException("نام محصول نامعتبر است.");
        }
        if (unitPrice < 0) {
            throw new ValidationException("قیمت واحد نمی‌تواند منفی باشد.");
        }
        if (quantity < 1) {
            throw new ValidationException("تعداد محصول در سبد خرید باید حداقل ۱ باشد.");
        }
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
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
        return unitPrice * quantity;
    }

    public CartItem withQuantity(int newQuantity) {
        return new CartItem(this.productId, this.productName, this.unitPrice, newQuantity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Double.compare(cartItem.unitPrice, unitPrice) == 0 &&
                quantity == cartItem.quantity &&
                Objects.equals(productId, cartItem.productId) &&
                Objects.equals(productName, cartItem.productName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, productName, unitPrice, quantity);
    }

    @Override
    public String toString() {
        return productName + " | تعداد: " + quantity + " | قیمت واحد: " + unitPrice + " | جمع: " + getSubtotal();
    }
}
