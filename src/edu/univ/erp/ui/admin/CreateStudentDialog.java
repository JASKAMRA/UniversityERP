package edu.univ.erp.ui.admin;

import edu.univ.erp.service.AdminService;

import javax.swing.*;
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

    public CreateStudentDialog(Window owner, AdminService adminService) {
        super(owner, "Create Student", ModalityType.APPLICATION_MODAL);
        this.adminService = adminService;
        init();
        pack();
    }

    private void init() {
        setLayout(new BorderLayout(8,8));
        JPanel form = new JPanel(new GridLayout(0,2,6,6));

        tfUsername = new JTextField();
        pfPassword = new JPasswordField();
        tfFullName = new JTextField();
        tfEmail = new JTextField();
        tfRoll = new JTextField();
        tfYear = new JTextField();
        tfProgram = new JTextField();

        form.add(new JLabel("Username:")); form.add(tfUsername);
        form.add(new JLabel("Password:")); form.add(pfPassword);
        form.add(new JLabel("Full name:")); form.add(tfFullName);
        form.add(new JLabel("Email:")); form.add(tfEmail);
        form.add(new JLabel("Roll no:")); form.add(tfRoll);
        form.add(new JLabel("Year (int):")); form.add(tfYear);
        form.add(new JLabel("Program:")); form.add(tfProgram);

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCreate = new JButton("Create");
        JButton btnCancel = new JButton("Cancel");
        bottom.add(btnCancel); bottom.add(btnCreate);
        add(bottom, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> { succeeded = false; setVisible(false); dispose(); });
        btnCreate.addActionListener(e -> doCreate());
    }

    private void doCreate() {
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword());
        String fullName = tfFullName.getText().trim();
        String email = tfEmail.getText().trim();
        String roll = tfRoll.getText().trim();
        Integer year = null;
        try {
            if (!tfYear.getText().trim().isEmpty()) year = Integer.parseInt(tfYear.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Year must be a number.", "Invalid", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String program = tfProgram.getText().trim();

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username, password and full name are required.", "Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String userId = adminService.createStudentUser(username, password, fullName, email, roll, year, program);
            if (userId != null) {
                succeeded = true;
                createdUserId = userId;
                JOptionPane.showMessageDialog(this, "Student created: " + username, "Success", JOptionPane.INFORMATION_MESSAGE);
                setVisible(false);
                dispose();
                return;
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create student. Username may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSucceeded() { return succeeded; }
    public String getCreatedUserId() { return createdUserId; }
}

