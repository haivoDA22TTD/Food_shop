package com.example.foodshop.client.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AnimationUtil {
    
    /**
     * Add hover effect to button
     */
    public static void addHoverEffect(JButton button, Color normalColor, Color hoverColor) {
        button.setBackground(normalColor);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(normalColor);
            }
        });
    }
    
    /**
     * Add scale animation to component
     */
    public static void addScaleEffect(JComponent component) {
        component.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                component.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                component.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }
    
    /**
     * Fade in animation
     */
    public static void fadeIn(JComponent component) {
        Timer timer = new Timer(10, null);
        final float[] opacity = {0.0f};
        
        timer.addActionListener(e -> {
            opacity[0] += 0.05f;
            if (opacity[0] >= 1.0f) {
                opacity[0] = 1.0f;
                timer.stop();
            }
            component.repaint();
        });
        
        timer.start();
    }
    
    /**
     * Create gradient panel
     */
    public static JPanel createGradientPanel(Color color1, Color color2) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, color1, w, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
    }
    
    /**
     * Create rounded panel
     */
    public static JPanel createRoundedPanel(int radius, Color bgColor) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
                g2.dispose();
            }
        };
    }
}
