package edu.univ.erp.ui.instructor;

import javax.swing.*;
import java.awt.*;

public class MySectionsPanel extends JPanel {
    public MySectionsPanel() {
        setLayout(new BorderLayout());
        JTable t = new JTable(new Object[][] { {"CS101","A1",20} }, new Object[] {"Course","Section","Enrolled"});
        add(new JScrollPane(t), BorderLayout.CENTER);
    }
}
