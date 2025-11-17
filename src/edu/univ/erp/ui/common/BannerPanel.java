package edu.univ.erp.ui.common;

import javax.swing.*;
import java.awt.*;

public class BannerPanel extends JPanel {
    private JLabel label;

    public BannerPanel() {
        setLayout(new BorderLayout());
        label = new JLabel(" ");
        label.setOpaque(true);
        label.setBackground(Color.LIGHT_GRAY);
        label.setForeground(Color.BLACK);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);
        setMaintenance(false);
    }

    public void setMaintenance(boolean on) {
        if (on) {
            label.setText("MAINTENANCE MODE: READ ONLY");
            label.setBackground(Color.RED);
            label.setForeground(Color.WHITE);
        } else {
            label.setText(" ");
            label.setBackground(Color.LIGHT_GRAY);
            label.setForeground(Color.BLACK);
        }
    }
}
