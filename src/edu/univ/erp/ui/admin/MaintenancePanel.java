package edu.univ.erp.ui.admin;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.ui.common.MainFrame;
import edu.univ.erp.ui.util.CurrentSession;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MaintenancePanel extends JPanel {
    private final AdminService adminService;
    private final JLabel lblState;
    private final JButton btnToggle;

    public MaintenancePanel(AdminService adminService) {
        this.adminService=adminService;
    
        setLayout(new GridBagLayout()); 
        setBorder(new EmptyBorder(25, 25, 25, 25));
        setBackground(Color.WHITE);

        GridBagConstraints gc=new GridBagConstraints();
        gc.insets=new Insets(15, 15, 15, 15);
        gc.gridx=0;
        gc.anchor=GridBagConstraints.CENTER;

        lblState=new JLabel("Loading...");
        lblState.setFont(new Font("Arial", Font.BOLD, 22));
        gc.gridy=0;
        add(lblState, gc);

        btnToggle = new JButton("Toggle");
        btnToggle.setPreferredSize(new Dimension(150, 40));
        btnToggle.setFocusPainted(false);
        gc.gridy = 1;
        add(btnToggle, gc);

        btnToggle.addActionListener(e -> toggle());

        refresh();
    }

    private void refresh() {
        try {
            boolean on = adminService.is_Maintenance_on();

            if (!on) {
                lblState.setText("🟢 MAINTENANCE IS OFF");
                lblState.setForeground(new Color(50, 160, 50));
                btnToggle.setText("Turn ON");
                btnToggle.setBackground(new Color(220, 50, 50));
                btnToggle.setForeground(Color.WHITE);
            } else {
                lblState.setText("🔴 MAINTENANCE IS ON");
                lblState.setForeground(new Color(220, 50, 50));
                btnToggle.setText("Turn OFF");
                btnToggle.setBackground(new Color(50, 160, 50));
                btnToggle.setForeground(Color.WHITE);
            }
            btnToggle.setEnabled(true);
            
        } 
        catch (Exception exception){
            lblState.setText("⚠️ Error reading state");
            lblState.setForeground(Color.GRAY);
            btnToggle.setText("Error");
            btnToggle.setEnabled(false);
        }
    }

    private void toggle() {
        btnToggle.setEnabled(false); 
        try {
            boolean on = adminService.is_Maintenance_on();
            boolean newState = !on;
    
            boolean ok = adminService.Set_Maintenance(newState);
    
            if (ok){
                CurrentSession.get().SetMant(newState);
                if (MainFrame.getInstance() != null) {
                    MainFrame.getInstance().togglemantainenceON(newState);
                }

                JOptionPane.showMessageDialog(this,
                    "System Maintenance Mode successfully set to: **" + (newState ? "ON" : "OFF") + "**",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            }
             else {
                JOptionPane.showMessageDialog(this,
                    "Failed to change maintenance status.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
    
            refresh();
    
        } catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error: " + exception.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            refresh();
        } 
        finally {
            btnToggle.setEnabled(true);
        }
    }
}