package edu.univ.erp.ui.instructor;

import javax.swing.*;
import java.awt.*;

public class CSVImportExportDialog extends JDialog {
    public CSVImportExportDialog(JFrame parent) {
        super(parent, "Import/Export CSV", true);
        setSize(400,200);
        setLayout(new BorderLayout());
        add(new JLabel("CSV Import/Export (stub)"), BorderLayout.CENTER);
        JButton ok = new JButton("Close");
        ok.addActionListener(e -> dispose());
        add(ok, BorderLayout.SOUTH);
    }
}
