package edu.univ.erp.ui.instructor;

import javax.swing.*;
import java.awt.*;

public class ClassStatsPanel extends JPanel {
    public ClassStatsPanel() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JLabel lbl = new JLabel("ClassStatsPanel (placeholder)");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(lbl, BorderLayout.CENTER);
    }
}
