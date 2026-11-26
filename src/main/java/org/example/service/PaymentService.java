package org.example.service;

import org.example.model.entity.Order;
import org.example.model.entity.Product;
import org.example.model.enums.OrderStatus;
import org.example.model.enums.PaymentStatus;
import org.example.model.exception.BusinessRuleException;
import org.example.model.exception.NotFoundException;
import org.example.model.value.OrderItem;
import org.example.model.value.PaymentTransaction;
import org.example.repository.ProductRepository;
import org.example.service.payment.PaymentGateway;

/**
 * Service managing payment processing, gateway delegation, and payment confirmation
 * (User Stories 23, 24, 25).
 */
public class PaymentService {
    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(OrderService orderService,
                          ProductRepository productRepository,
                          PaymentGateway paymentGateway) {
        this.orderService = orderService;
        this.productRepository = productRepository;
        this.paymentGateway = paymentGateway;
    }

    /**
     * User Story 23 & 25: Process payment for an order via {@link PaymentGateway}.
     */
    public PaymentTransaction processPayment(String userId, String orderId, boolean simulateSuccess) {
        Order order = orderService.getOrderDetails(userId, orderId);

        if (order.getStatus() == OrderStatus.PAID) {
            throw new BusinessRuleException("این سفارش قبلاً پرداخت شده است و پرداخت مجدد آن امکان‌پذیر نیست.");
        }
        if (!order.canBePaid()) {
            throw new BusinessRuleException("این سفارش در وضعیت قابل پرداخت قرار ندارد (وضعیت فعلی: " +
                    order.getStatus().getPersianLabel() + ").");
        }

        if (simulateSuccess) {
            for (OrderItem item : order.getItems()) {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new NotFoundException("محصول '" + item.getProductId() + "' یافت نشد."));
                if (product.getStock() < item.getQuantity()) {
                    throw new BusinessRuleException("موجودی محصول '" + product.getName() +
                            "' به پایان رسیده است و پرداخت امکان‌پذیر نیست.");
                }
            }
        }

        PaymentTransaction transaction = paymentGateway.charge(order, simulateSuccess);

        if (transaction.getStatus() == PaymentStatus.SUCCESS) {
            for (OrderItem item : order.getItems()) {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new NotFoundException("محصول '" + item.getProductId() + "' یافت نشد."));
                product.decreaseStock(item.getQuantity());
                productRepository.save(product);
            }
            order.markPaid(transaction);
        } else {
            order.markPaymentFailed(transaction);
        }
        return transaction;
    }

    /**
     * User Story 25: Explicit verification of payment callback.
     */
    public boolean verifyAndConfirmPayment(String orderId, PaymentTransaction transaction) {
        Order order = orderService.getOrderById(orderId);
        if (order.getStatus() == OrderStatus.PAID) {
            throw new BusinessRuleException("این پرداخت قبلاً تأیید و ثبت شده است.");
        }
        if (transaction.getStatus() != PaymentStatus.SUCCESS) {
            return false;
        }
        if (Double.compare(transaction.getAmount(), order.getTotalAmount()) != 0) {
            throw new BusinessRuleException("مبلغ تراکنش با مبلغ سفارش مغایرت دارد.");
        }
        order.markPaid(transaction);
        return true;
    }
}
