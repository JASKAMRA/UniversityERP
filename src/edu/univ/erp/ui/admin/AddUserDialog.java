package edu.univ.erp.ui.admin;

import javax.swing.*;
import java.awt.*;

public class AddUserDialog extends JDialog {
    public AddUserDialog(JFrame parent) {
        super(parent, "Add User", true);
        setLayout(new GridLayout(5,2,4,4));
        add(new JLabel("Username:")); add(new JTextField());
        add(new JLabel("Role:")); add(new JComboBox<>(new String[]{"STUDENT","INSTRUCTOR","ADMIN"}));
        JButton ok = new JButton("Create");
        ok.addActionListener(e -> { JOptionPane.showMessageDialog(this, "Created (stub)"); dispose(); });
        add(ok);
        setSize(400,200);
        setLocationRelativeTo(parent);
    }
}

