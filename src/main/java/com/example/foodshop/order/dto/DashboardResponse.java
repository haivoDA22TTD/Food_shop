package com.example.foodshop.order.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardResponse {

    private Long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private Long totalCustomers;
    private Long totalShippers;
    private Map<String, Long> ordersByStatus;
    private List<TopProduct> topProducts;
    private List<DailyRevenue> revenueByDay;

    public DashboardResponse() {}

    public Long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(BigDecimal averageOrderValue) { this.averageOrderValue = averageOrderValue; }

    public Long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(Long totalCustomers) { this.totalCustomers = totalCustomers; }

    public Long getTotalShippers() { return totalShippers; }
    public void setTotalShippers(Long totalShippers) { this.totalShippers = totalShippers; }

    public Map<String, Long> getOrdersByStatus() { return ordersByStatus; }
    public void setOrdersByStatus(Map<String, Long> ordersByStatus) { this.ordersByStatus = ordersByStatus; }

    public List<TopProduct> getTopProducts() { return topProducts; }
    public void setTopProducts(List<TopProduct> topProducts) { this.topProducts = topProducts; }

    public List<DailyRevenue> getRevenueByDay() { return revenueByDay; }
    public void setRevenueByDay(List<DailyRevenue> revenueByDay) { this.revenueByDay = revenueByDay; }

    public static class TopProduct {
        private Long productId;
        private String productName;
        private Long totalSold;
        private BigDecimal totalRevenue;

        public TopProduct() {}
        public TopProduct(Long productId, String productName, Long totalSold, BigDecimal totalRevenue) {
            this.productId = productId;
            this.productName = productName;
            this.totalSold = totalSold;
            this.totalRevenue = totalRevenue;
        }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Long getTotalSold() { return totalSold; }
        public void setTotalSold(Long totalSold) { this.totalSold = totalSold; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    }

    public static class DailyRevenue {
        private String date;
        private Long orders;
        private BigDecimal revenue;

        public DailyRevenue() {}
        public DailyRevenue(String date, Long orders, BigDecimal revenue) {
            this.date = date;
            this.orders = orders;
            this.revenue = revenue;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public Long getOrders() { return orders; }
        public void setOrders(Long orders) { this.orders = orders; }
        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }
    }
}
