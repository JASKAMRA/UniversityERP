package edu.univ.erp.ui.instructor;

import edu.univ.erp.ui.util.UserProfile;
import javax.swing.*;
import java.awt.*;

public class InstructorDashboardPanel extends JPanel {
    private JLabel lbl = new JLabel("Instructor Dashboard");
    public InstructorDashboardPanel() {
        setLayout(new BorderLayout());
        add(lbl, BorderLayout.NORTH);
        JPanel p = new JPanel();
        JButton b = new JButton("Open My Sections");
        b.addActionListener(e -> JOptionPane.showMessageDialog(this, "Open My Sections (stub)"));
        p.add(b);
        add(p, BorderLayout.CENTER);
    }

    public void loadData(UserProfile profile) {
        if (profile != null) lbl.setText("Welcome, " + profile.getName());
    }
}
