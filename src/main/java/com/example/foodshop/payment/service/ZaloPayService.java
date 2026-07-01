package com.example.foodshop.payment.service;

import com.example.foodshop.payment.config.ZaloPayConfig;
import com.example.foodshop.payment.entity.Payment;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ZaloPayService {

    private static final Logger log = LoggerFactory.getLogger(ZaloPayService.class);

    @Autowired
    private ZaloPayConfig zaloPayConfig;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String createPaymentUrl(Payment payment, String ipnUrl, String returnUrl) {
        try {
            // BUG 1 FIX: ZaloPay v2 bắt buộc có app_trans_id theo format yyMMdd_uniqueId
            String appTransId = new SimpleDateFormat("yyMMdd").format(new Date())
                    + "_" + payment.getPaymentNumber();

            long appTime = System.currentTimeMillis();

            String embedData = "{\"redirecturl\":\"" + (returnUrl != null ? returnUrl : "") + "\"}";
            String item = "[]";

            // BUG 2 FIX: MAC phải đúng thứ tự:
            // app_id|app_trans_id|app_user|amount|app_time|embed_data|item
            String macData = zaloPayConfig.getAppId()
                    + "|" + appTransId
                    + "|" + "foodshop_" + payment.getUserId()
                    + "|" + payment.getFinalAmount().longValue()
                    + "|" + appTime
                    + "|" + embedData
                    + "|" + item;

            String mac = hmacSHA256(zaloPayConfig.getKey1(), macData);

            Map<String, Object> params = new LinkedHashMap<>();
            params.put("app_id", zaloPayConfig.getAppId());
            params.put("app_trans_id", appTransId);
            params.put("app_user", "foodshop_" + payment.getUserId());
            params.put("app_time", appTime);
            params.put("amount", payment.getFinalAmount().longValue());
            params.put("item", item);
            params.put("embed_data", embedData);
            params.put("description", "FoodShop - Thanh toan don hang " + payment.getOrderId());
            params.put("bank_code", "");
            params.put("mac", mac);
            // BUG 3 FIX: callback_url là IPN để ZaloPay gọi lại sau khi thanh toán
            if (ipnUrl != null && !ipnUrl.isEmpty()) {
                params.put("callback_url", ipnUrl);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String requestBody = objectMapper.writeValueAsString(params);
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

            log.info("Calling ZaloPay API: app_trans_id={}, amount={}", appTransId, payment.getFinalAmount().longValue());

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    zaloPayConfig.getApiEndpoint(), request, Map.class);

            log.info("ZaloPay response: {}", response.getBody());

            if (response.getBody() != null) {
                Object returnCode = response.getBody().get("return_code");
                if (Integer.valueOf(1).equals(returnCode) || "1".equals(String.valueOf(returnCode))) {
                    String orderUrl = response.getBody().get("order_url").toString();
                    // Lưu app_trans_id để verify callback sau
                    payment.setTransactionId(appTransId);
                    log.info("ZaloPay payment URL created: {}", orderUrl);
                    return orderUrl;
                } else {
                    log.error("ZaloPay error: return_code={}, return_message={}",
                            returnCode, response.getBody().get("return_message"));
                    throw new RuntimeException("ZaloPay error: " + response.getBody().get("return_message"));
                }
            }

            throw new RuntimeException("Empty response from ZaloPay");

        } catch (Exception e) {
            log.error("Error creating ZaloPay payment URL: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating ZaloPay payment URL: " + e.getMessage(), e);
        }
    }

    public boolean verifyCallback(String data, String mac) {
        try {
            if (data == null || mac == null) return false;
            // ZaloPay callback: mac = HMAC_SHA256(key2, data)
            String calculatedMac = hmacSHA256(zaloPayConfig.getKey2(), data);
            return calculatedMac.equals(mac);
        } catch (Exception e) {
            log.error("Error verifying ZaloPay callback: {}", e.getMessage(), e);
            return false;
        }
    }

    private String hmacSHA256(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA256", e);
        }
    }
}