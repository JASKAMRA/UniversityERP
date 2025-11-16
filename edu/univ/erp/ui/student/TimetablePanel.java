package edu.univ.erp.ui.student;

import javax.swing.*;
import java.awt.*;

public class TimetablePanel extends JPanel {
    public TimetablePanel() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JLabel lbl = new JLabel("TimetablePanel (placeholder)");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(lbl, BorderLayout.CENTER);
    }
}
