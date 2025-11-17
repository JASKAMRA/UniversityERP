package edu.univ.erp.ui.student;

import edu.univ.erp.ui.util.UserProfile;
import javax.swing.*;
import java.awt.*;

public class StudentDashboardPanel extends JPanel {
    private JLabel lblWelcome = new JLabel("Student Dashboard");
    public StudentDashboardPanel() {
        setLayout(new BorderLayout());
        add(lblWelcome, BorderLayout.NORTH);
        JPanel center = new JPanel();
        JButton b1 = new JButton("Open Catalog");
        b1.addActionListener(e -> JOptionPane.showMessageDialog(this, "Open Catalog"));
        center.add(b1);
        add(center, BorderLayout.CENTER);
    }

    public void loadData(UserProfile profile) {
        if (profile != null) lblWelcome.setText("Welcome, " + profile.getName());
    }
}
