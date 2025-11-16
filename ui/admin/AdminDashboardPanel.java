package edu.univ.erp.ui.admin; // Yeh ui.admin package mein hai

import javax.swing.*;
import java.awt.*;

/**
 * Admin Dashboard ka UI Panel.
 * Yahaan admin users (students/instructors) add kar sakta hai,
 * naye courses aur sections bana sakta hai,
 * aur Maintenance Mode ko toggle kar sakta hai.
 * [Project Brief Ref: 39-44, 58]
 */
public class AdminDashboardPanel extends JPanel {

    private JTabbedPane tabbedPane;

    // Maintenance components
    private JToggleButton maintenanceToggleButton;
    private JLabel maintenanceStatusLabel;

    // User management components
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton addUserButton;
    
    // Course management components
    private JTextField courseCodeField;
    private JTextField courseTitleField;
    private JButton addCourseButton;

    public AdminDashboardPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tabbedPane = new JTabbedPane();

        // --- Tab 1: System Settings (Maintenance Mode) ---
        tabbedPane.addTab("Settings", createSettingsPanel());
        
        // --- Tab 2: User Management ---
        tabbedPane.addTab("User Management", createUserPanel());

        // --- Tab 3: Course Management ---
        tabbedPane.addTab("Course Management", createCoursePanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    /** Helper method to create the Settings panel */
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("System Controls"));

        maintenanceToggleButton = new JToggleButton("Toggle Maintenance Mode (Currently OFF)");
        maintenanceToggleButton.setPreferredSize(new Dimension(300, 40));
        
        maintenanceStatusLabel = new JLabel("System is LIVE.");
        maintenanceStatusLabel.setForeground(Color.GREEN.darker());

        panel.add(maintenanceToggleButton);
        panel.add(maintenanceStatusLabel);
        return panel;
    }

    /** Helper method to create the User Management panel */
    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Add New User"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.LINE_END;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.LINE_START;
        usernameField = new JTextField(20);
        panel.add(usernameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.LINE_START;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.LINE_START;
        roleComboBox = new JComboBox<>(new String[]{"Student", "Instructor", "Admin"});
        panel.add(roleComboBox, gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.anchor = GridBagConstraints.LINE_START;
        addUserButton = new JButton("Add User");
        panel.add(addUserButton, gbc);

        return panel;
    }

    /** Helper method to create the Course Management panel */
    private JPanel createCoursePanel() {
        // Yeh abhi simple rakha hai, baad mein section add karne ka bhi daal sakte hain
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Add New Course"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.LINE_END;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Course Code:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.LINE_START;
        courseCodeField = new JTextField(10);
        panel.add(courseCodeField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(new JLabel("Course Title:"), gbc);
        gbc.gridx = 1; gbc.anchor = GridBagConstraints.LINE_START;
        courseTitleField = new JTextField(25);
        panel.add(courseTitleField, gbc);
        
        gbc.gridx = 1; gbc.gridy = 2; gbc.anchor = GridBagConstraints.LINE_START;
        addCourseButton = new JButton("Add Course");
        panel.add(addCourseButton, gbc);
        
        return panel;
    }
}
