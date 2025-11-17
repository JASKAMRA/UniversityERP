package edu.univ.erp.ui.student;

import javax.swing.*;
import java.awt.*;

public class MyRegistrationsPanel extends JPanel {
    public MyRegistrationsPanel() {
        setLayout(new BorderLayout());
        JTable t = new JTable(new Object[][] { {"CS101","A1","Mon 9-11"} }, new Object[] {"Course","Section","Time"});
        add(new JScrollPane(t), BorderLayout.CENTER);
        JButton drop = new JButton("Drop Selected");
        drop.addActionListener(e -> JOptionPane.showMessageDialog(this, "Dropped (stub)"));
        add(drop, BorderLayout.SOUTH);
    }
}
