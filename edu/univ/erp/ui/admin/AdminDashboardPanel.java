package edu.univ.erp.ui.admin;

import javax.swing.*;
import java.awt.*;

public class AdminDashboardPanel extends JPanel {

    public AdminDashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // ---------- HEADER ----------
        JLabel title = new JLabel("Admin Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        add(title, BorderLayout.NORTH);

        // ---------- MAIN GRID ----------
        JPanel grid = new JPanel(new GridLayout(3, 3, 20, 20));
        grid.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        grid.setBackground(Color.WHITE);

        grid.add(createMenuButton("Manage Users"));
        grid.add(createMenuButton("Manage Courses"));
        grid.add(createMenuButton("Manage Sections"));
        grid.add(createMenuButton("Assign Instructor"));
        grid.add(createMenuButton("Maintenance"));
        grid.add(createMenuButton("Backup & Restore"));
        grid.add(createMenuButton("Reports"));
        grid.add(createMenuButton("Profile"));

        add(grid, BorderLayout.CENTER);
    }


    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setFocusPainted(false);
        btn.setBackground(new Color(52, 152, 219));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(41, 128, 185));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(52, 152, 219));
            }
        });

        return btn;
    }
}
