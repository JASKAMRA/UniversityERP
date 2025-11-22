package edu.univ.erp.ui.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class BannerPanel extends JPanel {
    private JLabel label;

    // --- Aesthetic constants ---
    private static final Font BANNER_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Color DEFAULT_BG = new Color(220, 220, 220); // Slightly darker gray
    private static final Color MAINTENANCE_BG = new Color(180, 0, 0); // Darker Red
    private static final Color MAINTENANCE_FG = Color.YELLOW; // High contrast for warning

    public BannerPanel() {
        setLayout(new BorderLayout());
        
        // Ensure the panel itself has minimal height if no message is showing
        setPreferredSize(new Dimension(1, 30)); 

        label = new JLabel(" ");
        label.setOpaque(true);
        label.setFont(BANNER_FONT);
        label.setBorder(new EmptyBorder(5, 10, 5, 10)); // Add padding
        label.setHorizontalAlignment(SwingConstants.CENTER);
        
        add(label, BorderLayout.CENTER);
        setMaintenance(false); // Set initial state
    }

    public void setMaintenance(boolean on) {
        if (on) {
            label.setText("⚠️ SYSTEM MAINTENANCE MODE: WRITE OPERATIONS DISABLED ⚠️");
            label.setBackground(MAINTENANCE_BG);
            label.setForeground(MAINTENANCE_FG);
        } else {
            label.setText(" ");
            label.setBackground(DEFAULT_BG);
            label.setForeground(Color.BLACK);
        }
        // Force height update if maintenance banner is shown/hidden
        revalidate(); 
        repaint();
    }
}