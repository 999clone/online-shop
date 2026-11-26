package org.example.model.enums;

/**
 * Status lifecycle of an order in the online shop.
 */
public enum OrderStatus {
    PENDING_PAYMENT("در انتظار پرداخت"),
    PAID("پرداخت شده"),
    FAILED_PAYMENT("پرداخت ناموفق"),
    PROCESSING("در حال پردازش"),
    SHIPPED("ارسال شده"),
    DELIVERED("تحویل داده شده"),
    CANCELLED("لغو شده");

    private final String persianLabel;

    OrderStatus(String persianLabel) {
        this.persianLabel = persianLabel;
    }

    public String getPersianLabel() {
        return persianLabel;
    }
}
