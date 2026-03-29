package com.example.foodshop.config;

import com.example.foodshop.entity.Product;
import com.example.foodshop.entity.User;
import com.example.foodshop.repository.ProductRepository;
import com.example.foodshop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${ADMIN_USERNAME:admin}")
    private String adminUsername;
    
    @Value("${ADMIN_PASSWORD:admin123}")
    private String adminPassword;
    
    @Value("${ADMIN_EMAIL:admin@foodshop.com}")
    private String adminEmail;
    
    @Value("${CUSTOMER_USERNAME:customer}")
    private String customerUsername;
    
    @Value("${CUSTOMER_PASSWORD:customer123}")
    private String customerPassword;
    
    @Override
    public void run(String... args) {
        // Tạo tài khoản admin nếu chưa tồn tại
        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setEmail(adminEmail);
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
            System.out.println("✓ Đã tạo tài khoản admin - Username: " + adminUsername);
            System.out.println("⚠️  BẢO MẬT: Vui lòng đổi mật khẩu admin sau khi đăng nhập lần đầu!");
        }
        
        // Tạo tài khoản khách hàng mẫu (chỉ cho development)
        if (userRepository.findByUsername(customerUsername).isEmpty()) {
            User customer = new User();
            customer.setUsername(customerUsername);
            customer.setPassword(passwordEncoder.encode(customerPassword));
            customer.setEmail("customer@example.com");
            customer.setPhone("0123456789");
            customer.setAddress("123 Đường ABC, TP.HCM");
            customer.setRole(User.Role.CUSTOMER);
            userRepository.save(customer);
            System.out.println("✓ Đã tạo tài khoản khách hàng demo - Username: " + customerUsername);
        }
        
        // Tạo sản phẩm mẫu nếu chưa có
        if (productRepository.count() == 0) {
            createSampleProducts();
            System.out.println("✓ Đã tạo " + productRepository.count() + " sản phẩm mẫu");
        }
    }
    
    private void createSampleProducts() {
        Product[] products = {
            createProduct("Bingsu Dâu", "Bingsu dâu tươi mát lạnh", 85000.0, "bingsu_dau.png", "Đồ ngọt", 50),
            createProduct("Chè Khúc Bạch", "Chè khúc bạch truyền thống", 35000.0, "che_khuc_bach.png", "Đồ ngọt", 30),
            createProduct("Donut Socola", "Donut phủ socola thơm ngon", 25000.0, "donus_socola.png", "Đồ ngọt", 40),
            createProduct("Donut Trắng", "Donut phủ kem trắng", 25000.0, "donus_trang.png", "Đồ ngọt", 40),
            createProduct("Gà Rán", "Gà rán giòn tan", 65000.0, "ga_ran.png", "Đồ ăn nhanh", 25),
            createProduct("Hamburger", "Hamburger bò phô mai", 55000.0, "hamberger.png", "Đồ ăn nhanh", 30),
            createProduct("Kem Khoai Môn", "Kem khoai môn béo ngậy", 30000.0, "kem_khoai_mon.png", "Đồ ngọt", 35),
            createProduct("Kem Socola Lốc Xoáy", "Kem socola lốc xoáy mát lạnh", 28000.0, "kem_socola_loc_xoay.png", "Đồ ngọt", 35),
            createProduct("Matcha Latte", "Matcha latte thơm béo", 45000.0, "matcha_latte.png", "Đồ uống", 50),
            createProduct("Sữa Tươi Trân Châu Đường Đen", "Sữa tươi trân châu đường đen", 38000.0, "sua_tuoi_tran_chau_duong_den.png", "Đồ uống", 45),
            createProduct("Tacos", "Tacos Mexico truyền thống", 48000.0, "tacos.png", "Đồ ăn nhanh", 30),
            createProduct("Trà Sữa Khoai Môn", "Trà sữa khoai môn thơm ngon", 35000.0, "tra_sua_khoai_mon.png", "Đồ uống", 50),
            createProduct("Trà Sữa Socola", "Trà sữa socola đậm đà", 35000.0, "tra_sua_socola.png", "Đồ uống", 50)
        };
        
        for (Product product : products) {
            productRepository.save(product);
        }
    }
    
    private Product createProduct(String name, String description, Double price, String image, String category, Integer stock) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setImage(image);
        product.setCategory(category);
        product.setStock(stock);
        return product;
    }
}
