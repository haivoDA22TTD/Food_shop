package com.example.foodshop.client.ui;

import com.example.foodshop.client.api.ApiClient;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private ApiClient apiClient;
    private String loggedInUsername;
    private String loggedInRole;
    
    public LoginFrame() {
        this.apiClient = ApiClient.getInstance();
        initComponents();
    }
    
    public String getLoggedInUsername() {
        return loggedInUsername;
    }
    
    public String getLoggedInRole() {
        return loggedInRole;
    }
    
    private void initComponents() {
        setTitle("Food Shop - Đăng nhập");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Set to maximized state but allow resize
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(800, 600));
        
        // Main panel with gradient background
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(new Color(240, 248, 255));
        
        // Center panel (login form container)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout(10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setPreferredSize(new Dimension(500, 450));
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(14, 165, 233), 2),
            BorderFactory.createEmptyBorder(40, 40, 40, 40)
        ));
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("🍔 Food Shop");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(new Color(14, 165, 233));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Ứng dụng quản lý cửa hàng");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(10));
        titlePanel.add(subtitleLabel);
        
        centerPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel usernameLabel = new JLabel("Tên đăng nhập:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(0, 40));
        formPanel.add(usernameField, gbc);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        JLabel passwordLabel = new JLabel("Mật khẩu:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(0, 40));
        formPanel.add(passwordField, gbc);
        
        // Login button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(25, 10, 10, 10);
        loginButton = new JButton("Đăng nhập");
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setPreferredSize(new Dimension(0, 50));
        loginButton.setBackground(new Color(14, 165, 233));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> handleLogin());
        formPanel.add(loginButton, gbc);
        
        // Register button
        gbc.gridy = 3;
        gbc.insets = new Insets(10, 10, 10, 10);
        JButton registerButton = new JButton("Đăng ký tài khoản mới");
        registerButton.setFont(new Font("Arial", Font.PLAIN, 14));
        registerButton.setPreferredSize(new Dimension(0, 45));
        registerButton.setBackground(new Color(34, 197, 94));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> handleRegister());
        formPanel.add(registerButton, gbc);
        
        centerPanel.add(formPanel, BorderLayout.CENTER);
        
        // Add center panel to main panel
        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.gridx = 0;
        mainGbc.gridy = 0;
        mainPanel.add(centerPanel, mainGbc);
        
        add(mainPanel);
        
        // Enter key to login
        passwordField.addActionListener(e -> handleLogin());
        
        // Set default button
        getRootPane().setDefaultButton(loginButton);
    }
    
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Vui lòng nhập tên đăng nhập và mật khẩu",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        loginButton.setEnabled(false);
        loginButton.setText("Đang đăng nhập...");
        
        // Login in background thread
        SwingWorker<ApiClient.LoginResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected ApiClient.LoginResponse doInBackground() throws Exception {
                return apiClient.login(username, password);
            }
            
            @Override
            protected void done() {
                try {
                    ApiClient.LoginResponse response = get();
                    apiClient.setJwtToken(response.getToken());
                    
                    // Save login info
                    loggedInUsername = response.getUsername();
                    loggedInRole = response.getRole();
                    
                    // Show success message
                    JOptionPane.showMessageDialog(LoginFrame.this,
                        "Đăng nhập thành công!\nChào mừng " + response.getUsername(),
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    // Close login frame (will trigger window listener in MainFrame)
                    dispose();
                    
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                        "Đăng nhập thất bại!\n" + ex.getMessage() + 
                        "\n\nVui lòng kiểm tra:\n" +
                        "1. Backend đã chạy chưa?\n" +
                        "2. Tên đăng nhập và mật khẩu có đúng không?",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                    
                    loginButton.setEnabled(true);
                    loginButton.setText("Đăng nhập");
                }
            }
        };
        
        worker.execute();
    }
    
    private void handleRegister() {
        // Create register dialog
        JDialog registerDialog = new JDialog(this, "Đăng ký tài khoản", true);
        registerDialog.setSize(450, 500);
        registerDialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title
        JLabel titleLabel = new JLabel("📝 Đăng ký tài khoản mới");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(14, 165, 233));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        
        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Username
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        JLabel usernameLabel = new JLabel("Tên đăng nhập:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 13));
        formPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField regUsernameField = new JTextField(20);
        regUsernameField.setFont(new Font("Arial", Font.PLAIN, 13));
        regUsernameField.setPreferredSize(new Dimension(0, 35));
        formPanel.add(regUsernameField, gbc);
        
        // Password
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel passwordLabel = new JLabel("Mật khẩu:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 13));
        formPanel.add(passwordLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        JPasswordField regPasswordField = new JPasswordField(20);
        regPasswordField.setFont(new Font("Arial", Font.PLAIN, 13));
        regPasswordField.setPreferredSize(new Dimension(0, 35));
        formPanel.add(regPasswordField, gbc);
        
        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        JLabel confirmLabel = new JLabel("Xác nhận mật khẩu:");
        confirmLabel.setFont(new Font("Arial", Font.BOLD, 13));
        formPanel.add(confirmLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        JPasswordField confirmPasswordField = new JPasswordField(20);
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 13));
        confirmPasswordField.setPreferredSize(new Dimension(0, 35));
        formPanel.add(confirmPasswordField, gbc);
        
        // Email
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Arial", Font.BOLD, 13));
        formPanel.add(emailLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField emailField = new JTextField(20);
        emailField.setFont(new Font("Arial", Font.PLAIN, 13));
        emailField.setPreferredSize(new Dimension(0, 35));
        formPanel.add(emailField, gbc);
        
        // Full Name
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        JLabel fullNameLabel = new JLabel("Họ và tên:");
        fullNameLabel.setFont(new Font("Arial", Font.BOLD, 13));
        formPanel.add(fullNameLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        JTextField fullNameField = new JTextField(20);
        fullNameField.setFont(new Font("Arial", Font.PLAIN, 13));
        fullNameField.setPreferredSize(new Dimension(0, 35));
        formPanel.add(fullNameField, gbc);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton cancelBtn = new JButton("Hủy");
        cancelBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        cancelBtn.setPreferredSize(new Dimension(100, 40));
        cancelBtn.addActionListener(e -> registerDialog.dispose());
        buttonPanel.add(cancelBtn);
        
        JButton submitBtn = new JButton("Đăng ký");
        submitBtn.setFont(new Font("Arial", Font.BOLD, 14));
        submitBtn.setPreferredSize(new Dimension(100, 40));
        submitBtn.setBackground(new Color(34, 197, 94));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.addActionListener(e -> {
            String username = regUsernameField.getText().trim();
            String password = new String(regPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            String email = emailField.getText().trim();
            String fullName = fullNameField.getText().trim();
            
            // Validate
            if (username.isEmpty() || password.isEmpty() || email.isEmpty() || fullName.isEmpty()) {
                JOptionPane.showMessageDialog(registerDialog,
                    "Vui lòng điền đầy đủ thông tin!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(registerDialog,
                    "Mật khẩu xác nhận không khớp!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (password.length() < 6) {
                JOptionPane.showMessageDialog(registerDialog,
                    "Mật khẩu phải có ít nhất 6 ký tự!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Register
            submitBtn.setEnabled(false);
            submitBtn.setText("Đang xử lý...");
            
            SwingWorker<String, Void> worker = new SwingWorker<>() {
                @Override
                protected String doInBackground() throws Exception {
                    return apiClient.register(username, password, email, fullName);
                }
                
                @Override
                protected void done() {
                    try {
                        String result = get();
                        JOptionPane.showMessageDialog(registerDialog,
                            "Đăng ký thành công!\nBạn có thể đăng nhập ngay bây giờ.",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE);
                        registerDialog.dispose();
                        
                        // Auto fill username
                        usernameField.setText(username);
                        passwordField.requestFocus();
                        
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(registerDialog,
                            "Đăng ký thất bại!\n" + ex.getMessage(),
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    } finally {
                        submitBtn.setEnabled(true);
                        submitBtn.setText("Đăng ký");
                    }
                }
            };
            
            worker.execute();
        });
        buttonPanel.add(submitBtn);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        registerDialog.add(mainPanel);
        registerDialog.setVisible(true);
    }
}
