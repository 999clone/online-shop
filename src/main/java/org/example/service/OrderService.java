package org.example.service;

import org.example.model.entity.*;
import org.example.model.enums.OrderStatus;
import org.example.model.exception.BusinessRuleException;
import org.example.model.exception.NotFoundException;
import org.example.model.exception.ValidationException;
import org.example.model.value.Address;
import org.example.model.value.CartItem;
import org.example.model.value.OrderItem;
import org.example.repository.OrderRepository;
import org.example.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing checkout, order creation, order querying, and admin order oversight
 * (User Stories 20, 21, 22, 26, 27, 36).
 */
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductRepository productRepository;
    private final UserService userService;

    public OrderService(OrderRepository orderRepository,
                        CartService cartService,
                        ProductRepository productRepository,
                        UserService userService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.productRepository = productRepository;
        this.userService = userService;
    }

    /**
     * User Stories 20, 21, 22: Create an order from user cart with selected shipping address.
     */
    public Order checkout(String userId, String addressId) {
        if (userId == null || userId.isBlank()) {
            throw new ValidationException("شناسه کاربر الزامی است.");
        }

        User user = userService.getProfile(userId);
        Cart cart = cartService.getOrCreateCart(userId);
        if (cart.isEmpty()) {
            throw new BusinessRuleException("سبد خرید شما خالی است و نمی‌توانید سفارش ثبت کنید.");
        }

        Address shippingAddress = user.getAddressById(addressId);
        if (shippingAddress == null) {
            throw new BusinessRuleException("آدرس انتخابی معتبر نیست یا به این کاربر تعلق ندارد.");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        double systemCalculatedTotal = 0.0;

        for (CartItem ci : cart.getItems()) {
            Product product = productRepository.findById(ci.getProductId())
                    .orElseThrow(() -> new NotFoundException("محصول '" + ci.getProductId() + "' یافت نشد."));
            if (!product.isActive()) {
                throw new BusinessRuleException("محصول '" + product.getName() + "' غیرفعال شده است.");
            }
            if (product.getStock() < ci.getQuantity()) {
                throw new BusinessRuleException("موجودی محصول '" + product.getName() +
                        "' کافی نیست (موجودی فعلی: " + product.getStock() + "، درخواستی: " + ci.getQuantity() + ").");
            }

            OrderItem orderItem = new OrderItem(product.getId(), product.getName(), product.getPrice(), ci.getQuantity());
            orderItems.add(orderItem);
            systemCalculatedTotal += orderItem.getSubtotal();
        }

        Order order = new Order(userId, orderItems, shippingAddress, systemCalculatedTotal);
        orderRepository.save(order);
        cartService.clearCart(userId);

        return order;
    }

    /**
     * User Story 26: View customer order history.
     */
    public List<Order> getCustomerOrders(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ValidationException("شناسه کاربر الزامی است.");
        }
        return orderRepository.findAll().stream()
                .filter(o -> o.getUserId().equals(userId))
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * User Story 27: View specific order details (must belong to customer).
     */
    public Order getOrderDetails(String userId, String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new ValidationException("شناسه سفارش الزامی است.");
        }
        Order order = getOrderById(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessRuleException("دسترسی غیرمجاز: این سفارش متعلق به حساب کاربری شما نیست.");
        }
        return order;
    }

    /**
     * Retrieve order by ID for payment and internal workflows.
     */
    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("سفارش با شناسه '" + orderId + "' یافت نشد."));
    }

    /**
     * User Story 36: Admin manage and view all orders, with optional status filter.
     */
    public List<Order> getAllOrdersForAdmin(OrderStatus statusFilter) {
        return orderRepository.findAll().stream()
                .filter(o -> statusFilter == null || o.getStatus() == statusFilter)
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Admin update order status (e.g. PROCESSING -> SHIPPED -> DELIVERED).
     */
    public void updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = getOrderById(orderId);
        order.updateStatus(newStatus);
        orderRepository.save(order);
    }
}
