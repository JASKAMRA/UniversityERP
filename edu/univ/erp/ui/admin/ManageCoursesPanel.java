package edu.univ.erp.ui.admin;

import javax.swing.*;
import java.awt.*;

public class ManageCoursesPanel extends JPanel {
    public ManageCoursesPanel() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JLabel lbl = new JLabel("ManageCoursesPanel (placeholder)");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(lbl, BorderLayout.CENTER);
    }
}
