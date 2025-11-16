package edu.univ.erp.ui.admin;

import javax.swing.*;
import java.awt.*;

public class BackupRestorePanel extends JPanel {
    public BackupRestorePanel() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JLabel lbl = new JLabel("BackupRestorePanel (placeholder)");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(lbl, BorderLayout.CENTER);
    }
}
