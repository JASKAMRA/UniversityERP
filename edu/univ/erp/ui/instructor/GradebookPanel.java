package edu.univ.erp.ui.instructor;

import javax.swing.*;
import java.awt.*;

public class GradebookPanel extends JPanel {
    public GradebookPanel() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JLabel lbl = new JLabel("GradebookPanel (placeholder)");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(lbl, BorderLayout.CENTER);
    }
}
