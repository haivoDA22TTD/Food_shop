package com.example.foodshop.client;

import com.formdev.flatlaf.FlatLightLaf;
import com.example.foodshop.client.ui.MainFrameModern;

import javax.swing.*;

/**
 * Main entry point for Food Shop Swing Desktop Application
 */
public class FoodShopSwingApp {
    
    public static void main(String[] args) {
        // Set FlatLaf Look and Feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf");
            e.printStackTrace();
        }
        
        // Set default font
        UIManager.put("defaultFont", new java.awt.Font("Arial", java.awt.Font.PLAIN, 13));
        
        // Run on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Start with main frame (no login required)
            MainFrameModern mainFrame = new MainFrameModern(null, null);
            mainFrame.setVisible(true);
        });
    }
}
