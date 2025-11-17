package edu.univ.erp.ui.student;

import javax.swing.*;
import java.awt.*;

public class RegisterDialog extends JDialog {
    public RegisterDialog(JFrame parent) {
        super(parent, "Register for Section", true);
        setLayout(new BorderLayout());
        add(new JLabel("Section details..."), BorderLayout.CENTER);
        JPanel p = new JPanel();
        JButton ok = new JButton("Register");
        ok.addActionListener(e -> { JOptionPane.showMessageDialog(this, "Registered (stub)"); dispose(); });
        p.add(ok);
        p.add(new JButton("Cancel"));
        add(p, BorderLayout.SOUTH);
        setSize(400,200);
        setLocationRelativeTo(parent);
    }
}

