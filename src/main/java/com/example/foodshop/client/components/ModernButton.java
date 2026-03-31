package com.example.foodshop.client.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class ModernButton extends JButton {
    
    private Color normalColor;
    private Color hoverColor;
    private Color pressedColor;
    private Color currentColor;
    private boolean isHovered = false;
    
    public ModernButton(String text) {
        super(text);
        init();
    }
    
    private void init() {
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Default colors (sky blue theme)
        normalColor = new Color(14, 165, 233);
        hoverColor = new Color(6, 182, 212);
        pressedColor = new Color(8, 145, 178);
        currentColor = normalColor;
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                animateColor(hoverColor);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                animateColor(normalColor);
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                currentColor = pressedColor;
                repaint();
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                currentColor = isHovered ? hoverColor : normalColor;
                repaint();
            }
        });
    }
    
    private void animateColor(Color targetColor) {
        Timer timer = new Timer(10, null);
        final int steps = 10;
        final int[] step = {0};
        
        final int startR = currentColor.getRed();
        final int startG = currentColor.getGreen();
        final int startB = currentColor.getBlue();
        
        final int deltaR = targetColor.getRed() - startR;
        final int deltaG = targetColor.getGreen() - startG;
        final int deltaB = targetColor.getBlue() - startB;
        
        timer.addActionListener(e -> {
            step[0]++;
            if (step[0] >= steps) {
                currentColor = targetColor;
                timer.stop();
            } else {
                int r = startR + (deltaR * step[0] / steps);
                int g = startG + (deltaG * step[0] / steps);
                int b = startB + (deltaB * step[0] / steps);
                currentColor = new Color(r, g, b);
            }
            repaint();
        });
        
        timer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw rounded rectangle background
        g2.setColor(currentColor);
        g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 8, 8));
        
        // Draw text
        g2.setColor(getForeground());
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
        g2.drawString(getText(), x, y);
        
        g2.dispose();
    }
    
    public void setColors(Color normal, Color hover, Color pressed) {
        this.normalColor = normal;
        this.hoverColor = hover;
        this.pressedColor = pressed;
        this.currentColor = normal;
        repaint();
    }
}
