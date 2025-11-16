package edu.univ.erp.ui.auth;

import javax.swing.*;
import java.awt.*;
// In dono ko import karna zaroori hai
import edu.univ.erp.MainApplication; // MainApplication ko call karne ke liye
import java.awt.event.ActionEvent; // Click event ke liye

/**
 * Login screen ka UI panel.
 * [Project Brief Ref: 22, 58]
 */
public class LoginPanel extends JPanel {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginPanel() {
        // ... (Aapka saara puraana layout code yahaan hai) ...
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); 

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Login"));

        // ... (Username/Password fields add karne ka code) ...
        // ... (usernameField, passwordField) ...
        
        // Row 3: Login Button (Yahaan update karna hai)
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginButton = new JButton("Login");
        
        // --- YEH NAYI LINE ADD KAREIN ---
        // Button ko batana ki click hone par kaunsa method call karna hai
        loginButton.addActionListener(e -> onLoginClicked()); 
        // ---------------------------------
        
        formPanel.add(loginButton, gbc);
        add(formPanel);
    }

    // --- YEH NAYA METHOD POORA ADD KAREIN ---
    
    /**
     * Yeh method tab call hota hai jab Login button click hota hai.
     */
    private void onLoginClicked() {
        // Step 1: Fields se data nikaalna
        String username = getUsername();
        String password = new String(getPassword()); // Password ko char[] se String mein convert karna

        // --- TEMPORARY LOGIN LOGIC ---
        // (Asli logic 'auth' package mein banega jo DB se check karega)
        // Hum abhi role ke hisaab se panel switch kar rahe hain 

        if (username.equals("student") && password.equals("pass")) {
            // Agar student hai, toh Student Dashboard dikhao
            MainApplication.switchToPanel("STUDENT_DASHBOARD");

        } else if (username.equals("instructor") && password.equals("pass")) {
            // Agar instructor hai, toh Instructor Dashboard dikhao
            MainApplication.switchToPanel("INSTRUCTOR_DASHBOARD");

        } else if (username.equals("admin") && password.equals("pass")) {
            // Agar admin hai, toh Admin Dashboard dikhao
            MainApplication.switchToPanel("ADMIN_DASHBOARD");

        } else {
            // Agar galat hai, toh error message dikhao [cite: 23, 109]
            JOptionPane.showMessageDialog(
                this, // Is panel ke upar dikhao
                "Incorrect username or password.", // Document ka suggested message [cite: 109]
                "Login Failed", // Title
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    // ----------------------------------------

    // --- Public methods (Yeh pehle se hain) ---
    public String getUsername() {
        return usernameField.getText();
    }

    public char[] getPassword() {
        return passwordField.getPassword();
    }

    public JButton getLoginButton() {
        return loginButton;
    }
}
