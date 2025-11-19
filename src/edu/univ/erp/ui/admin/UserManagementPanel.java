package edu.univ.erp.ui.admin;

import edu.univ.erp.data.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

/**
 * Admin Panel: Manage all system users.
 * Shows users_auth table + provides buttons for CRUD operations.
 */
public class UserManagementPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;

    private JComboBox<String> cbRoleFilter;
    private JButton btnRefresh;
    private JButton btnCreateInstructor;
    private JButton btnActivate;
    private JButton btnDeactivate;
    private JButton btnDeleteUser;

    public UserManagementPanel() {
        initUI();
        loadUsers("ALL");
    }

    private void initUI() {
        setLayout(new BorderLayout(10,10));

        JLabel title = new JLabel("User Management");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        add(title, BorderLayout.NORTH);

        // === USER TABLE ===
        model = new DefaultTableModel(
                new Object[]{"User ID", "Username", "Role", "Status"},
                0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // === ACTIONS PANEL ===
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(BorderFactory.createTitledBorder("Actions"));

        cbRoleFilter = new JComboBox<>(new String[]{"ALL", "STUDENT", "INSTRUCTOR", "ADMIN"});
        btnRefresh = new JButton("Refresh");
        btnCreateInstructor = new JButton("Create Instructor");
        btnActivate = new JButton("Activate");
        btnDeactivate = new JButton("Deactivate");
        btnDeleteUser = new JButton("Delete User");

        addAction(right, cbRoleFilter);
        addAction(right, btnRefresh);
        addAction(right, btnCreateInstructor);
        addAction(right, btnActivate);
        addAction(right, btnDeactivate);
        addAction(right, btnDeleteUser);

        add(right, BorderLayout.EAST);

        // === ACTION LISTENERS ===
        cbRoleFilter.addActionListener(e -> loadUsers(cbRoleFilter.getSelectedItem().toString()));
        btnRefresh.addActionListener(e -> loadUsers(cbRoleFilter.getSelectedItem().toString()));
        btnCreateInstructor.addActionListener(e -> openCreateInstructorDialog());
        btnActivate.addActionListener(e -> changeStatus("active"));
        btnDeactivate.addActionListener(e -> changeStatus("inactive"));
        btnDeleteUser.addActionListener(e -> deleteSelectedUser());
    }

    private void addAction(JPanel p, JComponent c) {
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(Box.createVerticalStrut(10));
        p.add(c);
    }

    // ================================================================
    //  LOAD ALL USERS FROM users_auth (WITH ROLE FILTER)
    // ================================================================

    private void loadUsers(String roleFilter) {
        model.setRowCount(0);

        String sql = "SELECT user_id, username, role, status FROM users_auth";
        if (!roleFilter.equals("ALL"))
            sql += " WHERE role = ?";

        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (!roleFilter.equals("ALL"))
                ps.setString(1, roleFilter);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getString("user_id"));
                    row.add(rs.getString("username"));
                    row.add(rs.getString("role"));
                    row.add(rs.getString("status"));
                    model.addRow(row);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load users.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getSelectedUserId() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return model.getValueAt(row, 0).toString();
    }

    // ================================================================
    //  ACTIVATE / DEACTIVATE USER
    // ================================================================

    private void changeStatus(String newStatus) {
        String uid = getSelectedUserId();
        if (uid == null) {
            JOptionPane.showMessageDialog(this, "Select a user.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE users_auth SET status = ? WHERE user_id = ?")) {

            ps.setString(1, newStatus);
            ps.setString(2, uid);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Updated status → " + newStatus);
            loadUsers(cbRoleFilter.getSelectedItem().toString());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================================================================
    //  DELETE USER (AUTH + PROFILE)
    // ================================================================

    private void deleteSelectedUser() {
        String uid = getSelectedUserId();
        if (uid == null) {
            JOptionPane.showMessageDialog(this, "Select a user first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to DELETE this user?\nThis will remove their profile as well.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (choice != JOptionPane.YES_OPTION) return;

        try {
            // 1) Delete student/instructor/admin profile (best-effort)
            deleteProfileFromStudentDB(uid);

            // 2) Delete from users_auth
            try (Connection conn = DBConnection.getAuthConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM users_auth WHERE user_id = ?")) {
                ps.setString(1, uid);
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "User deleted successfully.");
            loadUsers(cbRoleFilter.getSelectedItem().toString());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Delete failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Delete profile rows from erp_student:
     * - students
     * - instructors
     * - admins
     */
    private void deleteProfileFromStudentDB(String uid) {
        try (Connection conn = DBConnection.getStudentConnection()) {

            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM students WHERE user_id = ?")) {
                ps.setString(1, uid);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM instructors WHERE user_id = ?")) {
                ps.setString(1, uid);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM admins WHERE user_id = ?")) {
                ps.setString(1, uid);
                ps.executeUpdate();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ================================================================
    //  CREATE INSTRUCTOR USER DIALOG
    // ================================================================

    private void openCreateInstructorDialog() {
        JTextField tfUsername = new JTextField();
        JTextField tfPassword = new JTextField();
        JTextField tfName = new JTextField();
        JTextField tfEmail = new JTextField();
        JTextField tfDept = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0,2,6,6));
        panel.add(new JLabel("Username:")); panel.add(tfUsername);
        panel.add(new JLabel("Password:")); panel.add(tfPassword);
        panel.add(new JLabel("Name:")); panel.add(tfName);
        panel.add(new JLabel("Email:")); panel.add(tfEmail);
        panel.add(new JLabel("Department:")); panel.add(tfDept);

        int result = JOptionPane.showConfirmDialog(this, panel, "Create Instructor", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        try {
            createInstructorUser(
                    tfUsername.getText(),
                    tfPassword.getText(),
                    tfName.getText(),
                    tfEmail.getText(),
                    tfDept.getText()
            );
            loadUsers(cbRoleFilter.getSelectedItem().toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to create instructor.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Inserts into users_auth + instructors table.
     */
    private void createInstructorUser(String username, String password, String name, String email, String dept) throws Exception {

        if (username.isEmpty() || password.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Required fields missing.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1) Create auth entry
        String userId = java.util.UUID.randomUUID().toString();
        String pwHash = edu.univ.erp.auth.PasswordUtil.hash(password);

        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users_auth(user_id, username, role, password_hash, status) VALUES (?, ?, 'INSTRUCTOR', ?, 'active')"
             )) {
            ps.setString(1, userId);
            ps.setString(2, username);
            ps.setString(3, pwHash);
            ps.executeUpdate();
        }

        // 2) Create instructor profile
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO instructors(user_id, name, email, department) VALUES (?, ?, ?, ?)"
             )) {
            ps.setString(1, userId);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.setString(4, dept);
            ps.executeUpdate();
        }

        JOptionPane.showMessageDialog(this, "Instructor user created.");
    }
}
