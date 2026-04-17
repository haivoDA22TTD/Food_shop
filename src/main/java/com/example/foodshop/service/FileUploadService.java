<<<<<<< HEAD
package com.example.foodshop.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class FileUploadService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);
    
    @Autowired(required = false)
    private Cloudinary cloudinary;
    
    @Value("${cloudinary.enabled:false}")
    private boolean cloudinaryEnabled;
    
    @Value("${cloudinary.folder:food-shop}")
    private String cloudinaryFolder;
    
    // Allowed image MIME types
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    // Max file size: 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    /**
     * Upload file to Cloudinary
     * Returns the public URL of the uploaded image
     */
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new RuntimeException("Invalid file type. Only images are allowed (JPEG, PNG, GIF, WebP)");
        }
        
        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File size exceeds 5MB limit");
        }
        
        // Validate file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (!Set.of("jpg", "jpeg", "png", "gif", "webp").contains(extension)) {
                throw new RuntimeException("Invalid file extension");
            }
        }
        
        if (!cloudinaryEnabled || cloudinary == null) {
            // Fallback to local storage for development
            return uploadToLocal(file);
        }
        
        try {
            // Generate unique public_id (reuse originalFilename from above)
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
            String publicId = cloudinaryFolder + "/" + UUID.randomUUID().toString();
            
            // Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), 
                ObjectUtils.asMap(
                    "public_id", publicId,
                    "folder", cloudinaryFolder,
                    "resource_type", "image",
                    "overwrite", true
                )
            );
            
            // Return the secure URL
            String secureUrl = (String) uploadResult.get("secure_url");
            logger.info("✓ Uploaded to Cloudinary: {}", secureUrl);
            
            return secureUrl;
        } catch (IOException e) {
            logger.error("Failed to upload to Cloudinary: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to Cloudinary", e);
        }
    }
    
    /**
     * Delete file from Cloudinary
     * @param imageUrl The full Cloudinary URL or public_id
     */
    public void deleteFile(String imageUrl) {
        if (!cloudinaryEnabled || cloudinary == null || imageUrl == null || imageUrl.isEmpty()) {
            return;
        }
        
        try {
            // Extract public_id from URL
            String publicId = extractPublicId(imageUrl);
            
            if (publicId != null) {
                Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                logger.info("✓ Deleted from Cloudinary: {} - Result: {}", publicId, result.get("result"));
            }
        } catch (Exception e) {
            // Log error but don't throw exception
            logger.error("Failed to delete file from Cloudinary: {} - {}", imageUrl, e.getMessage());
        }
    }
    
    /**
     * Extract public_id from Cloudinary URL
     * Example: https://res.cloudinary.com/demo/image/upload/v1234567890/food-shop/abc-123.jpg
     * Returns: food-shop/abc-123
     */
    private String extractPublicId(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return null;
        }
        
        try {
            // Find the position after "/upload/"
            int uploadIndex = imageUrl.indexOf("/upload/");
            if (uploadIndex == -1) {
                return null;
            }
            
            // Get the part after "/upload/v1234567890/"
            String afterUpload = imageUrl.substring(uploadIndex + 8);
            
            // Remove version number (v1234567890/)
            int slashIndex = afterUpload.indexOf("/");
            if (slashIndex != -1) {
                afterUpload = afterUpload.substring(slashIndex + 1);
            }
            
            // Remove file extension
            int dotIndex = afterUpload.lastIndexOf(".");
            if (dotIndex != -1) {
                afterUpload = afterUpload.substring(0, dotIndex);
            }
            
            return afterUpload;
        } catch (Exception e) {
            logger.error("Failed to extract public_id from URL: {}", imageUrl);
            return null;
        }
    }
    
    /**
     * Fallback method for local storage (development only)
     */
    private String uploadToLocal(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
            String filename = UUID.randomUUID().toString() + extension;
            
            // For local development, just return the filename
            // The actual file saving is handled by the old logic if needed
            logger.warn("⚠️ Cloudinary disabled, using local storage: {}", filename);
            
            return filename;
        } catch (Exception e) {
            logger.error("Failed to store file locally", e);
            throw new RuntimeException("Failed to store file locally", e);
        }
    }
}
=======
package com.example.foodshop.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class FileUploadService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileUploadService.class);
    
    @Autowired(required = false)
    private Cloudinary cloudinary;
    
    @Value("${cloudinary.enabled:false}")
    private boolean cloudinaryEnabled;
    
    @Value("${cloudinary.folder:food-shop}")
    private String cloudinaryFolder;
    
    // Allowed image MIME types
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    // Max file size: 5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    /**
     * Upload file to Cloudinary
     * Returns the public URL of the uploaded image
     */
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new RuntimeException("Invalid file type. Only images are allowed (JPEG, PNG, GIF, WebP)");
        }
        
        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File size exceeds 5MB limit");
        }
        
        // Validate file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (!Set.of("jpg", "jpeg", "png", "gif", "webp").contains(extension)) {
                throw new RuntimeException("Invalid file extension");
            }
        }
        
        if (!cloudinaryEnabled || cloudinary == null) {
            // Fallback to local storage for development
            return uploadToLocal(file);
        }
        
        try {
            // Generate unique public_id (reuse originalFilename from above)
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
            String publicId = cloudinaryFolder + "/" + UUID.randomUUID().toString();
            
            // Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), 
                ObjectUtils.asMap(
                    "public_id", publicId,
                    "folder", cloudinaryFolder,
                    "resource_type", "image",
                    "overwrite", true
                )
            );
            
            // Return the secure URL
            String secureUrl = (String) uploadResult.get("secure_url");
            logger.info("✓ Uploaded to Cloudinary: {}", secureUrl);
            
            return secureUrl;
        } catch (IOException e) {
            logger.error("Failed to upload to Cloudinary: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to Cloudinary", e);
        }
    }
    
    /**
     * Delete file from Cloudinary
     * @param imageUrl The full Cloudinary URL or public_id
     */
    public void deleteFile(String imageUrl) {
        if (!cloudinaryEnabled || cloudinary == null || imageUrl == null || imageUrl.isEmpty()) {
            return;
        }
        
        try {
            // Extract public_id from URL
            String publicId = extractPublicId(imageUrl);
            
            if (publicId != null) {
                Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                logger.info("✓ Deleted from Cloudinary: {} - Result: {}", publicId, result.get("result"));
            }
        } catch (Exception e) {
            // Log error but don't throw exception
            logger.error("Failed to delete file from Cloudinary: {} - {}", imageUrl, e.getMessage());
        }
    }
    
    /**
     * Extract public_id from Cloudinary URL
     * Example: https://res.cloudinary.com/demo/image/upload/v1234567890/food-shop/abc-123.jpg
     * Returns: food-shop/abc-123
     */
    private String extractPublicId(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return null;
        }
        
        try {
            // Find the position after "/upload/"
            int uploadIndex = imageUrl.indexOf("/upload/");
            if (uploadIndex == -1) {
                return null;
            }
            
            // Get the part after "/upload/v1234567890/"
            String afterUpload = imageUrl.substring(uploadIndex + 8);
            
            // Remove version number (v1234567890/)
            int slashIndex = afterUpload.indexOf("/");
            if (slashIndex != -1) {
                afterUpload = afterUpload.substring(slashIndex + 1);
            }
            
            // Remove file extension
            int dotIndex = afterUpload.lastIndexOf(".");
            if (dotIndex != -1) {
                afterUpload = afterUpload.substring(0, dotIndex);
            }
            
            return afterUpload;
        } catch (Exception e) {
            logger.error("Failed to extract public_id from URL: {}", imageUrl);
            return null;
        }
    }
    
    /**
     * Fallback method for local storage (development only)
     */
    private String uploadToLocal(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
            String filename = UUID.randomUUID().toString() + extension;
            
            // For local development, just return the filename
            // The actual file saving is handled by the old logic if needed
            logger.warn("⚠️ Cloudinary disabled, using local storage: {}", filename);
            
            return filename;
        } catch (Exception e) {
            logger.error("Failed to store file locally", e);
            throw new RuntimeException("Failed to store file locally", e);
        }
    }
}
>>>>>>> d48504e (The OAuth2 login feature has been updated with Google, directly hiding the token URL)
