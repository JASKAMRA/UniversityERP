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

    public CourseManagementPanel() {
        initUI();
        loadCourses();
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        JLabel title=new JLabel("Course Catalog Management");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBorder(new EmptyBorder(0, 0, 12, 0));
        add(title, BorderLayout.NORTH);

        model=new DefaultTableModel(new Object[]{"Course ID", "Title", "Credits", "Department"}, 0) {
            @Override public boolean isCellEditable(int r, int c){ return false; }
        };
        table=new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        JScrollPane scrollPane=new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel=new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        buttonPanel.setBackground(Color.WHITE);
        GridBagConstraints constraint=new GridBagConstraints();
        constraint.fill=GridBagConstraints.HORIZONTAL;
        constraint.insets=new Insets(12 / 2, 0, 12 / 2, 0);
        constraint.gridx=0; 
        constraint.weightx=1.0;

        btnRefresh=new JButton("Refresh List");
        btnCreate=new JButton("Create New Course");
        btnEdit=new JButton("Edit Selected");
        btnDelete=new JButton("Delete Selected");

        styleButton(btnRefresh, Color.LIGHT_GRAY);
        styleButton(btnCreate, new Color(180, 220, 255));
        styleButton(btnEdit, new Color(255, 230, 180));
        styleButton(btnDelete, new Color(255, 180, 180));
        btnCreate.setForeground(new Color(70, 130, 180));
        btnDelete.setForeground(Color.RED);

        constraint.gridy=0; buttonPanel.add(btnRefresh, constraint);
        constraint.gridy=1; buttonPanel.add(Box.createVerticalStrut(12), constraint);
        constraint.gridy=2; buttonPanel.add(btnCreate, constraint);
        constraint.gridy=3; buttonPanel.add(btnEdit, constraint);
        constraint.gridy=4; buttonPanel.add(btnDelete, constraint);

        constraint.gridy=5; 
        constraint.weighty=1.0;
        buttonPanel.add(Box.createVerticalGlue(), constraint);

        add(buttonPanel, BorderLayout.EAST);

        btnRefresh.addActionListener(e -> loadCourses());
        btnCreate.addActionListener(e -> openCreateDialog());
        btnEdit.addActionListener(e -> openEditDialog());
        btnDelete.addActionListener(e -> deleteSelected());
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setPreferredSize(new Dimension(160, 35));
        btn.setMinimumSize(new Dimension(160, 35));
        btn.setMaximumSize(new Dimension(160, 35));
        btn.setFocusPainted(false);
        btn.setBackground(bg);
    }

    private void loadCourses() {
        model.setRowCount(0);
        String sql = "SELECT course_id, title, credits, department_id FROM courses ORDER BY course_id";
        try (Connection connect = DBConnection.getStudentConnection();
             PreparedStatement prepStatement = connect.prepareStatement(sql);
             ResultSet resultSet = prepStatement.executeQuery()) {
            while (resultSet.next()) {
                Vector<Object> row=new Vector<>();
                row.add(resultSet.getString("course_id"));
                row.add(resultSet.getString("title"));
                row.add(resultSet.getObject("credits"));
                row.add(resultSet.getString("department_id"));
                model.addRow(row);
            }
        } 
        catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load courses: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openCreateDialog() {
        JTextField tfId=new JTextField(15);
        JTextField tfTitle=new JTextField(15);
        JTextField tfCredits=new JTextField(15);
        JTextField tfDept=new JTextField(15);

        JPanel p = createFormPanel(
            new String[]{"Course ID:", "Title:", "Credits:", "Department ID:"},
            new JComponent[]{tfId, tfTitle, tfCredits, tfDept}
        );

        int r=JOptionPane.showConfirmDialog(this, p, "Create New Course", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) {
            return;
        }
        String id=tfId.getText().trim();
        String title=tfTitle.getText().trim();
        Integer credits=null;
        try {
            if (!tfCredits.getText().trim().isEmpty()) {
                credits=Integer.parseInt(tfCredits.getText().trim());
            }
        } 
        catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this, "Credits must be a valid whole number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String dept=tfDept.getText().trim();

        if ( title.isEmpty() || id.isEmpty() ) {
            JOptionPane.showMessageDialog(this, "Course ID and title are required fields.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection connect = DBConnection.getStudentConnection();
             PreparedStatement prepStatement = connect.prepareStatement("INSERT INTO courses (course_id, title, credits, department_id) VALUES (?, ?, ?, ?)")) {
            prepStatement.setString(1, id);
            prepStatement.setString(2, title);
            if (credits!=null) {
                prepStatement.setInt(3, credits);
            }
            else {
                prepStatement.setNull(3, Types.INTEGER);
            }
            prepStatement.setString(4, dept);
            prepStatement.executeUpdate();
            JOptionPane.showMessageDialog(this, "Course " + id + " created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadCourses();
        } 
        catch (SQLIntegrityConstraintViolationException exception) {
            JOptionPane.showMessageDialog(this, "Course ID already exists or Department ID is invalid.", "Duplicate/Invalid Data", JOptionPane.ERROR_MESSAGE);
        } 
        catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this, "Create failed: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openEditDialog() {
        int selectView=table.getSelectedRow();
        if (selectView < 0) { 
            JOptionPane.showMessageDialog(this, "Please select a course to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE); 
            return; 
        }

        int modelRow=table.convertRowIndexToModel(selectView);

        String id=model.getValueAt(modelRow,0).toString();
        String curTitle=model.getValueAt(modelRow,1).toString();
        String curCredits=String.valueOf(model.getValueAt(modelRow,2));
        String curDept=String.valueOf(model.getValueAt(modelRow,3));

        JTextField tfTitle=new JTextField(curTitle, 15);
        JTextField tfCredits=new JTextField(curCredits.equals("null") ? "" : curCredits, 15);
        JTextField tfDept=new JTextField(curDept.equals("null") ? "" : curDept, 15);
        JLabel lblId=new JLabel(id);

        JPanel p=createFormPanel(
            new String[]{"Course ID:", "Title:", "Credits:", "Department ID:"},
            new JComponent[]{lblId, tfTitle, tfCredits, tfDept}
        );

        int r = JOptionPane.showConfirmDialog(this, p, "Edit Course: " + id, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) {
            return;
        }

        String title=tfTitle.getText().trim();
        Integer credits=null;
        try {
            if (!tfCredits.getText().trim().isEmpty()) {
                credits = Integer.parseInt(tfCredits.getText().trim());
            }
        } 
        catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this, "Credits must be a valid whole number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String dept=tfDept.getText().trim();

        try (Connection connect = DBConnection.getStudentConnection();
             PreparedStatement prepStatement = connect.prepareStatement("UPDATE courses SET title = ?, credits = ?, department_id = ? WHERE course_id = ?")) {
            prepStatement.setString(1, title);
            if (credits != null) {
                prepStatement.setInt(2, credits);
            } 
            else {
                prepStatement.setNull(2, Types.INTEGER);
            }
            prepStatement.setString(3, dept);
            prepStatement.setString(4, id);
            prepStatement.executeUpdate();
            JOptionPane.showMessageDialog(this, "Course " + id + " updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadCourses();
        } 
        catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this, "Update failed: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int selectView=table.getSelectedRow();
        if (selectView < 0){ 
            JOptionPane.showMessageDialog(this, "Please select a course to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE); 
            return; 
        }

        int modelRow=table.convertRowIndexToModel(selectView);
        String id=model.getValueAt(modelRow,0).toString();
        String title=model.getValueAt(modelRow,1).toString();

        int c = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete course " + id + " - " + title + "?\nWARNING: This action is permanent and will fail if sections currently reference it.",
            "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (c != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection connect = DBConnection.getStudentConnection();
             PreparedStatement prepStatement = connect.prepareStatement("DELETE FROM courses WHERE course_id = ?")) {
            prepStatement.setString(1, id);
            int row=prepStatement.executeUpdate();
            if (row == 0) {
                JOptionPane.showMessageDialog(this, "No course was deleted (it may no longer exist).", "Info", JOptionPane.INFORMATION_MESSAGE);
            } 
            else {
                JOptionPane.showMessageDialog(this, "Course " + id + " deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            loadCourses();
        } 
        catch (SQLIntegrityConstraintViolationException exception) {
            JOptionPane.showMessageDialog(this, "Cannot delete course " + id + " because it is referenced by existing sections or enrollments.", "Constraint Error", JOptionPane.ERROR_MESSAGE);
        } 
        catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this, "Delete failed: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createFormPanel(String[] labels, JComponent[] fields) {
        JPanel p=new JPanel(new GridBagLayout());
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.insets=new Insets(6, 6, 6, 6);
        gbc.fill=GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; 
            gbc.gridy = i; 
            gbc.weightx = 0;
            p.add(new JLabel(labels[i]), gbc);

            gbc.gridx = 1; 
            gbc.gridy = i; 
            gbc.weightx = 1.0;
            p.add(fields[i], gbc);
        }
        return p;
    }
}
