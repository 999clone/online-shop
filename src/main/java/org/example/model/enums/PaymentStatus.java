package org.example.model.enums;

/**
 * Status of payment transaction.
 */
public enum PaymentStatus {
    SUCCESS("موفق"),
    FAILED("ناموفق");

    private final String persianLabel;

    PaymentStatus(String persianLabel) {
        this.persianLabel = persianLabel;
    }

    public String getPersianLabel() {
        return persianLabel;
    }
}
