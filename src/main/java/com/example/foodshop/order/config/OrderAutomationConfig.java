package com.example.foodshop.order.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.order.automation")
public class OrderAutomationConfig {
    
    /**
     * Enable/disable auto-confirmation of orders
     */
    private boolean autoConfirmEnabled = true;
    
    /**
     * Enable/disable auto-cancellation of expired orders
     */
    private boolean autoCancelEnabled = true;
    
    /**
     * Minutes before auto-cancelling pending orders
     */
    private int autoCancelMinutes = 30;
    
    /**
     * Minimum order amount for auto-confirmation (in VND)
     */
    private double minOrderAmount = 0;
    
    /**
     * Maximum order amount for auto-confirmation (in VND)
     * Orders above this amount require manual confirmation
     */
    private double maxOrderAmount = 10000000; // 10 million VND
    
    /**
     * Business hours start (24-hour format)
     */
    private int businessHoursStart = 6;
    
    /**
     * Business hours end (24-hour format)
     */
    private int businessHoursEnd = 22;
    
    /**
     * Auto-confirm only during business hours
     */
    private boolean autoConfirmOnlyInBusinessHours = true;
    
    // Getters and Setters
    
    public boolean isAutoConfirmEnabled() {
        return autoConfirmEnabled;
    }
    
    public void setAutoConfirmEnabled(boolean autoConfirmEnabled) {
        this.autoConfirmEnabled = autoConfirmEnabled;
    }
    
    public boolean isAutoCancelEnabled() {
        return autoCancelEnabled;
    }
    
    public void setAutoCancelEnabled(boolean autoCancelEnabled) {
        this.autoCancelEnabled = autoCancelEnabled;
    }
    
    public int getAutoCancelMinutes() {
        return autoCancelMinutes;
    }
    
    public void setAutoCancelMinutes(int autoCancelMinutes) {
        this.autoCancelMinutes = autoCancelMinutes;
    }
    
    public double getMinOrderAmount() {
        return minOrderAmount;
    }
    
    public void setMinOrderAmount(double minOrderAmount) {
        this.minOrderAmount = minOrderAmount;
    }
    
    public double getMaxOrderAmount() {
        return maxOrderAmount;
    }
    
    public void setMaxOrderAmount(double maxOrderAmount) {
        this.maxOrderAmount = maxOrderAmount;
    }
    
    public int getBusinessHoursStart() {
        return businessHoursStart;
    }
    
    public void setBusinessHoursStart(int businessHoursStart) {
        this.businessHoursStart = businessHoursStart;
    }
    
    public int getBusinessHoursEnd() {
        return businessHoursEnd;
    }
    
    public void setBusinessHoursEnd(int businessHoursEnd) {
        this.businessHoursEnd = businessHoursEnd;
    }
    
    public boolean isAutoConfirmOnlyInBusinessHours() {
        return autoConfirmOnlyInBusinessHours;
    }
    
    public void setAutoConfirmOnlyInBusinessHours(boolean autoConfirmOnlyInBusinessHours) {
        this.autoConfirmOnlyInBusinessHours = autoConfirmOnlyInBusinessHours;
    }
}
