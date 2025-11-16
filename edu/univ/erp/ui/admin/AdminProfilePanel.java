package edu.univ.erp.ui.admin;

import javax.swing.*;
import java.awt.*;

public class AdminProfilePanel extends JPanel {
    public AdminProfilePanel() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JLabel lbl = new JLabel("AdminProfilePanel (placeholder)");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(lbl, BorderLayout.CENTER);
    }
}
