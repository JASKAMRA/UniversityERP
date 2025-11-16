package edu.univ.erp.ui.instructor;

import javax.swing.*;
import java.awt.*;

public class MySectionsPanel extends JPanel {
    public MySectionsPanel() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JLabel lbl = new JLabel("MySectionsPanel (placeholder)");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(lbl, BorderLayout.CENTER);
    }
}
