package com.example.foodshop.controller;

import com.example.foodshop.entity.Order;
import com.example.foodshop.entity.Product;
import com.example.foodshop.entity.User;
import com.example.foodshop.repository.OrderRepository;
import com.example.foodshop.repository.UserRepository;
import com.example.foodshop.service.FileUploadService;
import com.example.foodshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;
    
    @GetMapping
    public String adminDashboard(Model model) {
        List<Product> products = productService.getAllProducts();
        List<Order> orders = orderRepository.findAll();
        List<User> users = userRepository.findAll();
        
        long totalProducts = products.size();
        long totalOrders = orders.size();
        long totalCustomers = users.stream().filter(u -> u.getRole() == User.Role.CUSTOMER).count();
        double totalRevenue = orders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED)
            .mapToDouble(Order::getTotalAmount)
            .sum();
        
        long pendingOrders = orders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.PENDING)
            .count();
        
        long lowStockProducts = products.stream()
            .filter(p -> p.getStock() < 10)
            .count();
        
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("recentOrders", orders.stream().limit(5).toList());
        model.addAttribute("lowStockProductsList", products.stream().filter(p -> p.getStock() < 10).toList());
        
        return "admin/dashboard";
    }
    
    @GetMapping("/products")
    public String manageProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "admin/products";
    }
    
    @GetMapping("/products/add")
    public String addProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "admin/product-form";
    }
    
    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "admin/product-form";
    }
    
    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product, 
                            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        
        if (imageFile != null && !imageFile.isEmpty()) {
            String filename = fileUploadService.uploadFile(imageFile);
            if (filename != null) {
                if (product.getId() != null) {
                    Product oldProduct = productService.getProductById(product.getId());
                    if (oldProduct != null && oldProduct.getImage() != null) {
                        fileUploadService.deleteFile(oldProduct.getImage());
                    }
                }
                product.setImage(filename);
            }
        }
        
        productService.saveProduct(product);
        return "redirect:/admin/products";
    }
    
    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product != null && product.getImage() != null) {
            fileUploadService.deleteFile(product.getImage());
        }
        productService.deleteProduct(id);
        return "redirect:/admin/products";
    }
    
    @GetMapping("/orders")
    public String manageOrders(Model model) {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
        
        // Calculate stats manually
        long pendingCount = 0;
        long shippingCount = 0;
        long deliveredCount = 0;
        
        for (Order order : orders) {
            if (order.getStatus() == Order.OrderStatus.PENDING) pendingCount++;
            if (order.getStatus() == Order.OrderStatus.SHIPPING) shippingCount++;
            if (order.getStatus() == Order.OrderStatus.DELIVERED) deliveredCount++;
        }
        
        model.addAttribute("orders", orders);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("shippingCount", shippingCount);
        model.addAttribute("deliveredCount", deliveredCount);
        
        return "admin/orders";
    }
    
    @GetMapping("/orders/update/{id}")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order != null) {
            try {
                order.setStatus(Order.OrderStatus.valueOf(status));
                orderRepository.save(order);
            } catch (IllegalArgumentException e) {
                // Invalid status
            }
        }
        return "redirect:/admin/orders";
    }
    
    @GetMapping("/orders/detail/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return "redirect:/admin/orders";
        }
        model.addAttribute("order", order);
        return "admin/order-detail";
    }
    
    @GetMapping("/users")
    public String manageUsers(Model model) {
        List<User> users = userRepository.findAll();
        
        // Calculate stats manually
        long adminCount = 0;
        long customerCount = 0;
        
        for (User user : users) {
            if (user.getRole() == User.Role.ADMIN) adminCount++;
            if (user.getRole() == User.Role.CUSTOMER) customerCount++;
        }
        
        model.addAttribute("users", users);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("customerCount", customerCount);
        
        return "admin/users";
    }
    
    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null && user.getRole() != User.Role.ADMIN) {
            userRepository.deleteById(id);
        }
        return "redirect:/admin/users";
    }
}
