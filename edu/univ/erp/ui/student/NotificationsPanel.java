package edu.univ.erp.ui.student;

import javax.swing.*;
import java.awt.*;

public class NotificationsPanel extends JPanel {
    public NotificationsPanel() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JLabel lbl = new JLabel("NotificationsPanel (placeholder)");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(lbl, BorderLayout.CENTER);
    }
}
