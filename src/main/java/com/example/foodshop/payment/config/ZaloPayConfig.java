package com.example.foodshop.payment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZaloPayConfig {

    @Value("${zalopay.app-id:2554}")
    private int appId;

    @Value("${zalopay.key1:sdngKKJmqEMzvh5QQcdD2A9XBSKUNaYn}")
    private String key1;

    @Value("${zalopay.key2:trMrHtvjo6myautxDUiAcYsVtaeQ8nhf}")
    private String key2;

    @Value("${zalopay.api-endpoint:https://sb-openapi.zalopay.vn/v2/create}")
    private String apiEndpoint;

    public int getAppId() { return appId; }
    public String getKey1() { return key1; }
    public String getKey2() { return key2; }
    public String getApiEndpoint() { return apiEndpoint; }
}