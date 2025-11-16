package edu.univ.erp.ui.admin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * AdminProfilePanel
 * - Display admin profile details
 * - Inline editing (Save / Cancel)
 * - Change password dialog
 * - Replace TODOs with actual DB/service integration
 */
public class AdminProfilePanel extends JPanel {

    private final JLabel avatarLabel;
    private final JTextField nameField;
    private final JTextField emailField;
    private final JTextField phoneField;
    private final JTextField roleField;

    private final JButton editBtn;
    private final JButton saveBtn;
    private final JButton cancelBtn;
    private final JButton changePasswordBtn;

    private boolean editMode = false;

    // in-memory demo admin (replace by loaded user object)
    private Admin admin = new Admin(1001, "Admin Name", "admin@uni.edu", "+91-9876543210", "Administrator");

    public AdminProfilePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JLabel title = new JLabel("Admin Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(title, BorderLayout.NORTH);

        // Center: profile card
        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        card.setBackground(Color.WHITE);

        // Left: avatar
        avatarLabel = createAvatarLabel(admin.name);
        card.add(avatarLabel, BorderLayout.WEST);

        // Center: details grid
        JPanel details = new JPanel(new GridBagLayout());
        details.setBackground(Color.WHITE);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        // Name
        g.gridx = 0; g.gridy = 0; g.weightx = 0.0;
        details.add(new JLabel("Name:"), g);
        nameField = new JTextField(admin.name, 30);
        nameField.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        nameField.setEditable(false);
        g.gridx = 1; g.weightx = 1.0;
        details.add(nameField, g);

        // Email
        g.gridx = 0; g.gridy++;
        details.add(new JLabel("Email:"), g);
        emailField = new JTextField(admin.email, 30);
        emailField.setEditable(false);
        g.gridx = 1;
        details.add(emailField, g);

        // Phone
        g.gridx = 0; g.gridy++;
        details.add(new JLabel("Phone:"), g);
        phoneField = new JTextField(admin.phone, 20);
        phoneField.setEditable(false);
        g.gridx = 1;
        details.add(phoneField, g);

        // Role (read-only)
        g.gridx = 0; g.gridy++;
        details.add(new JLabel("Role:"), g);
        roleField = new JTextField(admin.role, 20);
        roleField.setEditable(false);
        g.gridx = 1;
        details.add(roleField, g);

        card.add(details, BorderLayout.CENTER);

        // Right: action buttons
        JPanel actions = new JPanel(new GridLayout(0, 1, 8, 8));
        actions.setBackground(Color.WHITE);

        editBtn = new JButton("Edit");
        saveBtn = new JButton("Save");
        cancelBtn = new JButton("Cancel");
        changePasswordBtn = new JButton("Change Password");

        styleButton(editBtn);
        styleButton(saveBtn);
        styleButton(cancelBtn);
        styleButton(changePasswordBtn);

        saveBtn.setVisible(false);
        cancelBtn.setVisible(false);

        actions.add(editBtn);
        actions.add(saveBtn);
        actions.add(cancelBtn);
        actions.add(changePasswordBtn);

        card.add(actions, BorderLayout.EAST);

        add(card, BorderLayout.CENTER);

        // Events
        editBtn.addActionListener(e -> enterEditMode(true));
        cancelBtn.addActionListener(e -> enterEditMode(false));
        saveBtn.addActionListener(e -> saveProfile());
        changePasswordBtn.addActionListener(e -> openChangePasswordDialog());
    }

    private JLabel createAvatarLabel(String name) {
        String initials = initialsOf(name);
        JLabel lbl = new JLabel(initials, SwingConstants.CENTER);
        lbl.setPreferredSize(new Dimension(110, 110));
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lbl.setOpaque(true);
        lbl.setBackground(new Color(52, 152, 219));
        lbl.setForeground(Color.WHITE);
        lbl.setBorder(BorderFactory.createLineBorder(new Color(200,200,200)));
        return lbl;
    }

    private void styleButton(JButton b) {
        b.setFocusPainted(false);
        b.setBackground(new Color(52, 152, 219));
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    }

    private String initialsOf(String name) {
        if (name == null || name.trim().isEmpty()) return "A";
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(2, parts.length); i++) {
            sb.append(Character.toUpperCase(parts[i].charAt(0)));
        }
        return sb.toString();
    }

    private void enterEditMode(boolean enable) {
        editMode = enable;
        nameField.setEditable(enable);
        emailField.setEditable(enable);
        phoneField.setEditable(enable);

        editBtn.setVisible(!enable);
        changePasswordBtn.setVisible(!enable);

        saveBtn.setVisible(enable);
        cancelBtn.setVisible(enable);
    }

    private void saveProfile() {
        String newName = nameField.getText().trim();
        String newEmail = emailField.getText().trim();
        String newPhone = phoneField.getText().trim();

        if (newName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!isValidEmail(newEmail)) {
            JOptionPane.showMessageDialog(this, "Enter a valid email address.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // TODO: call service / DB to update admin profile. Example:
        // AdminService.updateProfile(admin.id, newName, newEmail, newPhone);
        // On success:
        admin.name = newName;
        admin.email = newEmail;
        admin.phone = newPhone;

        // update avatar
        avatarLabel.setText(initialsOf(admin.name));

        enterEditMode(false);
        JOptionPane.showMessageDialog(this, "Profile saved successfully.");
    }

    private boolean isValidEmail(String e) {
        return e != null && e.contains("@") && e.contains(".") && e.length() >= 5;
    }

    private void openChangePasswordDialog() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.anchor = GridBagConstraints.WEST;

        JPasswordField current = new JPasswordField(20);
        JPasswordField nw = new JPasswordField(20);
        JPasswordField confirm = new JPasswordField(20);

        g.gridx = 0; g.gridy = 0;
        panel.add(new JLabel("Current password:"), g);
        g.gridx = 1;
        panel.add(current, g);

        g.gridx = 0; g.gridy++;
        panel.add(new JLabel("New password:"), g);
        g.gridx = 1;
        panel.add(nw, g);

        g.gridx = 0; g.gridy++;
        panel.add(new JLabel("Confirm new password:"), g);
        g.gridx = 1;
        panel.add(confirm, g);

        int option = JOptionPane.showConfirmDialog(this, panel, "Change Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            String cur = new String(current.getPassword());
            String n = new String(nw.getPassword());
            String c = new String(confirm.getPassword());

            if (n.length() < 6) {
                JOptionPane.showMessageDialog(this, "New password must be at least 6 characters.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!n.equals(c)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // TODO: verify current password and update in DB.
            // Example:
            // boolean ok = AuthService.changePassword(admin.id, cur, n);
            // if (!ok) { show error }
            // For demo, assume success:
            JOptionPane.showMessageDialog(this, "Password changed successfully.");
        }
    }

    // Simple in-memory Admin record - replace with your model
    private static class Admin {
        int id;
        String name;
        String email;
        String phone;
        String role;

        Admin(int id, String name, String email, String phone, String role) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.role = role;
        }
    }

    // Optional: expose a method to load a real admin object
    public void loadAdmin(Admin a) {
        if (a == null) return;
        this.admin = a;
        nameField.setText(a.name);
        emailField.setText(a.email);
        phoneField.setText(a.phone);
        roleField.setText(a.role);
        avatarLabel.setText(initialsOf(a.name));
    }
}
