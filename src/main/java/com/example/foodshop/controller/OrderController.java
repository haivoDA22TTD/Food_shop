package com.example.foodshop.controller;

import com.example.foodshop.entity.Order;
import com.example.foodshop.entity.OrderItem;
import com.example.foodshop.entity.Product;
import com.example.foodshop.entity.User;
import com.example.foodshop.repository.OrderRepository;
import com.example.foodshop.repository.UserRepository;
import com.example.foodshop.service.ProductService;
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
        
        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);
        
        orderRepository.save(order);
        
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
}
