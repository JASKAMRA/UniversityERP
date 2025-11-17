package edu.univ.erp.ui.auth;

import edu.univ.erp.ui.common.MainFrame;
import edu.univ.erp.ui.common.MessageDialog;
import edu.univ.erp.ui.util.AuthService;
import edu.univ.erp.ui.util.CurrentSession;
import edu.univ.erp.ui.util.CurrentUser;
import edu.univ.erp.ui.util.SettingsService;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private MainFrame main;
    private JTextField txtUsername = new JTextField(20);
    private JPasswordField txtPassword = new JPasswordField(20);
    private JButton btnLogin = new JButton("Login");

    public LoginPanel(MainFrame main) {
        this.main = main;
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4,4,4,4);
        c.gridx = 0; c.gridy = 0; add(new JLabel("Username:"), c);
        c.gridx = 1; add(txtUsername, c);
        c.gridx = 0; c.gridy = 1; add(new JLabel("Password:"), c);
        c.gridx = 1; add(txtPassword, c);
        c.gridx = 1; c.gridy = 2; add(btnLogin, c);

        btnLogin.addActionListener(e -> onLogin());
    }

    private void onLogin() {
        String u = txtUsername.getText().trim();
        String p = new String(txtPassword.getPassword());

        if (u.isEmpty() || p.isEmpty()) {
            MessageDialog.showError(this, "Enter username and password");
            return;
        }

        try {
            AuthService.AuthResult res = AuthService.authenticate(u, p);
            if (!res.success) {
                MessageDialog.showError(this, res.message);
                return;
            }
            // load settings
            boolean maintenance = SettingsService.isMaintenanceOn();

            // create session user
            CurrentUser cu = new CurrentUser(res.userId, res.role, res.profile);
            CurrentSession.get().setUser(cu);
            CurrentSession.get().setMaintenance(maintenance);

            // show main
            main.showForUser(cu);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageDialog.showError(this, "Login error: " + ex.getMessage());
        }
    }
}
