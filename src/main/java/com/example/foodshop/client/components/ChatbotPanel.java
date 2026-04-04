package com.example.foodshop.client.components;

import com.example.foodshop.client.api.ApiClient;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Chatbot Panel for Desktop App
 */
public class ChatbotPanel extends JPanel {
    
    private JTextPane chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private ApiClient apiClient;
    private List<ChatMessage> messages;
    private Gson gson;
    
    private static class ChatMessage {
        String sender; // "user" or "bot"
        String message;
        LocalDateTime timestamp;
        
        ChatMessage(String sender, String message) {
            this.sender = sender;
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }
    }
    
    public ChatbotPanel() {
        this.apiClient = ApiClient.getInstance();
        this.messages = new ArrayList<>();
        this.gson = new Gson();
        
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(15, 15, 15, 15));
        
        initComponents();
        addWelcomeMessage();
    }
    
    private void initComponents() {
        // Header
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);
        
        // Chat area
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setContentType("text/html");
        chatArea.setBackground(new Color(248, 250, 252));
        chatArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
        
        // Input area
        JPanel inputPanel = createInputPanel();
        add(inputPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(14, 165, 233));
        header.setBorder(new EmptyBorder(12, 15, 12, 15));
        
        JLabel titleLabel = new JLabel("🤖 AI Trợ lý");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel, BorderLayout.WEST);
        
        JLabel statusLabel = new JLabel("● Online");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(134, 239, 172));
        header.add(statusLabel, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
            new EmptyBorder(10, 12, 10, 12)
        ));
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });
        
        sendButton = new JButton("Gửi");
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setForeground(Color.WHITE);
        sendButton.setBackground(new Color(14, 165, 233));
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.setPreferredSize(new Dimension(80, 42));
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendButton.addActionListener(e -> sendMessage());
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        
        return inputPanel;
    }
    
    private void addWelcomeMessage() {
        String welcome = "Xin chào! Tôi là trợ lý AI của Food Shop. Tôi có thể giúp bạn:\n\n" +
                        "• Tìm kiếm sản phẩm\n" +
                        "• Nhận mã giảm giá\n" +
                        "• Tư vấn đặt hàng\n" +
                        "• Trả lời câu hỏi về menu\n\n" +
                        "Hãy hỏi tôi bất cứ điều gì! 😊";
        messages.add(new ChatMessage("bot", welcome));
        updateChatDisplay();
    }
    
    private void sendMessage() {
        String userMessage = inputField.getText().trim();
        if (userMessage.isEmpty()) {
            return;
        }
        
        // Add user message
        messages.add(new ChatMessage("user", userMessage));
        updateChatDisplay();
        inputField.setText("");
        inputField.setEnabled(false);
        sendButton.setEnabled(false);
        
        // Send to API
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                try {
                    return apiClient.sendChatMessage(userMessage);
                } catch (Exception e) {
                    System.err.println("Error sending chat message: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }
            
            @Override
            protected void done() {
                try {
                    String response = get();
                    if (response != null && !response.trim().isEmpty()) {
                        messages.add(new ChatMessage("bot", response));
                    } else {
                        messages.add(new ChatMessage("bot", "Xin lỗi, tôi không nhận được phản hồi."));
                    }
                } catch (Exception ex) {
                    System.err.println("Error in chatbot done(): " + ex.getMessage());
                    ex.printStackTrace();
                    String errorMsg = ex.getMessage();
                    if (errorMsg == null || errorMsg.isEmpty()) {
                        errorMsg = "Không thể kết nối đến server";
                    }
                    messages.add(new ChatMessage("bot", "Xin lỗi, tôi gặp lỗi: " + errorMsg));
                }
                updateChatDisplay();
                inputField.setEnabled(true);
                sendButton.setEnabled(true);
                inputField.requestFocus();
            }
        };
        
        worker.execute();
    }
    
    private void updateChatDisplay() {
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family: Arial; font-size: 13px; padding: 10px;'>");
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        for (ChatMessage msg : messages) {
            String time = msg.timestamp.format(timeFormatter);
            
            if ("user".equals(msg.sender)) {
                html.append("<div style='text-align: right; margin: 10px 0;'>");
                html.append("<div style='display: inline-block; background: linear-gradient(135deg, #0ea5e9, #06b6d4); " +
                           "color: white; padding: 10px 15px; border-radius: 18px 18px 4px 18px; " +
                           "max-width: 70%; text-align: left;'>");
                html.append(escapeHtml(msg.message));
                html.append("</div>");
                html.append("<div style='font-size: 10px; color: #94a3b8; margin-top: 4px;'>");
                html.append(time);
                html.append("</div>");
                html.append("</div>");
            } else {
                html.append("<div style='text-align: left; margin: 10px 0;'>");
                html.append("<div style='display: inline-block; background: #f1f5f9; " +
                           "color: #1e293b; padding: 10px 15px; border-radius: 18px 18px 18px 4px; " +
                           "max-width: 70%; text-align: left;'>");
                html.append(formatBotMessage(msg.message));
                html.append("</div>");
                html.append("<div style='font-size: 10px; color: #94a3b8; margin-top: 4px;'>");
                html.append(time);
                html.append("</div>");
                html.append("</div>");
            }
        }
        
        html.append("</body></html>");
        
        chatArea.setText(html.toString());
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    private String formatBotMessage(String message) {
        // Format line breaks
        message = message.replace("\n", "<br>");
        
        // Format bold text
        message = message.replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>");
        
        // Format voucher codes
        message = message.replaceAll("(WELCOME\\d+|COMEBACK\\d+|VIP\\d+|DEAL\\d+)", 
                                     "<span style='background: #fef3c7; color: #92400e; padding: 2px 6px; " +
                                     "border-radius: 4px; font-weight: bold;'>$1</span>");
        
        return message;
    }
    
    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("\n", "<br>");
    }
}
