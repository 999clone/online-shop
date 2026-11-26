package org.example.service.payment;

import org.example.model.entity.Order;
import org.example.model.value.PaymentTransaction;

/**
 * Abstraction for external payment gateway integration (OCP & DIP).
 */
public interface PaymentGateway {
    PaymentTransaction charge(Order order, boolean simulateSuccess);
}
