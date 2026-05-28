package com.example.foodshop.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateOrderRequest {
    
    @NotBlank(message = "Shipping address is required")
    @Size(min = 10, max = 500, message = "Shipping address must be between 10 and 500 characters")
    private String shippingAddress;
    
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9+\\-\\s()]{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;
    
    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
    
    private String paymentMethod = "COD"; // Default to COD
    
    /**
     * List of product IDs to checkout
     * If null or empty, all items in cart will be checked out (backward compatible)
     */
    private List<Long> selectedProductIds;
    
    // Constructors
    public CreateOrderRequest() {
    }
    
    public CreateOrderRequest(String shippingAddress, String phoneNumber, String notes, String paymentMethod) {
        this.shippingAddress = shippingAddress;
        this.phoneNumber = phoneNumber;
        this.notes = notes;
        this.paymentMethod = paymentMethod;
    }
    
    public CreateOrderRequest(String shippingAddress, String phoneNumber, String notes, 
                            String paymentMethod, List<Long> selectedProductIds) {
        this.shippingAddress = shippingAddress;
        this.phoneNumber = phoneNumber;
        this.notes = notes;
        this.paymentMethod = paymentMethod;
        this.selectedProductIds = selectedProductIds;
    }
    
    // Getters and Setters
    public String getShippingAddress() {
        return shippingAddress;
    }
    
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public List<Long> getSelectedProductIds() {
        return selectedProductIds;
    }
    
    public void setSelectedProductIds(List<Long> selectedProductIds) {
        this.selectedProductIds = selectedProductIds;
    }
}