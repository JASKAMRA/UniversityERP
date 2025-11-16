package edu.univ.erp.ui.student;

import javax.swing.*;
import java.awt.*;

public class StudentDashboardPanel extends JPanel {
    public StudentDashboardPanel() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JLabel lbl = new JLabel("StudentDashboardPanel (placeholder)");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(lbl, BorderLayout.CENTER);
    }
}
