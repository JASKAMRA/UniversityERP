package edu.univ.erp.ui.student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;

/**
 * GradesPanel
 * - Lists course grades, shows GPA calculation (student-side)
 */
public class GradesPanel extends JPanel {
    private final DefaultTableModel model;
    private final JLabel lblGPA = new JLabel("0.00");

    public GradesPanel() {
        setLayout(new BorderLayout(8,8));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Grades");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"Course","Term","Credits","Grade","Grade Points"},0) {
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(26);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(new JLabel("Current GPA: "));
        lblGPA.setFont(new Font("Segoe UI", Font.BOLD, 16));
        bottom.add(lblGPA);
        add(bottom, BorderLayout.SOUTH);

        loadSample();
    }

    private void loadSample() {
        model.setRowCount(0);
        addRow("CS101", "Fall 2025", 4, "A", 10.0);
        addRow("CS201", "Fall 2025", 3, "B", 8.0);
        addRow("MA101", "Spring 2025", 4, "A", 10.0);
        computeGPA();
    }

    private void addRow(String course, String term, int credits, String grade, double points) {
        model.addRow(new Object[]{course, term, credits, grade, points});
    }

    private void computeGPA() {
        int totalCredits = 0;
        double totalPoints = 0;
        for (int r = 0; r < model.getRowCount(); r++) {
            int cr = (int) model.getValueAt(r, 2);
            double gp = (double) model.getValueAt(r, 4);
            totalCredits += cr;
            totalPoints += gp * cr;
        }
        double gpa = totalCredits == 0 ? 0.0 : (totalPoints / totalCredits);
        lblGPA.setText(String.format("%.2f", gpa));
    }
}
