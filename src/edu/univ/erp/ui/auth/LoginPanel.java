package edu.univ.erp.ui.auth;

import edu.univ.erp.ui.common.MainFrame;
import edu.univ.erp.ui.common.MessageDialog;
import edu.univ.erp.auth.AuthServiceBackend;
import edu.univ.erp.auth.AuthServiceBackend.LoginResult;
import edu.univ.erp.domain.User;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Admin;
import edu.univ.erp.domain.Role;
import edu.univ.erp.ui.util.CurrentSession;
import edu.univ.erp.ui.util.CurrentUser;
import edu.univ.erp.ui.util.SettingsService;
import edu.univ.erp.ui.util.UserProfile;

import javax.swing.*;
import java.awt.*;

/**
 * LoginPanel that calls AuthServiceBackend (directly) instead of a ui adapter.
 */
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
        char[] passChars = txtPassword.getPassword();
        String p = new String(passChars);

        if (u.isEmpty() || p.isEmpty()) {
            MessageDialog.showError(this, "Enter username and password");
            // zero-out password chars
            java.util.Arrays.fill(passChars, '\0');
            return;
        }

        try {
            AuthServiceBackend backend = new AuthServiceBackend();
            LoginResult lr = backend.login(u, p);

            // zero-out password chars immediately after use
            java.util.Arrays.fill(passChars, '\0');

            if (lr == null) {
                MessageDialog.showError(this, "Authentication service error");
                return;
            }

            if (!lr.success) {
                MessageDialog.showError(this, lr.message);
                return;
            }

            // load settings
            boolean maintenance = SettingsService.isMaintenanceOn();

            // Build a UserProfile (UI-friendly) from backend result:
            UserProfile profileForUI;
            Object profileObj = lr.profile; // may be Student, Instructor, Admin or null

            if (profileObj instanceof Student) {
                Student s = (Student) profileObj;
                profileForUI = new UserProfile(s.GetName(), s.GetEmail());
            } else if (profileObj instanceof Instructor) {
                Instructor i = (Instructor) profileObj;
                profileForUI = new UserProfile(i.GetName(), i.GetEmail());
            } else if (profileObj instanceof Admin) {
                Admin a = (Admin) profileObj;
                profileForUI = new UserProfile(a.getName(), a.getEmail());
            } else if (lr.user != null) {
                User user = lr.user;
                profileForUI = new UserProfile(user.GetUsername(), user.GetEmail());
            } else {
                profileForUI = new UserProfile(u, "");
            }

            // Now create CurrentUser expected by rest of UI
            // CurrentUser constructor: CurrentUser(String userId, Role role, UserProfile profile)
            String userId = lr.user != null ? lr.user.GetID() : null;
            Role role = lr.user != null ? lr.user.GetRole() : null;
            CurrentUser cu = new CurrentUser(userId, role, profileForUI);

            // set session
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
