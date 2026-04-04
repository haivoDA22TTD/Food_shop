package com.example.foodshop.client.ui;

import com.example.foodshop.client.api.ApiClient;
import com.example.foodshop.client.components.ModernButton;
import com.example.foodshop.client.util.CartManager;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckoutFrame extends JFrame {
    
    private CartManager cartManager;
    private ApiClient apiClient;
    private JFrame parentFrame;
    private NumberFormat currencyFormat;
    
    private JTextField nameField;
    private JTextField phoneField;
    private JTextArea addressArea;
    private ButtonGroup paymentGroup;
    private JTextField voucherField;
    private JLabel discountLabel;
    private JLabel totalLabel;
    private double discountAmount = 0;
    
    public CheckoutFrame(JFrame parent) {
        this.parentFrame = parent;
        this.cartManager = CartManager.getInstance();
        this.apiClient = ApiClient.getInstance();
        this.currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Thanh toán - Food Shop");
        setSize(600, 700);
        setLocationRelativeTo(parentFrame);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(new Color(240, 248, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JLabel titleLabel = new JLabel("📦 Thông tin giao hàng");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(14, 165, 233));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Form
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(14, 165, 233), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Name
        formPanel.add(createLabel("Họ và tên:"));
        nameField = new JTextField();
        nameField.setFont(new Font("Arial", Font.PLAIN, 14));
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        formPanel.add(nameField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Phone
        formPanel.add(createLabel("Số điện thoại:"));
        phoneField = new JTextField();
        phoneField.setFont(new Font("Arial", Font.PLAIN, 14));
        phoneField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        formPanel.add(phoneField);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Address
        formPanel.add(createLabel("Địa chỉ giao hàng:"));
        addressArea = new JTextArea(4, 20);
        addressArea.setFont(new Font("Arial", Font.PLAIN, 14));
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        formPanel.add(addressScroll);
        formPanel.add(Box.createVerticalStrut(15));
        
        // Payment method
        formPanel.add(createLabel("Phương thức thanh toán:"));
        formPanel.add(Box.createVerticalStrut(10));
        
        paymentGroup = new ButtonGroup();
        
        JRadioButton codRadio = new JRadioButton("💵 Thanh toán khi nhận hàng (COD)");
        codRadio.setFont(new Font("Arial", Font.PLAIN, 14));
        codRadio.setOpaque(false);
        codRadio.setActionCommand("COD");
        codRadio.setSelected(true);
        paymentGroup.add(codRadio);
        formPanel.add(codRadio);
        
        formPanel.add(Box.createVerticalStrut(10));
        
        JRadioButton bankRadio = new JRadioButton("🏦 Chuyển khoản ngân hàng");
        bankRadio.setFont(new Font("Arial", Font.PLAIN, 14));
        bankRadio.setOpaque(false);
        bankRadio.setActionCommand("Chuyển khoản");
        paymentGroup.add(bankRadio);
        formPanel.add(bankRadio);
        
        formPanel.add(Box.createVerticalStrut(20));
        
        // Order summary
        JPanel summaryPanel = new JPanel();
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setBackground(new Color(240, 249, 255));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        summaryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel summaryTitle = new JLabel("Tóm tắt đơn hàng");
        summaryTitle.setFont(new Font("Arial", Font.BOLD, 16));
        summaryTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryPanel.add(summaryTitle);
        summaryPanel.add(Box.createVerticalStrut(10));
        
        double subtotal = cartManager.getSubtotal();
        double shipping = 30000;
        double total = subtotal + shipping;
        
        summaryPanel.add(createSummaryRow("Tạm tính:", formatPrice(subtotal)));
        summaryPanel.add(createSummaryRow("Phí vận chuyển:", formatPrice(shipping)));
        
        // Voucher section
        summaryPanel.add(Box.createVerticalStrut(10));
        JPanel voucherPanel = new JPanel(new BorderLayout(10, 0));
        voucherPanel.setOpaque(false);
        voucherPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        voucherField = new JTextField();
        voucherField.setFont(new Font("Arial", Font.PLAIN, 13));
        voucherField.setToolTipText("Nhập mã giảm giá");
        
        ModernButton applyVoucherBtn = new ModernButton("Áp dụng");
        applyVoucherBtn.setPreferredSize(new Dimension(90, 35));
        applyVoucherBtn.setFont(new Font("Arial", Font.BOLD, 12));
        applyVoucherBtn.setColors(
            new Color(34, 197, 94),
            new Color(22, 163, 74),
            new Color(21, 128, 61)
        );
        applyVoucherBtn.addActionListener(e -> applyVoucher());
        
        voucherPanel.add(voucherField, BorderLayout.CENTER);
        voucherPanel.add(applyVoucherBtn, BorderLayout.EAST);
        summaryPanel.add(voucherPanel);
        
        // Discount row (initially hidden)
        discountLabel = new JLabel(formatPrice(0));
        discountLabel.setFont(new Font("Arial", Font.BOLD, 14));
        discountLabel.setForeground(new Color(34, 197, 94));
        JPanel discountRow = createSummaryRow("Giảm giá:", "");
        discountRow.add(discountLabel, BorderLayout.EAST);
        discountRow.setVisible(false);
        summaryPanel.add(discountRow);
        summaryPanel.add(Box.createVerticalStrut(5));
        
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        summaryPanel.add(sep);
        summaryPanel.add(Box.createVerticalStrut(5));
        
        JPanel totalPanel = createSummaryRow("Tổng cộng:", formatPrice(total));
        totalPanel.setBackground(new Color(224, 242, 254));
        ((JLabel) totalPanel.getComponent(0)).setFont(new Font("Arial", Font.BOLD, 16));
        
        totalLabel = new JLabel(formatPrice(total));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalLabel.setForeground(new Color(14, 165, 233));
        totalPanel.add(totalLabel, BorderLayout.EAST);
        
        summaryPanel.add(totalPanel);
        
        formPanel.add(summaryPanel);
        formPanel.add(Box.createVerticalStrut(20));
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        
        ModernButton cancelBtn = new ModernButton("Hủy");
        cancelBtn.setPreferredSize(new Dimension(120, 45));
        cancelBtn.setColors(
            new Color(156, 163, 175),
            new Color(107, 114, 128),
            new Color(75, 85, 99)
        );
        cancelBtn.addActionListener(e -> dispose());
        buttonPanel.add(cancelBtn);
        
        ModernButton confirmBtn = new ModernButton("Đặt hàng");
        confirmBtn.setPreferredSize(new Dimension(120, 45));
        confirmBtn.setFont(new Font("Arial", Font.BOLD, 14));
        confirmBtn.addActionListener(e -> handlePlaceOrder());
        buttonPanel.add(confirmBtn);
        
        formPanel.add(buttonPanel);
        
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    private JPanel createSummaryRow(String label, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(labelComp, BorderLayout.WEST);
        
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(valueComp, BorderLayout.EAST);
        
        return panel;
    }
    
    private String formatPrice(double price) {
        return currencyFormat.format(price) + "đ";
    }
    
    private void applyVoucher() {
        String voucherCode = voucherField.getText().trim();
        if (voucherCode.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập mã giảm giá!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!apiClient.isLoggedIn()) {
            JOptionPane.showMessageDialog(this,
                "Bạn cần đăng nhập để sử dụng mã giảm giá!",
                "Yêu cầu đăng nhập",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        double subtotal = cartManager.getSubtotal();
        double shipping = 30000;
        double total = subtotal + shipping;
        
        System.out.println("Applying voucher: " + voucherCode + ", total: " + total);
        
        SwingWorker<ApiClient.VoucherValidationResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected ApiClient.VoucherValidationResponse doInBackground() throws Exception {
                try {
                    return apiClient.validateVoucher(voucherCode, total);
                } catch (Exception e) {
                    System.err.println("Error validating voucher: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }
            
            @Override
            protected void done() {
                try {
                    ApiClient.VoucherValidationResponse response = get();
                    System.out.println("Voucher validation response: valid=" + response.isValid() + 
                                     ", discount=" + response.getDiscountAmount());
                    
                    if (response.isValid()) {
                        discountAmount = response.getDiscountAmount();
                        updateOrderSummary();
                        
                        JOptionPane.showMessageDialog(CheckoutFrame.this,
                            response.getMessage(),
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(CheckoutFrame.this,
                            response.getMessage(),
                            "Thông báo",
                            JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    System.err.println("Error in applyVoucher done(): " + ex.getMessage());
                    ex.printStackTrace();
                    
                    String errorMsg = ex.getMessage();
                    if (errorMsg == null || errorMsg.isEmpty()) {
                        errorMsg = "Không thể kết nối đến server";
                    }
                    
                    JOptionPane.showMessageDialog(CheckoutFrame.this,
                        errorMsg,
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void updateOrderSummary() {
        double subtotal = cartManager.getSubtotal();
        double shipping = 30000;
        double total = subtotal + shipping - discountAmount;
        
        // Update discount label
        if (discountAmount > 0) {
            discountLabel.setText("-" + formatPrice(discountAmount));
            discountLabel.getParent().setVisible(true);
        } else {
            discountLabel.getParent().setVisible(false);
        }
        
        // Update total
        totalLabel.setText(formatPrice(total));
    }
    
    private void handlePlaceOrder() {
        // Check if logged in
        if (!apiClient.isLoggedIn()) {
            int result = JOptionPane.showConfirmDialog(this,
                "Bạn cần đăng nhập để đặt hàng!\nBạn có muốn đăng nhập không?",
                "Yêu cầu đăng nhập",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
            
            if (result == JOptionPane.YES_OPTION) {
                // Show login dialog
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        // If logged in successfully, try to place order again
                        if (apiClient.isLoggedIn()) {
                            handlePlaceOrder();
                        }
                    }
                });
                loginFrame.setVisible(true);
            }
            return;
        }
        
        // Validate
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressArea.getText().trim();
        
        if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng điền đầy đủ thông tin!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String paymentMethod = paymentGroup.getSelection().getActionCommand();
        String shippingAddress = String.format("Tên: %s\nSĐT: %s\nĐịa chỉ: %s", name, phone, address);
        
        // Create order request
        List<ApiClient.OrderItemRequest> items = new ArrayList<>();
        for (CartManager.CartItem cartItem : cartManager.getCartItems()) {
            items.add(new ApiClient.OrderItemRequest(
                cartItem.getProduct().getId(),
                cartItem.getQuantity()
            ));
        }
        
        ApiClient.OrderRequest orderRequest = new ApiClient.OrderRequest(
            shippingAddress,
            paymentMethod,
            items
        );
        
        // Add voucher if applied
        String voucherCode = voucherField.getText().trim();
        if (!voucherCode.isEmpty() && discountAmount > 0) {
            orderRequest.setVoucherCode(voucherCode);
        }
        
        // Show loading
        JDialog loadingDialog = new JDialog(this, "Đang xử lý...", true);
        JPanel loadingPanel = new JPanel();
        loadingPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        loadingPanel.add(new JLabel("Đang đặt hàng..."));
        loadingDialog.add(loadingPanel);
        loadingDialog.pack();
        loadingDialog.setLocationRelativeTo(this);
        
        SwingWorker<ApiClient.OrderDTO, Void> worker = new SwingWorker<>() {
            @Override
            protected ApiClient.OrderDTO doInBackground() throws Exception {
                return apiClient.createOrder(orderRequest);
            }
            
            @Override
            protected void done() {
                loadingDialog.dispose();
                try {
                    ApiClient.OrderDTO order = get();
                    cartManager.clearCart();
                    
                    JOptionPane.showMessageDialog(CheckoutFrame.this,
                        "Đặt hàng thành công!\nMã đơn hàng: #" + order.getId(),
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    dispose();
                    if (parentFrame != null) {
                        parentFrame.dispose();
                    }
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(CheckoutFrame.this,
                        "Đặt hàng thất bại!\n" + ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
        loadingDialog.setVisible(true);
    }
}
