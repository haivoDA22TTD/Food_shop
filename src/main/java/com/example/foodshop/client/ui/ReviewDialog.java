package com.example.foodshop.client.ui;

import com.example.foodshop.client.api.ApiClient;
import com.example.foodshop.client.components.ModernButton;

import javax.swing.*;
import java.awt.*;

public class ReviewDialog extends JDialog {
    
    private ApiClient apiClient;
    private Long productId;
    private Long orderId;
    private String productName;
    private int selectedRating = 0;
    private JTextArea commentArea;
    private JLabel[] stars;
    
    public ReviewDialog(JFrame parent, Long productId, Long orderId, String productName) {
        super(parent, "Đánh giá sản phẩm", true);
        this.apiClient = ApiClient.getInstance();
        this.productId = productId;
        this.orderId = orderId;
        this.productName = productName;
        
        initComponents();
    }
    
    private void initComponents() {
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("⭐ Đánh giá sản phẩm");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(14, 165, 233));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel productLabel = new JLabel(productName);
        productLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        productLabel.setForeground(Color.GRAY);
        productLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(10));
        headerPanel.add(productLabel);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        
        // Star rating
        JLabel ratingLabel = new JLabel("Chọn số sao:");
        ratingLabel.setFont(new Font("Arial", Font.BOLD, 14));
        ratingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(ratingLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        
        JPanel starsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        starsPanel.setOpaque(false);
        stars = new JLabel[5];
        
        for (int i = 0; i < 5; i++) {
            final int rating = i + 1;
            stars[i] = new JLabel("☆");
            stars[i].setFont(new Font("Arial", Font.PLAIN, 40));
            stars[i].setForeground(Color.LIGHT_GRAY);
            stars[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            stars[i].addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    selectedRating = rating;
                    updateStars();
                }
                
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    for (int j = 0; j < rating; j++) {
                        stars[j].setText("★");
                        stars[j].setForeground(new Color(251, 191, 36));
                    }
                    for (int j = rating; j < 5; j++) {
                        stars[j].setText("☆");
                        stars[j].setForeground(Color.LIGHT_GRAY);
                    }
                }
                
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    updateStars();
                }
            });
            
            starsPanel.add(stars[i]);
        }
        
        contentPanel.add(starsPanel);
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Comment
        JLabel commentLabel = new JLabel("Nhận xét của bạn:");
        commentLabel.setFont(new Font("Arial", Font.BOLD, 14));
        commentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(commentLabel);
        contentPanel.add(Box.createVerticalStrut(10));
        
        commentArea = new JTextArea(5, 30);
        commentArea.setFont(new Font("Arial", Font.PLAIN, 13));
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(224, 242, 254), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JScrollPane scrollPane = new JScrollPane(commentArea);
        scrollPane.setBorder(null);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(scrollPane);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        
        ModernButton submitBtn = new ModernButton("Gửi đánh giá");
        submitBtn.setPreferredSize(new Dimension(150, 40));
        submitBtn.setColors(
            new Color(14, 165, 233),
            new Color(6, 182, 212),
            new Color(8, 145, 178)
        );
        submitBtn.addActionListener(e -> submitReview());
        
        ModernButton cancelBtn = new ModernButton("Hủy");
        cancelBtn.setPreferredSize(new Dimension(100, 40));
        cancelBtn.setColors(
            new Color(156, 163, 175),
            new Color(107, 114, 128),
            new Color(75, 85, 99)
        );
        cancelBtn.addActionListener(e -> dispose());
        
        buttonPanel.add(submitBtn);
        buttonPanel.add(cancelBtn);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void updateStars() {
        for (int i = 0; i < 5; i++) {
            if (i < selectedRating) {
                stars[i].setText("★");
                stars[i].setForeground(new Color(251, 191, 36));
            } else {
                stars[i].setText("☆");
                stars[i].setForeground(Color.LIGHT_GRAY);
            }
        }
    }
    
    private void submitReview() {
        if (selectedRating == 0) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng chọn số sao đánh giá!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String comment = commentArea.getText().trim();
        if (comment.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập nhận xét!",
                "Thông báo",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Show loading
        JDialog loadingDialog = new JDialog(this, "Đang gửi...", true);
        JPanel loadingPanel = new JPanel();
        loadingPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        loadingPanel.add(new JLabel("Đang gửi đánh giá..."));
        loadingDialog.add(loadingPanel);
        loadingDialog.pack();
        loadingDialog.setLocationRelativeTo(this);
        
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                ApiClient.ReviewRequest request = new ApiClient.ReviewRequest(
                    productId, orderId, selectedRating, comment
                );
                return apiClient.createReview(request);
            }
            
            @Override
            protected void done() {
                loadingDialog.dispose();
                try {
                    get();
                    JOptionPane.showMessageDialog(ReviewDialog.this,
                        "Đánh giá thành công!\nCảm ơn bạn đã đánh giá sản phẩm.",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ReviewDialog.this,
                        "Lỗi: " + ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
        loadingDialog.setVisible(true);
    }
}
