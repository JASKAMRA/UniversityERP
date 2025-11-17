package edu.univ.erp.ui.admin;

import javax.swing.*;
import java.awt.*;

public class UserManagementPanel extends JPanel {
    public UserManagementPanel() {
        setLayout(new BorderLayout());
        JTable t = new JTable(new Object[][] { {"admin","ADMIN"}, {"inst","INSTRUCTOR"} }, new Object[] {"Username","Role"});
        add(new JScrollPane(t), BorderLayout.CENTER);
        JButton add = new JButton("Add User");
        add.addActionListener(e -> JOptionPane.showMessageDialog(this, "Add User (stub)"));
        add(add, BorderLayout.SOUTH);
    }
}

