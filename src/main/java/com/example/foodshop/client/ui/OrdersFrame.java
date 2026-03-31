package com.example.foodshop.client.ui;

import com.example.foodshop.client.api.ApiClient;
import com.example.foodshop.client.components.ModernButton;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrdersFrame extends JFrame {
    
    private ApiClient apiClient;
    private JPanel ordersPanel;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;
    private Map<String, String> statusNames;
    private Map<String, Color> statusColors;
    
    public OrdersFrame() {
        this.apiClient = ApiClient.getInstance();
        this.currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        initStatusMaps();
        initComponents();
        loadOrders();
    }
    
    private void initStatusMaps() {
        statusNames = new HashMap<>();
        statusNames.put("PENDING", "Chờ xác nhận");
        statusNames.put("CONFIRMED", "Đã xác nhận");
        statusNames.put("SHIPPING", "Đang giao hàng");
        statusNames.put("DELIVERED", "Đã giao hàng");
        statusNames.put("CANCELLED", "Đã hủy");
        
        statusColors = new HashMap<>();
        statusColors.put("PENDING", new Color(251, 191, 36));
        statusColors.put("CONFIRMED", new Color(59, 130, 246));
        statusColors.put("SHIPPING", new Color(139, 92, 246));
        statusColors.put("DELIVERED", new Color(34, 197, 94));
        statusColors.put("CANCELLED", new Color(239, 68, 68));
    }
    
    private void initComponents() {
        setTitle("Đơn hàng của tôi - Food Shop");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(new Color(240, 248, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("📦 Đơn hàng của tôi");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(14, 165, 233));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        ModernButton refreshBtn = new ModernButton("🔄 Làm mới");
        refreshBtn.setPreferredSize(new Dimension(120, 40));
        refreshBtn.setColors(
            new Color(34, 197, 94),
            new Color(22, 163, 74),
            new Color(21, 128, 61)
        );
        refreshBtn.addActionListener(e -> loadOrders());
        buttonPanel.add(refreshBtn);
        
        ModernButton closeBtn = new ModernButton("✕ Đóng");
        closeBtn.setPreferredSize(new Dimension(100, 40));
        closeBtn.addActionListener(e -> dispose());
        buttonPanel.add(closeBtn);
        
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Orders list
        ordersPanel = new JPanel();
        ordersPanel.setLayout(new BoxLayout(ordersPanel, BoxLayout.Y_AXIS));
        ordersPanel.setBackground(new Color(240, 248, 255));
        
        JScrollPane scrollPane = new JScrollPane(ordersPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private void loadOrders() {
        ordersPanel.removeAll();
        
        JPanel loadingPanel = new JPanel();
        loadingPanel.setOpaque(false);
        loadingPanel.add(new JLabel("Đang tải đơn hàng..."));
        ordersPanel.add(loadingPanel);
        ordersPanel.revalidate();
        ordersPanel.repaint();
        
        SwingWorker<List<ApiClient.OrderDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ApiClient.OrderDTO> doInBackground() throws Exception {
                return apiClient.getMyOrders();
            }
            
            @Override
            protected void done() {
                try {
                    List<ApiClient.OrderDTO> orders = get();
                    displayOrders(orders);
                } catch (Exception ex) {
                    ordersPanel.removeAll();
                    JPanel errorPanel = new JPanel();
                    errorPanel.setOpaque(false);
                    errorPanel.add(new JLabel("❌ Lỗi: " + ex.getMessage()));
                    ordersPanel.add(errorPanel);
                    ordersPanel.revalidate();
                    ordersPanel.repaint();
                }
            }
        };
        
        worker.execute();
    }
    
    private void displayOrders(List<ApiClient.OrderDTO> orders) {
        ordersPanel.removeAll();
        
        if (orders.isEmpty()) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
            emptyPanel.setOpaque(false);
            emptyPanel.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20));
            
            JLabel emptyIcon = new JLabel("📦", SwingConstants.CENTER);
            emptyIcon.setFont(new Font("Arial", Font.PLAIN, 80));
            emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel emptyText = new JLabel("Chưa có đơn hàng nào");
            emptyText.setFont(new Font("Arial", Font.BOLD, 20));
            emptyText.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            emptyPanel.add(emptyIcon);
            emptyPanel.add(Box.createVerticalStrut(20));
            emptyPanel.add(emptyText);
            
            ordersPanel.add(emptyPanel);
        } else {
            for (ApiClient.OrderDTO order : orders) {
                ordersPanel.add(createOrderCard(order));
                ordersPanel.add(Box.createVerticalStrut(15));
            }
        }
        
        ordersPanel.revalidate();
        ordersPanel.repaint();
    }
    
    private JPanel createOrderCard(ApiClient.OrderDTO order) {
        JPanel card = new JPanel(new BorderLayout(15, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(14, 165, 233), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JPanel leftHeader = new JPanel();
        leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));
        leftHeader.setOpaque(false);
        
        JLabel orderIdLabel = new JLabel("Đơn hàng #" + order.getId());
        orderIdLabel.setFont(new Font("Arial", Font.BOLD, 16));
        orderIdLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel dateLabel = new JLabel(formatDate(order.getCreatedAt()));
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        dateLabel.setForeground(Color.GRAY);
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        leftHeader.add(orderIdLabel);
        leftHeader.add(Box.createVerticalStrut(5));
        leftHeader.add(dateLabel);
        
        headerPanel.add(leftHeader, BorderLayout.WEST);
        
        // Status badge
        String status = order.getStatus();
        JLabel statusLabel = new JLabel(statusNames.getOrDefault(status, status));
        statusLabel.setFont(new Font("Arial", Font.BOLD, 13));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setOpaque(true);
        statusLabel.setBackground(statusColors.getOrDefault(status, Color.GRAY));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        headerPanel.add(statusLabel, BorderLayout.EAST);
        
        card.add(headerPanel, BorderLayout.NORTH);
        
        // Items
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setOpaque(false);
        
        int itemCount = 0;
        for (ApiClient.OrderItemDTO item : order.getOrderItems()) {
            if (itemCount >= 3) {
                JLabel moreLabel = new JLabel("... và " + (order.getOrderItems().size() - 3) + " sản phẩm khác");
                moreLabel.setFont(new Font("Arial", Font.ITALIC, 12));
                moreLabel.setForeground(Color.GRAY);
                itemsPanel.add(moreLabel);
                break;
            }
            
            JPanel itemPanel = new JPanel(new BorderLayout(10, 0));
            itemPanel.setOpaque(false);
            itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            
            JLabel itemLabel = new JLabel(item.getProduct().getName() + " x" + item.getQuantity());
            itemLabel.setFont(new Font("Arial", Font.PLAIN, 13));
            itemPanel.add(itemLabel, BorderLayout.WEST);
            
            JLabel itemPrice = new JLabel(formatPrice(item.getPrice() * item.getQuantity()));
            itemPrice.setFont(new Font("Arial", Font.BOLD, 13));
            itemPrice.setForeground(new Color(14, 165, 233));
            itemPanel.add(itemPrice, BorderLayout.EAST);
            
            itemsPanel.add(itemPanel);
            itemCount++;
        }
        
        card.add(itemsPanel, BorderLayout.CENTER);
        
        // Footer
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(224, 242, 254)));
        footerPanel.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        
        JLabel totalLabel = new JLabel("Tổng: " + formatPrice(order.getTotalAmount()));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalLabel.setForeground(new Color(14, 165, 233));
        footerPanel.add(totalLabel, BorderLayout.WEST);
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        
        // Review button (only for DELIVERED orders)
        if ("DELIVERED".equals(order.getStatus())) {
            ModernButton reviewBtn = new ModernButton("⭐ Đánh giá");
            reviewBtn.setPreferredSize(new Dimension(120, 35));
            reviewBtn.setColors(
                new Color(251, 191, 36),
                new Color(245, 158, 11),
                new Color(217, 119, 6)
            );
            reviewBtn.addActionListener(e -> showReviewDialog(order));
            btnPanel.add(reviewBtn);
        }
        
        ModernButton detailBtn = new ModernButton("Chi tiết");
        detailBtn.setPreferredSize(new Dimension(100, 35));
        detailBtn.addActionListener(e -> showOrderDetail(order));
        btnPanel.add(detailBtn);
        
        footerPanel.add(btnPanel, BorderLayout.EAST);
        
        card.add(footerPanel, BorderLayout.SOUTH);
        
        return card;
    }
    
    private void showOrderDetail(ApiClient.OrderDTO order) {
        JDialog dialog = new JDialog(this, "Chi tiết đơn hàng #" + order.getId(), true);
        dialog.setSize(700, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        
        // Status
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setOpaque(false);
        JLabel statusLabel = new JLabel("Trạng thái: " + statusNames.getOrDefault(order.getStatus(), order.getStatus()));
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setForeground(statusColors.getOrDefault(order.getStatus(), Color.GRAY));
        statusPanel.add(statusLabel);
        contentPanel.add(statusPanel);
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Items
        JLabel itemsTitle = new JLabel("Sản phẩm:");
        itemsTitle.setFont(new Font("Arial", Font.BOLD, 14));
        itemsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(itemsTitle);
        contentPanel.add(Box.createVerticalStrut(10));
        
        for (ApiClient.OrderItemDTO item : order.getOrderItems()) {
            JPanel itemPanel = new JPanel(new BorderLayout(10, 5));
            itemPanel.setOpaque(false);
            itemPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 242, 254), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);
            
            JLabel nameLabel = new JLabel(item.getProduct().getName());
            nameLabel.setFont(new Font("Arial", Font.BOLD, 13));
            nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JLabel qtyLabel = new JLabel("Số lượng: " + item.getQuantity());
            qtyLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            qtyLabel.setForeground(Color.GRAY);
            qtyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            infoPanel.add(nameLabel);
            infoPanel.add(Box.createVerticalStrut(5));
            infoPanel.add(qtyLabel);
            
            itemPanel.add(infoPanel, BorderLayout.CENTER);
            
            JLabel priceLabel = new JLabel(formatPrice(item.getPrice() * item.getQuantity()));
            priceLabel.setFont(new Font("Arial", Font.BOLD, 14));
            priceLabel.setForeground(new Color(14, 165, 233));
            itemPanel.add(priceLabel, BorderLayout.EAST);
            
            contentPanel.add(itemPanel);
            contentPanel.add(Box.createVerticalStrut(10));
        }
        
        contentPanel.add(Box.createVerticalStrut(10));
        
        // Shipping info
        JLabel shippingTitle = new JLabel("Thông tin giao hàng:");
        shippingTitle.setFont(new Font("Arial", Font.BOLD, 14));
        shippingTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(shippingTitle);
        contentPanel.add(Box.createVerticalStrut(10));
        
        JTextArea shippingArea = new JTextArea(order.getShippingAddress());
        shippingArea.setFont(new Font("Arial", Font.PLAIN, 13));
        shippingArea.setEditable(false);
        shippingArea.setLineWrap(true);
        shippingArea.setWrapStyleWord(true);
        shippingArea.setBackground(new Color(240, 249, 255));
        shippingArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.add(shippingArea);
        
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Payment method
        JLabel paymentLabel = new JLabel("Phương thức thanh toán: " + order.getPaymentMethod());
        paymentLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        paymentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(paymentLabel);
        
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Total
        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setOpaque(false);
        totalPanel.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(14, 165, 233)));
        totalPanel.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
        
        JLabel totalLabel = new JLabel("Tổng cộng: " + formatPrice(order.getTotalAmount()));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 20));
        totalLabel.setForeground(new Color(14, 165, 233));
        totalPanel.add(totalLabel, BorderLayout.CENTER);
        
        contentPanel.add(totalPanel);
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Close button
        ModernButton closeBtn = new ModernButton("Đóng");
        closeBtn.setPreferredSize(new Dimension(100, 40));
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setOpaque(false);
        btnPanel.add(closeBtn);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
    
    private String formatPrice(double price) {
        return currencyFormat.format(price) + "đ";
    }
    
    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date date = inputFormat.parse(dateStr);
            return dateFormat.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }
    
    private void showReviewDialog(ApiClient.OrderDTO order) {
        // Show dialog to select which product to review
        if (order.getOrderItems().size() == 1) {
            // Only one product, review directly
            ApiClient.OrderItemDTO item = order.getOrderItems().get(0);
            ReviewDialog dialog = new ReviewDialog(
                this,
                item.getProduct().getId(),
                order.getId(),
                item.getProduct().getName()
            );
            dialog.setVisible(true);
            loadOrders(); // Refresh after review
        } else {
            // Multiple products, let user choose
            showProductSelectionDialog(order);
        }
    }
    
    private void showProductSelectionDialog(ApiClient.OrderDTO order) {
        JDialog dialog = new JDialog(this, "Chọn sản phẩm để đánh giá", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("Chọn sản phẩm bạn muốn đánh giá:");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel productsPanel = new JPanel();
        productsPanel.setLayout(new BoxLayout(productsPanel, BoxLayout.Y_AXIS));
        productsPanel.setBackground(Color.WHITE);
        
        for (ApiClient.OrderItemDTO item : order.getOrderItems()) {
            JPanel productPanel = new JPanel(new BorderLayout(10, 10));
            productPanel.setBackground(Color.WHITE);
            productPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 242, 254), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            productPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
            
            JLabel nameLabel = new JLabel(item.getProduct().getName());
            nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
            productPanel.add(nameLabel, BorderLayout.CENTER);
            
            ModernButton reviewBtn = new ModernButton("Đánh giá");
            reviewBtn.setPreferredSize(new Dimension(100, 35));
            reviewBtn.addActionListener(e -> {
                dialog.dispose();
                ReviewDialog reviewDialog = new ReviewDialog(
                    this,
                    item.getProduct().getId(),
                    order.getId(),
                    item.getProduct().getName()
                );
                reviewDialog.setVisible(true);
                loadOrders();
            });
            productPanel.add(reviewBtn, BorderLayout.EAST);
            
            productsPanel.add(productPanel);
            productsPanel.add(Box.createVerticalStrut(10));
        }
        
        JScrollPane scrollPane = new JScrollPane(productsPanel);
        scrollPane.setBorder(null);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        ModernButton closeBtn = new ModernButton("Đóng");
        closeBtn.setPreferredSize(new Dimension(100, 40));
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setOpaque(false);
        btnPanel.add(closeBtn);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }
}
