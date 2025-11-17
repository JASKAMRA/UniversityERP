package edu.univ.erp.ui.student;

import javax.swing.*;
import java.awt.*;

public class CourseCatalogPanel extends JPanel {
    public CourseCatalogPanel() {
        setLayout(new BorderLayout());
        JTable t = new JTable(new Object[][] { {"CS101","Intro",3,"A1"} , {"CS102","Data",4,"B1"} },
                new Object[] {"Code","Title","Credits","Section"});
        add(new JScrollPane(t), BorderLayout.CENTER);
        JButton register = new JButton("Register (opens dialog)");
        register.addActionListener(e -> JOptionPane.showMessageDialog(this, "Open RegisterDialog"));
        add(register, BorderLayout.SOUTH);
    }
}
