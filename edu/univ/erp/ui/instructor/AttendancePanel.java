package edu.univ.erp.ui.instructor;

import javax.swing.*;
import java.awt.*;

public class AttendancePanel extends JPanel {
    public AttendancePanel() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JLabel lbl = new JLabel("AttendancePanel (placeholder)");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(lbl, BorderLayout.CENTER);
    }
}
