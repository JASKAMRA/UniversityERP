package edu.univ.erp.ui.admin;

import edu.univ.erp.service.AdminService;
import edu.univ.erp.ui.common.MainFrame;
import edu.univ.erp.ui.util.CurrentSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Small UI to view and toggle maintenance mode.
 */
public class MaintenancePanel extends JPanel {
    private final AdminService adminService;
    private final JLabel lblState;
    private final JButton btnToggle;

    // --- Aesthetic constants ---
    private static final int PADDING = 25;
    private static final int GAP = 15;
    private static final Font STATUS_FONT = new Font("Arial", Font.BOLD, 22);
    private static final Dimension BUTTON_SIZE = new Dimension(150, 40);
    private static final Color ON_COLOR = new Color(220, 50, 50); // Red for ON
    private static final Color OFF_COLOR = new Color(50, 160, 50); // Green for OFF
    private static final Color LOADING_COLOR = Color.GRAY;


    public MaintenancePanel(AdminService adminService) {
        this.adminService = adminService;
        
        // 1. Overall Layout & Padding
        setLayout(new GridBagLayout()); // Use GridBagLayout for clean centering
        setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        setBackground(Color.WHITE);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(GAP, GAP, GAP, GAP);
        c.gridx = 0;
        c.anchor = GridBagConstraints.CENTER;
        
        // 2. Status Label
        lblState = new JLabel("Loading...");
        lblState.setFont(STATUS_FONT);
        c.gridy = 0;
        add(lblState, c);

        // 3. Toggle Button
        btnToggle = new JButton("Toggle");
        btnToggle.setPreferredSize(BUTTON_SIZE);
        btnToggle.setFocusPainted(false);
        c.gridy = 1;
        add(btnToggle, c);

        // 4. Action Listener
        btnToggle.addActionListener(e -> toggle());

        refresh();
    }

    private void refresh() {
        try {
            boolean on = adminService.IS_Maintenance_on();
            
            // Update UI based on state
            if (on) {
                lblState.setText("🔴 MAINTENANCE IS ON");
                lblState.setForeground(ON_COLOR);
                btnToggle.setText("Turn OFF");
                btnToggle.setBackground(OFF_COLOR);
                btnToggle.setForeground(Color.WHITE);
            } else {
                lblState.setText("🟢 MAINTENANCE IS OFF");
                lblState.setForeground(OFF_COLOR);
                btnToggle.setText("Turn ON");
                btnToggle.setBackground(ON_COLOR);
                btnToggle.setForeground(Color.WHITE);
            }
            btnToggle.setEnabled(true);
            
        } catch (Exception ex) {
            lblState.setText("⚠️ Error reading state");
            lblState.setForeground(LOADING_COLOR);
            btnToggle.setText("Error");
            btnToggle.setEnabled(false);
        }
    }

    private void toggle() {
        btnToggle.setEnabled(false); // Disable during operation
        try {
            boolean on = adminService.IS_Maintenance_on(); // current state
            boolean newState = !on; // flipped state
    
            boolean ok = adminService.Set_Maintenance(newState);
    
            if (ok) {
                // Update session + banner with NEW state
                CurrentSession.get().SetMant(newState);
                // Note: MainFrame.getInstance() is checked externally in AdminDashboardPanel, 
                // but we keep the call here if it is needed internally by this panel's use case.
                if (MainFrame.getInstance() != null) {
                    MainFrame.getInstance().togglemantainenceON(newState);
                }

                JOptionPane.showMessageDialog(this,
                    "System Maintenance Mode successfully set to: **" + (newState ? "ON" : "OFF") + "**",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to change maintenance status.",
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
            refresh(); // Attempt to refresh state even if error occurred
        } finally {
            btnToggle.setEnabled(true);
        }
    }
}