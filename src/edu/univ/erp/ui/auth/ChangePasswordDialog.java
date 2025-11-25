package edu.univ.erp.ui.auth;

import edu.univ.erp.data.DBConnection;
import edu.univ.erp.auth.PasswordUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Modal dialog for changing a user's password.
 *
 * Usage:
 *    ChangePasswordDialog dlg = new ChangePasswordDialog(owner);
 *    dlg.setLocationRelativeTo(owner);
 *    dlg.setVisible(true);
 */
public class ChangePasswordDialog extends JDialog {
    private final JTextField tfUsername = new JTextField(20);
    private final JPasswordField pfOld = new JPasswordField(20);
    private final JPasswordField pfNew = new JPasswordField(20);
    private final JPasswordField pfConfirm = new JPasswordField(20);
    private final JButton btnChange = new JButton("Change Password");
    private final JButton btnCancel = new JButton("Cancel");

    public ChangePasswordDialog(Window owner) {
        super(owner, "Change Password", ModalityType.APPLICATION_MODAL);
        init();
        pack();
    }

    private void init() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(12,12,12,12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        int r = 0;
        c.gridx = 0; c.gridy = r; panel.add(new JLabel("Username:"), c);
        c.gridx = 1; panel.add(tfUsername, c); r++;

        c.gridx = 0; c.gridy = r; panel.add(new JLabel("Existing password:"), c);
        c.gridx = 1; panel.add(pfOld, c); r++;

        c.gridx = 0; c.gridy = r; panel.add(new JLabel("New password:"), c);
        c.gridx = 1; panel.add(pfNew, c); r++;

        c.gridx = 0; c.gridy = r; panel.add(new JLabel("Confirm new password:"), c);
        c.gridx = 1; panel.add(pfConfirm, c); r++;

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.add(btnCancel);
        btns.add(btnChange);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(btns, BorderLayout.SOUTH);

        // Actions
        btnCancel.addActionListener(e -> {
            setVisible(false);
            dispose();
        });

        btnChange.addActionListener(e -> doChangePassword());
        getRootPane().setDefaultButton(btnChange);
    }

    private void doChangePassword() {
        String username = tfUsername.getText().trim();
        String oldPw = new String(pfOld.getPassword());
        String newPw = new String(pfNew.getPassword());
        String confirmPw = new String(pfConfirm.getPassword());

        if (username.isEmpty() || oldPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!newPw.equals(confirmPw)) {
            JOptionPane.showMessageDialog(this, "New password and confirmation do not match.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (newPw.length() < 6) {
            JOptionPane.showMessageDialog(this, "New password must be at least 6 characters.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // You can add stronger password rules here (uppercase, digits, symbols etc.)

        // DB: verify username and existing password, then update
        try (Connection conn = DBConnection.getAuthConnection()) {
            // 1) fetch existing hash
            String q = "SELECT password_hash FROM users_auth WHERE username = ? LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(q)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(this, "Username not found.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String storedHash = rs.getString("password_hash");
                    if (storedHash == null || storedHash.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "No password set on the account. Contact admin.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    // verify old password
                    boolean ok = PasswordUtil.verify(oldPw, storedHash);
                    if (!ok) {
                        JOptionPane.showMessageDialog(this, "Existing password does not match.", "Authentication failed", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            // 2) update hash with new password
            String newHash = PasswordUtil.hash(newPw); // uses BCrypt
            String u = "UPDATE users_auth SET password_hash = ? WHERE username = ?";
            try (PreparedStatement ups = conn.prepareStatement(u)) {
                ups.setString(1, newHash);
                ups.setString(2, username);
                int rows = ups.executeUpdate();
                if (rows == 1) {
                    JOptionPane.showMessageDialog(this, "Password changed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    setVisible(false);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update password. Contact admin.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

