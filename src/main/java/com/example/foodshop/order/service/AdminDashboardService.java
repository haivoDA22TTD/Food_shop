package com.example.foodshop.order.service;

import com.example.foodshop.order.dto.DashboardResponse;
import com.example.foodshop.order.entity.OrderStatus;
import com.example.foodshop.order.repository.OrderItemRepository;
import com.example.foodshop.order.repository.OrderRepository;
import com.example.foodshop.order.repository.ShipperRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminDashboardService {

    private static final Logger log = LoggerFactory.getLogger(AdminDashboardService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ShipperRepository shipperRepository;

    public DashboardResponse getDashboard() {
        log.info("Building admin dashboard data");

        DashboardResponse dashboard = new DashboardResponse();

        // Total orders
        long totalOrders = orderRepository.count();
        dashboard.setTotalOrders(totalOrders);

        // Total revenue (all delivered orders)
        List<Object[]> stats = orderRepository.getOrderStatistics(
                LocalDateTime.of(2020, 1, 1, 0, 0),
                LocalDateTime.now()
        );
        BigDecimal totalRevenue = BigDecimal.ZERO;
        Map<String, Long> ordersByStatus = new LinkedHashMap<>();

        for (Object[] row : stats) {
            String status = row[0].toString();
            Long count = (Long) row[1];
            BigDecimal revenue = row[2] != null ? BigDecimal.valueOf((Double) row[2]) : BigDecimal.ZERO;
            ordersByStatus.put(status, count);
            if ("DELIVERED".equals(status)) {
                totalRevenue = totalRevenue.add(revenue);
            }
        }
        dashboard.setOrdersByStatus(ordersByStatus);
        dashboard.setTotalRevenue(totalRevenue);

        // Average order value
        if (totalOrders > 0) {
            BigDecimal avg = totalRevenue.divide(BigDecimal.valueOf(totalOrders), 0, RoundingMode.HALF_UP);
            dashboard.setAverageOrderValue(avg);
        } else {
            dashboard.setAverageOrderValue(BigDecimal.ZERO);
        }

        // Top selling products (last 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Object[]> topProductData = orderItemRepository.findTopSellingProducts(thirtyDaysAgo, LocalDateTime.now());
        List<DashboardResponse.TopProduct> topProducts = topProductData.stream()
                .limit(10)
                .map(row -> new DashboardResponse.TopProduct(
                        (Long) row[0],
                        (String) row[1],
                        (Long) row[2],
                        BigDecimal.ZERO
                ))
                .collect(Collectors.toList());
        dashboard.setTopProducts(topProducts);

        // Revenue by day (last 7 days)
        List<DashboardResponse.DailyRevenue> revenueByDay = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            List<Object[]> dayStats = orderRepository.getOrderStatistics(startOfDay, endOfDay);
            long dayOrders = 0;
            BigDecimal dayRevenue = BigDecimal.ZERO;
            for (Object[] row : dayStats) {
                dayOrders += (Long) row[1];
                if (row[2] != null) {
                    dayRevenue = dayRevenue.add(BigDecimal.valueOf((Double) row[2]));
                }
            }
            revenueByDay.add(new DashboardResponse.DailyRevenue(
                    date.format(formatter), dayOrders, dayRevenue
            ));
        }
        dashboard.setRevenueByDay(revenueByDay);

        // Total customers (distinct userIds) - approximate from orders
        dashboard.setTotalCustomers(0L);

        // Total active shippers
        try {
            Object[] shipperStats = shipperRepository.getShipperStatistics();
            if (shipperStats != null && shipperStats[0] != null) {
                dashboard.setTotalShippers(((Long) shipperStats[0]));
            } else {
                dashboard.setTotalShippers(0L);
            }
        } catch (Exception e) {
            log.warn("Could not get shipper stats: {}", e.getMessage());
            dashboard.setTotalShippers(0L);
        }

        return dashboard;
    }
}
