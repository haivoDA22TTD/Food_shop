package com.example.foodshop.client.ui;

import com.example.foodshop.client.api.ApiClient;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MainFrame extends JFrame {
    
    private String username;
    private String role;
    private ApiClient apiClient;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JLabel userLabel;
    private JLabel statusLabel;
    
    public MainFrame(String username, String role) {
        this.username = username;
        this.role = role;
        this.apiClient = ApiClient.getInstance();
        initComponents();
        loadProducts();
    }
    
    private void initComponents() {
        setTitle("Food Shop - Quản lý sản phẩm");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Set to maximized state
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1024, 768));
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(240, 248, 255));
        
        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(14, 165, 233)),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        // Title
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("🍔 Food Shop");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(new Color(14, 165, 233));
        
        JLabel subtitleLabel = new JLabel("Danh sách sản phẩm");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        
        topPanel.add(titlePanel, BorderLayout.WEST);
        
        // User info
        userLabel = new JLabel("👤 " + username + " (" + role + ")");
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
        userLabel.setForeground(new Color(14, 165, 233));
        topPanel.add(userLabel, BorderLayout.EAST);
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        // Table
        String[] columns = {"ID", "Tên sản phẩm", "Mô tả", "Giá", "Danh mục", "Tồn kho", "Hình ảnh"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        productTable = new JTable(tableModel);
        productTable.setRowHeight(40);
        productTable.setFont(new Font("Arial", Font.PLAIN, 13));
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setShowGrid(true);
        productTable.setGridColor(new Color(230, 230, 230));
        
        // Header styling
        JTableHeader header = productTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 13));
        header.setBackground(new Color(14, 165, 233));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 45));
        
        // Center align for numeric columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        productTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        productTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        
        // Column widths
        productTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        productTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        productTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        productTable.getColumnModel().getColumn(6).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(null);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        
        // Bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(15, 30, 15, 30)
        ));
        
        // Status label
        statusLabel = new JLabel("Sẵn sàng");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(Color.GRAY);
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton refreshButton = new JButton("🔄 Làm mới");
        refreshButton.setFont(new Font("Arial", Font.BOLD, 13));
        refreshButton.setPreferredSize(new Dimension(130, 40));
        refreshButton.setBackground(new Color(34, 197, 94));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> loadProducts());
        buttonPanel.add(refreshButton);
        
        JButton logoutButton = new JButton("🚪 Đăng xuất");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 13));
        logoutButton.setPreferredSize(new Dimension(130, 40));
        logoutButton.setBackground(new Color(239, 68, 68));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> handleLogout());
        buttonPanel.add(logoutButton);
        
        bottomPanel.add(buttonPanel, BorderLayout.EAST);
        
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private void loadProducts() {
        statusLabel.setText("Đang tải dữ liệu...");
        statusLabel.setForeground(Color.BLUE);
        
        SwingWorker<List<ApiClient.ProductDTO>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ApiClient.ProductDTO> doInBackground() throws Exception {
                return apiClient.getAllProducts();
            }
            
            @Override
            protected void done() {
                try {
                    List<ApiClient.ProductDTO> products = get();
                    updateTable(products);
                    statusLabel.setText("Đã tải " + products.size() + " sản phẩm");
                    statusLabel.setForeground(new Color(34, 197, 94));
                } catch (Exception ex) {
                    statusLabel.setText("Lỗi: " + ex.getMessage());
                    statusLabel.setForeground(Color.RED);
                    
                    JOptionPane.showMessageDialog(MainFrame.this,
                        "Không thể tải danh sách sản phẩm!\n" + ex.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void updateTable(List<ApiClient.ProductDTO> products) {
        tableModel.setRowCount(0);
        
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        
        for (ApiClient.ProductDTO product : products) {
            String imageType = "Chưa có";
            if (product.getImage() != null) {
                imageType = product.getImage().startsWith("http") ? "☁️ Cloudinary" : "💾 Local";
            }
            
            Object[] row = {
                product.getId(),
                product.getName(),
                product.getDescription(),
                currencyFormat.format(product.getPrice()) + "đ",
                product.getCategory(),
                product.getStock(),
                imageType
            };
            tableModel.addRow(row);
        }
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
