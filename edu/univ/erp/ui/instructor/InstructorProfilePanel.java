package edu.univ.erp.ui.instructor;

import javax.swing.*;
import java.awt.*;

public class InstructorProfilePanel extends JPanel {
    public InstructorProfilePanel() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JLabel lbl = new JLabel("InstructorProfilePanel (placeholder)");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(lbl, BorderLayout.CENTER);
    }
}
