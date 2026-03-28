package com.example.foodshop.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {
    
    private final Path uploadPath = Paths.get("img");
    
    public FileUploadService() {
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }
    
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            return null;
        }
        
        try {
            // Get original filename and extension
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            
            // Generate unique filename
            String filename = UUID.randomUUID().toString() + extension;
            
            // Save file
            Path targetLocation = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
    
    public void deleteFile(String filename) {
        try {
            Path filePath = uploadPath.resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but don't throw exception
            System.err.println("Failed to delete file: " + filename);
        }
    }
}
