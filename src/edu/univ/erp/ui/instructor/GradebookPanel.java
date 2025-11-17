package edu.univ.erp.ui.instructor;

import javax.swing.*;
import java.awt.*;

public class GradebookPanel extends JPanel {
    public GradebookPanel() {
        setLayout(new BorderLayout());
        JTable t = new JTable(new Object[][] { {"Alice","101","8","20","50"} }, new Object[] {"Name","Roll","Quiz","Mid","End"});
        add(new JScrollPane(t), BorderLayout.CENTER);
        JButton save = new JButton("Save Scores");
        save.addActionListener(e -> JOptionPane.showMessageDialog(this, "Saved (stub)"));
        add(save, BorderLayout.SOUTH);
    }
}
