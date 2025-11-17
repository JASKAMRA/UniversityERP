package edu.univ.erp.ui.admin;

import javax.swing.*;
import java.awt.*;

public class AssignInstructorPanel extends JPanel {
    public AssignInstructorPanel() {
        setLayout(new FlowLayout());
        add(new JLabel("Section:"));
        add(new JComboBox<>(new String[] {"CS101-A1"}));
        add(new JLabel("Instructor:"));
        add(new JComboBox<>(new String[] {"inst1"}));
        JButton assign = new JButton("Assign");
        assign.addActionListener(e -> JOptionPane.showMessageDialog(this, "Assigned (stub)"));
        add(assign);
    }
}
