package com.example.foodshop.order.service;

import com.example.foodshop.order.dto.CreateOrderItemRequest;
import com.example.foodshop.order.dto.CreateOrderRequest;
import com.example.foodshop.order.entity.Order;
import com.example.foodshop.order.entity.OrderItem;
import com.example.foodshop.order.entity.ProductRef;
import com.example.foodshop.order.repository.OrderRepository;
import com.example.foodshop.order.repository.ProductRefRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    private static final double SHIPPING_FEE = 30000.0;

    private final OrderRepository orderRepository;
    private final ProductRefRepository productRefRepository;

    public OrderService(OrderRepository orderRepository, ProductRefRepository productRefRepository) {
        this.orderRepository = orderRepository;
        this.productRefRepository = productRefRepository;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setVoucherCode(request.getVoucherCode());
        order.setStatus(Order.OrderStatus.PENDING);

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (CreateOrderItemRequest itemRequest : request.getItems()) {
            ProductRef product = productRefRepository.findById(itemRequest.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + itemRequest.getId()));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setQuantity(itemRequest.getQuantity());
            item.setPrice(product.getPrice());

            orderItems.add(item);
            totalAmount += product.getPrice() * itemRequest.getQuantity();
        }

        totalAmount += SHIPPING_FEE;
        order.setOriginalAmount(totalAmount);
        order.setDiscountAmount(0.0);
        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);

        return orderRepository.save(order);
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Order cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found or no permission"));

        if (order.getStatus() != Order.OrderStatus.PENDING
                && order.getStatus() != Order.OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order cannot be cancelled at this stage");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }
}
