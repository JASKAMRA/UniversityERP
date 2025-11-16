package edu.univ.erp.ui.admin;

import javax.swing.*;
import java.awt.*;

public class AssignInstructorPanel extends JPanel {
    public AssignInstructorPanel() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        JLabel lbl = new JLabel("AssignInstructorPanel (placeholder)");
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        add(lbl, BorderLayout.CENTER);
    }
}
