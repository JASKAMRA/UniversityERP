package edu.univ.erp.ui.admin;

import javax.swing.*;
import java.awt.*;

public class MaintenancePanel extends JPanel {
    public MaintenancePanel() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JLabel lbl = new JLabel("MaintenancePanel (placeholder)");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(lbl, BorderLayout.CENTER);
    }
}
