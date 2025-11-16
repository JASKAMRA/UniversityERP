package edu.univ.erp.ui.instructor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * AttendancePanel
 * - Select section and session date
 * - Mark Present / Absent for each student
 * - Save attendance (TODO persist)
 */
public class AttendancePanel extends JPanel {

    private final JComboBox<String> sectionBox;
    private final JSpinner dateSpinner;
    private final JPanel listPanel;
    private final JButton saveBtn;

    // sample data
    private final List<AttendanceSection> sections = new ArrayList<>();

    public AttendancePanel() {
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Attendance");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.setOpaque(false);
        sectionBox = new JComboBox<>();
        dateSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        top.add(new JLabel("Section:"));
        top.add(sectionBox);
        top.add(new JLabel("Date:"));
        top.add(dateSpinner);
        add(top, BorderLayout.CENTER);

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        JScrollPane sp = new JScrollPane(listPanel);
        sp.setPreferredSize(new Dimension(700, 360));
        add(sp, BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveBtn = new JButton("Save Attendance");
        stylePrimary(saveBtn);
        bottom.add(saveBtn);
        add(bottom, BorderLayout.PAGE_END);

        loadSample();
        populateSections();

        sectionBox.addActionListener(e -> renderStudentList());
        saveBtn.addActionListener(e -> saveAttendance());
    }

    private void loadSample() {
        AttendanceSection s1 = new AttendanceSection("SEC101","CS101 - Intro");
        for (int i=1;i<=12;i++) s1.students.add(new AttendanceStudent("S"+i,"Student "+i,false));
        sections.add(s1);

        AttendanceSection s2 = new AttendanceSection("SEC202","CS201 - DS");
        for (int i=1;i<=8;i++) s2.students.add(new AttendanceStudent("T"+i,"Learner "+i,false));
        sections.add(s2);
    }

    private void populateSections() {
        sectionBox.removeAllItems();
        for (AttendanceSection s : sections) sectionBox.addItem(s.sectionId + " - " + s.title);
        if (sectionBox.getItemCount()>0) sectionBox.setSelectedIndex(0);
        renderStudentList();
    }

    private void renderStudentList() {
        listPanel.removeAll();
        int idx = sectionBox.getSelectedIndex(); if (idx<0) return;
        AttendanceSection s = sections.get(idx);
        JPanel header = new JPanel(new GridLayout(1,3));
        header.add(new JLabel("Student ID"));
        header.add(new JLabel("Name"));
        header.add(new JLabel("Present"));
        listPanel.add(header);
        for (AttendanceStudent st : s.students) {
            JPanel row = new JPanel(new GridLayout(1,3));
            row.add(new JLabel(st.id));
            row.add(new JLabel(st.name));
            JCheckBox cb = new JCheckBox();
            cb.setSelected(st.present);
            cb.addActionListener(e -> st.present = cb.isSelected());
            row.add(cb);
            listPanel.add(row);
        }
        listPanel.revalidate();
        listPanel.repaint();
    }

    private void saveAttendance() {
        // get date and selected section
        Date d = (Date) dateSpinner.getValue();
        int idx = sectionBox.getSelectedIndex(); if (idx<0) return;
        AttendanceSection s = sections.get(idx);

        // TODO: persist attendance records to DB for date d
        JOptionPane.showMessageDialog(this, "Attendance saved for " + s.sectionId + " on " + new java.text.SimpleDateFormat("yyyy-MM-dd").format(d) + " (demo).");
    }

    private void stylePrimary(JButton b) {
        b.setBackground(new Color(52,152,219));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
    }

    private static class AttendanceSection {
        String sectionId, title;
        List<AttendanceStudent> students = new ArrayList<>();
        AttendanceSection(String id, String title) { this.sectionId=id; this.title=title; }
    }
    private static class AttendanceStudent {
        String id, name; boolean present;
        AttendanceStudent(String id, String name, boolean pres){ this.id=id; this.name=name; this.present=pres; }
    }
}
