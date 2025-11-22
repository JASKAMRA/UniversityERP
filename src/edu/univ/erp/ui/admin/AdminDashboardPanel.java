package edu.univ.erp.ui.admin;

import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.AdminServiceImpl;
import edu.univ.erp.ui.common.MainFrame;
import edu.univ.erp.ui.util.CurrentSession; // Assuming this is the correct import

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminDashboardPanel extends JPanel {
    private final AdminService adminService = new AdminServiceImpl();

    private JButton btnCreateStudent;
    private JButton btnCreateCourseSection;
    private JButton btnMaintenance;
    private JLabel lblStatus;

    // --- Aesthetic constants ---
    private static final int PADDING = 20; // Overall padding
    private static final int GAP = 15;     // Spacing between components
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 24);
    private static final Font BUTTON_FONT = new Font("Arial", Font.PLAIN, 14);
    private static final Dimension BUTTON_SIZE = new Dimension(250, 40); // Consistent button size

    public AdminDashboardPanel() {
        init();
        loadData(); // Load initial data (maintenance status)
    }

    private void init() {
        // 1. Overall Layout & Padding
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING)); // Add padding around the panel
        setBackground(Color.WHITE); // Use a clean background color

        // 2. Title Section (North)
        JLabel title = new JLabel("📚 Administrator Dashboard");
        title.setFont(TITLE_FONT);
        title.setBorder(new EmptyBorder(0, 0, PADDING, 0)); // Padding below the title
        title.setHorizontalAlignment(SwingConstants.LEFT);
        add(title, BorderLayout.NORTH);

        // 3. Center Panel for Buttons (GridBagLayout remains, but optimized)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(GAP / 2, GAP, GAP / 2, GAP); // Smaller insets for a tighter grid
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL; // Fill horizontally
        c.weightx = 1.0; // Allow the column to stretch

        // Initialize Buttons with improved styling
        btnCreateStudent = new JButton("🧑‍🎓 Create Student User");
        btnCreateCourseSection = new JButton("📝 Create Course & Section");
        btnMaintenance = new JButton("🛠️ Toggle System Maintenance");

        // Apply consistent styling to buttons
        styleButton(btnCreateStudent);
        styleButton(btnCreateCourseSection);
        styleButton(btnMaintenance);

        // Add buttons to the center panel
        c.gridy = 0;
        centerPanel.add(btnCreateStudent, c);

        c.gridy++;
        centerPanel.add(btnCreateCourseSection, c);

        c.gridy++;
        centerPanel.add(btnMaintenance, c);
        
        // Add center panel inside another panel to keep it centered horizontally 
        // in the overall dashboard (GridBagLayout centers its content if not constrained)
        JPanel wrapPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapPanel.setBackground(Color.WHITE);
        wrapPanel.add(centerPanel);
        
        add(wrapPanel, BorderLayout.CENTER);

        // 4. Status Bar (South)
        lblStatus = new JLabel("Status: ready");
        lblStatus.setFont(lblStatus.getFont().deriveFont(Font.ITALIC, 12f));
        lblStatus.setBorder(new EmptyBorder(PADDING, 0, 0, 0)); // Padding above status
        add(lblStatus, BorderLayout.SOUTH);

        // 5. Action Listeners (Functionality unchanged)
        btnCreateStudent.addActionListener(e -> openCreateStudentDialog());
        btnCreateCourseSection.addActionListener(e -> openCreateCourseSectionDialog());
        btnMaintenance.addActionListener(e -> openMaintenancePanel());
    }

    /**
     * Helper method to apply consistent styling to buttons.
     */
    private void styleButton(JButton button) {
        button.setFont(BUTTON_FONT);
        button.setPreferredSize(BUTTON_SIZE);
        button.setMinimumSize(BUTTON_SIZE);
        button.setFocusPainted(false); // Remove border around text when focused
        button.setBackground(new Color(230, 240, 255)); // Light blue/grey background
        button.setForeground(Color.BLACK);
        // Add a subtle border
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 180, 255), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }
    
    // --- Existing Functionality Methods (Unchanged logic) ---

    private void openCreateStudentDialog() {
        // ... (implementation unchanged)
        Window owner = SwingUtilities.getWindowAncestor(this);
        CreateStudentDialog dlg = new CreateStudentDialog(owner, adminService);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        if (dlg.isSucceeded()) {
            lblStatus.setText("Created student user: " + dlg.getCreatedUserId());
        }
    }

    private void openCreateCourseSectionDialog() {
        // ... (implementation unchanged)
        Window owner = SwingUtilities.getWindowAncestor(this);
        CreateCourseSectionDialog dlg = new CreateCourseSectionDialog(owner, adminService);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        if (dlg.isSucceeded()) {
            lblStatus.setText("Created section id: " + dlg.getCreatedSectionId());
        }
    }

    private void openMaintenancePanel() {
        // ... (implementation unchanged)
        Window owner = SwingUtilities.getWindowAncestor(this);
        MaintenancePanel mp = new MaintenancePanel(adminService);
        JDialog dlg = new JDialog(owner, "Maintenance", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.getContentPane().add(mp);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        // After dialog closed - refresh session & banner
        try {
            boolean on = adminService.isMaintenanceOn();
            // update CurrentSession so rest of app can read it
            CurrentSession.get().setMaintenance(on);
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
        // ... (implementation unchanged)
        try {
            boolean on = adminService.isMaintenanceOn();
            lblStatus.setText("Maintenance: " + (on ? "ON" : "OFF"));
        } catch (Exception ex) {
            ex.printStackTrace();
            lblStatus.setText("Maintenance: (error)");
        }
    }
}