package edu.univ.erp.ui.admin;

import edu.univ.erp.data.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

/**
 * Course management UI: list, create, edit, delete courses.
 * Table: courses(course_id PK, title, credits, department_id)
 */
public class CourseManagementPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JButton btnRefresh, btnCreate, btnEdit, btnDelete;

    public CourseManagementPanel() {
        initUI();
        loadCourses();
    }

    private void initUI() {
        setLayout(new BorderLayout(8,8));
        JLabel title = new JLabel("Course Management");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"Course ID","Title","Credits","Department"}, 0) {
            @Override public boolean isCellEditable(int r, int c){ return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        btnRefresh = new JButton("Refresh");
        btnCreate  = new JButton("Create Course");
        btnEdit    = new JButton("Edit Selected");
        btnDelete  = new JButton("Delete Selected");

        addRight(right, btnRefresh);
        addRight(right, btnCreate);
        addRight(right, btnEdit);
        addRight(right, btnDelete);

        add(right, BorderLayout.EAST);

        // actions
        btnRefresh.addActionListener(e -> loadCourses());
        btnCreate.addActionListener(e -> openCreateDialog());
        btnEdit.addActionListener(e -> openEditDialog());
        btnDelete.addActionListener(e -> deleteSelected());
    }

    private void addRight(JPanel p, JComponent c) {
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(Box.createVerticalStrut(8));
        p.add(c);
    }

    private void loadCourses() {
        model.setRowCount(0);
        String sql = "SELECT course_id, title, credits, department_id FROM courses ORDER BY course_id";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("course_id"));
                row.add(rs.getString("title"));
                row.add(rs.getObject("credits"));
                row.add(rs.getString("department_id"));
                model.addRow(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load courses.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openCreateDialog() {
        JTextField tfId = new JTextField();
        JTextField tfTitle = new JTextField();
        JTextField tfCredits = new JTextField();
        JTextField tfDept = new JTextField();

        JPanel p = new JPanel(new GridLayout(0,2,6,6));
        p.add(new JLabel("Course ID:")); p.add(tfId);
        p.add(new JLabel("Title:")); p.add(tfTitle);
        p.add(new JLabel("Credits:")); p.add(tfCredits);
        p.add(new JLabel("Department ID:")); p.add(tfDept);

        int r = JOptionPane.showConfirmDialog(this, p, "Create Course", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        String id = tfId.getText().trim();
        String title = tfTitle.getText().trim();
        Integer credits = null;
        try { if (!tfCredits.getText().trim().isEmpty()) credits = Integer.parseInt(tfCredits.getText().trim()); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Credits must be a number"); return; }
        String dept = tfDept.getText().trim();

        if (id.isEmpty() || title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Course ID and title required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO courses (course_id, title, credits, department_id) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, id);
            ps.setString(2, title);
            if (credits == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, credits);
            ps.setString(4, dept);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Course created.");
            loadCourses();
        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "Course ID already exists.", "Duplicate", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Create failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openEditDialog() {
        int sel = table.getSelectedRow();
        if (sel < 0) { JOptionPane.showMessageDialog(this, "Select a course."); return; }
        String id = model.getValueAt(sel,0).toString();
        String curTitle = model.getValueAt(sel,1).toString();
        String curCredits = String.valueOf(model.getValueAt(sel,2));
        String curDept = String.valueOf(model.getValueAt(sel,3));

        JTextField tfTitle = new JTextField(curTitle);
        JTextField tfCredits = new JTextField(curCredits.equals("null") ? "" : curCredits);
        JTextField tfDept = new JTextField(curDept);

        JPanel p = new JPanel(new GridLayout(0,2,6,6));
        p.add(new JLabel("Course ID:")); p.add(new JLabel(id));
        p.add(new JLabel("Title:")); p.add(tfTitle);
        p.add(new JLabel("Credits:")); p.add(tfCredits);
        p.add(new JLabel("Department ID:")); p.add(tfDept);

        int r = JOptionPane.showConfirmDialog(this, p, "Edit Course", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        String title = tfTitle.getText().trim();
        Integer credits = null;
        try { if (!tfCredits.getText().trim().isEmpty()) credits = Integer.parseInt(tfCredits.getText().trim()); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Credits must be a number"); return; }
        String dept = tfDept.getText().trim();

        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE courses SET title = ?, credits = ?, department_id = ? WHERE course_id = ?")) {
            ps.setString(1, title);
            if (credits == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, credits);
            ps.setString(3, dept);
            ps.setString(4, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Course updated.");
            loadCourses();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int sel = table.getSelectedRow();
        if (sel < 0) { JOptionPane.showMessageDialog(this, "Select a course."); return; }
        String id = model.getValueAt(sel,0).toString();
        int c = JOptionPane.showConfirmDialog(this, "Delete course " + id + " ? This will fail if sections reference it.", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM courses WHERE course_id = ?")) {
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) JOptionPane.showMessageDialog(this, "No course deleted — maybe it does not exist.", "Info", JOptionPane.INFORMATION_MESSAGE);
            else JOptionPane.showMessageDialog(this, "Course deleted.");
            loadCourses();
        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "Cannot delete course — referenced by sections.", "Constraint", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Delete failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
