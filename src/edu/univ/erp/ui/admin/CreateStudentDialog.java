package edu.univ.erp.ui.admin;

import edu.univ.erp.service.AdminService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * Dialog to create a student user (auth + student profile).
 * Uses AdminService.createStudentUser(...)
 */
public class CreateStudentDialog extends JDialog {
    private final AdminService adminService;
    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private JTextField tfFullName;
    private JTextField tfEmail;
    private JTextField tfRoll;
    private JTextField tfYear;
    private JTextField tfProgram;
    private boolean succeeded = false;
    private String createdUserId;

    // --- Aesthetic constants ---
    private static final int PADDING = 15;
    private static final int GAP = 10;
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180); // Steel blue
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 18);
    private static final Font BORDER_FONT = new Font("Arial", Font.BOLD, 12);


    public CreateStudentDialog(Window owner, AdminService adminService) {
        super(owner, "🧑‍🎓 Create Student User", ModalityType.APPLICATION_MODAL);
        this.adminService = adminService;
        init();
        // Add padding around the whole dialog content
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        pack();
        setResizable(false);
    }

    private void init() {
        setLayout(new BorderLayout(GAP, GAP));

        // 1. Title (North)
        JLabel title = new JLabel("Enter Authentication and Profile Details");
        title.setFont(TITLE_FONT);
        add(title, BorderLayout.NORTH);

        // 2. Main Form Panel (Center) - Divided into two logical groups
        JPanel mainForm = new JPanel(new GridBagLayout());
        mainForm.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(GAP / 2, 0, GAP / 2, 0);
        gbc.weightx = 1.0;

        // --- Group 1: Authentication ---
        JPanel authPanel = createTitledPanel("🔑 Login Details");
        
        tfUsername = new JTextField(15);
        pfPassword = new JPasswordField(15);
        
        fillFormPanel(authPanel, new String[]{"Username:", "Password:"}, new JComponent[]{tfUsername, pfPassword});
        
        gbc.gridx = 0; gbc.gridy = 0;
        mainForm.add(authPanel, gbc);

        // --- Group 2: Student Profile ---
        JPanel profilePanel = createTitledPanel("📄 Student Profile");
        
        tfFullName = new JTextField(15);
        tfEmail = new JTextField(15);
        tfRoll = new JTextField(15);
        tfYear = new JTextField(15);
        tfProgram = new JTextField(15);
        
        fillFormPanel(profilePanel, new String[]{
            "Full Name:", "Email:", "Roll No.:", "Year (int):", "Program:"
        }, new JComponent[]{
            tfFullName, tfEmail, tfRoll, tfYear, tfProgram
        });

        gbc.gridy = 1;
        mainForm.add(profilePanel, gbc);

        add(mainForm, BorderLayout.CENTER);

        // 3. Buttons (South)
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP, 0));
        JButton btnCancel = new JButton("Cancel");
        JButton btnCreate = new JButton("Create Student");
        
        // Style buttons
        btnCreate.setBackground(PRIMARY_COLOR);
        btnCreate.setForeground(Color.WHITE);

        bottom.add(btnCancel); 
        bottom.add(btnCreate);
        add(bottom, BorderLayout.SOUTH);

        // 4. Action Listeners
        btnCancel.addActionListener(e -> { succeeded = false; setVisible(false); dispose(); });
        btnCreate.addActionListener(e -> doCreate());
    }
    
    /** Helper to create a titled, bordered container */
    private JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
            title, 
            TitledBorder.LEFT, 
            TitledBorder.TOP, 
            BORDER_FONT, 
            PRIMARY_COLOR
        ));
        return panel;
    }
    
    /** Helper to populate a GridBagLayout panel with labels and components */
    private void fillFormPanel(JPanel panel, String[] labels, JComponent[] fields) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < labels.length; i++) {
            // Label
            c.gridx = 0; c.gridy = i; c.weightx = 0;
            panel.add(new JLabel(labels[i]), c);
            
            // Field
            c.gridx = 1; c.gridy = i; c.weightx = 1.0;
            panel.add(fields[i], c);
        }
    }


    private void doCreate() {
        String username = tfUsername.getText().trim();
        // NOTE: Getting password from JPasswordField is correct
        String password = new String(pfPassword.getPassword()); 
        String fullName = tfFullName.getText().trim();
        String email = tfEmail.getText().trim();
        String roll = tfRoll.getText().trim();
        
        Integer year = null;
        try {
            if (!tfYear.getText().trim().isEmpty()) {
                year = Integer.parseInt(tfYear.getText().trim());
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Year must be a whole number (e.g., 2024).", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String program = tfProgram.getText().trim();

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username, password, and full name are required fields.", "Missing Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String userId = adminService.CreateStuUser(username, password, fullName, email, roll, year, program);
            
            if (userId != null) {
                succeeded = true;
                createdUserId = userId;
                JOptionPane.showMessageDialog(this, "Student user **" + username + "** created successfully (User ID: " + userId + ").", "Success", JOptionPane.INFORMATION_MESSAGE);
                setVisible(false);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create student. The username **" + username + "** may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSucceeded() { return succeeded; }
    public String getCreatedUserId() { return createdUserId; }
}