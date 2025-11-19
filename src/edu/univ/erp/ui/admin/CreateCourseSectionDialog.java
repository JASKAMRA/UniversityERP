package edu.univ.erp.ui.admin;

import edu.univ.erp.service.AdminService;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog to create a course (if needed) and section assigned to an instructor (instructor's user_id).
 */
public class CreateCourseSectionDialog extends JDialog {
    private final AdminService adminService;
    private JTextField tfCourseId, tfCourseTitle, tfCredits, tfDepartment;
    private JTextField tfCapacity, tfDay, tfSemester, tfYear, tfInstructorUserId;
    private boolean succeeded = false;
    private int createdSectionId = -1;

    public CreateCourseSectionDialog(Window owner, AdminService adminService) {
        super(owner, "Create Course & Section", ModalityType.APPLICATION_MODAL);
        this.adminService = adminService;
        init();
        pack();
    }

    private void init() {
        setLayout(new BorderLayout(8,8));
        JPanel form = new JPanel(new GridLayout(0,2,6,6));

        tfCourseId = new JTextField();
        tfCourseTitle = new JTextField();
        tfCredits = new JTextField();
        tfDepartment = new JTextField();

        tfCapacity = new JTextField("30");
        tfDay = new JTextField("MONDAY");
        tfSemester = new JTextField("Fall");
        tfYear = new JTextField(String.valueOf(java.time.Year.now().getValue()));
        tfInstructorUserId = new JTextField();

        form.add(new JLabel("Course ID:")); form.add(tfCourseId);
        form.add(new JLabel("Course Title:")); form.add(tfCourseTitle);
        form.add(new JLabel("Credits:")); form.add(tfCredits);
        form.add(new JLabel("Department ID:")); form.add(tfDepartment);

        form.add(new JLabel("Capacity:")); form.add(tfCapacity);
        form.add(new JLabel("Day (MONDAY...):")); form.add(tfDay);
        form.add(new JLabel("Semester:")); form.add(tfSemester);
        form.add(new JLabel("Year:")); form.add(tfYear);
        form.add(new JLabel("Instructor user_id:")); form.add(tfInstructorUserId);

        add(form, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCancel = new JButton("Cancel");
        JButton btnCreate = new JButton("Create");
        bottom.add(btnCancel); bottom.add(btnCreate);
        add(bottom, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> { succeeded = false; setVisible(false); dispose(); });
        btnCreate.addActionListener(e -> doCreate());
    }

    private void doCreate() {
        String courseId = tfCourseId.getText().trim();
        String title = tfCourseTitle.getText().trim();
        Integer credits = null;
        try { if (!tfCredits.getText().trim().isEmpty()) credits = Integer.parseInt(tfCredits.getText().trim()); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this,"Credits must be a number"); return; }
        String dept = tfDepartment.getText().trim();

        int capacity;
        try { capacity = Integer.parseInt(tfCapacity.getText().trim()); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this,"Capacity must be a number"); return; }
        String day = tfDay.getText().trim();
        String sem = tfSemester.getText().trim();
        int year;
        try { year = Integer.parseInt(tfYear.getText().trim()); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this,"Year must be a number"); return; }
        String instructorUserId = tfInstructorUserId.getText().trim();

        if (courseId.isEmpty() || instructorUserId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Course ID and Instructor user_id are required.", "Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int secId = adminService.createCourseAndSection(courseId, title, credits, dept, capacity, day, sem, year, instructorUserId);
            if (secId > 0) {
                succeeded = true;
                createdSectionId = secId;
                JOptionPane.showMessageDialog(this, "Section created: " + secId, "Success", JOptionPane.INFORMATION_MESSAGE);
                setVisible(false);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create section. Instructor may not exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSucceeded() { return succeeded; }
    public int getCreatedSectionId() { return createdSectionId; }
}
