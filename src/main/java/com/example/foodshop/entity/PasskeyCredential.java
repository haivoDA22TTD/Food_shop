package com.example.foodshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "passkey_credentials")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasskeyCredential {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "credential_id", unique = true, nullable = false, length = 512)
    private String credentialId;
    
    @Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
    private String publicKey;
    
    @Column(name = "sign_count", nullable = false)
    private Long signCount = 0L;
    
    @Column(name = "aaguid", length = 36)
    private String aaguid;
    
    @Column(name = "credential_type", length = 50)
    private String credentialType = "public-key";
    
    @Column(name = "transports")
    private String transports;
    
    @Column(name = "attestation_format", length = 50)
    private String attestationFormat;
    
    @Column(name = "user_handle", length = 255)
    private String userHandle;
    
    @Column(name = "nickname", length = 100)
    private String nickname;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
