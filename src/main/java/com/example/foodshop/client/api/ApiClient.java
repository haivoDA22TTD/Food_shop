package com.example.foodshop.client.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    
    // Production backend URL
    private static final String BASE_URL = "https://food-shop-iswi.onrender.com";
    
    // For local development, uncomment this:
    // private static final String BASE_URL = "http://localhost:8080";
    
    private static ApiClient instance;
    private final OkHttpClient client;
    private final Gson gson;
    private String jwtToken;
    
    private ApiClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .create();
    }
    
    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }
    
    public void setJwtToken(String token) {
        this.jwtToken = token;
    }
    
    public String getJwtToken() {
        return jwtToken;
    }
    
    public boolean isLoggedIn() {
        return jwtToken != null && !jwtToken.isEmpty();
    }
    
    // Login
    public LoginResponse login(String username, String password) throws IOException {
        LoginRequest loginRequest = new LoginRequest(username, password);
        String json = gson.toJson(loginRequest);
        
        RequestBody body = RequestBody.create(
            json,
            MediaType.parse("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/auth/login")
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Đăng nhập thất bại: " + response.code());
            }
            
            String responseBody = response.body().string();
            return gson.fromJson(responseBody, LoginResponse.class);
        }
    }
    
    // Register
    public String register(String username, String password, String email, String fullName) throws IOException {
        RegisterRequest registerRequest = new RegisterRequest(username, password, email, fullName);
        String json = gson.toJson(registerRequest);
        
        RequestBody body = RequestBody.create(
            json,
            MediaType.parse("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/auth/register")
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                throw new IOException("Đăng ký thất bại: " + errorBody);
            }
            
            return response.body().string();
        }
    }
    
    // Get all products
    public List<ProductDTO> getAllProducts() throws IOException {
        Request.Builder requestBuilder = new Request.Builder()
                .url(BASE_URL + "/api/products")
                .get();
        
        if (isLoggedIn()) {
            requestBuilder.addHeader("Authorization", "Bearer " + jwtToken);
        }
        
        Request request = requestBuilder.build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Không thể tải danh sách sản phẩm: " + response.code());
            }
            
            String responseBody = response.body().string();
            return gson.fromJson(responseBody, new TypeToken<List<ProductDTO>>(){}.getType());
        }
    }
    
    // Get my orders
    public List<OrderDTO> getMyOrders() throws IOException {
        if (!isLoggedIn()) {
            throw new IOException("Vui lòng đăng nhập!");
        }
        
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/orders/my-orders")
                .addHeader("Authorization", "Bearer " + jwtToken)
                .get()
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Không thể tải đơn hàng: " + response.code());
            }
            
            String responseBody = response.body().string();
            return gson.fromJson(responseBody, new TypeToken<List<OrderDTO>>(){}.getType());
        }
    }
    
    // Create order
    public OrderDTO createOrder(OrderRequest orderRequest) throws IOException {
        if (!isLoggedIn()) {
            throw new IOException("Vui lòng đăng nhập!");
        }
        
        String json = gson.toJson(orderRequest);
        RequestBody body = RequestBody.create(
            json,
            MediaType.parse("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/orders/create")
                .addHeader("Authorization", "Bearer " + jwtToken)
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Không thể tạo đơn hàng: " + response.code());
            }
            
            String responseBody = response.body().string();
            return gson.fromJson(responseBody, OrderDTO.class);
        }
    }
    
    // Get product reviews
    public List<ReviewDTO> getProductReviews(Long productId) throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/reviews/product/" + productId)
                .get()
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Không thể tải đánh giá: " + response.code());
            }
            
            String responseBody = response.body().string();
            return gson.fromJson(responseBody, new TypeToken<List<ReviewDTO>>(){}.getType());
        }
    }
    
    // Create review
    public String createReview(ReviewRequest reviewRequest) throws IOException {
        if (!isLoggedIn()) {
            throw new IOException("Vui lòng đăng nhập!");
        }
        
        String json = gson.toJson(reviewRequest);
        RequestBody body = RequestBody.create(
            json,
            MediaType.parse("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/reviews/create")
                .addHeader("Authorization", "Bearer " + jwtToken)
                .post(body)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                throw new IOException("Không thể gửi đánh giá: " + errorBody);
            }
            
            return response.body().string();
        }
    }
    
    // DTOs
    public static class LoginRequest {
        private String username;
        private String password;
        
        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
    
    public static class RegisterRequest {
        private String username;
        private String password;
        private String email;
        private String fullName;
        
        public RegisterRequest(String username, String password, String email, String fullName) {
            this.username = username;
            this.password = password;
            this.email = email;
            this.fullName = fullName;
        }
    }
    
    public static class LoginResponse {
        private String token;
        private String username;
        private String role;
        
        public String getToken() { return token; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
    }
    
    public static class ProductDTO {
        private Long id;
        private String name;
        private String description;
        private Double price;
        private String category;
        private Integer stock;
        private String image;
        
        // Getters
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Double getPrice() { return price; }
        public String getCategory() { return category; }
        public Integer getStock() { return stock; }
        public String getImage() { return image; }
    }
    
    public static class OrderRequest {
        private String shippingAddress;
        private String paymentMethod;
        private List<OrderItemRequest> items;
        
        public OrderRequest(String shippingAddress, String paymentMethod, List<OrderItemRequest> items) {
            this.shippingAddress = shippingAddress;
            this.paymentMethod = paymentMethod;
            this.items = items;
        }
    }
    
    public static class OrderItemRequest {
        private Long id;  // Backend expects "id" not "productId"
        private Integer quantity;
        
        public OrderItemRequest(Long productId, Integer quantity) {
            this.id = productId;
            this.quantity = quantity;
        }
        
        public Long getId() { return id; }
        public Integer getQuantity() { return quantity; }
    }
    
    public static class OrderDTO {
        private Long id;
        private String shippingAddress;
        private String paymentMethod;
        private Double totalAmount;
        private String status;
        private String createdAt;
        private List<OrderItemDTO> orderItems;
        
        public Long getId() { return id; }
        public String getShippingAddress() { return shippingAddress; }
        public String getPaymentMethod() { return paymentMethod; }
        public Double getTotalAmount() { return totalAmount; }
        public String getStatus() { return status; }
        public String getCreatedAt() { return createdAt; }
        public List<OrderItemDTO> getOrderItems() { return orderItems; }
    }
    
    public static class OrderItemDTO {
        private Long id;
        private Integer quantity;
        private Double price;
        private ProductDTO product;
        
        public Long getId() { return id; }
        public Integer getQuantity() { return quantity; }
        public Double getPrice() { return price; }
        public ProductDTO getProduct() { return product; }
    }
    
    public static class ReviewRequest {
        private Long productId;
        private Long orderId;
        private Integer rating;
        private String comment;
        
        public ReviewRequest(Long productId, Long orderId, Integer rating, String comment) {
            this.productId = productId;
            this.orderId = orderId;
            this.rating = rating;
            this.comment = comment;
        }
    }
    
    public static class ReviewDTO {
        private Long id;
        private Integer rating;
        private String comment;
        private String createdAt;
        private UserDTO user;
        
        public Long getId() { return id; }
        public Integer getRating() { return rating; }
        public String getComment() { return comment; }
        public String getCreatedAt() { return createdAt; }
        public UserDTO getUser() { return user; }
    }
    
    public static class UserDTO {
        private Long id;
        private String username;
        private String fullName;
        
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
    }
}
