package com.example.foodshop.product.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class FileUploadService {
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final Cloudinary cloudinary;

    @Value("${cloudinary.enabled:false}")
    private boolean cloudinaryEnabled;

    @Value("${cloudinary.folder:food-shop}")
    private String cloudinaryFolder;

    public FileUploadService(org.springframework.beans.factory.ObjectProvider<Cloudinary> cloudinaryProvider) {
        this.cloudinary = cloudinaryProvider.getIfAvailable();
    }

    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Only images are allowed (JPEG, PNG, GIF, WebP)");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new IllegalArgumentException("Invalid file extension");
            }
        }

        if (!cloudinaryEnabled || cloudinary == null) {
            return uploadToLocal(file);
        }

        try {
            String publicId = cloudinaryFolder + "/" + UUID.randomUUID();
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", cloudinaryFolder,
                            "resource_type", "image",
                            "overwrite", true
                    )
            );
            return (String) result.get("secure_url");
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Cloudinary", e);
        }
    }

    public void deleteFile(String imageUrl) {
        if (!cloudinaryEnabled || cloudinary == null || imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        String publicId = extractPublicId(imageUrl);
        if (publicId == null) {
            return;
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception ignored) {
            // Do not fail product flows when image cleanup fails.
        }
    }

    private String extractPublicId(String imageUrl) {
        if (!imageUrl.contains("cloudinary.com")) {
            return null;
        }
        int uploadIndex = imageUrl.indexOf("/upload/");
        if (uploadIndex == -1) {
            return null;
        }
        String afterUpload = imageUrl.substring(uploadIndex + 8);
        int slashIndex = afterUpload.indexOf("/");
        if (slashIndex != -1) {
            afterUpload = afterUpload.substring(slashIndex + 1);
        }
        int dotIndex = afterUpload.lastIndexOf(".");
        if (dotIndex != -1) {
            afterUpload = afterUpload.substring(0, dotIndex);
        }
        return afterUpload;
    }

    private String uploadToLocal(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID() + extension;
    }
}
