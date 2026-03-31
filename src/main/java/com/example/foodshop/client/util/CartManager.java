package com.example.foodshop.client.util;

import com.example.foodshop.client.api.ApiClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartManager {
    private static CartManager instance;
    private Map<Long, CartItem> cartItems;
    private List<CartChangeListener> listeners;
    
    private CartManager() {
        this.cartItems = new HashMap<>();
        this.listeners = new ArrayList<>();
    }
    
    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }
    
    public void addToCart(ApiClient.ProductDTO product, int quantity) {
        Long productId = product.getId();
        if (cartItems.containsKey(productId)) {
            CartItem item = cartItems.get(productId);
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            cartItems.put(productId, new CartItem(product, quantity));
        }
        notifyListeners();
    }
    
    public void updateQuantity(Long productId, int quantity) {
        if (quantity <= 0) {
            removeFromCart(productId);
        } else if (cartItems.containsKey(productId)) {
            cartItems.get(productId).setQuantity(quantity);
            notifyListeners();
        }
    }
    
    public void removeFromCart(Long productId) {
        cartItems.remove(productId);
        notifyListeners();
    }
    
    public void clearCart() {
        cartItems.clear();
        notifyListeners();
    }
    
    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems.values());
    }
    
    public int getTotalItems() {
        return cartItems.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
    
    public double getSubtotal() {
        return cartItems.values().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }
    
    public double getTotal() {
        double subtotal = getSubtotal();
        double shipping = subtotal > 0 ? 30000 : 0;
        return subtotal + shipping;
    }
    
    public void addListener(CartChangeListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(CartChangeListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyListeners() {
        for (CartChangeListener listener : listeners) {
            listener.onCartChanged();
        }
    }
    
    public interface CartChangeListener {
        void onCartChanged();
    }
    
    public static class CartItem {
        private ApiClient.ProductDTO product;
        private int quantity;
        
        public CartItem(ApiClient.ProductDTO product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
        
        public ApiClient.ProductDTO getProduct() { return product; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
}
