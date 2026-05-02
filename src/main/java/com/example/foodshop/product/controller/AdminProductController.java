package com.example.foodshop.product.controller;

import com.example.foodshop.product.entity.Product;
import com.example.foodshop.product.service.FileUploadService;
import com.example.foodshop.product.service.ProductService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {
    private final ProductService productService;
    private final FileUploadService fileUploadService;

    public AdminProductController(ProductService productService, FileUploadService fileUploadService) {
        this.productService = productService;
        this.fileUploadService = fileUploadService;
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> createProduct(@RequestParam @NotBlank String name,
                                           @RequestParam(required = false) String description,
                                           @RequestParam @NotNull @Min(0) Double price,
                                           @RequestParam @NotNull @Min(0) Integer stock,
                                           @RequestParam(required = false) String category,
                                           @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        try {
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setStock(stock);
            product.setCategory(category);
            if (imageFile != null && !imageFile.isEmpty()) {
                product.setImage(fileUploadService.uploadFile(imageFile));
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(productService.saveProduct(product));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Cannot create product"));
        }
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateProduct(@PathVariable Long id,
                                           @RequestParam @NotBlank String name,
                                           @RequestParam(required = false) String description,
                                           @RequestParam @NotNull @Min(0) Double price,
                                           @RequestParam @NotNull @Min(0) Integer stock,
                                           @RequestParam(required = false) String category,
                                           @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        Product existing = productService.getProductById(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Product not found"));
        }

        try {
            String oldImage = existing.getImage();
            existing.setName(name);
            existing.setDescription(description);
            existing.setPrice(price);
            existing.setStock(stock);
            existing.setCategory(category);

            if (imageFile != null && !imageFile.isEmpty()) {
                String newImageUrl = fileUploadService.uploadFile(imageFile);
                existing.setImage(newImageUrl);
                if (oldImage != null && !oldImage.isBlank()) {
                    fileUploadService.deleteFile(oldImage);
                }
            }

            return ResponseEntity.ok(productService.saveProduct(existing));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Cannot update product"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        Product existing = productService.getProductById(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Product not found"));
        }

        try {
            if (existing.getImage() != null && !existing.getImage().isBlank()) {
                fileUploadService.deleteFile(existing.getImage());
            }
            productService.deleteProduct(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Product deleted"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Cannot delete product"));
        }
    }
}
