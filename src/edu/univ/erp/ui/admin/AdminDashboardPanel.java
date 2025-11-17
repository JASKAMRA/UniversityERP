package edu.univ.erp.ui.admin;

import javax.swing.*;
import java.awt.*;

public class AdminDashboardPanel extends JPanel {
    public AdminDashboardPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Admin Dashboard"), BorderLayout.NORTH);
        JPanel p = new JPanel();
        JButton users = new JButton("User Management");
        users.addActionListener(e -> JOptionPane.showMessageDialog(this, "Open User Management"));
        p.add(users);
        add(p, BorderLayout.CENTER);
    }

    public void loadData(Object ignored) { /* stub */ }
}
