package edu.univ.erp.ui.auth;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {
    public ChangePasswordDialog(JFrame parent) {
        super(parent, "Change Password", true);
        setLayout(new GridLayout(4,2,4,4));
        add(new JLabel("Old Password:")); add(new JPasswordField());
        add(new JLabel("New Password:")); add(new JPasswordField());
        add(new JLabel("Confirm New:")); add(new JPasswordField());
        JButton ok = new JButton("Change");
        JButton cancel = new JButton("Cancel");
        add(ok); add(cancel);
        setSize(400,200);
        setLocationRelativeTo(parent);
        cancel.addActionListener(e -> dispose());
    }
}

