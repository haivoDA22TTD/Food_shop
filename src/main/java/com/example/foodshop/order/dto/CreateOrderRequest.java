package com.example.foodshop.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    // Optional for guest checkout.
    private Long userId;

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @NotEmpty(message = "Order items are required")
    private List<@Valid CreateOrderItemRequest> items;

    // Kept for payload compatibility with monolithic API.
    private String voucherCode;
}
