package com.example.foodshop.order.service;

import com.example.foodshop.order.dto.CartItemRequest;
import com.example.foodshop.order.dto.CartItemResponse;
import com.example.foodshop.order.dto.CartResponse;
import com.example.foodshop.order.dto.CartSummaryResponse;
import com.example.foodshop.order.entity.Cart;
import com.example.foodshop.order.entity.CartItem;
import com.example.foodshop.order.repository.CartItemRepository;
import com.example.foodshop.order.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {
    
    private static final Logger log = LoggerFactory.getLogger(CartService.class);
    private static final String CART_CACHE_PREFIX = "cart:user:";
    
    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private ProductValidationService productValidationService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Value("${app.cart.redis-ttl:86400}")
    private long cartCacheTtl;
    
    @Value("${app.cart.max-items:50}")
    private int maxCartItems;
    
    public CartResponse getOrCreateCart(Long userId) {
        try {
            // Try to get from Redis cache first
            Cart cachedCart = getCachedCart(userId);
            if (cachedCart != null) {
                log.debug("Cart found in cache for user: {}", userId);
                return convertToCartResponse(cachedCart);
            }
            
            // Fallback to database
            Optional<Cart> cartOpt = cartRepository.findByUserIdWithItems(userId);
            Cart cart;
            
            if (cartOpt.isPresent()) {
                cart = cartOpt.get();
                log.debug("Cart found in database for user: {}", userId);
            } else {
                // Create new cart
                cart = new Cart();
                cart.setUserId(userId);
                cart = cartRepository.save(cart);
                log.info("Created new cart for user: {}", userId);
            }
            
            // Enrich cart items with product details
            enrichCartItemsWithProductDetails(cart);
            
            // Cache the cart
            cacheCart(cart);
            
            return convertToCartResponse(cart);
            
        } catch (Exception e) {
            log.error("Error getting cart for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Unable to retrieve cart", e);
        }
    }
    
    public CartResponse addToCart(Long userId, CartItemRequest request) {
        try {
            // Validate product
            if (!productValidationService.isProductAvailable(request.getProductId(), request.getQuantity())) {
                throw new IllegalArgumentException("Product is not available or insufficient stock");
            }
            
            Cart cart = getOrCreateCartEntity(userId);
            
            // Check cart item limit
            if (cart.getCartItems().size() >= maxCartItems) {
                throw new IllegalArgumentException("Cart is full. Maximum " + maxCartItems + " items allowed");
            }
            
            // Check if item already exists in cart
            CartItem existingItem = cart.findCartItemByProductId(request.getProductId());
            
            if (existingItem != null) {
                // Update quantity
                int newQuantity = existingItem.getQuantity() + request.getQuantity();
                
                // Validate new quantity
                if (!productValidationService.isProductAvailable(request.getProductId(), newQuantity)) {
                    throw new IllegalArgumentException("Insufficient stock for requested quantity");
                }
                
                existingItem.updateQuantity(newQuantity);
                cartItemRepository.save(existingItem);
                log.info("Updated cart item quantity for user {} product {}: {}", userId, request.getProductId(), newQuantity);
            } else {
                // Add new item
                CartItem newItem = new CartItem(request.getProductId(), request.getQuantity());
                cart.addCartItem(newItem);
                cartItemRepository.save(newItem);
                log.info("Added new item to cart for user {} product {}: {}", userId, request.getProductId(), request.getQuantity());
            }
            
            cart = cartRepository.save(cart);
            
            // Enrich with product details
            enrichCartItemsWithProductDetails(cart);
            
            // Update cache
            cacheCart(cart);
            
            return convertToCartResponse(cart);
            
        } catch (Exception e) {
            log.error("Error adding item to cart for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
    
    public CartResponse updateCartItem(Long userId, Long productId, CartItemRequest request) {
        try {
            Cart cart = getOrCreateCartEntity(userId);
            CartItem cartItem = cart.findCartItemByProductId(productId);
            
            if (cartItem == null) {
                throw new IllegalArgumentException("Item not found in cart");
            }
            
            if (request.getQuantity() <= 0) {
                // Remove item if quantity is 0 or negative
                return removeFromCart(userId, productId);
            }
            
            // Validate new quantity
            if (!productValidationService.isProductAvailable(productId, request.getQuantity())) {
                throw new IllegalArgumentException("Insufficient stock for requested quantity");
            }
            
            cartItem.updateQuantity(request.getQuantity());
            cartItemRepository.save(cartItem);
            
            cart = cartRepository.save(cart);
            
            // Enrich with product details
            enrichCartItemsWithProductDetails(cart);
            
            // Update cache
            cacheCart(cart);
            
            log.info("Updated cart item for user {} product {}: {}", userId, productId, request.getQuantity());
            
            return convertToCartResponse(cart);
            
        } catch (Exception e) {
            log.error("Error updating cart item for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
    
    public CartResponse removeFromCart(Long userId, Long productId) {
        try {
            Cart cart = getOrCreateCartEntity(userId);
            CartItem cartItem = cart.findCartItemByProductId(productId);
            
            if (cartItem == null) {
                throw new IllegalArgumentException("Item not found in cart");
            }
            
            cart.removeCartItem(cartItem);
            cartItemRepository.delete(cartItem);
            
            cart = cartRepository.save(cart);
            
            // Enrich with product details
            enrichCartItemsWithProductDetails(cart);
            
            // Update cache
            cacheCart(cart);
            
            log.info("Removed item from cart for user {} product {}", userId, productId);
            
            return convertToCartResponse(cart);
            
        } catch (Exception e) {
            log.error("Error removing item from cart for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }
    
    public void clearCart(Long userId) {
        try {
            Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
            if (cartOpt.isPresent()) {
                Cart cart = cartOpt.get();
                cart.clearItems();
                cartRepository.save(cart);
                
                // Clear cache
                clearCachedCart(userId);
                
                log.info("Cleared cart for user: {}", userId);
            }
        } catch (Exception e) {
            log.error("Error clearing cart for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Unable to clear cart", e);
        }
    }
    
    public CartSummaryResponse getCartSummary(Long userId) {
        try {
            CartResponse cart = getOrCreateCart(userId);
            return new CartSummaryResponse(cart.getTotalItems(), cart.getTotalAmount());
        } catch (Exception e) {
            log.error("Error getting cart summary for user {}: {}", userId, e.getMessage(), e);
            return new CartSummaryResponse(0, BigDecimal.ZERO);
        }
    }
    
    // Helper methods
    
    private Cart getOrCreateCartEntity(Long userId) {
        Optional<Cart> cartOpt = cartRepository.findByUserIdWithItems(userId);
        if (cartOpt.isPresent()) {
            return cartOpt.get();
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            return cartRepository.save(cart);
        }
    }
    
    private void enrichCartItemsWithProductDetails(Cart cart) {
        for (CartItem item : cart.getCartItems()) {
            try {
                var productDetails = productValidationService.getProductDetails(item.getProductId());
                if (productDetails != null) {
                    item.setProductName(productDetails.getName());
                    item.setProductPrice(productDetails.getPrice());
                    item.setProductImage(productDetails.getImage());
                    item.setAvailableStock(productDetails.getStock());
                }
            } catch (Exception e) {
                log.warn("Failed to enrich cart item {} with product details: {}", item.getProductId(), e.getMessage());
                // Set default values if product service is unavailable
                item.setProductName("Product #" + item.getProductId());
                item.setProductPrice(BigDecimal.ZERO);
                item.setAvailableStock(0);
            }
        }
    }
    
    private CartResponse convertToCartResponse(Cart cart) {
        List<CartItemResponse> cartItemResponses = cart.getCartItems().stream()
                .map(this::convertToCartItemResponse)
                .collect(Collectors.toList());
        
        return new CartResponse(cart.getId(), cart.getUserId(), cartItemResponses, cart.getUpdatedAt());
    }
    
    private CartItemResponse convertToCartItemResponse(CartItem item) {
        return new CartItemResponse(
                item.getId(),
                item.getProductId(),
                item.getProductName(),
                item.getProductPrice(),
                item.getProductImage(),
                item.getQuantity(),
                item.getAvailableStock(),
                item.getAddedAt()
        );
    }
    
    // Redis caching methods
    
    private String getCacheKey(Long userId) {
        return CART_CACHE_PREFIX + userId;
    }
    
    private void cacheCart(Cart cart) {
        try {
            String key = getCacheKey(cart.getUserId());
            redisTemplate.opsForValue().set(key, cart, Duration.ofSeconds(cartCacheTtl));
            log.debug("Cached cart for user: {}", cart.getUserId());
        } catch (Exception e) {
            log.warn("Failed to cache cart for user {}: {}", cart.getUserId(), e.getMessage());
        }
    }
    
    private Cart getCachedCart(Long userId) {
        try {
            String key = getCacheKey(userId);
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof Cart) {
                return (Cart) cached;
            }
        } catch (Exception e) {
            log.warn("Failed to get cached cart for user {}: {}", userId, e.getMessage());
        }
        return null;
    }
    
    private void clearCachedCart(Long userId) {
        try {
            String key = getCacheKey(userId);
            redisTemplate.delete(key);
            log.debug("Cleared cached cart for user: {}", userId);
        } catch (Exception e) {
            log.warn("Failed to clear cached cart for user {}: {}", userId, e.getMessage());
        }
    }
}