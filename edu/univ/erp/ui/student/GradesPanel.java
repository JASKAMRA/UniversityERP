package edu.univ.erp.ui.student;

import javax.swing.*;
import java.awt.*;

public class GradesPanel extends JPanel {
    public GradesPanel() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JLabel lbl = new JLabel("GradesPanel (placeholder)");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(lbl, BorderLayout.CENTER);
    }
}
