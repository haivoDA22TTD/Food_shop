package com.example.foodshop.payment.service;

import com.example.foodshop.payment.config.ZaloPayConfig;
import com.example.foodshop.payment.entity.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ZaloPayService {

    private static final Logger log = LoggerFactory.getLogger(ZaloPayService.class);

    @Autowired
    private ZaloPayConfig zaloPayConfig;

    private final RestTemplate restTemplate = new RestTemplate();

    public String createPaymentUrl(Payment payment, String ipnUrl, String returnUrl) {
        try {
            Map<String, String> params = new LinkedHashMap<>();
            params.put("app_id", String.valueOf(zaloPayConfig.getAppId()));
            params.put("app_user", "foodshop_" + payment.getUserId());
            params.put("app_time", String.valueOf(System.currentTimeMillis()));
            params.put("amount", String.valueOf(payment.getFinalAmount().longValue()));
            params.put("bank_code", "");
            params.put("description", "Thanh toan don hang " + payment.getOrderId());
            params.put("item", "[{}]");
            params.put("embed_data", "{\"promotionCode\":\"\"}");
            params.put("mac", "");
            params.put("callback_url", ipnUrl);

            String data = buildSignData(params);
            String mac = hmacSHA256(zaloPayConfig.getKey1(), data);
            params.put("mac", mac);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            StringBuilder formBody = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (formBody.length() > 0) formBody.append("&");
                formBody.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
                formBody.append("=");
                formBody.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }

            HttpEntity<String> request = new HttpEntity<>(formBody.toString(), headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    zaloPayConfig.getApiEndpoint(), request, Map.class);

            if (response.getBody() != null && response.getBody().get("order_url") != null) {
                String orderUrl = response.getBody().get("order_url").toString();
                log.info("Created ZaloPay payment URL for payment: {}", payment.getPaymentNumber());
                return orderUrl;
            }

            log.error("ZaloPay response: {}", response.getBody());
            throw new RuntimeException("No order_url in ZaloPay response");

        } catch (Exception e) {
            log.error("Error creating ZaloPay payment URL: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating ZaloPay payment URL", e);
        }
    }

    public boolean verifyCallback(Map<String, String> params) {
        try {
            String mac = params.get("mac");
            Map<String, String> dataParams = new LinkedHashMap<>(params);
            dataParams.remove("mac");

            String data = buildSignData(dataParams);
            String calculatedMac = hmacSHA256(zaloPayConfig.getKey2(), data);

            return calculatedMac.equals(mac);
        } catch (Exception e) {
            log.error("Error verifying ZaloPay callback: {}", e.getMessage(), e);
            return false;
        }
    }

    private String buildSignData(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (sb.length() > 0) sb.append("&");
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    private String hmacSHA256(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
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
