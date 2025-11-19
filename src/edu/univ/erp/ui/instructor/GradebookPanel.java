package edu.univ.erp.ui.instructor;

import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class GradebookPanel extends JPanel {

    private final InstructorService instructorService;
    private final int sectionId;
    private final String courseTitle;
    private final String instructorUserId;

    private JTable table;
    private DefaultTableModel model;
    private JButton btnSave;
    private JButton btnFinalize;

    public GradebookPanel(InstructorService service, int sectionId, String courseTitle, String instructorUserId) {
        this.instructorService = service;
        this.sectionId = sectionId;
        this.courseTitle = courseTitle;
        this.instructorUserId = instructorUserId;

        initComponents();
        loadStudents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10,10));

        model = new DefaultTableModel(
                new Object[]{"Enrollment ID","Roll No","Name","Component","Score"},0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 3 || c == 4; // Component & Score editable only
            }
        };

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSave = new JButton("Save Grade");
        btnFinalize = new JButton("Finalize Grades");

        bottom.add(btnSave);
        bottom.add(btnFinalize);
        add(bottom, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> saveSelectedGrade());
        btnFinalize.addActionListener(e -> finalizeSection());
    }

    private void loadStudents() {
        model.setRowCount(0);
        List<Map<String,Object>> rows = instructorService.getStudentsInSection(sectionId);
        for (Map<String,Object> r : rows) {
            model.addRow(new Object[]{
                    r.get("enrollment_id"),
                    r.get("roll_no"),
                    r.get("name"),
                    "",   // component
                    ""    // score
            });
        }
    }

    private void saveSelectedGrade() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a student row.", "No row", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!instructorService.isInstructorOfSection(instructorUserId, sectionId)) {
            JOptionPane.showMessageDialog(this, "Not your section.", "Permission denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int enrollmentId = Integer.parseInt(model.getValueAt(row, 0).toString());
        String comp = model.getValueAt(row, 3).toString().trim();
        String scoreStr = model.getValueAt(row, 4).toString().trim();

        if (comp.isEmpty() || scoreStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter component and score.", "Missing fields", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal score;
        try {
            score = new BigDecimal(scoreStr);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid score format.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean ok = instructorService.saveGrade(enrollmentId, comp.toUpperCase(), score);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Grade saved.");
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to save grade.\nThe section might be finalized or a final grade already exists.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void finalizeSection() {
        if (!instructorService.isInstructorOfSection(instructorUserId, sectionId)) {
            JOptionPane.showMessageDialog(this, "Not your section.", "Permission denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure? After this you cannot add/edit grades.",
                "Confirm Finalize",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = instructorService.finalizeGrades(sectionId);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Grades finalized.");
        } else {
            JOptionPane.showMessageDialog(this, "Error finalizing grades.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
