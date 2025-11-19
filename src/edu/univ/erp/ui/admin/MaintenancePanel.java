package edu.univ.erp.ui.admin;

import edu.univ.erp.service.AdminService;
import edu.univ.erp.ui.common.MainFrame;
import edu.univ.erp.ui.util.CurrentSession;

import javax.swing.*;
import java.awt.*;

/**
 * Small UI to view and toggle maintenance mode.
 */
public class MaintenancePanel extends JPanel {
    private final AdminService adminService;
    private final JLabel lblState;
    private final JButton btnToggle;

    public MaintenancePanel(AdminService adminService) {
        this.adminService = adminService;
        setLayout(new BorderLayout(8,8));
        lblState = new JLabel("Loading...");
        lblState.setFont(lblState.getFont().deriveFont(14f));
        btnToggle = new JButton("Toggle");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(lblState);
        add(top, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnToggle);
        add(bottom, BorderLayout.SOUTH);

        btnToggle.addActionListener(e -> toggle());

        refresh();
    }

    private void refresh() {
        try {
            boolean on = adminService.isMaintenanceOn();
            lblState.setText("Maintenance is " + (on ? "ON" : "OFF"));
            btnToggle.setText(on ? "Turn OFF" : "Turn ON");
        } catch (Exception ex) {
            lblState.setText("Error reading state");
            btnToggle.setEnabled(true);
        }
    }

    private void toggle() {
    try {
        boolean on = adminService.isMaintenanceOn();  // current state
        boolean newState = !on;                       // flipped state

        boolean ok = adminService.setMaintenance(newState);

        if (ok) {
            // Update session + banner with NEW state
            CurrentSession.get().setMaintenance(newState);
            MainFrame.getInstance().setBannerMaintenance(newState);

            JOptionPane.showMessageDialog(this,
                    "Maintenance set to: " + (newState ? "ON" : "OFF"));
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to change maintenance.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        refresh();

    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
                "Error: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}

}
