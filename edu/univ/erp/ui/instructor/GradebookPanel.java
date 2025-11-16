package edu.univ.erp.ui.instructor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * GradebookPanel
 * - Choose a section
 * - Edit student grades inline (table)
 * - Save grades (TODO: persist)
 */
public class GradebookPanel extends JPanel {
    private final JComboBox<String> sectionBox;
    private final DefaultTableModel model;
    private final JTable table;
    private final JButton saveBtn;
    private final List<SectionData> sampleSections = new ArrayList<>();

    public GradebookPanel() {
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setBackground(Color.WHITE);

        JPanel top = new JPanel(new BorderLayout(8,8));
        top.setOpaque(false);
        JLabel title = new JLabel("Gradebook");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        top.add(title, BorderLayout.WEST);

        sectionBox = new JComboBox<>();
        top.add(sectionBox, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        String[] cols = {"Student ID", "Name", "Continuous Assessment", "Midterm", "Final", "Total", "Grade"};
        model = new DefaultTableModel(cols,0) {
            @Override public boolean isCellEditable(int r, int c) { return c>=2 && c<=4; } // editable for numeric grades
        };
        table = new JTable(model);
        table.setRowHeight(26);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveBtn = new JButton("Save Grades");
        stylePrimary(saveBtn);
        bottom.add(saveBtn);
        add(bottom, BorderLayout.SOUTH);

        loadSampleData();
        populateSections();

        sectionBox.addActionListener(e -> loadSelectedSectionGrades());
        saveBtn.addActionListener(e -> saveGrades());

        // recalc total when cell edited
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.getDefaultEditor(Object.class).addCellEditorListener(new javax.swing.event.CellEditorListener() {
            public void editingStopped(javax.swing.event.ChangeEvent e) {
                recalcRow((TableCellEditor)e.getSource());
            }
            public void editingCanceled(javax.swing.event.ChangeEvent e) { }
        });
    }

    private void loadSampleData() {
        // sample
        SectionData s1 = new SectionData("SEC101","CS101 - Intro to CS");
        for (int i=1;i<=8;i++) s1.students.add(new StudentGrade("S"+i,"Student "+i, 12+i%3, 20+i%5, 45+i%6));
        sampleSections.add(s1);

        SectionData s2 = new SectionData("SEC202","CS201 - Data Structures");
        for (int i=1;i<=6;i++) s2.students.add(new StudentGrade("T"+i,"Learner "+i, 10+i%4, 18+i%6, 40+i%3));
        sampleSections.add(s2);
    }

    private void populateSections() {
        sectionBox.removeAllItems();
        for (SectionData sd : sampleSections) sectionBox.addItem(sd.sectionId + " - " + sd.title);
        if (sectionBox.getItemCount()>0) sectionBox.setSelectedIndex(0);
        loadSelectedSectionGrades();
    }

    private void loadSelectedSectionGrades() {
        model.setRowCount(0);
        int idx = sectionBox.getSelectedIndex();
        if (idx < 0) return;
        SectionData sd = sampleSections.get(idx);
        for (StudentGrade sg : sd.students) {
            int total = sg.ca + sg.midterm + sg.finalExam;
            String grade = calcLetter(total);
            model.addRow(new Object[]{sg.studentId, sg.name, sg.ca, sg.midterm, sg.finalExam, total, grade});
        }
    }

    private void recalcRow(TableCellEditor editor) {
        try {
            int row = table.getEditingRow();
            if (row == -1) return;
            Object caObj = model.getValueAt(row,2);
            Object mObj = model.getValueAt(row,3);
            Object fObj = model.getValueAt(row,4);
            int ca = parseIntSafe(caObj);
            int m = parseIntSafe(mObj);
            int f = parseIntSafe(fObj);
            int total = ca + m + f;
            model.setValueAt(total, row, 5);
            model.setValueAt(calcLetter(total), row, 6);
        } catch (Exception ignored) {}
    }

    private int parseIntSafe(Object o) {
        if (o == null) return 0;
        try { return Integer.parseInt(o.toString().trim()); } catch (Exception ex) { return 0; }
    }

    private String calcLetter(int total) {
        if (total >= 85) return "A";
        if (total >= 70) return "B";
        if (total >= 55) return "C";
        if (total >= 40) return "D";
        return "F";
    }

    private void saveGrades() {
        int idx = sectionBox.getSelectedIndex(); if (idx<0) return;
        SectionData sd = sampleSections.get(idx);

        // collect table values back into sd.students (in real app: persist to DB)
        for (int r=0;r<model.getRowCount();r++) {
            String sid = (String) model.getValueAt(r,0);
            int ca = parseIntSafe(model.getValueAt(r,2));
            int mid = parseIntSafe(model.getValueAt(r,3));
            int fin = parseIntSafe(model.getValueAt(r,4));
            // find student
            sd.students.stream().filter(s->s.studentId.equals(sid)).findFirst().ifPresent(sg->{
                sg.ca = ca; sg.midterm = mid; sg.finalExam = fin; sg.total = ca+mid+fin; sg.grade = calcLetter(sg.total);
            });
        }

        // TODO: persist grades to DB here
        JOptionPane.showMessageDialog(this, "Grades saved (demo). Replace TODO with DB code.");
    }

    private void stylePrimary(JButton b) {
        b.setBackground(new Color(52,152,219));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
    }

    // data holders
    private static class SectionData {
        String sectionId, title;
        List<StudentGrade> students = new ArrayList<>();
        SectionData(String sectionId, String title){ this.sectionId=sectionId; this.title=title; }
    }

    private static class StudentGrade {
        String studentId, name;
        int ca, midterm, finalExam, total;
        String grade;
        StudentGrade(String sid,String name,int ca,int mid,int fin){ this.studentId=sid; this.name=name; this.ca=ca; this.midterm=mid; this.finalExam=fin; this.total=ca+mid+fin; this.grade=""; }
    }
}
