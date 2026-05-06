package com.example.foodshop.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class ProductValidationService {
    
    private static final Logger log = LoggerFactory.getLogger(ProductValidationService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${app.product-service.url:http://localhost:8082}")
    private String productServiceUrl;
    
    public boolean isProductAvailable(Long productId, Integer requestedQuantity) {
        try {
            ProductDetails product = getProductDetails(productId);
            if (product == null) {
                log.warn("Product not found: {}", productId);
                return false;
            }
            
            boolean available = product.getStock() >= requestedQuantity;
            log.debug("Product {} availability check: requested={}, available={}, result={}", 
                     productId, requestedQuantity, product.getStock(), available);
            
            return available;
        } catch (Exception e) {
            log.error("Error checking product availability for {}: {}", productId, e.getMessage());
            return false;
        }
    }
    
    public ProductDetails getProductDetails(Long productId) {
        try {
            String url = productServiceUrl + "/api/products/" + productId;
            ProductDetails product = restTemplate.getForObject(url, ProductDetails.class);
            
            if (product != null) {
                log.debug("Retrieved product details for {}: {}", productId, product.getName());
            } else {
                log.warn("Product not found: {}", productId);
            }
            
            return product;
        } catch (Exception e) {
            log.error("Error fetching product details for {}: {}", productId, e.getMessage());
            throw new RuntimeException("Unable to validate product: " + productId, e);
        }
    }
    
    public boolean validateProductExists(Long productId) {
        try {
            ProductDetails product = getProductDetails(productId);
            return product != null;
        } catch (Exception e) {
            log.error("Error validating product existence for {}: {}", productId, e.getMessage());
            return false;
        }
    }
    
    // DTO for product details from Product Service
    public static class ProductDetails {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private String image;
        private Integer stock;
        private String category;
        
        // Constructors
        public ProductDetails() {}
        
        public ProductDetails(Long id, String name, String description, BigDecimal price, 
                             String image, Integer stock, String category) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = price;
            this.image = image;
            this.stock = stock;
            this.category = category;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }
        
        public Integer getStock() { return stock; }
        public void setStock(Integer stock) { this.stock = stock; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        @Override
        public String toString() {
            return "ProductDetails{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", price=" + price +
                    ", stock=" + stock +
                    '}';
        }
    }
}