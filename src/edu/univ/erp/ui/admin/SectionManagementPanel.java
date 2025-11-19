package edu.univ.erp.ui.admin;

import edu.univ.erp.data.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Vector;

/**
 * Section management UI: list sections, edit capacity/day/semester/year/registration_deadline, delete.
 * Table: sections(section_id PK, course_id, instructor_id, day, capacity, semester, year, registration_deadline)
 */
public class SectionManagementPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JButton btnRefresh, btnEdit, btnDelete;

    public SectionManagementPanel() {
        initUI();
        loadSections();
    }

    private void initUI() {
        setLayout(new BorderLayout(8,8));
        JLabel title = new JLabel("Section Management");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"Section ID","Course ID","Instructor ID","Day","Capacity","Semester","Year","Reg Deadline"}, 0) {
            @Override public boolean isCellEditable(int r, int c){ return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        btnRefresh = new JButton("Refresh");
        btnEdit = new JButton("Edit Selected");
        btnDelete = new JButton("Delete Selected");

        addRight(right, btnRefresh);
        addRight(right, btnEdit);
        addRight(right, btnDelete);

        add(right, BorderLayout.EAST);

        btnRefresh.addActionListener(e -> loadSections());
        btnEdit.addActionListener(e -> openEditDialog());
        btnDelete.addActionListener(e -> deleteSelected());
    }

    private void addRight(JPanel p, JComponent c) {
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(Box.createVerticalStrut(8));
        p.add(c);
    }

    private void loadSections() {
        model.setRowCount(0);
        String sql = "SELECT section_id, course_id, instructor_id, day, capacity, semester, year, registration_deadline FROM sections ORDER BY section_id";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("section_id"));
                row.add(rs.getString("course_id"));
                row.add(rs.getObject("instructor_id"));
                row.add(rs.getString("day"));
                row.add(rs.getInt("capacity"));
                row.add(rs.getString("semester"));
                row.add(rs.getInt("year"));
                Timestamp ts = rs.getTimestamp("registration_deadline");
                row.add(ts == null ? "" : sdf.format(ts));
                model.addRow(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load sections.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openEditDialog() {
        int sel = table.getSelectedRow();
        if (sel < 0) { JOptionPane.showMessageDialog(this, "Select a section."); return; }
        int sectionId = Integer.parseInt(model.getValueAt(sel,0).toString());
        String courseId = model.getValueAt(sel,1).toString();
        String instr = String.valueOf(model.getValueAt(sel,2));
        String day = String.valueOf(model.getValueAt(sel,3));
        String capacity = model.getValueAt(sel,4).toString();
        String semester = String.valueOf(model.getValueAt(sel,5));
        String year = model.getValueAt(sel,6).toString();
        String regDeadline = String.valueOf(model.getValueAt(sel,7));

        JTextField tfDay = new JTextField(day);
        JTextField tfCapacity = new JTextField(capacity);
        JTextField tfSemester = new JTextField(semester);
        JTextField tfYear = new JTextField(year);
        JTextField tfDeadline = new JTextField(regDeadline); // format yyyy-MM-dd HH:mm:ss
        JTextField tfInstructorUserId = new JTextField(instr.equals("null") ? "" : instr);

        JPanel p = new JPanel(new GridLayout(0,2,6,6));
        p.add(new JLabel("Section ID:")); p.add(new JLabel(String.valueOf(sectionId)));
        p.add(new JLabel("Course ID:")); p.add(new JLabel(courseId));
        p.add(new JLabel("Instructor user_id (optional):")); p.add(tfInstructorUserId);
        p.add(new JLabel("Day:")); p.add(tfDay);
        p.add(new JLabel("Capacity:")); p.add(tfCapacity);
        p.add(new JLabel("Semester:")); p.add(tfSemester);
        p.add(new JLabel("Year:")); p.add(tfYear);
        p.add(new JLabel("Registration Deadline (yyyy-MM-dd HH:mm:ss or blank):")); p.add(tfDeadline);

        int r = JOptionPane.showConfirmDialog(this, p, "Edit Section", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        String newDay = tfDay.getText().trim();
        int newCapacity;
        try { newCapacity = Integer.parseInt(tfCapacity.getText().trim()); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this,"Capacity must be number"); return; }
        String newSemester = tfSemester.getText().trim();
        int newYear;
        try { newYear = Integer.parseInt(tfYear.getText().trim()); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this,"Year must be number"); return; }
        String newDeadline = tfDeadline.getText().trim();
        String instructorUserId = tfInstructorUserId.getText().trim();

        // If instructorUserId provided, try to find instructor_id
        Integer instructorId = null;
        if (!instructorUserId.isEmpty()) {
            try (Connection conn = DBConnection.getStudentConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT instructor_id FROM instructors WHERE user_id = ? LIMIT 1")) {
                ps.setString(1, instructorUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) instructorId = rs.getInt("instructor_id");
                    else {
                        JOptionPane.showMessageDialog(this, "Instructor user_id not found.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, "Error checking instructor.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        }

        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE sections SET instructor_id = ?, day = ?, capacity = ?, semester = ?, year = ?, registration_deadline = ? WHERE section_id = ?")) {
            if (instructorId == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, instructorId);
            ps.setString(2, newDay);
            ps.setInt(3, newCapacity);
            ps.setString(4, newSemester);
            ps.setInt(5, newYear);
            if (newDeadline.isEmpty()) ps.setNull(6, Types.TIMESTAMP);
            else ps.setTimestamp(6, Timestamp.valueOf(newDeadline.replace('T',' ')));
            ps.setInt(7, sectionId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Section updated.");
            loadSections();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int sel = table.getSelectedRow();
        if (sel < 0) { JOptionPane.showMessageDialog(this, "Select a section."); return; }
        int sectionId = Integer.parseInt(model.getValueAt(sel,0).toString());
        int c = JOptionPane.showConfirmDialog(this, "Delete section " + sectionId + " ? This will remove enrollments too.", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM sections WHERE section_id = ?")) {
            ps.setInt(1, sectionId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Section deleted.");
            loadSections();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Delete failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

