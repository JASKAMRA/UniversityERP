package edu.univ.erp.ui.admin;

import javax.swing.*;
import java.awt.*;

public class SectionManagementPanel extends JPanel {
    public SectionManagementPanel() {
        setLayout(new BorderLayout());
        JTable t = new JTable(new Object[][] { {"CS101","A1","Mon 9-11"} }, new Object[] {"Course","Section","Time"});
        add(new JScrollPane(t), BorderLayout.CENTER);
    }
}
