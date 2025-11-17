package edu.univ.erp.ui.student;

import javax.swing.*;
import java.awt.*;

public class GradesPanel extends JPanel {
    public GradesPanel() {
        setLayout(new BorderLayout());
        JTable t = new JTable(new Object[][] { {"CS101", "Quiz: 8/10", "Final: A"} }, new Object[] {"Course","Components","Final"});
        add(new JScrollPane(t), BorderLayout.CENTER);
    }
}
