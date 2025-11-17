package edu.univ.erp.ui.admin;

import javax.swing.*;
import java.awt.*;

public class BackupRestorePanel extends JPanel {
    public BackupRestorePanel() {
        setLayout(new FlowLayout());
        JButton backup = new JButton("Backup DB");
        backup.addActionListener(e -> JOptionPane.showMessageDialog(this, "Backup done (stub)"));
        JButton restore = new JButton("Restore DB");
        restore.addActionListener(e -> JOptionPane.showMessageDialog(this, "Restore done (stub)"));
        add(backup); add(restore);
    }
}
