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
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

/**
 * LoginPanel that calls AuthServiceBackend (directly) instead of a ui adapter.
 */
public class LoginPanel extends JPanel {
    private MainFrame main;
    private JTextField txtUsername = new JTextField(20);
    private JPasswordField txtPassword = new JPasswordField(20);
    private JButton btnLogin = new JButton("Login");

    // --- Aesthetic constants ---
    private static final int PADDING = 30;
    private static final int GAP = 15; // Increased gap
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 28); // Increased size
    private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 14);
    private static final Dimension FIELD_SIZE = new Dimension(280, 35); // Consistent field size
    private static final Color PRIMARY_COLOR = new Color(30, 80, 130); // Darker, more corporate blue
    private static final Color BORDER_COLOR = new Color(200, 200, 200);

    public LoginPanel(MainFrame main) {
        this.main = main;
        
        // 1. Overall Layout & Styling
        // Use BorderLayout on the main panel to center the login form (formWrapper)
        setLayout(new GridBagLayout()); // Using a central GridBagLayout simplifies centering
        setBackground(Color.WHITE);
        
        // 2. Create the wrapper panel for the form elements
        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setBackground(Color.WHITE);
        formWrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(PADDING, PADDING, PADDING, PADDING)
        ));

        // 3. Create the centered form panel (GridBagLayout for structure)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(GAP / 2, GAP, GAP / 2, GAP);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        // --- Title/Header ---
        JLabel title = new JLabel("🏛️ ERP Access Portal");
        title.setFont(TITLE_FONT);
        title.setForeground(PRIMARY_COLOR);
        title.setBorder(new EmptyBorder(0, 0, GAP * 2, 0)); // Extra space below title
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; c.anchor = GridBagConstraints.CENTER;
        formPanel.add(title, c);
        
        // --- Username Label ---
        c.gridx = 0; c.gridy = 1; c.gridwidth = 1; c.weightx = 0; c.anchor = GridBagConstraints.WEST;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(LABEL_FONT);
        formPanel.add(userLabel, c);
        
        // --- Username Field ---
        c.gridx = 1; c.gridy = 1; c.weightx = 1.0;
        txtUsername.setPreferredSize(FIELD_SIZE);
        formPanel.add(txtUsername, c);

        // --- Password Label ---
        c.gridx = 0; c.gridy = 2; c.weightx = 0;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(LABEL_FONT);
        formPanel.add(passLabel, c);
        
        // --- Password Field ---
        c.gridx = 1; c.gridy = 2; c.weightx = 1.0;
        txtPassword.setPreferredSize(FIELD_SIZE);
        formPanel.add(txtPassword, c);

        // --- Login Button ---
        btnLogin.setFont(btnLogin.getFont().deriveFont(Font.BOLD, 16f));
        btnLogin.setBackground(PRIMARY_COLOR);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setPreferredSize(new Dimension(FIELD_SIZE.width, 45)); // Larger button
        
        c.gridx = 1; c.gridy = 3; c.gridwidth = 1; c.anchor = GridBagConstraints.EAST; 
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(GAP * 2, GAP, 0, GAP); // Extra space above button
        formPanel.add(btnLogin, c);
        
        // Add the form panel to the wrapper
        formWrapper.add(formPanel, BorderLayout.CENTER);

        // Add the wrapper to the main container (GridBagLayout centers content automatically)
        add(formWrapper, new GridBagConstraints());

        // 4. Action Listener
        btnLogin.addActionListener(e -> onLogin());
    }

    private void onLogin() {
        String u = txtUsername.getText().trim();
        char[] passChars = txtPassword.getPassword();
        String p = new String(passChars);

        if (u.isEmpty() || p.isEmpty()) {
            MessageDialog.showError(this, "Enter username and password");
            // zero-out password chars
            Arrays.fill(passChars, '\0');
            return;
        }

        try {
            AuthServiceBackend backend = new AuthServiceBackend();
            LoginResult lr = backend.login(u, p);

            // zero-out password chars immediately after use
            Arrays.fill(passChars, '\0');

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

            // Build a UserProfile (UI-friendly) from backend result (Logic Unchanged):
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
                // Fallback for generic user or partially loaded profile
                profileForUI = new UserProfile(user.GetUsername(), user.GetEmail()); 
            } else {
                profileForUI = new UserProfile(u, "");
            }

            // Now create CurrentUser expected by rest of UI
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