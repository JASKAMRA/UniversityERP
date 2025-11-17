package edu.univ.erp.ui.student;

import javax.swing.*;
import java.awt.*;

public class TranscriptPanel extends JPanel {
    public TranscriptPanel() {
        setLayout(new BorderLayout());
        add(new JLabel("Transcript Preview (stub)"), BorderLayout.CENTER);
        JButton download = new JButton("Download CSV");
        download.addActionListener(e -> JOptionPane.showMessageDialog(this, "Downloaded (stub)"));
        add(download, BorderLayout.SOUTH);
    }
}
