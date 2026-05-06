package com.example.foodshop.order.service;

import com.example.foodshop.order.dto.*;
import com.example.foodshop.order.entity.Cart;
import com.example.foodshop.order.entity.Order;
import com.example.foodshop.order.entity.OrderItem;
import com.example.foodshop.order.entity.OrderStatus;
import com.example.foodshop.order.repository.CartRepository;
import com.example.foodshop.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {
    
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private ProductValidationService productValidationService;
    
    public OrderResponse createOrderFromCart(Long userId, CreateOrderRequest request) {
        try {
            // Get user's cart
            Optional<Cart> cartOpt = cartRepository.findByUserIdWithItems(userId);
            if (cartOpt.isEmpty() || cartOpt.get().isEmpty()) {
                throw new IllegalArgumentException("Cart is empty");
            }
            
            Cart cart = cartOpt.get();
            
            // Validate all cart items
            validateCartItems(cart);
            
            // Create order
            Order order = new Order();
            order.setUserId(userId);
            order.setShippingAddress(request.getShippingAddress());
            order.setPhoneNumber(request.getPhoneNumber());
            order.setNotes(request.getNotes());
            order.setStatus(OrderStatus.PENDING);
            
            // Create order items from cart items
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (var cartItem : cart.getCartItems()) {
                var productDetails = productValidationService.getProductDetails(cartItem.getProductId());
                
                OrderItem orderItem = new OrderItem(
                    cartItem.getProductId(),
                    productDetails.getName(),
                    productDetails.getPrice(),
                    productDetails.getImage(),
                    cartItem.getQuantity()
                );
                
                order.addOrderItem(orderItem);
                totalAmount = totalAmount.add(orderItem.getSubtotal());
            }
            
            order.setTotalAmount(totalAmount);
            
            // Save order
            order = orderRepository.save(order);
            
            // Clear cart after successful order creation
            cartService.clearCart(userId);
            
            log.info("Created order {} for user {} with total amount {}", 
                    order.getOrderNumber(), userId, totalAmount);
            
            return convertToOrderResponse(order);
            
        } catch (Exception e) {
            log.error("Error creating order for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
    
    public Page<OrderResponse> getUserOrders(Long userId, Pageable pageable, OrderStatus status) {
        try {
            Page<Order> orders;
            
            if (status != null) {
                orders = orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
            } else {
                orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
            }
            
            return orders.map(this::convertToOrderResponse);
            
        } catch (Exception e) {
            log.error("Error getting orders for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Unable to retrieve orders", e);
        }
    }
    
    public OrderResponse getOrderById(Long orderId, Long userId) {
        try {
            Optional<Order> orderOpt = orderRepository.findByIdAndUserId(orderId, userId);
            if (orderOpt.isEmpty()) {
                throw new IllegalArgumentException("Order not found or access denied");
            }
            
            return convertToOrderResponse(orderOpt.get());
            
        } catch (Exception e) {
            log.error("Error getting order {} for user {}: {}", orderId, userId, e.getMessage(), e);
            throw e;
        }
    }
    
    public OrderResponse cancelOrder(Long orderId, Long userId) {
        try {
            Optional<Order> orderOpt = orderRepository.findByIdAndUserId(orderId, userId);
            if (orderOpt.isEmpty()) {
                throw new IllegalArgumentException("Order not found or access denied");
            }
            
            Order order = orderOpt.get();
            
            if (!order.canBeCancelled()) {
                throw new IllegalArgumentException("Order cannot be cancelled in current status: " + order.getStatus());
            }
            
            order.setStatus(OrderStatus.CANCELLED);
            order = orderRepository.save(order);
            
            log.info("Cancelled order {} for user {}", order.getOrderNumber(), userId);
            
            return convertToOrderResponse(order);
            
        } catch (Exception e) {
            log.error("Error cancelling order {} for user {}: {}", orderId, userId, e.getMessage(), e);
            throw e;
        }
    }
    
    public OrderTrackingResponse getOrderTracking(Long orderId, Long userId) {
        try {
            Optional<Order> orderOpt = orderRepository.findByIdAndUserId(orderId, userId);
            if (orderOpt.isEmpty()) {
                throw new IllegalArgumentException("Order not found or access denied");
            }
            
            Order order = orderOpt.get();
            
            // Create tracking response
            OrderTrackingResponse tracking = new OrderTrackingResponse();
            tracking.setOrderNumber(order.getOrderNumber());
            tracking.setCurrentStatus(order.getStatus());
            tracking.setStatusDisplayName(order.getStatus().getDisplayName());
            
            // Estimate delivery time (simple logic - can be enhanced)
            if (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.PREPARING) {
                tracking.setEstimatedDelivery(order.getCreatedAt().plusHours(2));
            } else if (order.getStatus() == OrderStatus.READY_FOR_PICKUP) {
                tracking.setEstimatedDelivery(order.getCreatedAt().plusMinutes(30));
            }
            
            // Create status history (simplified - in real app, you'd store status changes)
            List<OrderTrackingResponse.OrderStatusHistory> history = new ArrayList<>();
            history.add(new OrderTrackingResponse.OrderStatusHistory(
                OrderStatus.PENDING, OrderStatus.PENDING.getDisplayName(), 
                order.getCreatedAt(), "Order placed successfully"
            ));
            
            if (order.getStatus() != OrderStatus.PENDING) {
                history.add(new OrderTrackingResponse.OrderStatusHistory(
                    order.getStatus(), order.getStatus().getDisplayName(),
                    order.getUpdatedAt(), "Status updated"
                ));
            }
            
            tracking.setStatusHistory(history);
            
            return tracking;
            
        } catch (Exception e) {
            log.error("Error getting order tracking {} for user {}: {}", orderId, userId, e.getMessage(), e);
            throw e;
        }
    }
    
    // Admin methods
    
    public Page<OrderResponse> getAllOrders(Pageable pageable, OrderStatus status, String orderNumber, Long userId) {
        try {
            Page<Order> orders;
            
            if (orderNumber != null && !orderNumber.trim().isEmpty()) {
                orders = orderRepository.findByOrderNumberContainingIgnoreCaseOrderByCreatedAtDesc(orderNumber, pageable);
            } else if (userId != null) {
                if (status != null) {
                    orders = orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
                } else {
                    orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
                }
            } else if (status != null) {
                orders = orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
            } else {
                orders = orderRepository.findAll(pageable);
            }
            
            return orders.map(this::convertToOrderResponse);
            
        } catch (Exception e) {
            log.error("Error getting all orders: {}", e.getMessage(), e);
            throw new RuntimeException("Unable to retrieve orders", e);
        }
    }
    
    public OrderResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        try {
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (orderOpt.isEmpty()) {
                throw new IllegalArgumentException("Order not found");
            }
            
            Order order = orderOpt.get();
            
            if (!order.getStatus().canTransitionTo(request.getStatus())) {
                throw new IllegalArgumentException(
                    String.format("Cannot transition from %s to %s", 
                                order.getStatus(), request.getStatus())
                );
            }
            
            order.setStatus(request.getStatus());
            order = orderRepository.save(order);
            
            log.info("Updated order {} status to {} by admin. Reason: {}", 
                    order.getOrderNumber(), request.getStatus(), request.getReason());
            
            return convertToOrderResponse(order);
            
        } catch (Exception e) {
            log.error("Error updating order {} status: {}", orderId, e.getMessage(), e);
            throw e;
        }
    }
    
    public OrderStatisticsResponse getOrderStatistics(LocalDate startDate, LocalDate endDate) {
        try {
            if (startDate == null) startDate = LocalDate.now().minusDays(30);
            if (endDate == null) endDate = LocalDate.now();
            
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            
            List<Object[]> stats = orderRepository.getOrderStatistics(startDateTime, endDateTime);
            
            OrderStatisticsResponse response = new OrderStatisticsResponse(startDate, endDate);
            Map<String, Long> ordersByStatus = new HashMap<>();
            Map<String, BigDecimal> revenueByStatus = new HashMap<>();
            
            long totalOrders = 0;
            BigDecimal totalRevenue = BigDecimal.ZERO;
            
            for (Object[] stat : stats) {
                OrderStatus status = (OrderStatus) stat[0];
                Long count = (Long) stat[1];
                BigDecimal revenue = (BigDecimal) stat[2];
                
                ordersByStatus.put(status.name(), count);
                revenueByStatus.put(status.name(), revenue != null ? revenue : BigDecimal.ZERO);
                
                totalOrders += count;
                if (revenue != null) {
                    totalRevenue = totalRevenue.add(revenue);
                }
            }
            
            response.setTotalOrders(totalOrders);
            response.setTotalRevenue(totalRevenue);
            response.setOrdersByStatus(ordersByStatus);
            response.setRevenueByStatus(revenueByStatus);
            response.calculateAverageOrderValue();
            
            return response;
            
        } catch (Exception e) {
            log.error("Error getting order statistics: {}", e.getMessage(), e);
            throw new RuntimeException("Unable to retrieve order statistics", e);
        }
    }
    
    // Helper methods
    
    private void validateCartItems(Cart cart) {
        for (var cartItem : cart.getCartItems()) {
            if (!productValidationService.isProductAvailable(cartItem.getProductId(), cartItem.getQuantity())) {
                var productDetails = productValidationService.getProductDetails(cartItem.getProductId());
                String productName = productDetails != null ? productDetails.getName() : "Product #" + cartItem.getProductId();
                throw new IllegalArgumentException("Insufficient stock for product: " + productName);
            }
        }
    }
    
    private OrderResponse convertToOrderResponse(Order order) {
        List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
                .map(this::convertToOrderItemResponse)
                .collect(Collectors.toList());
        
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setStatus(order.getStatus());
        response.setStatusDisplayName(order.getStatus().getDisplayName());
        response.setTotalAmount(order.getTotalAmount());
        response.setShippingAddress(order.getShippingAddress());
        response.setPhoneNumber(order.getPhoneNumber());
        response.setNotes(order.getNotes());
        response.setOrderItems(orderItemResponses);
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        
        return response;
    }
    
    private OrderItemResponse convertToOrderItemResponse(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getId(),
                orderItem.getProductId(),
                orderItem.getProductName(),
                orderItem.getProductPrice(),
                orderItem.getProductImage(),
                orderItem.getQuantity(),
                orderItem.getSubtotal()
        );
    }
}