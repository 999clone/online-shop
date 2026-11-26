package org.example.model.value;

import org.example.model.enums.PaymentStatus;
import org.example.model.exception.ValidationException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Value Object snapshot for payment transaction details according to User Stories 23, 24, 25.
 */
public class PaymentTransaction {
    private final String transactionId;
    private final double amount;
    private final PaymentStatus status;
    private final LocalDateTime timestamp;
    private final String gatewayReference;
    private final String failureReason;

    public PaymentTransaction(String transactionId, double amount, PaymentStatus status,
                              LocalDateTime timestamp, String gatewayReference, String failureReason) {
        if (amount < 0) {
            throw new ValidationException("مبلغ تراکنش نمی‌تواند منفی باشد.");
        }
        if (status == null) {
            throw new ValidationException("وضعیت تراکنش پرداخت الزامی است.");
        }
        this.transactionId = (transactionId == null || transactionId.isBlank()) ? UUID.randomUUID().toString() : transactionId;
        this.amount = amount;
        this.status = status;
        this.timestamp = (timestamp == null) ? LocalDateTime.now() : timestamp;
        this.gatewayReference = (gatewayReference == null) ? "" : gatewayReference;
        this.failureReason = (failureReason == null) ? "" : failureReason;
    }

    public static PaymentTransaction createSuccess(double amount, String gatewayRef) {
        return new PaymentTransaction(UUID.randomUUID().toString(), amount, PaymentStatus.SUCCESS,
                LocalDateTime.now(), gatewayRef, null);
    }

    public static PaymentTransaction createFailure(double amount, String failureReason) {
        return new PaymentTransaction(UUID.randomUUID().toString(), amount, PaymentStatus.FAILED,
                LocalDateTime.now(), null, failureReason);
    }

    public String getTransactionId() {
        return transactionId;
    }

    public double getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getGatewayReference() {
        return gatewayReference;
    }

    public String getFailureReason() {
        return failureReason;
    }

    /**
     * Masked representation for display to protect sensitive info (US 36).
     */
    public String toMaskedString() {
        String maskedRef = (gatewayReference != null && gatewayReference.length() > 4)
                ? "****" + gatewayReference.substring(gatewayReference.length() - 4)
                : "****";
        return "تراکنش: " + transactionId.substring(0, Math.min(8, transactionId.length())) +
                "... | وضعیت: " + status.getPersianLabel() +
                " | زمان: " + timestamp +
                " | درگاه: " + maskedRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentTransaction that = (PaymentTransaction) o;
        return Double.compare(that.amount, amount) == 0 &&
                Objects.equals(transactionId, that.transactionId) &&
                status == that.status &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(gatewayReference, that.gatewayReference) &&
                Objects.equals(failureReason, that.failureReason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, amount, status, timestamp, gatewayReference, failureReason);
    }
}
