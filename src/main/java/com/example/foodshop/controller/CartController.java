package com.example.foodshop.controller;

import com.example.foodshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    
    private final ProductService productService;
    
    @GetMapping
    public String viewCart(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "cart";
    }
}
