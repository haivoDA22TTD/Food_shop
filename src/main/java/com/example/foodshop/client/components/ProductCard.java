package com.example.foodshop.client.components;

import com.example.foodshop.client.api.ApiClient;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;

public class ProductCard extends JPanel {
    
    private ApiClient.ProductDTO product;
    private boolean isHovered = false;
    private float shadowOpacity = 0.1f;
    private JLabel imageLabel;
    
    public ProductCard(ApiClient.ProductDTO product) {
        this.product = product;
        initComponents();
        addHoverEffect();
        loadImage();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        setPreferredSize(new Dimension(280, 380));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Image panel
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setOpaque(false);
        imagePanel.setPreferredSize(new Dimension(260, 200));
        imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(240, 249, 255));
        
        // Loading placeholder
        imageLabel.setText("⏳");
        imageLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        imageLabel.setForeground(new Color(14, 165, 233));
        
        imagePanel.add(imageLabel, BorderLayout.CENTER);
        add(imagePanel, BorderLayout.NORTH);
        
        // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(5, 10, 10, 10));
        
        // Category badge
        JLabel categoryLabel = new JLabel(product.getCategory());
        categoryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        categoryLabel.setForeground(new Color(6, 182, 212));
        categoryLabel.setOpaque(true);
        categoryLabel.setBackground(new Color(6, 182, 212, 25));
        categoryLabel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        categoryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(categoryLabel);
        
        infoPanel.add(Box.createVerticalStrut(8));
        
        // Product name
        JLabel nameLabel = new JLabel(product.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(new Color(17, 24, 39));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(nameLabel);
        
        infoPanel.add(Box.createVerticalStrut(5));
        
        // Description
        JLabel descLabel = new JLabel("<html>" + truncate(product.getDescription(), 50) + "</html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(107, 114, 128));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(descLabel);
        
        infoPanel.add(Box.createVerticalStrut(10));
        
        // Price and stock panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        JLabel priceLabel = new JLabel(currencyFormat.format(product.getPrice()) + "đ");
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        priceLabel.setForeground(new Color(14, 165, 233));
        bottomPanel.add(priceLabel, BorderLayout.WEST);
        
        JLabel stockLabel = new JLabel("Còn " + product.getStock());
        stockLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        stockLabel.setForeground(product.getStock() > 10 ? new Color(34, 197, 94) : new Color(239, 68, 68));
        bottomPanel.add(stockLabel, BorderLayout.EAST);
        
        infoPanel.add(bottomPanel);
        
        add(infoPanel, BorderLayout.CENTER);
    }
    
    private void loadImage() {
        new Thread(() -> {
            try {
                String imageUrl = product.getImage();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    URL url = new URL(imageUrl);
                    BufferedImage img = ImageIO.read(url);
                    if (img != null) {
                        // Scale image to fit
                        Image scaledImg = img.getScaledInstance(240, 180, Image.SCALE_SMOOTH);
                        SwingUtilities.invokeLater(() -> {
                            imageLabel.setIcon(new ImageIcon(scaledImg));
                            imageLabel.setText(null);
                            imageLabel.setBackground(Color.WHITE);
                        });
                    } else {
                        setFallbackIcon();
                    }
                } else {
                    setFallbackIcon();
                }
            } catch (Exception e) {
                setFallbackIcon();
            }
        }).start();
    }
    
    private void setFallbackIcon() {
        SwingUtilities.invokeLater(() -> {
            imageLabel.setText("🍽️");
            imageLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 64));
            imageLabel.setForeground(new Color(14, 165, 233));
            imageLabel.setBackground(new Color(240, 249, 255));
        });
    }
    
    private void addHoverEffect() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                animateShadow(0.25f);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                animateShadow(0.1f);
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }
    
    private void animateShadow(float targetOpacity) {
        Timer timer = new Timer(20, null);
        final float delta = (targetOpacity - shadowOpacity) / 10;
        
        timer.addActionListener(e -> {
            shadowOpacity += delta;
            if ((delta > 0 && shadowOpacity >= targetOpacity) || 
                (delta < 0 && shadowOpacity <= targetOpacity)) {
                shadowOpacity = targetOpacity;
                ((Timer)e.getSource()).stop();
            }
            repaint();
        });
        
        timer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw shadow
        int shadowSize = 8;
        g2.setColor(new Color(0, 0, 0, (int)(shadowOpacity * 255)));
        g2.fill(new RoundRectangle2D.Float(
            shadowSize, shadowSize, 
            getWidth() - shadowSize * 2, 
            getHeight() - shadowSize * 2, 
            16, 16
        ));
        
        // Draw card background
        g2.setColor(Color.WHITE);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
        
        // Draw border
        g2.setColor(new Color(229, 231, 235));
        g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
        
        g2.dispose();
        super.paintComponent(g);
    }
    
    private String truncate(String text, int length) {
        if (text == null) return "";
        return text.length() > length ? text.substring(0, length) + "..." : text;
    }
    
    public ApiClient.ProductDTO getProduct() {
        return product;
    }
}
