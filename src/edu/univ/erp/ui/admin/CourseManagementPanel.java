package edu.univ.erp.ui.admin;

import javax.swing.*;
import java.awt.*;

public class CourseManagementPanel extends JPanel {
    public CourseManagementPanel() {
        setLayout(new BorderLayout());
        JTable t = new JTable(new Object[][] { {"CS101","Intro"} }, new Object[] {"Code","Title"});
        add(new JScrollPane(t), BorderLayout.CENTER);
        JButton add = new JButton("Add Course");
        add.addActionListener(e -> JOptionPane.showMessageDialog(this, "Add Course"));
        add(add, BorderLayout.SOUTH);
    }
}

