package edu.univ.erp.ui.admin;

import edu.univ.erp.data.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class CourseManagementPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JButton btnRefresh, btnCreate, btnEdit, btnDelete;

    private static final int PADDING = 20;
    private static final int GAP = 12;
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 20);
    private static final Dimension BUTTON_SIZE = new Dimension(160, 35);
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);

    public CourseManagementPanel() {
        initUI();
        loadCourses();
    }

    private void initUI() {
        setLayout(new BorderLayout(GAP, GAP));
        setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Course Catalog Management");
        title.setFont(TITLE_FONT);
        title.setBorder(new EmptyBorder(0, 0, GAP, 0));
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"Course ID", "Title", "Credits", "Department"}, 0) {
            @Override public boolean isCellEditable(int r, int c){ return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        buttonPanel.setBackground(Color.WHITE);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(GAP / 2, 0, GAP / 2, 0);
        c.gridx = 0; c.weightx = 1.0;

        btnRefresh = new JButton("Refresh List");
        btnCreate = new JButton("Create New Course");
        btnEdit = new JButton("Edit Selected");
        btnDelete = new JButton("Delete Selected");

        styleButton(btnRefresh, Color.LIGHT_GRAY);
        styleButton(btnCreate, new Color(180, 220, 255));
        styleButton(btnEdit, new Color(255, 230, 180));
        styleButton(btnDelete, new Color(255, 180, 180));
        btnCreate.setForeground(PRIMARY_COLOR);
        btnDelete.setForeground(Color.RED);

        c.gridy = 0; buttonPanel.add(btnRefresh, c);
        c.gridy = 1; buttonPanel.add(Box.createVerticalStrut(GAP), c);
        c.gridy = 2; buttonPanel.add(btnCreate, c);
        c.gridy = 3; buttonPanel.add(btnEdit, c);
        c.gridy = 4; buttonPanel.add(btnDelete, c);

        c.gridy = 5; c.weighty = 1.0;
        buttonPanel.add(Box.createVerticalGlue(), c);

        add(buttonPanel, BorderLayout.EAST);

        btnRefresh.addActionListener(e -> loadCourses());
        btnCreate.addActionListener(e -> openCreateDialog());
        btnEdit.addActionListener(e -> openEditDialog());
        btnDelete.addActionListener(e -> deleteSelected());
    }

    private void styleButton(JButton button, Color background) {
        button.setPreferredSize(BUTTON_SIZE);
        button.setMinimumSize(BUTTON_SIZE);
        button.setMaximumSize(BUTTON_SIZE);
        button.setFocusPainted(false);
        button.setBackground(background);
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
            JOptionPane.showMessageDialog(this, "Failed to load courses: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openCreateDialog() {
        JTextField tfId = new JTextField(15);
        JTextField tfTitle = new JTextField(15);
        JTextField tfCredits = new JTextField(15);
        JTextField tfDept = new JTextField(15);

        JPanel p = createFormPanel(
            new String[]{"Course ID:", "Title:", "Credits:", "Department ID:"},
            new JComponent[]{tfId, tfTitle, tfCredits, tfDept}
        );

        int r = JOptionPane.showConfirmDialog(this, p, "Create New Course", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;

        String id = tfId.getText().trim();
        String title = tfTitle.getText().trim();
        Integer credits = null;
        try {
            if (!tfCredits.getText().trim().isEmpty()) {
                credits = Integer.parseInt(tfCredits.getText().trim());
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Credits must be a valid whole number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String dept = tfDept.getText().trim();

        if (id.isEmpty() || title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Course ID and title are required fields.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO courses (course_id, title, credits, department_id) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, id);
            ps.setString(2, title);
            if (credits == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, credits);
            ps.setString(4, dept);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Course " + id + " created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadCourses();
        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "Course ID already exists or Department ID is invalid.", "Duplicate/Invalid Data", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Create failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openEditDialog() {
        int selView = table.getSelectedRow();
        if (selView < 0) { JOptionPane.showMessageDialog(this, "Please select a course to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE); return; }

        int modelRow = table.convertRowIndexToModel(selView);

        String id = model.getValueAt(modelRow,0).toString();
        String curTitle = model.getValueAt(modelRow,1).toString();
        String curCredits = String.valueOf(model.getValueAt(modelRow,2));
        String curDept = String.valueOf(model.getValueAt(modelRow,3));

        JTextField tfTitle = new JTextField(curTitle, 15);
        JTextField tfCredits = new JTextField(curCredits.equals("null") ? "" : curCredits, 15);
        JTextField tfDept = new JTextField(curDept.equals("null") ? "" : curDept, 15);
        JLabel lblId = new JLabel(id);

        JPanel p = createFormPanel(
            new String[]{"Course ID:", "Title:", "Credits:", "Department ID:"},
            new JComponent[]{lblId, tfTitle, tfCredits, tfDept}
        );

        int r = JOptionPane.showConfirmDialog(this, p, "Edit Course: " + id, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;

        String title = tfTitle.getText().trim();
        Integer credits = null;
        try {
            if (!tfCredits.getText().trim().isEmpty()) {
                credits = Integer.parseInt(tfCredits.getText().trim());
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Credits must be a valid whole number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String dept = tfDept.getText().trim();

        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE courses SET title = ?, credits = ?, department_id = ? WHERE course_id = ?")) {
            ps.setString(1, title);
            if (credits == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, credits);
            ps.setString(3, dept);
            ps.setString(4, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Course " + id + " updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadCourses();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int selView = table.getSelectedRow();
        if (selView < 0) { JOptionPane.showMessageDialog(this, "Please select a course to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE); return; }

        int modelRow = table.convertRowIndexToModel(selView);
        String id = model.getValueAt(modelRow,0).toString();
        String title = model.getValueAt(modelRow,1).toString();

        int c = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete course " + id + " - " + title + "?\nWARNING: This action is permanent and will fail if sections currently reference it.",
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (c != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM courses WHERE course_id = ?")) {
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                JOptionPane.showMessageDialog(this, "No course was deleted (it may no longer exist).", "Info", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Course " + id + " deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            loadCourses();
        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "Cannot delete course " + id + " because it is referenced by existing sections or enrollments.", "Constraint Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createFormPanel(String[] labels, JComponent[] fields) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < labels.length; i++) {
            // Label
            c.gridx = 0; c.gridy = i; c.weightx = 0;
            p.add(new JLabel(labels[i]), c);

            // Field
            c.gridx = 1; c.gridy = i; c.weightx = 1.0;
            p.add(fields[i], c);
        }
        return p;
    }
}
