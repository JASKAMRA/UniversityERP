package edu.univ.erp.ui.student;

import javax.swing.*;
import java.awt.*;

public class TranscriptPanel extends JPanel {
    public TranscriptPanel() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JLabel lbl = new JLabel("TranscriptPanel (placeholder)");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(lbl, BorderLayout.CENTER);
    }
}
