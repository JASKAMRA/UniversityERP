package edu.univ.erp.ui.student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * TranscriptPanel
 * - Shows semester-wise transcript and cumulative statistics
 * - Export to CSV (UI-only)
 */
public class TranscriptPanel extends JPanel {
    private final DefaultTableModel model;

    public TranscriptPanel() {
        setLayout(new BorderLayout(8,8));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Transcript");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"Semester","Course","Credits","Grade"}, 0) {
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton export = new JButton("Export CSV");
        export.addActionListener(e -> exportCsv());
        bottom.add(export);
        add(bottom, BorderLayout.SOUTH);

        loadSample();
    }

    private void loadSample() {
        model.setRowCount(0);
        model.addRow(new Object[]{"Fall 2025","CS101",4,"A"});
        model.addRow(new Object[]{"Fall 2025","CS201",3,"B"});
        model.addRow(new Object[]{"Spring 2025","MA101",4,"A"});
    }

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("transcript.csv"));
        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        try (java.io.PrintWriter pw = new java.io.PrintWriter(chooser.getSelectedFile())) {
            pw.println("Semester,Course,Credits,Grade");
            for (int r=0;r<model.getRowCount();r++) {
                pw.printf("%s,%s,%s,%s%n", model.getValueAt(r,0), model.getValueAt(r,1), model.getValueAt(r,2), model.getValueAt(r,3));
            }
            JOptionPane.showMessageDialog(this, "Transcript exported.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
