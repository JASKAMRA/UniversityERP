package edu.univ.erp.ui.student;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * CourseCatalogPanel
 * - Browse courses, search/filter, view course details
 * - Register button (UI-only; TODO call registration service)
 */
public class CourseCatalogPanel extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField searchField;
    private final List<Course> courses = new ArrayList<>();

    public CourseCatalogPanel() {
        setLayout(new BorderLayout(8,8));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Course Catalog");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        JPanel top = new JPanel(new BorderLayout(8,8));
        top.setOpaque(false);
        searchField = new JTextField(30);
        searchField.setToolTipText("Search by code/title/department...");
        top.add(searchField, BorderLayout.WEST);

        JButton viewBtn = new JButton("View");
        JButton registerBtn = new JButton("Register");
        viewBtn.setFocusPainted(false); registerBtn.setFocusPainted(false);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(viewBtn); actions.add(registerBtn);
        top.add(actions, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        String[] cols = {"Course ID", "Code", "Title", "Department", "Credits", "Seats"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // actions
        viewBtn.addActionListener(e -> viewSelectedCourse());
        registerBtn.addActionListener(e -> registerSelectedCourse());

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { apply(); }
            public void removeUpdate(DocumentEvent e) { apply(); }
            public void changedUpdate(DocumentEvent e) { apply(); }
            private void apply() {
                String t = searchField.getText().trim();
                if (t.isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(t), 1,2,3));
            }
        });

        loadSample();
        refreshTable();
        table.addMouseListener(new MouseAdapter(){ public void mouseClicked(MouseEvent e){ if (e.getClickCount()==2) viewSelectedCourse(); }});
    }

    private void viewSelectedCourse() {
        int r = table.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Select a course."); return; }
        int m = table.convertRowIndexToModel(r);
        Course c = courses.get(m);
        String info = String.format("%s (%s)\nDepartment: %s\nCredits: %d\nSeats available: %d\n\nDescription:\n%s",
                c.title, c.code, c.department, c.credits, c.seats, c.description);
        JOptionPane.showMessageDialog(this, info, "Course Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void registerSelectedCourse() {
        int r = table.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Select a course to register."); return; }
        int m = table.convertRowIndexToModel(r);
        Course c = courses.get(m);
        // TODO: call registration service, check prerequisites, seat availability, etc.
        JOptionPane.showMessageDialog(this, "Requested registration for: " + c.code + " - " + c.title);
    }

    private void refreshTable() {
        SwingUtilities.invokeLater(() -> {
            model.setRowCount(0);
            for (Course c : courses) model.addRow(new Object[]{c.id, c.code, c.title, c.department, c.credits, c.seats});
        });
    }

    private void loadSample() {
        courses.clear();
        courses.add(new Course(1001, "CS101", "Intro to Computer Science", "Computer Science", 4, 12, "Intro course for CS majors."));
        courses.add(new Course(1002, "CS201", "Data Structures", "Computer Science", 3, 5, "Core data structures and algorithms."));
        courses.add(new Course(2001, "MA101", "Calculus I", "Mathematics", 4, 20, "Differential calculus."));
    }

    private static class Course {
        int id; String code, title, department; int credits, seats; String description;
        Course(int id, String code, String title, String dept, int credits, int seats, String desc){
            this.id=id; this.code=code; this.title=title; this.department=dept; this.credits=credits; this.seats=seats; this.description=desc;
        }
    }
}
