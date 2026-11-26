package org.example.model.entity;

import org.example.model.enums.OrderStatus;
import org.example.model.enums.PaymentStatus;
import org.example.model.exception.BusinessRuleException;
import org.example.model.exception.ValidationException;
import org.example.model.value.Address;
import org.example.model.value.OrderItem;
import org.example.model.value.PaymentTransaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity / Aggregate root representing a Customer Order according to User Stories 20 to 27 and 36.
 */
public class Order {
    private final String id;
    private final String userId;
    private final List<OrderItem> items = new ArrayList<>();
    private final Address shippingAddress;
    private final double totalAmount;
    private OrderStatus status;
    private PaymentTransaction paymentTransaction;
    private final LocalDateTime createdAt;
    private LocalDateTime paidAt;

    public Order(String id, String userId, List<OrderItem> items, Address shippingAddress,
                 double totalAmount, OrderStatus status, PaymentTransaction paymentTransaction,
                 LocalDateTime createdAt, LocalDateTime paidAt) {
        validateOrder(userId, items, shippingAddress, totalAmount);
        this.id = (id == null || id.isBlank()) ? UUID.randomUUID().toString() : id;
        this.userId = userId;
        this.items.addAll(items);
        this.shippingAddress = shippingAddress;
        this.totalAmount = totalAmount;
        this.status = (status == null) ? OrderStatus.PENDING_PAYMENT : status;
        this.paymentTransaction = paymentTransaction;
        this.createdAt = (createdAt == null) ? LocalDateTime.now() : createdAt;
        this.paidAt = paidAt;
    }

    public Order(String userId, List<OrderItem> items, Address shippingAddress, double totalAmount) {
        this(UUID.randomUUID().toString(), userId, items, shippingAddress, totalAmount,
                OrderStatus.PENDING_PAYMENT, null, LocalDateTime.now(), null);
    }

    private void validateOrder(String userId, List<OrderItem> items, Address shippingAddress, double totalAmount) {
        if (userId == null || userId.isBlank()) {
            throw new ValidationException("شناسه مشتری برای سفارش الزامی است.");
        }
        if (items == null || items.isEmpty()) {
            throw new ValidationException("سفارش باید حداقل دارای یک آیتم باشد.");
        }
        if (shippingAddress == null) {
            throw new ValidationException("آدرس ارسال سفارش الزامی است.");
        }
        if (totalAmount < 0) {
            throw new ValidationException("مبلغ نهایی سفارش نمی‌تواند منفی باشد.");
        }
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public PaymentTransaction getPaymentTransaction() {
        return paymentTransaction;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public boolean canBePaid() {
        return status == OrderStatus.PENDING_PAYMENT || status == OrderStatus.FAILED_PAYMENT;
    }

    public void markPaid(PaymentTransaction transaction) {
        if (this.status == OrderStatus.PAID) {
            throw new BusinessRuleException("این سفارش قبلاً پرداخت شده است و پرداخت مجدد آن مجاز نیست.");
        }
        if (transaction == null || transaction.getStatus() != PaymentStatus.SUCCESS) {
            throw new BusinessRuleException("تنها تراکنش‌های پرداخت موفق و تأییدشده می‌توانند وضعیت سفارش را تغییر دهند.");
        }
        if (Double.compare(transaction.getAmount(), this.totalAmount) != 0) {
            throw new BusinessRuleException("مبلغ پرداخت شده با مبلغ نهایی سفارش یکسان نیست.");
        }
        this.status = OrderStatus.PAID;
        this.paymentTransaction = transaction;
        this.paidAt = LocalDateTime.now();
    }

    public void markPaymentFailed(PaymentTransaction transaction) {
        if (this.status == OrderStatus.PAID) {
            throw new BusinessRuleException("سفارش پرداخت‌شده نمی‌تواند به پرداخت ناموفق تغییر یابد.");
        }
        this.status = OrderStatus.FAILED_PAYMENT;
        this.paymentTransaction = transaction;
    }

    public void updateStatus(OrderStatus newStatus) {
        if (newStatus == null) {
            throw new ValidationException("وضعیت جدید سفارش نمی‌تواند خالی باشد.");
        }
        this.status = newStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "سفارش #" + id.substring(0, Math.min(8, id.length())) +
                " | مبلغ کل: " + totalAmount + " تومان | وضعیت: " + status.getPersianLabel();
    }
}
