package edu.univ.erp.ui.admin;

import edu.univ.erp.ui.util.CurrentSession;
import javax.swing.*;
import java.awt.*;

public class MaintenancePanel extends JPanel {
    public MaintenancePanel() {
        setLayout(new FlowLayout());
        JCheckBox cb = new JCheckBox("Maintenance Mode");
        cb.setSelected(CurrentSession.get().isMaintenance());
        cb.addActionListener(e -> {
            boolean on = cb.isSelected();
            CurrentSession.get().setMaintenance(on);
            JOptionPane.showMessageDialog(this, "Maintenance set to: " + on);
        });
        add(cb);
    }
}
