package edu.univ.erp.ui.admin;

import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.AdminServiceImpl;
import edu.univ.erp.ui.common.MainFrame;

import javax.swing.*;
import java.awt.*;

public class AdminDashboardPanel extends JPanel {
    private final AdminService adminService = new AdminServiceImpl();

    private JButton btnCreateStudent;
    private JButton btnCreateCourseSection;
    private JButton btnMaintenance;
    private JLabel lblStatus;

    public AdminDashboardPanel() {
        init();
    }

    private void init() {
        setLayout(new BorderLayout(8,8));

        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8,8,8,8);
        c.gridx = 0; c.gridy = 0; c.fill = GridBagConstraints.HORIZONTAL;

        btnCreateStudent = new JButton("Create Student User");
        btnCreateCourseSection = new JButton("Create Course & Section");
        btnMaintenance = new JButton("Toggle Maintenance");

        center.add(btnCreateStudent, c);
        c.gridy++;
        center.add(btnCreateCourseSection, c);
        c.gridy++;
        center.add(btnMaintenance, c);

        add(center, BorderLayout.CENTER);

        lblStatus = new JLabel("Status: ready");
        add(lblStatus, BorderLayout.SOUTH);

        btnCreateStudent.addActionListener(e -> openCreateStudentDialog());
        btnCreateCourseSection.addActionListener(e -> openCreateCourseSectionDialog());
        btnMaintenance.addActionListener(e -> openMaintenancePanel());
    }

    private void openCreateStudentDialog() {
        CreateStudentDialog dlg = new CreateStudentDialog(SwingUtilities.getWindowAncestor(this), adminService);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        if (dlg.isSucceeded()) {
            lblStatus.setText("Created student user: " + dlg.getCreatedUserId());
        }
    }

    private void openCreateCourseSectionDialog() {
        CreateCourseSectionDialog dlg = new CreateCourseSectionDialog(SwingUtilities.getWindowAncestor(this), adminService);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        if (dlg.isSucceeded()) {
            lblStatus.setText("Created section id: " + dlg.getCreatedSectionId());
        }
    }

    private void openMaintenancePanel() {
    MaintenancePanel mp = new MaintenancePanel(adminService);
    JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Maintenance", Dialog.ModalityType.APPLICATION_MODAL);
    dlg.getContentPane().add(mp);
    dlg.pack();
    dlg.setLocationRelativeTo(this);
    dlg.setVisible(true);

    // After dialog closed - refresh session & banner
    try {
        boolean on = adminService.isMaintenanceOn();
        // update CurrentSession so rest of app can read it
        edu.univ.erp.ui.util.CurrentSession.get().setMaintenance(on);
        // update banner in MainFrame immediately
        if (MainFrame.getInstance() != null) {
            MainFrame.getInstance().setBannerMaintenance(on);
        }
        lblStatus.setText("Maintenance: " + (on ? "ON" : "OFF"));
    } catch (Exception ex) {
        ex.printStackTrace();
        lblStatus.setText("Maintenance: (unknown)");
    }
}

    public void loadData() {
        try {
            boolean on = adminService.isMaintenanceOn();
            lblStatus.setText("Maintenance: " + (on ? "ON" : "OFF"));
        } catch (Exception ex) {
            ex.printStackTrace();
            lblStatus.setText("Maintenance: (error)");
        }
    }
}
