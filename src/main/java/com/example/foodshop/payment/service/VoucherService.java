package com.example.foodshop.payment.service;

import com.example.foodshop.payment.dto.request.ApplyVoucherRequest;
import com.example.foodshop.payment.dto.request.CreateVoucherRequest;
import com.example.foodshop.payment.dto.response.VoucherResponse;
import com.example.foodshop.payment.entity.Voucher;
import com.example.foodshop.payment.entity.VoucherType;
import com.example.foodshop.payment.entity.VoucherUsage;
import com.example.foodshop.payment.exception.VoucherException;
import com.example.foodshop.payment.repository.VoucherRepository;
import com.example.foodshop.payment.repository.VoucherUsageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
public class VoucherService {
    
    private static final Logger log = LoggerFactory.getLogger(VoucherService.class);
    private static final String VOUCHER_CACHE_PREFIX = "voucher:";
    private static final String VOUCHER_LOCK_PREFIX = "voucher:lock:";
    
    @Autowired
    private VoucherRepository voucherRepository;
    
    @Autowired
    private VoucherUsageRepository voucherUsageRepository;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Value("${app.voucher.cache-ttl:3600}")
    private long voucherCacheTtl;
    
    public VoucherResponse createVoucher(CreateVoucherRequest request) {
        if (request.getValidTo().isBefore(request.getValidFrom())) {
            throw new VoucherException("Valid to date must be after valid from date");
        }
        
        if (voucherRepository.findByCode(request.getCode()).isPresent()) {
            throw new VoucherException("Voucher code already exists");
        }
        
        if (request.getVoucherType() == VoucherType.PERCENTAGE && 
            request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new VoucherException("Percentage discount cannot exceed 100%");
        }
        
        Voucher voucher = new Voucher();
        voucher.setCode(request.getCode().toUpperCase());
        voucher.setName(request.getName());
        voucher.setDescription(request.getDescription());
        voucher.setVoucherType(request.getVoucherType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        voucher.setMinOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : BigDecimal.ZERO);
        voucher.setUsageLimit(request.getUsageLimit());
        voucher.setPerUserLimit(request.getPerUserLimit());
        voucher.setIsActive(request.getIsActive());
        voucher.setValidFrom(request.getValidFrom());
        voucher.setValidTo(request.getValidTo());
        
        voucher = voucherRepository.save(voucher);
        cacheVoucher(voucher);
        
        log.info("Created voucher: {}", voucher.getCode());
        return convertToResponse(voucher);
    }
    
    public VoucherResponse applyVoucher(String code, Long userId, BigDecimal orderAmount) {
        Voucher voucher = getVoucherByCode(code);
        validateVoucher(voucher, userId, orderAmount);
        
        BigDecimal discountAmount = calculateDiscount(voucher, orderAmount);
        BigDecimal finalAmount = orderAmount.subtract(discountAmount);
        
        VoucherResponse response = convertToResponse(voucher);
        response.setDiscountAmount(discountAmount);
        response.setFinalAmount(finalAmount);
        
        return response;
    }
    
    public void reserveVoucher(Long voucherId, Long userId, Long paymentId, BigDecimal discountAmount) {
        String lockKey = VOUCHER_LOCK_PREFIX + voucherId;
        Boolean locked = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, "locked", Duration.ofSeconds(30));
        
        if (Boolean.FALSE.equals(locked)) {
            throw new VoucherException("Voucher is being used by another transaction. Please try again.");
        }
        
        try {
            Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new VoucherException("Voucher not found"));
            
            int userUsageCount = voucherUsageRepository.countByVoucherIdAndUserId(voucherId, userId);
            if (userUsageCount >= voucher.getPerUserLimit()) {
                throw new VoucherException("You have reached the usage limit for this voucher");
            }
            
            if (voucher.hasReachedLimit()) {
                throw new VoucherException("Voucher usage limit has been reached");
            }
            
            voucher.incrementUsage();
            voucherRepository.save(voucher);
            
            VoucherUsage usage = new VoucherUsage(voucherId, userId, paymentId, discountAmount);
            voucherUsageRepository.save(usage);
            
            cacheVoucher(voucher);
            
            log.info("Reserved voucher {} for user {} with discount {}", 
                    voucher.getCode(), userId, discountAmount);
            
        } finally {
            redisTemplate.delete(lockKey);
        }
    }
    
    public List<VoucherResponse> getAvailableVouchers() {
        LocalDateTime now = LocalDateTime.now();
        List<Voucher> vouchers = voucherRepository.findAvailableVouchers(now);
        
        return vouchers.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    public Page<VoucherResponse> getAllVouchers(Pageable pageable) {
        return voucherRepository.findAll(pageable)
            .map(this::convertToResponse);
    }
    
    public VoucherResponse getVoucherById(Long id) {
        Voucher voucher = voucherRepository.findById(id)
            .orElseThrow(() -> new VoucherException("Voucher not found"));
        return convertToResponse(voucher);
    }
    
    public VoucherResponse updateVoucher(Long id, CreateVoucherRequest request) {
        Voucher voucher = voucherRepository.findById(id)
            .orElseThrow(() -> new VoucherException("Voucher not found"));
        
        if (!voucher.getCode().equals(request.getCode())) {
            if (voucherRepository.findByCode(request.getCode()).isPresent()) {
                throw new VoucherException("Voucher code already exists");
            }
        }
        
        voucher.setCode(request.getCode().toUpperCase());
        voucher.setName(request.getName());
        voucher.setDescription(request.getDescription());
        voucher.setVoucherType(request.getVoucherType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
        voucher.setMinOrderAmount(request.getMinOrderAmount());
        voucher.setUsageLimit(request.getUsageLimit());
        voucher.setPerUserLimit(request.getPerUserLimit());
        voucher.setIsActive(request.getIsActive());
        voucher.setValidFrom(request.getValidFrom());
        voucher.setValidTo(request.getValidTo());
        
        voucher = voucherRepository.save(voucher);
        cacheVoucher(voucher);
        
        log.info("Updated voucher: {}", voucher.getCode());
        return convertToResponse(voucher);
    }
    
    public void deleteVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
            .orElseThrow(() -> new VoucherException("Voucher not found"));
        
        long usageCount = voucherUsageRepository.countByVoucherId(id);
        if (usageCount > 0) {
            throw new VoucherException("Cannot delete voucher that has been used. Consider deactivating it instead.");
        }
        
        voucherRepository.delete(voucher);
        redisTemplate.delete(VOUCHER_CACHE_PREFIX + voucher.getCode());
        
        log.info("Deleted voucher: {}", voucher.getCode());
    }
    
    public Page<VoucherUsage> getVoucherUsages(Long voucherId, Pageable pageable) {
        return voucherUsageRepository.findByVoucherIdOrderByUsedAtDesc(voucherId, pageable);
    }
    
    public List<VoucherUsage> getUserVoucherUsages(Long userId) {
        return voucherUsageRepository.findByUserIdOrderByUsedAtDesc(userId);
    }
    
    private Voucher getVoucherByCode(String code) {
        String cacheKey = VOUCHER_CACHE_PREFIX + code.toUpperCase();
        Voucher voucher = (Voucher) redisTemplate.opsForValue().get(cacheKey);
        
        if (voucher == null) {
            voucher = voucherRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new VoucherException("Voucher not found"));
            cacheVoucher(voucher);
        }
        
        return voucher;
    }
    
    private void validateVoucher(Voucher voucher, Long userId, BigDecimal orderAmount) {
        if (!voucher.getIsActive()) {
            throw new VoucherException("Voucher is not active");
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getValidFrom())) {
            throw new VoucherException("Voucher is not yet valid");
        }
        if (now.isAfter(voucher.getValidTo())) {
            throw new VoucherException("Voucher has expired");
        }
        
        if (orderAmount.compareTo(voucher.getMinOrderAmount()) < 0) {
            throw new VoucherException(String.format(
                "Minimum order amount is %s", voucher.getMinOrderAmount()));
        }
        
        if (voucher.hasReachedLimit()) {
            throw new VoucherException("Voucher usage limit has been reached");
        }
        
        int userUsageCount = voucherUsageRepository
            .countByVoucherIdAndUserId(voucher.getId(), userId);
        if (userUsageCount >= voucher.getPerUserLimit()) {
            throw new VoucherException("You have reached the usage limit for this voucher");
        }
    }
    
    private BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderAmount) {
        BigDecimal discount;
        
        if (voucher.getVoucherType() == VoucherType.PERCENTAGE) {
            discount = orderAmount.multiply(voucher.getDiscountValue())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            
            if (voucher.getMaxDiscountAmount() != null) {
                discount = discount.min(voucher.getMaxDiscountAmount());
            }
        } else if (voucher.getVoucherType() == VoucherType.FIXED_AMOUNT) {
            discount = voucher.getDiscountValue();
        } else {
            discount = BigDecimal.ZERO;
        }
        
        return discount.min(orderAmount);
    }
    
    private void cacheVoucher(Voucher voucher) {
        String cacheKey = VOUCHER_CACHE_PREFIX + voucher.getCode();
        redisTemplate.opsForValue().set(cacheKey, voucher, voucherCacheTtl, TimeUnit.SECONDS);
    }
    
    private VoucherResponse convertToResponse(Voucher voucher) {
        VoucherResponse response = new VoucherResponse();
        response.setId(voucher.getId());
        response.setCode(voucher.getCode());
        response.setName(voucher.getName());
        response.setDescription(voucher.getDescription());
        response.setVoucherType(voucher.getVoucherType());
        response.setDiscountValue(voucher.getDiscountValue());
        response.setMaxDiscountAmount(voucher.getMaxDiscountAmount());
        response.setMinOrderAmount(voucher.getMinOrderAmount());
        response.setUsageLimit(voucher.getUsageLimit());
        response.setUsageCount(voucher.getUsageCount());
        response.setPerUserLimit(voucher.getPerUserLimit());
        response.setIsActive(voucher.getIsActive());
        response.setValidFrom(voucher.getValidFrom());
        response.setValidTo(voucher.getValidTo());
        response.setCreatedAt(voucher.getCreatedAt());
        response.setUpdatedAt(voucher.getUpdatedAt());
        return response;
    }
}
