package com.example.foodshop.order.service;

import com.example.foodshop.order.dto.*;
import com.example.foodshop.order.entity.Cart;
import com.example.foodshop.order.entity.Order;
import com.example.foodshop.order.entity.OrderItem;
import com.example.foodshop.order.entity.OrderStatus;
import com.example.foodshop.order.repository.CartRepository;
import com.example.foodshop.order.repository.OrderRepository;
import com.example.foodshop.order.client.PaymentServiceClient;
import com.example.foodshop.order.client.CreatePaymentRequest;
import com.example.foodshop.order.client.PaymentResponse;
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
    
    @Autowired
    private PaymentServiceClient paymentServiceClient;
    
    @Autowired
    private com.example.foodshop.order.client.IdentityServiceClient identityServiceClient;
    
    @Autowired
    private OrderAutomationService orderAutomationService;
    
    @Autowired
    private com.example.foodshop.order.repository.ShipperRepository shipperRepository;
    
    public OrderResponse createOrderFromCart(Long userId, CreateOrderRequest request, String authToken) {
        try {
            // Get user's cart
            Optional<Cart> cartOpt = cartRepository.findByUserIdWithItems(userId);
            if (cartOpt.isEmpty() || cartOpt.get().isEmpty()) {
                throw new IllegalArgumentException("Cart is empty");
            }
            
            Cart cart = cartOpt.get();
            
            // Determine which items to checkout
            List<com.example.foodshop.order.entity.CartItem> itemsToCheckout;
            if (request.getSelectedProductIds() != null && !request.getSelectedProductIds().isEmpty()) {
                // Checkout only selected items
                itemsToCheckout = cart.getCartItems().stream()
                    .filter(item -> request.getSelectedProductIds().contains(item.getProductId()))
                    .collect(Collectors.toList());
                
                if (itemsToCheckout.isEmpty()) {
                    throw new IllegalArgumentException("No valid items selected for checkout");
                }
                
                log.info("Checking out {} selected items out of {} total items in cart", 
                        itemsToCheckout.size(), cart.getCartItems().size());
            } else {
                // Checkout all items (backward compatible)
                itemsToCheckout = new ArrayList<>(cart.getCartItems());
                log.info("Checking out all {} items in cart", itemsToCheckout.size());
            }
            
            // Validate all items to checkout
            validateCartItems(itemsToCheckout);
            
            // Create order
            Order order = new Order();
            order.setUserId(userId);
            order.setShippingAddress(request.getShippingAddress());
            order.setPhoneNumber(request.getPhoneNumber());
            order.setNotes(request.getNotes());
            order.setPaymentMethod(request.getPaymentMethod());
            order.setStatus(OrderStatus.PENDING); // Start with PENDING status
            
            // Create order items from selected cart items
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (var cartItem : itemsToCheckout) {
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
            
            // Save order first
            order = orderRepository.save(order);
            
            // Try to auto-confirm the order
            orderAutomationService.autoConfirmOrder(order);
            
            // Reload order to get updated status
            order = orderRepository.findById(order.getId())
                .orElseThrow(() -> new RuntimeException("Order not found after creation"));
            
            // Create payment record
            try {
                CreatePaymentRequest paymentRequest = new CreatePaymentRequest(
                    order.getId(),
                    userId,
                    totalAmount,
                    request.getPaymentMethod()
                );
                
                PaymentResponse paymentResponse = paymentServiceClient.createPayment(
                    paymentRequest, 
                    authToken
                ).getBody();
                
                log.info("Created payment {} for order {}", 
                        paymentResponse != null ? paymentResponse.getPaymentNumber() : "unknown", 
                        order.getOrderNumber());
                        
            } catch (Exception e) {
                log.warn("Failed to create payment for order {}: {}", order.getOrderNumber(), e.getMessage());
                // Continue with order creation even if payment creation fails
            }
            
            // Clear cart after successful order creation
            if (request.getSelectedProductIds() != null && !request.getSelectedProductIds().isEmpty()) {
                // Remove only selected items from cart
                for (Long productId : request.getSelectedProductIds()) {
                    try {
                        cartService.removeFromCart(userId, productId);
                    } catch (Exception e) {
                        log.warn("Failed to remove item {} from cart: {}", productId, e.getMessage());
                    }
                }
                log.info("Removed {} selected items from cart", request.getSelectedProductIds().size());
            } else {
                // Clear entire cart (backward compatible)
                cartService.clearCart(userId);
                log.info("Cleared entire cart");
            }
            
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
                OrderStatus.CONFIRMED, OrderStatus.CONFIRMED.getDisplayName(), 
                order.getCreatedAt(), "Order placed and confirmed automatically"
            ));
            
            if (order.getStatus() != OrderStatus.CONFIRMED) {
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
    
    private void validateCartItems(List<com.example.foodshop.order.entity.CartItem> cartItems) {
        for (var cartItem : cartItems) {
            if (!productValidationService.isProductAvailable(cartItem.getProductId(), cartItem.getQuantity())) {
                var productDetails = productValidationService.getProductDetails(cartItem.getProductId());
                String productName = productDetails != null ? productDetails.getName() : "Product #" + cartItem.getProductId();
                throw new IllegalArgumentException("Insufficient stock for product: " + productName);
            }
        }
    }
    
    private void validateCartItems(Cart cart) {
        validateCartItems(cart.getCartItems());
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
        response.setPaymentMethod(order.getPaymentMethod());
        response.setAutoConfirmed(order.getAutoConfirmed());
        response.setCancellationReason(order.getCancellationReason());
        response.setOrderItems(orderItemResponses);
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        
        // Fetch user information from Identity Service
        response.setUserId(order.getUserId());
        try {
            com.example.foodshop.order.client.UserDTO user = identityServiceClient.getUserById(order.getUserId());
            response.setUsername(user.getUsername());
            response.setUserEmail(user.getEmail());
        } catch (Exception e) {
            log.warn("Failed to fetch user info for userId {}: {}", order.getUserId(), e.getMessage());
            // Fallback to showing User ID if Identity Service is unavailable
            response.setUsername("User #" + order.getUserId());
            response.setUserEmail(null);
        }
        
        // Add shipper information
        response.setShipperId(order.getShipperId());
        response.setAssignedAt(order.getAssignedAt());
        response.setPickedUpAt(order.getPickedUpAt());
        response.setDeliveredAt(order.getDeliveredAt());
        response.setDeliveryNotes(order.getDeliveryNotes());
        
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
    
    /**
     * Assign shipper to order
     */
    @Transactional
    public OrderResponse assignShipperToOrder(Long orderId, AssignShipperRequest request) {
        log.info("Assigning shipper {} to order {}", request.getShipperId(), orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        // Check if order can be assigned to shipper
        if (!order.canBeAssignedToShipper()) {
            throw new IllegalArgumentException(
                "Order cannot be assigned to shipper. Current status: " + order.getStatus() + 
                ", Already assigned: " + order.isAssignedToShipper());
        }
        
        // Assign shipper
        order.setShipperId(request.getShipperId());
        order.setAssignedAt(LocalDateTime.now());
        if (request.getNotes() != null && !request.getNotes().trim().isEmpty()) {
            order.setDeliveryNotes(request.getNotes());
        }
        
        Order updatedOrder = orderRepository.save(order);
        log.info("Shipper assigned successfully to order {}", orderId);
        
        return convertToOrderResponse(updatedOrder);
    }
    
    /**
     * Unassign shipper from order
     */
    @Transactional
    public OrderResponse unassignShipperFromOrder(Long orderId) {
        log.info("Unassigning shipper from order {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        if (order.getShipperId() == null) {
            throw new IllegalArgumentException("Order is not assigned to any shipper");
        }
        
        // Only allow unassignment if order is not yet delivered
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Cannot unassign shipper from delivered order");
        }
        
        order.setShipperId(null);
        order.setAssignedAt(null);
        order.setPickedUpAt(null);
        order.setDeliveryNotes(null);
        
        Order updatedOrder = orderRepository.save(order);
        log.info("Shipper unassigned successfully from order {}", orderId);
        
        return convertToOrderResponse(updatedOrder);
    }
    
    /**
     * Mark order as picked up by shipper
     */
    @Transactional
    public OrderResponse markOrderAsPickedUp(Long orderId) {
        log.info("Marking order {} as picked up", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        if (order.getShipperId() == null) {
            throw new IllegalArgumentException("Order is not assigned to any shipper");
        }
        
        if (order.getStatus() != OrderStatus.READY_FOR_PICKUP) {
            throw new IllegalArgumentException("Order must be in READY_FOR_PICKUP status");
        }
        
        order.setPickedUpAt(LocalDateTime.now());
        
        Order updatedOrder = orderRepository.save(order);
        log.info("Order {} marked as picked up", orderId);
        
        return convertToOrderResponse(updatedOrder);
    }
    
    /**
     * Get orders assigned to a specific shipper
     */
    public Page<OrderResponse> getOrdersByShipper(Long shipperId, Pageable pageable, OrderStatus status) {
        log.info("Getting orders for shipper {} - status: {}", shipperId, status);
        
        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByShipperIdAndStatusOrderByCreatedAtDesc(shipperId, status, pageable);
        } else {
            orders = orderRepository.findByShipperIdOrderByCreatedAtDesc(shipperId, pageable);
        }
        
        return orders.map(this::convertToOrderResponse);
    }
    
    /**
     * Get orders ready for assignment (CONFIRMED, PREPARING, READY_FOR_PICKUP without shipper)
     */
    public Page<OrderResponse> getOrdersReadyForAssignment(Pageable pageable) {
        log.info("Getting orders ready for shipper assignment");
        
        Page<Order> orders = orderRepository.findOrdersReadyForAssignment(pageable);
        return orders.map(this::convertToOrderResponse);
    }
    
    /**
     * Get order by ID for shipper (verify ownership)
     */
    public OrderResponse getOrderByIdForShipper(Long orderId, Long shipperId) {
        log.info("Getting order {} for shipper {}", orderId, shipperId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        if (!shipperId.equals(order.getShipperId())) {
            throw new IllegalArgumentException("Order is not assigned to this shipper");
        }
        
        return convertToOrderResponse(order);
    }
    
    /**
     * Mark order as picked up by shipper
     */
    @Transactional
    public OrderResponse markOrderAsPickedUpByShipper(Long orderId, Long shipperId) {
        log.info("Shipper {} marking order {} as picked up", shipperId, orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        // Verify shipper
        if (!shipperId.equals(order.getShipperId())) {
            throw new IllegalArgumentException("Order is not assigned to this shipper");
        }
        
        // Verify status
        if (order.getStatus() != OrderStatus.READY_FOR_PICKUP) {
            throw new IllegalArgumentException("Order must be in READY_FOR_PICKUP status");
        }
        
        order.setPickedUpAt(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);
        
        log.info("Order {} marked as picked up", orderId);
        return convertToOrderResponse(updatedOrder);
    }
    
    /**
     * Mark order as delivered by shipper
     */
    @Transactional
    public OrderResponse markOrderAsDeliveredByShipper(Long orderId, Long shipperId, String notes) {
        log.info("Shipper {} marking order {} as delivered", shipperId, orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        // Verify shipper
        if (!shipperId.equals(order.getShipperId())) {
            throw new IllegalArgumentException("Order is not assigned to this shipper");
        }
        
        // Verify status
        if (order.getStatus() != OrderStatus.READY_FOR_PICKUP) {
            throw new IllegalArgumentException("Order must be in READY_FOR_PICKUP status");
        }
        
        // Update order
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        if (notes != null && !notes.trim().isEmpty()) {
            order.setDeliveryNotes(notes);
        }
        
        Order updatedOrder = orderRepository.save(order);
        
        // Update shipper statistics
        try {
            com.example.foodshop.order.entity.Shipper shipper = 
                shipperRepository.findById(shipperId).orElse(null);
            if (shipper != null) {
                shipper.incrementSuccessfulDeliveries();
                shipper.setStatus(com.example.foodshop.order.entity.ShipperStatus.AVAILABLE);
                shipperRepository.save(shipper);
            }
        } catch (Exception e) {
            log.warn("Failed to update shipper statistics: {}", e.getMessage());
        }
        
        log.info("Order {} marked as delivered", orderId);
        return convertToOrderResponse(updatedOrder);
    }
    
    /**
     * Add delivery notes by shipper
     */
    @Transactional
    public OrderResponse addDeliveryNotesByShipper(Long orderId, Long shipperId, String notes) {
        log.info("Shipper {} adding notes to order {}", shipperId, orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        // Verify shipper
        if (!shipperId.equals(order.getShipperId())) {
            throw new IllegalArgumentException("Order is not assigned to this shipper");
        }
        
        order.setDeliveryNotes(notes);
        Order updatedOrder = orderRepository.save(order);
        
        log.info("Delivery notes added to order {}", orderId);
        return convertToOrderResponse(updatedOrder);
    }
}
