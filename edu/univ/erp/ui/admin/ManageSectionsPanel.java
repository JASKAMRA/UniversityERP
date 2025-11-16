package edu.univ.erp.ui.admin;

import javax.swing.*;
import java.awt.*;

public class ManageSectionsPanel extends JPanel {
    public ManageSectionsPanel() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JLabel lbl = new JLabel("ManageSectionsPanel (placeholder)");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(lbl, BorderLayout.CENTER);
    }
}
