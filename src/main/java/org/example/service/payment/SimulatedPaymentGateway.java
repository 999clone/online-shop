package org.example.service.payment;

import org.example.model.entity.Order;
import org.example.model.value.PaymentTransaction;

import java.util.UUID;

/**
 * Simulated payment gateway implementation for local testing and CLI interaction.
 */
public class SimulatedPaymentGateway implements PaymentGateway {

    @Override
    public PaymentTransaction charge(Order order, boolean simulateSuccess) {
        double exactAmount = order.getTotalAmount();
        if (simulateSuccess) {
            String gatewayRef = "GW-TRX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            return PaymentTransaction.createSuccess(exactAmount, gatewayRef);
        }
        return PaymentTransaction.createFailure(
                exactAmount, "موجودی حساب کافی نیست یا خطای ارتباط با بانک.");
    }
}
