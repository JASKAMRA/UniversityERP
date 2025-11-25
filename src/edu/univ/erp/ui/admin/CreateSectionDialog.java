package edu.univ.erp.ui.admin;

import edu.univ.erp.data.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.sql.Timestamp;

public class CreateSectionDialog extends JDialog {

    private boolean succeeded = false;
    private int createdSectionId = -1;

    private JTextField tfCourseId = new JTextField(20);
    private JTextField tfInstructorUser = new JTextField(20);
    private JTextField tfDays = new JTextField(20);
    private JTextField tfStart = new JTextField(20);
    private JTextField tfEnd = new JTextField(20);
    private JTextField tfCapacity = new JTextField(20);
    private JTextField tfSemester = new JTextField(20);
    private JTextField tfYear = new JTextField(20);
    private JTextField tfDeadline = new JTextField(20);

    public CreateSectionDialog(Window owner) {
        super(owner, "Create Section", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initUI() {

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int r = 0;

        addRow(form, gbc, r++, "Course ID:", tfCourseId);
        addRow(form, gbc, r++, "Instructor user_id (optional):", tfInstructorUser);
        addRow(form, gbc, r++, "Days (CSV e.g. MONDAY,WEDNESDAY):", tfDays);
        addRow(form, gbc, r++, "Start Time (HH:mm):", tfStart);
        addRow(form, gbc, r++, "End Time (HH:mm):", tfEnd);
        addRow(form, gbc, r++, "Capacity:", tfCapacity);
        addRow(form, gbc, r++, "Semester:", tfSemester);
        addRow(form, gbc, r++, "Year:", tfYear);
        addRow(form, gbc, r++, "Reg Deadline (yyyy-MM-dd HH:mm:ss):", tfDeadline);

        JButton btnOk = new JButton("OK");
        JButton btnCancel = new JButton("Cancel");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(btnOk);
        btnPanel.add(btnCancel);

        btnCancel.addActionListener(e -> dispose());
        btnOk.addActionListener(e -> onCreate());

        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(btnPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnOk);
    }

    private void addRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.2;
        p.add(new JLabel(label), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        p.add(field, gbc);
    }

    private void onCreate() {
        String courseId = tfCourseId.getText().trim();
        if (courseId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Course ID required");
            return;
        }

        String instrUser = tfInstructorUser.getText().trim();
        String days = tfDays.getText().trim();
        String start = tfStart.getText().trim();
        String end = tfEnd.getText().trim();

        Integer cap;
        try { cap = Integer.parseInt(tfCapacity.getText().trim()); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid capacity"); return; }

        String sem = tfSemester.getText().trim();

        Integer year;
        try { year = Integer.parseInt(tfYear.getText().trim()); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid year"); return; }

        String deadline = tfDeadline.getText().trim();

        // ---- Instructor lookup ----
        String instructorId = null;
        if (!instrUser.isEmpty()) {
            try (Connection conn = DBConnection.getStudentConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT instructor_id FROM instructors WHERE user_id = ? LIMIT 1")) {
                ps.setString(1, instrUser);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) instructorId = String.valueOf(rs.getInt("instructor_id"));
                else { JOptionPane.showMessageDialog(this, "Instructor not found"); return; }
            } catch (Exception ex) { 
                JOptionPane.showMessageDialog(this, "Error checking instructor"); 
                return; 
            }
        }

        // ---- INSERT ----
        String sql = """
            INSERT INTO sections 
            (course_id, instructor_id, day, days, start_time, end_time, capacity, semester, year, registration_deadline)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            ps.setString(i++, courseId);
            if (instructorId == null) ps.setNull(i++, Types.VARCHAR); else ps.setString(i++, instructorId);

            String primaryDay = days.contains(",") ? days.split(",")[0].trim() : days.trim();
            ps.setString(i++, primaryDay.isEmpty() ? null : primaryDay);

            ps.setString(i++, days.isEmpty() ? null : days);
            ps.setString(i++, start.isEmpty() ? null : start);
            ps.setString(i++, end.isEmpty() ? null : end);
            ps.setInt(i++, cap);
            ps.setString(i++, sem);
            ps.setInt(i++, year);

            if (deadline.isEmpty()) ps.setNull(i++, Types.TIMESTAMP);
            else ps.setTimestamp(i++, Timestamp.valueOf(deadline));

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) createdSectionId = keys.getInt(1);
            }

            succeeded = true;
            JOptionPane.showMessageDialog(this, "Section created: " + createdSectionId);
            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Create failed: " + ex.getMessage());
        }
    }

    public boolean isSucceeded() { return succeeded; }
    public int getCreatedSectionId() { return createdSectionId; }
}
