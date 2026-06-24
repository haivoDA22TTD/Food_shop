package com.example.foodshop.order.service;

import com.example.foodshop.order.dto.DashboardResponse;
import com.example.foodshop.order.repository.OrderItemRepository;
import com.example.foodshop.order.repository.OrderRepository;
import com.example.foodshop.order.repository.ShipperRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AdminDashboardService {

    private static final Logger log = LoggerFactory.getLogger(AdminDashboardService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ShipperRepository shipperRepository;

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        return BigDecimal.ZERO;
    }

    public DashboardResponse getDashboard() {
        log.info("Building admin dashboard data");

        DashboardResponse dashboard = new DashboardResponse();

        try {
            long totalOrders = orderRepository.count();
            dashboard.setTotalOrders(totalOrders);

            List<Object[]> stats = orderRepository.getOrderStatistics(
                    LocalDateTime.of(2020, 1, 1, 0, 0),
                    LocalDateTime.now()
            );
            BigDecimal totalRevenue = BigDecimal.ZERO;
            Map<String, Long> ordersByStatus = new LinkedHashMap<>();

            for (Object[] row : stats) {
                String status = row[0].toString();
                Long count = ((Number) row[1]).longValue();
                BigDecimal revenue = toBigDecimal(row[2]);
                ordersByStatus.put(status, count);
                if ("DELIVERED".equals(status)) {
                    totalRevenue = totalRevenue.add(revenue);
                }
            }
            dashboard.setOrdersByStatus(ordersByStatus);
            dashboard.setTotalRevenue(totalRevenue);

            if (totalOrders > 0) {
                dashboard.setAverageOrderValue(totalRevenue.divide(BigDecimal.valueOf(totalOrders), 0, RoundingMode.HALF_UP));
            } else {
                dashboard.setAverageOrderValue(BigDecimal.ZERO);
            }
        } catch (Exception e) {
            log.error("Error loading order stats: {}", e.getMessage(), e);
            dashboard.setTotalOrders(0L);
            dashboard.setTotalRevenue(BigDecimal.ZERO);
            dashboard.setAverageOrderValue(BigDecimal.ZERO);
            dashboard.setOrdersByStatus(new LinkedHashMap<>());
        }

        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            List<Object[]> topProductData = orderItemRepository.findTopSellingProducts(thirtyDaysAgo, LocalDateTime.now());
            List<DashboardResponse.TopProduct> topProducts = new ArrayList<>();
            for (Object[] row : topProductData) {
                topProducts.add(new DashboardResponse.TopProduct(
                        ((Number) row[0]).longValue(),
                        row[1] != null ? row[1].toString() : "Unknown",
                        ((Number) row[2]).longValue(),
                        BigDecimal.ZERO
                ));
                if (topProducts.size() >= 10) break;
            }
            dashboard.setTopProducts(topProducts);
        } catch (Exception e) {
            log.error("Error loading top products: {}", e.getMessage(), e);
            dashboard.setTopProducts(new ArrayList<>());
        }

        try {
            List<DashboardResponse.DailyRevenue> revenueByDay = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
            for (int i = 6; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                List<Object[]> dayStats = orderRepository.getOrderStatistics(date.atStartOfDay(), date.atTime(LocalTime.MAX));
                long dayOrders = 0;
                BigDecimal dayRevenue = BigDecimal.ZERO;
                for (Object[] row : dayStats) {
                    dayOrders += ((Number) row[1]).longValue();
                    dayRevenue = dayRevenue.add(toBigDecimal(row[2]));
                }
                revenueByDay.add(new DashboardResponse.DailyRevenue(date.format(formatter), dayOrders, dayRevenue));
            }
            dashboard.setRevenueByDay(revenueByDay);
        } catch (Exception e) {
            log.error("Error loading revenue by day: {}", e.getMessage(), e);
            dashboard.setRevenueByDay(new ArrayList<>());
        }

        try {
            Object[] shipperStats = shipperRepository.getShipperStatistics();
            if (shipperStats != null && shipperStats[0] != null) {
                dashboard.setTotalShippers(((Number) shipperStats[0]).longValue());
            } else {
                dashboard.setTotalShippers(0L);
            }
        } catch (Exception e) {
            log.warn("Could not get shipper stats: {}", e.getMessage());
            dashboard.setTotalShippers(0L);
        }

        dashboard.setTotalCustomers(0L);

        return dashboard;
    }
}
