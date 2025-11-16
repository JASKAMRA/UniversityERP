package edu.univ.erp.ui.student;

import javax.swing.*;
import java.awt.*;

public class CourseCatalogPanel extends JPanel {
    public CourseCatalogPanel() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JLabel lbl = new JLabel("CourseCatalogPanel (placeholder)");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(lbl, BorderLayout.CENTER);
    }
}
