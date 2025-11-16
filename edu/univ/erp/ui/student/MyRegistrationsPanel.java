package edu.univ.erp.ui.student;

import javax.swing.*;
import java.awt.*;

public class MyRegistrationsPanel extends JPanel {
    public MyRegistrationsPanel() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JLabel lbl = new JLabel("MyRegistrationsPanel (placeholder)");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(lbl, BorderLayout.CENTER);
    }
}
