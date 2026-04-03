package com.example.foodshop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {
    private String message;
    private String type; // text, product_list, voucher, order_status
    private Map<String, Object> data; // Additional data (products, voucher code, etc.)
    private List<String> suggestions; // Quick reply suggestions
}
