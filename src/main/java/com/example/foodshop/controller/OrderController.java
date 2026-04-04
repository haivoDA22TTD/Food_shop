package com.example.foodshop.controller;

import com.example.foodshop.entity.Order;
import com.example.foodshop.entity.OrderItem;
import com.example.foodshop.entity.Product;
import com.example.foodshop.entity.User;
import com.example.foodshop.repository.OrderRepository;
import com.example.foodshop.repository.UserRepository;
import com.example.foodshop.service.ProductService;
import com.example.foodshop.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final VoucherService voucherService;
    
    @GetMapping("/orders")
    public String viewOrders(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "orders";
    }
    
    @GetMapping("/checkout")
    public String checkout(Model model, Authentication authentication) {
        model.addAttribute("products", productService.getAllProducts());
        
        // Get user info if authenticated
        if (authentication != null) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                model.addAttribute("user", user);
            }
        }
        
        return "checkout";
    }
    
    @PostMapping("/api/orders/create")
    @ResponseBody
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> orderData, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }
        
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress((String) orderData.get("shippingAddress"));
        order.setPaymentMethod((String) orderData.get("paymentMethod"));
        order.setStatus(Order.OrderStatus.PENDING);
        
        List<Map<String, Object>> items = (List<Map<String, Object>>) orderData.get("items");
        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0;
        
        for (Map<String, Object> item : items) {
            Long productId = Long.valueOf(item.get("id").toString());
            Integer quantity = Integer.valueOf(item.get("quantity").toString());
            
            Product product = productService.getProductById(productId);
            if (product != null) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setQuantity(quantity);
                orderItem.setPrice(product.getPrice());
                
                orderItems.add(orderItem);
                totalAmount += product.getPrice() * quantity;
            }
        }
        
        // Add shipping fee
        totalAmount += 30000;
        
        // Store original amount
        order.setOriginalAmount(totalAmount);
        
        // Apply voucher if provided
        String voucherCode = (String) orderData.get("voucherCode");
        double discountAmount = 0;
        
        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            try {
                java.math.BigDecimal discount = voucherService.applyVoucher(
                    voucherCode, 
                    java.math.BigDecimal.valueOf(totalAmount), 
                    user
                );
                discountAmount = discount.doubleValue();
                totalAmount -= discountAmount;
                
                order.setVoucherCode(voucherCode);
                order.setDiscountAmount(discountAmount);
            } catch (Exception e) {
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Lỗi áp dụng voucher: " + e.getMessage()
                ));
            }
        }
        
        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);
        
        orderRepository.save(order);
        
        // Mark voucher as used if applied
        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            voucherService.markVoucherAsUsed(voucherCode, user, order, discountAmount);
        }
        
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/api/orders/my-orders")
    @ResponseBody
    public ResponseEntity<?> getMyOrders(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }
        
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        return ResponseEntity.ok(orders);
    }
    
    @PostMapping("/api/orders/{orderId}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelOrder(@PathVariable Long orderId, Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Vui lòng đăng nhập"));
            }
            
            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            
            if (user == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy người dùng"));
            }
            
            // Check if account is locked
            if (user.getAccountLocked() != null && user.getAccountLocked()) {
                return ResponseEntity.status(403).body(Map.of(
                    "error", "Tài khoản của bạn đã bị khóa",
                    "reason", user.getLockReason() != null ? user.getLockReason() : "Hủy đơn hàng quá nhiều lần"
                ));
            }
            
            Order order = orderRepository.findById(orderId).orElse(null);
            
            if (order == null) {
                return ResponseEntity.status(404).body(Map.of("error", "Không tìm thấy đơn hàng"));
            }
            
            // Check if order belongs to user
            if (!order.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Bạn không có quyền hủy đơn hàng này"));
            }
            
            // Check if order can be cancelled (only PENDING or CONFIRMED)
            if (order.getStatus() != Order.OrderStatus.PENDING && 
                order.getStatus() != Order.OrderStatus.CONFIRMED) {
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Không thể hủy đơn hàng đã giao hoặc đang giao"
                ));
            }
            
            // Update order status
            order.setStatus(Order.OrderStatus.CANCELLED);
            orderRepository.save(order);
            
            // Update user's cancelled orders count
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime lastCancelled = user.getLastCancelledAt();
            
            // Reset count if it's a new month
            if (lastCancelled == null || 
                lastCancelled.getMonth() != now.getMonth() || 
                lastCancelled.getYear() != now.getYear()) {
                user.setCancelledOrdersThisMonth(1);
            } else {
                user.setCancelledOrdersThisMonth(user.getCancelledOrdersThisMonth() + 1);
            }
            
            user.setLastCancelledAt(now);
            
            // Check if user should be locked (more than 3 cancellations this month)
            if (user.getCancelledOrdersThisMonth() > 3) {
                user.setAccountLocked(true);
                user.setLockReason("Hủy đơn hàng quá 3 lần trong tháng " + now.getMonth().getValue() + "/" + now.getYear());
                userRepository.save(user);
                
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đơn hàng đã được hủy",
                    "warning", "Tài khoản của bạn đã bị khóa do hủy đơn hàng quá 3 lần trong tháng. Vui lòng liên hệ admin để mở khóa."
                ));
            }
            
            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đơn hàng đã được hủy thành công",
                "cancelledCount", user.getCancelledOrdersThisMonth(),
                "warning", user.getCancelledOrdersThisMonth() >= 3 ? 
                    "Cảnh báo: Bạn đã hủy " + user.getCancelledOrdersThisMonth() + " đơn hàng trong tháng này. Hủy thêm sẽ bị khóa tài khoản." : null
            ));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }
}
