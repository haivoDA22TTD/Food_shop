package com.example.foodshop.client.ui;

import com.example.foodshop.client.api.ApiClient;
import com.example.foodshop.client.components.ModernButton;
import com.example.foodshop.client.util.CartManager;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProductDetailFrame extends JFrame {
    
    private ApiClient apiClient;
    private ApiClient.ProductDTO product;
    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;
    private JPanel reviewsPanel;
    
    public ProductDetailFrame(ApiClient.ProductDTO product) {
        this.apiClient = ApiClient.getInstance();
        this.product = product;
        this.currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        initComponents();
        loadReviews();
    }
    
    private void initComponents() {
        setTitle(product.getName() + " - Food Shop");
        setSize(900, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(new Color(240, 248, 255));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Product info panel
        JPanel productPanel = new JPanel(new BorderLayout(20, 20));
        productPanel.setBackground(Color.WHITE);
        productPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(14, 165, 233), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Image
        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(300, 300));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadProductImage(imageLabel);
        productPanel.add(imageLabel, BorderLayout.WEST);
        
        // Info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(product.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel categoryLabel = new JLabel(product.getCategory());
        categoryLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        categoryLabel.setForeground(Color.GRAY);
        categoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel priceLabel = new JLabel(formatPrice(product.getPrice()));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 28));
        priceLabel.setForeground(new Color(14, 165, 233));
        priceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JTextArea descArea = new JTextArea(product.getDescription());
        descArea.setFont(new Font("Arial", Font.PLAIN, 13));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setOpaque(false);
        descArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel stockLabel = new JLabel("Còn " + product.getStock() + " sản phẩm");
        stockLabel.setFont(new Font("Arial", Font.BOLD, 13));
        stockLabel.setForeground(product.getStock() > 0 ? new Color(34, 197, 94) : Color.RED);
        stockLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        ModernButton addToCartBtn = new ModernButton("🛒 Thêm vào giỏ hàng");
        addToCartBtn.setPreferredSize(new Dimension(200, 45));
        addToCartBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addToCartBtn.setEnabled(product.getStock() > 0);
        addToCartBtn.addActionListener(e -> addToCart());
        
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(categoryLabel);
        infoPanel.add(Box.createVerticalStrut(15));
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createVerticalStrut(15));
        infoPanel.add(descArea);
        infoPanel.add(Box.createVerticalStrut(15));
        infoPanel.add(stockLabel);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(addToCartBtn);
        
        productPanel.add(infoPanel, BorderLayout.CENTER);
        
        mainPanel.add(productPanel, BorderLayout.NORTH);
        
        // Reviews section
        JPanel reviewsSection = new JPanel(new BorderLayout(10, 10));
        reviewsSection.setBackground(Color.WHITE);
        reviewsSection.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(14, 165, 233), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel reviewsTitle = new JLabel("📝 Đánh giá sản phẩm");
        reviewsTitle.setFont(new Font("Arial", Font.BOLD, 20));
        reviewsTitle.setForeground(new Color(14, 165, 233));
        reviewsSection.add(reviewsTitle, BorderLayout.NORTH);
        
        reviewsPanel = new JPanel();
        reviewsPanel.setLayout(new BoxLayout(reviewsPanel, BoxLayout.Y_AXIS));
        reviewsPanel.setBackground(Color.WHITE);
        
        JScrollPane reviewsScroll = new JScrollPane(reviewsPanel);
        reviewsScroll.setBorder(null);
        reviewsScroll.getVerticalScrollBar().setUnitIncrement(16);
        reviewsSection.add(reviewsScroll, BorderLayout.CENTER);
        
        mainPanel.add(reviewsSection, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private void loadProductImage(JLabel imageLabel) {
        SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                String imageUrl = product.getImage();
                if (imageUrl != null && imageUrl.startsWith("http")) {
                    try {
                        URL url = new URL(imageUrl);
                        BufferedImage img = ImageIO.read(url);
                        if (img != null) {
                            Image scaledImg = img.getScaledInstance(300, 300, Image.SCALE_SMOOTH);
                            return new ImageIcon(scaledImg);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to load image: " + e.getMessage());
                    }
                }
                return new ImageIcon(new BufferedImage(300, 300, BufferedImage.TYPE_INT_RGB));
            }
            
            @Override
            protected void done() {
                try {
                    imageLabel.setIcon(get());
                } catch (Exception e) {
                    imageLabel.setText("Không có ảnh");
                }
            }
        };
        worker.execute();
    }
    
    private void loadReviews() {
        reviewsPanel.removeAll();
        
        JLabel loadingLabel = new JLabel("Đang tải đánh giá...");
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        reviewsPanel.add(loadingLabel);
        reviewsPanel.revalidate();
        reviewsPanel.repaint();
        
        SwingWorker<List<ApiClient.ReviewDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ApiClient.ReviewDTO> doInBackground() throws Exception {
                return apiClient.getProductReviews(product.getId());
            }
            
            @Override
            protected void done() {
                try {
                    List<ApiClient.ReviewDTO> reviews = get();
                    displayReviews(reviews);
                } catch (Exception ex) {
                    reviewsPanel.removeAll();
                    JLabel errorLabel = new JLabel("❌ Không thể tải đánh giá");
                    errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    reviewsPanel.add(errorLabel);
                    reviewsPanel.revalidate();
                    reviewsPanel.repaint();
                }
            }
        };
        worker.execute();
    }
    
    private void displayReviews(List<ApiClient.ReviewDTO> reviews) {
        reviewsPanel.removeAll();
        
        if (reviews.isEmpty()) {
            JLabel emptyLabel = new JLabel("Chưa có đánh giá nào cho sản phẩm này");
            emptyLabel.setFont(new Font("Arial", Font.ITALIC, 14));
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            reviewsPanel.add(Box.createVerticalStrut(20));
            reviewsPanel.add(emptyLabel);
        } else {
            // Calculate average rating
            double avgRating = reviews.stream()
                    .mapToInt(ApiClient.ReviewDTO::getRating)
                    .average()
                    .orElse(0.0);
            
            JPanel avgPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            avgPanel.setOpaque(false);
            avgPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            
            JLabel avgLabel = new JLabel(String.format("%.1f/5", avgRating));
            avgLabel.setFont(new Font("Arial", Font.BOLD, 20));
            avgLabel.setForeground(new Color(251, 191, 36));
            
            JLabel starsLabel = new JLabel(getStarsText((int) Math.round(avgRating)));
            starsLabel.setFont(new Font("Arial", Font.PLAIN, 20));
            starsLabel.setForeground(new Color(251, 191, 36));
            
            JLabel countLabel = new JLabel("(" + reviews.size() + " đánh giá)");
            countLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            countLabel.setForeground(Color.GRAY);
            
            avgPanel.add(avgLabel);
            avgPanel.add(Box.createHorizontalStrut(10));
            avgPanel.add(starsLabel);
            avgPanel.add(Box.createHorizontalStrut(10));
            avgPanel.add(countLabel);
            
            reviewsPanel.add(avgPanel);
            reviewsPanel.add(Box.createVerticalStrut(15));
            
            // Reviews list
            for (ApiClient.ReviewDTO review : reviews) {
                reviewsPanel.add(createReviewCard(review));
                reviewsPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        reviewsPanel.revalidate();
        reviewsPanel.repaint();
    }
    
    private JPanel createReviewCard(ApiClient.ReviewDTO review) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(248, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        // Header with user and date
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        JLabel userLabel = new JLabel("👤 " + review.getUser().getUsername());
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        headerPanel.add(userLabel, BorderLayout.WEST);
        
        JLabel dateLabel = new JLabel(formatDate(review.getCreatedAt()));
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        dateLabel.setForeground(Color.GRAY);
        headerPanel.add(dateLabel, BorderLayout.EAST);
        
        card.add(headerPanel);
        card.add(Box.createVerticalStrut(10));
        
        // Stars rating
        JLabel starsLabel = new JLabel(getStarsText(review.getRating()));
        starsLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        starsLabel.setForeground(new Color(251, 191, 36));
        starsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(starsLabel);
        card.add(Box.createVerticalStrut(10));
        
        // Comment text
        JTextArea commentArea = new JTextArea(review.getComment());
        commentArea.setFont(new Font("Arial", Font.PLAIN, 13));
        commentArea.setForeground(new Color(55, 65, 81));
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        commentArea.setEditable(false);
        commentArea.setOpaque(false);
        commentArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        commentArea.setBorder(null);
        card.add(commentArea);
        
        return card;
    }
    
    private String getStarsText(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            stars.append("★");
        }
        for (int i = rating; i < 5; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }
    
    private void addToCart() {
        CartManager.getInstance().addToCart(product, 1);
        JOptionPane.showMessageDialog(this,
            "Đã thêm " + product.getName() + " vào giỏ hàng!",
            "Thành công",
            JOptionPane.INFORMATION_MESSAGE);
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
}
