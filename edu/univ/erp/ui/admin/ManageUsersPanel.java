package edu.univ.erp.ui.admin;

import javax.swing.*;
import java.awt.*;

public class ManageUsersPanel extends JPanel {
    public ManageUsersPanel() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JLabel lbl = new JLabel("ManageUsersPanel (placeholder)");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(lbl, BorderLayout.CENTER);
    }
}
