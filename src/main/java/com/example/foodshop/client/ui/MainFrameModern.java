package com.example.foodshop.client.ui;

import com.example.foodshop.client.api.ApiClient;
import com.example.foodshop.client.components.ChatbotPanel;
import com.example.foodshop.client.components.ModernButton;
import com.example.foodshop.client.components.ProductCard;
import com.example.foodshop.client.util.CartManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainFrameModern extends JFrame implements CartManager.CartChangeListener {
    
    private String username;
    private String role;
    private ApiClient apiClient;
    private CartManager cartManager;
    private JPanel productGridPanel;
    private JLabel statusLabel;
    private JLabel cartCountLabel;
    
    public MainFrameModern(String username, String role) {
        this.username = username;
        this.role = role;
        this.apiClient = ApiClient.getInstance();
        this.cartManager = CartManager.getInstance();
        this.cartManager.addListener(this);
        initComponents();
        loadProducts();
    }
    
    private boolean isLoggedIn() {
        return username != null && apiClient.isLoggedIn();
    }
    
    private void initComponents() {
        setTitle("Food Shop - Cửa hàng thực phẩm");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1024, 768));
        
        // Main container
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.setBackground(new Color(240, 248, 255));
        
        // Header (Navbar)
        JPanel header = createHeader();
        mainContainer.add(header, BorderLayout.NORTH);
        
        // Content area with scroll
        JScrollPane scrollPane = new JScrollPane(createContentPanel());
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainContainer.add(scrollPane, BorderLayout.CENTER);
        
        add(mainContainer);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(14, 165, 233)),
            BorderFactory.createEmptyBorder(15, 30, 15, 30)
        ));
        
        // Logo and title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);
        
        JLabel logoLabel = new JLabel("🍔");
        logoLabel.setFont(new Font("Arial", Font.PLAIN, 32));
        leftPanel.add(logoLabel);
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Food Shop");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(14, 165, 233));
        
        statusLabel = new JLabel("Đang tải...");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(Color.GRAY);
        
        titlePanel.add(titleLabel);
        titlePanel.add(statusLabel);
        leftPanel.add(titlePanel);
        
        header.add(leftPanel, BorderLayout.WEST);
        
        // Right panel (user info and buttons)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        
        // User info or login button
        if (isLoggedIn()) {
            JLabel userLabel = new JLabel("👤 " + username + " (" + role + ")");
            userLabel.setFont(new Font("Arial", Font.BOLD, 14));
            userLabel.setForeground(new Color(14, 165, 233));
            rightPanel.add(userLabel);
        } else {
            ModernButton loginBtn = new ModernButton("🔐 Đăng nhập");
            loginBtn.setFont(new Font("Arial", Font.BOLD, 13));
            loginBtn.setForeground(Color.WHITE);
            loginBtn.setPreferredSize(new Dimension(130, 40));
            loginBtn.setColors(
                new Color(34, 197, 94),
                new Color(22, 163, 74),
                new Color(21, 128, 61)
            );
            loginBtn.addActionListener(e -> showLoginDialog());
            rightPanel.add(loginBtn);
        }
        
        // Cart button with count
        ModernButton cartBtn = new ModernButton("🛒 Giỏ hàng");
        cartBtn.setFont(new Font("Arial", Font.BOLD, 13));
        cartBtn.setForeground(Color.WHITE);
        cartBtn.setPreferredSize(new Dimension(140, 40));
        cartBtn.setColors(
            new Color(14, 165, 233),
            new Color(6, 182, 212),
            new Color(8, 145, 178)
        );
        
        // Add cart count badge
        JPanel cartPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        cartPanel.setOpaque(false);
        cartPanel.add(cartBtn);
        
        cartCountLabel = new JLabel("0");
        cartCountLabel.setFont(new Font("Arial", Font.BOLD, 11));
        cartCountLabel.setForeground(Color.WHITE);
        cartCountLabel.setOpaque(true);
        cartCountLabel.setBackground(new Color(239, 68, 68));
        cartCountLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        cartCountLabel.setPreferredSize(new Dimension(24, 20));
        cartCountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel badgePanel = new JPanel(null);
        badgePanel.setOpaque(false);
        badgePanel.setPreferredSize(new Dimension(140, 40));
        cartBtn.setBounds(0, 0, 140, 40);
        cartCountLabel.setBounds(110, 5, 24, 20);
        badgePanel.add(cartBtn);
        badgePanel.add(cartCountLabel);
        
        cartBtn.addActionListener(e -> openCart());
        rightPanel.add(badgePanel);
        
        // Orders button (only show if logged in)
        if (isLoggedIn()) {
            ModernButton ordersBtn = new ModernButton("📦 Đơn hàng");
            ordersBtn.setFont(new Font("Arial", Font.BOLD, 13));
            ordersBtn.setForeground(Color.WHITE);
            ordersBtn.setPreferredSize(new Dimension(130, 40));
            ordersBtn.setColors(
                new Color(139, 92, 246),
                new Color(124, 58, 237),
                new Color(109, 40, 217)
            );
            ordersBtn.addActionListener(e -> openOrders());
            rightPanel.add(ordersBtn);
        }
        
        // Chatbot button
        ModernButton chatbotBtn = new ModernButton("🤖 AI Trợ lý");
        chatbotBtn.setFont(new Font("Arial", Font.BOLD, 13));
        chatbotBtn.setForeground(Color.WHITE);
        chatbotBtn.setPreferredSize(new Dimension(130, 40));
        chatbotBtn.setColors(
            new Color(236, 72, 153),
            new Color(219, 39, 119),
            new Color(190, 24, 93)
        );
        chatbotBtn.addActionListener(e -> openChatbot());
        rightPanel.add(chatbotBtn);
        
        ModernButton refreshBtn = new ModernButton("🔄 Làm mới");
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 13));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setPreferredSize(new Dimension(130, 40));
        refreshBtn.setColors(
            new Color(34, 197, 94),
            new Color(22, 163, 74),
            new Color(21, 128, 61)
        );
        refreshBtn.addActionListener(e -> loadProducts());
        rightPanel.add(refreshBtn);
        
        // Logout button (only show if logged in)
        if (isLoggedIn()) {
            ModernButton logoutBtn = new ModernButton("🚪 Đăng xuất");
            logoutBtn.setFont(new Font("Arial", Font.BOLD, 13));
            logoutBtn.setForeground(Color.WHITE);
            logoutBtn.setPreferredSize(new Dimension(130, 40));
            logoutBtn.setColors(
                new Color(239, 68, 68),
                new Color(220, 38, 38),
                new Color(185, 28, 28)
            );
            logoutBtn.addActionListener(e -> handleLogout());
            rightPanel.add(logoutBtn);
        }
        
        header.add(rightPanel, BorderLayout.EAST);
        
        updateCartCount();
        
        return header;
    }
    
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                // Sky blue gradient like web
                GradientPaint gp = new GradientPaint(0, 0, new Color(240, 249, 255), 0, h, new Color(224, 242, 254));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Section title
        JPanel titleSection = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titleSection.setOpaque(false);
        
        JLabel sectionTitle = new JLabel("Sản phẩm nổi bật");
        sectionTitle.setFont(new Font("Arial", Font.BOLD, 28));
        sectionTitle.setForeground(Color.BLACK);
        titleSection.add(sectionTitle);
        
        contentPanel.add(titleSection, BorderLayout.NORTH);
        
        // Product grid
        productGridPanel = new JPanel(new GridLayout(0, 4, 20, 20));
        productGridPanel.setOpaque(false);
        
        contentPanel.add(productGridPanel, BorderLayout.CENTER);
        
        return contentPanel;
    }
    
    private void loadProducts() {
        statusLabel.setText("Đang tải...");
        statusLabel.setForeground(new Color(14, 165, 233));
        productGridPanel.removeAll();
        productGridPanel.revalidate();
        productGridPanel.repaint();
        
        SwingWorker<List<ApiClient.ProductDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ApiClient.ProductDTO> doInBackground() throws Exception {
                return apiClient.getAllProducts();
            }
            
            @Override
            protected void done() {
                try {
                    List<ApiClient.ProductDTO> products = get();
                    displayProducts(products);
                    statusLabel.setText(products.size() + " sản phẩm");
                    statusLabel.setForeground(new Color(34, 197, 94));
                } catch (Exception ex) {
                    statusLabel.setText("Lỗi: " + ex.getMessage());
                    statusLabel.setForeground(Color.RED);
                    
                    JOptionPane.showMessageDialog(MainFrameModern.this,
                        "Không thể tải danh sách sản phẩm!\n" + ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void displayProducts(List<ApiClient.ProductDTO> products) {
        productGridPanel.removeAll();
        
        // Adjust grid columns based on window width
        int width = getWidth();
        int columns = Math.max(2, Math.min(5, width / 320));
        productGridPanel.setLayout(new GridLayout(0, columns, 20, 20));
        
        for (ApiClient.ProductDTO product : products) {
            ProductCard card = new ProductCard(product);
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    showProductDetail(product);
                }
            });
            productGridPanel.add(card);
        }
        
        productGridPanel.revalidate();
        productGridPanel.repaint();
        
        // Animate cards
        Timer timer = new Timer(50, null);
        final int[] index = {0};
        timer.addActionListener(e -> {
            if (index[0] < productGridPanel.getComponentCount()) {
                Component comp = productGridPanel.getComponent(index[0]);
                comp.setVisible(true);
                index[0]++;
            } else {
                timer.stop();
            }
        });
        
        // Hide all cards first
        for (Component comp : productGridPanel.getComponents()) {
            comp.setVisible(false);
        }
        
        timer.start();
    }
    
    private void showProductDetail(ApiClient.ProductDTO product) {
        ProductDetailFrame detailFrame = new ProductDetailFrame(product);
        detailFrame.setVisible(true);
    }
    
    private void openCart() {
        CartFrame cartFrame = new CartFrame();
        cartFrame.setVisible(true);
    }
    
    private void openOrders() {
        if (!isLoggedIn()) {
            int result = JOptionPane.showConfirmDialog(this,
                "Bạn cần đăng nhập để xem đơn hàng!\nBạn có muốn đăng nhập không?",
                "Yêu cầu đăng nhập",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
            
            if (result == JOptionPane.YES_OPTION) {
                showLoginDialog();
            }
            return;
        }
        
        OrdersFrame ordersFrame = new OrdersFrame();
        ordersFrame.setVisible(true);
    }
    
    private void openChatbot() {
        JDialog chatDialog = new JDialog(this, "🤖 AI Trợ lý - Food Shop", false);
        chatDialog.setSize(450, 600);
        chatDialog.setLocationRelativeTo(this);
        chatDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        ChatbotPanel chatbotPanel = new ChatbotPanel();
        chatDialog.add(chatbotPanel);
        
        chatDialog.setVisible(true);
    }
    
    private void showLoginDialog() {
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                // Refresh main frame after login
                if (apiClient.isLoggedIn()) {
                    dispose();
                    String user = loginFrame.getLoggedInUsername();
                    String userRole = loginFrame.getLoggedInRole();
                    MainFrameModern newFrame = new MainFrameModern(user, userRole);
                    newFrame.setVisible(true);
                }
            }
        });
        loginFrame.setVisible(true);
    }
    
    private void updateCartCount() {
        int count = cartManager.getTotalItems();
        cartCountLabel.setText(String.valueOf(count));
        cartCountLabel.setVisible(count > 0);
    }
    
    @Override
    public void onCartChanged() {
        SwingUtilities.invokeLater(this::updateCartCount);
    }
    
    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn đăng xuất?",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            apiClient.setJwtToken(null);
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            dispose();
        }
    }
}
