package com.example.foodshop.client.ui;

import com.example.foodshop.client.api.ApiClient;
import com.example.foodshop.client.components.ModernButton;
import com.example.foodshop.client.util.CartManager;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartFrame extends JFrame {
    
    private CartManager cartManager;
    private JPanel cartItemsPanel;
    private JLabel subtotalLabel;
    private JLabel shippingLabel;
    private JLabel totalLabel;
    private NumberFormat currencyFormat;
    
    public CartFrame() {
        this.cartManager = CartManager.getInstance();
        this.currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        initComponents();
        loadCart();
    }
    
    private void initComponents() {
        setTitle("Giỏ hàng - Food Shop");
        setSize(900, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(new Color(240, 248, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("🛒 Giỏ hàng của bạn");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(14, 165, 233));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        ModernButton closeBtn = new ModernButton("✕ Đóng");
        closeBtn.setPreferredSize(new Dimension(100, 40));
        closeBtn.addActionListener(e -> dispose());
        headerPanel.add(closeBtn, BorderLayout.EAST);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Content
        JPanel contentPanel = new JPanel(new BorderLayout(20, 0));
        contentPanel.setOpaque(false);
        
        // Cart items (left)
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(14, 165, 233), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        cartItemsPanel = new JPanel();
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));
        cartItemsPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(cartItemsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        leftPanel.add(scrollPane, BorderLayout.CENTER);
        
        contentPanel.add(leftPanel, BorderLayout.CENTER);
        
        // Summary (right)
        JPanel summaryPanel = createSummaryPanel();
        summaryPanel.setPreferredSize(new Dimension(300, 0));
        contentPanel.add(summaryPanel, BorderLayout.EAST);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(14, 165, 233), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel titleLabel = new JLabel("Tóm tắt đơn hàng");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        
        panel.add(Box.createVerticalStrut(20));
        
        // Subtotal
        JPanel subtotalPanel = createSummaryRow("Tạm tính:");
        subtotalLabel = (JLabel) subtotalPanel.getComponent(1);
        panel.add(subtotalPanel);
        
        panel.add(Box.createVerticalStrut(10));
        
        // Shipping
        JPanel shippingPanel = createSummaryRow("Phí vận chuyển:");
        shippingLabel = (JLabel) shippingPanel.getComponent(1);
        panel.add(shippingPanel);
        
        panel.add(Box.createVerticalStrut(15));
        
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        separator.setForeground(new Color(14, 165, 233));
        panel.add(separator);
        
        panel.add(Box.createVerticalStrut(15));
        
        // Total
        JPanel totalPanel = createSummaryRow("Tổng cộng:");
        totalPanel.setBackground(new Color(240, 249, 255));
        totalLabel = (JLabel) totalPanel.getComponent(1);
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalLabel.setForeground(new Color(14, 165, 233));
        ((JLabel) totalPanel.getComponent(0)).setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(totalPanel);
        
        panel.add(Box.createVerticalStrut(20));
        
        // Checkout button
        ModernButton checkoutBtn = new ModernButton("Thanh toán");
        checkoutBtn.setFont(new Font("Arial", Font.BOLD, 16));
        checkoutBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        checkoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkoutBtn.addActionListener(e -> handleCheckout());
        panel.add(checkoutBtn);
        
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JPanel createSummaryRow(String label) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(labelComp, BorderLayout.WEST);
        
        JLabel valueComp = new JLabel("0đ");
        valueComp.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(valueComp, BorderLayout.EAST);
        
        return panel;
    }
    
    private void loadCart() {
        cartItemsPanel.removeAll();
        
        List<CartManager.CartItem> items = cartManager.getCartItems();
        
        if (items.isEmpty()) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
            emptyPanel.setBackground(Color.WHITE);
            emptyPanel.setBorder(BorderFactory.createEmptyBorder(50, 20, 50, 20));
            
            JLabel emptyIcon = new JLabel("🛒", SwingConstants.CENTER);
            emptyIcon.setFont(new Font("Arial", Font.PLAIN, 80));
            emptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            JLabel emptyText = new JLabel("Giỏ hàng trống");
            emptyText.setFont(new Font("Arial", Font.BOLD, 20));
            emptyText.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            emptyPanel.add(emptyIcon);
            emptyPanel.add(Box.createVerticalStrut(20));
            emptyPanel.add(emptyText);
            
            cartItemsPanel.add(emptyPanel);
        } else {
            for (CartManager.CartItem item : items) {
                cartItemsPanel.add(createCartItemPanel(item));
                cartItemsPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        updateSummary();
        cartItemsPanel.revalidate();
        cartItemsPanel.repaint();
    }
    
    private JPanel createCartItemPanel(CartManager.CartItem item) {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(224, 242, 254), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        ApiClient.ProductDTO product = item.getProduct();
        
        // Product info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(product.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel priceLabel = new JLabel(formatPrice(product.getPrice()));
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        priceLabel.setForeground(new Color(14, 165, 233));
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(priceLabel);
        
        panel.add(infoPanel, BorderLayout.CENTER);
        
        // Quantity controls
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        
        JPanel quantityPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        quantityPanel.setOpaque(false);
        
        ModernButton minusBtn = new ModernButton("−");
        minusBtn.setPreferredSize(new Dimension(35, 35));
        minusBtn.setFont(new Font("Arial", Font.BOLD, 16));
        minusBtn.addActionListener(e -> {
            cartManager.updateQuantity(product.getId(), item.getQuantity() - 1);
            loadCart();
        });
        
        JLabel quantityLabel = new JLabel(String.valueOf(item.getQuantity()));
        quantityLabel.setFont(new Font("Arial", Font.BOLD, 14));
        quantityLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        
        ModernButton plusBtn = new ModernButton("+");
        plusBtn.setPreferredSize(new Dimension(35, 35));
        plusBtn.setFont(new Font("Arial", Font.BOLD, 16));
        plusBtn.addActionListener(e -> {
            cartManager.updateQuantity(product.getId(), item.getQuantity() + 1);
            loadCart();
        });
        
        quantityPanel.add(minusBtn);
        quantityPanel.add(quantityLabel);
        quantityPanel.add(plusBtn);
        
        JLabel itemTotalLabel = new JLabel(formatPrice(product.getPrice() * item.getQuantity()));
        itemTotalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        itemTotalLabel.setForeground(new Color(14, 165, 233));
        itemTotalLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        
        rightPanel.add(quantityPanel);
        rightPanel.add(Box.createVerticalStrut(5));
        rightPanel.add(itemTotalLabel);
        
        panel.add(rightPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void updateSummary() {
        double subtotal = cartManager.getSubtotal();
        double shipping = subtotal > 0 ? 30000 : 0;
        double total = subtotal + shipping;
        
        subtotalLabel.setText(formatPrice(subtotal));
        shippingLabel.setText(formatPrice(shipping));
        totalLabel.setText(formatPrice(total));
    }
    
    private String formatPrice(double price) {
        return currencyFormat.format(price) + "đ";
    }
    
    private void handleCheckout() {
        if (cartManager.getCartItems().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Giỏ hàng trống!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        CheckoutFrame checkoutFrame = new CheckoutFrame(this);
        checkoutFrame.setVisible(true);
    }
}
