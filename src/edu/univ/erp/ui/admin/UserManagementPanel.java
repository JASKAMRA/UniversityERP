package edu.univ.erp.ui.admin;

import edu.univ.erp.data.DBConnection;
import edu.univ.erp.auth.PasswordUtil; // Assuming this is needed for PasswordUtil.hash

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;
import java.util.UUID;

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

    // --- Aesthetic constants ---
    private static final int PADDING = 20;
    private static final int GAP = 12;
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 20);
    private static final Dimension BUTTON_SIZE = new Dimension(170, 35);
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180); 


    public UserManagementPanel() {
        initUI();
        loadUsers("ALL");
    }

    private void initUI() {
        // 1. Overall Layout and Padding
        setLayout(new BorderLayout(GAP, GAP));
        setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        setBackground(Color.WHITE);

        // 2. Title Section (North)
        JLabel title = new JLabel("👥 System User Management");
        title.setFont(TITLE_FONT);
        title.setBorder(new EmptyBorder(0, 0, GAP, 0));
        add(title, BorderLayout.NORTH);

        // 3. User Table (Center)
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
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        add(scrollPane, BorderLayout.CENTER);

        // 4. Actions Panel (East)
        JPanel right = new JPanel();
        right.setLayout(new GridBagLayout()); // Use GridBag for better control
        right.setBackground(Color.WHITE);
        right.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
            "User Actions", 
            TitledBorder.LEFT, 
            TitledBorder.TOP, 
            new Font("Arial", Font.BOLD, 12), PRIMARY_COLOR));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0; gbc.weightx = 1.0;
        
        // Filter + Refresh (Top Group)
        cbRoleFilter = new JComboBox<>(new String[]{"ALL", "STUDENT", "INSTRUCTOR", "ADMIN"});
        cbRoleFilter.setPreferredSize(BUTTON_SIZE);
        gbc.gridy = 0; right.add(cbRoleFilter, gbc);

        btnRefresh = new JButton("🔄 Refresh List");
        styleButton(btnRefresh, Color.LIGHT_GRAY, Color.BLACK);
        gbc.gridy = 1; right.add(btnRefresh, gbc);

        // Separator
        gbc.gridy = 2; right.add(Box.createVerticalStrut(GAP), gbc); 

        // Creation
        btnCreateInstructor = new JButton("➕ Create Instructor");
        styleButton(btnCreateInstructor, new Color(180, 220, 255), PRIMARY_COLOR);
        gbc.gridy = 3; right.add(btnCreateInstructor, gbc);

        // Status Change
        btnActivate = new JButton("✅ Activate User");
        styleButton(btnActivate, new Color(220, 255, 220), Color.GREEN.darker());
        gbc.gridy = 4; right.add(btnActivate, gbc);
        
        btnDeactivate = new JButton("🛑 Deactivate User");
        styleButton(btnDeactivate, new Color(255, 220, 220), Color.RED.darker());
        gbc.gridy = 5; right.add(btnDeactivate, gbc);

        // Separator
        gbc.gridy = 6; right.add(Box.createVerticalStrut(GAP), gbc);

        // Deletion
        btnDeleteUser = new JButton("🗑️ Delete User");
        styleButton(btnDeleteUser, new Color(255, 180, 180), Color.RED);
        gbc.gridy = 7; right.add(btnDeleteUser, gbc);
        
        // Glue to push components up
        gbc.gridy = 8; gbc.weighty = 1.0;
        right.add(Box.createVerticalGlue(), gbc);


        add(right, BorderLayout.EAST);

        // 5. Action Listeners
        cbRoleFilter.addActionListener(e -> loadUsers(cbRoleFilter.getSelectedItem().toString()));
        btnRefresh.addActionListener(e -> loadUsers(cbRoleFilter.getSelectedItem().toString()));
        btnCreateInstructor.addActionListener(e -> openCreateInstructorDialog());
        btnActivate.addActionListener(e -> changeStatus("active"));
        btnDeactivate.addActionListener(e -> changeStatus("inactive"));
        btnDeleteUser.addActionListener(e -> deleteSelectedUser());
    }

    private void styleButton(JButton button, Color background, Color foreground) {
        button.setPreferredSize(BUTTON_SIZE);
        button.setMinimumSize(BUTTON_SIZE);
        button.setFocusPainted(false);
        button.setBackground(background);
        button.setForeground(foreground);
    }

    // ================================================================
    //  LOAD ALL USERS FROM users_auth (WITH ROLE FILTER)
    // ================================================================

    private void loadUsers(String roleFilter) {
        model.setRowCount(0);

        String sql = "SELECT user_id, username, role, status FROM users_auth";
        if (!roleFilter.equals("ALL"))
            sql += " WHERE role = ?";
        
        sql += " ORDER BY username"; // Added ordering for better display

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
    
    // Helper to get selected user's role
    private String getSelectedUserRole() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        return model.getValueAt(row, 2).toString();
    }

    // ================================================================
    //  ACTIVATE / DEACTIVATE USER
    // ================================================================

    private void changeStatus(String newStatus) {
        String uid = getSelectedUserId();
        String role = getSelectedUserRole();
        
        if (uid == null) {
            JOptionPane.showMessageDialog(this, "Select a user.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (role.equals("ADMIN") && newStatus.equals("inactive")) {
            JOptionPane.showMessageDialog(this, "Deactivating an ADMIN user is typically restricted.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE users_auth SET status = ? WHERE user_id = ?")) {

            ps.setString(1, newStatus);
            ps.setString(2, uid);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Updated user status to **" + newStatus.toUpperCase() + "**.");
            loadUsers(cbRoleFilter.getSelectedItem().toString());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Status update failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ================================================================
    //  DELETE USER (AUTH + PROFILE)
    // ================================================================

    private void deleteSelectedUser() {
        String uid = getSelectedUserId();
        String username = getSelectedUserUsername();
        String role = getSelectedUserRole();
        
        if (uid == null) {
            JOptionPane.showMessageDialog(this, "Select a user first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (role.equals("ADMIN")) {
            JOptionPane.showMessageDialog(this, "Deleting an ADMIN user is a critical, restricted operation.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "<html>Are you sure you want to DELETE user **" + username + "** (" + role + ")?<br/>" +
                "**WARNING:** This will permanently remove their user authentication record and profile data.</html>",
                "🗑️ Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
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

            JOptionPane.showMessageDialog(this, "User deleted successfully: " + username);
            loadUsers(cbRoleFilter.getSelectedItem().toString());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Delete failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String getSelectedUserUsername() {
        int row = table.getSelectedRow();
        if (row < 0) return "N/A";
        return model.getValueAt(row, 1).toString();
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
            // Log the error but continue, as failure here shouldn't stop auth deletion
            ex.printStackTrace();
        }
    }

    // ================================================================
    //  CREATE INSTRUCTOR USER DIALOG
    // ================================================================

    private void openCreateInstructorDialog() {
        JTextField tfUsername = new JTextField(15);
        JPasswordField tfPassword = new JPasswordField(15); // Use JPasswordField
        JTextField tfName = new JTextField(15);
        JTextField tfEmail = new JTextField(15);
        JTextField tfDept = new JTextField(15);

        // Improved Dialog Layout
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Helper to add rows
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; panel.add(tfUsername, gbc);
        
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; panel.add(tfPassword, gbc);
        
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; panel.add(tfName, gbc);
        
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Email (Optional):"), gbc);
        gbc.gridx = 1; panel.add(tfEmail, gbc);
        
        gbc.gridx = 0; gbc.gridy = row++; panel.add(new JLabel("Department (Optional):"), gbc);
        gbc.gridx = 1; panel.add(tfDept, gbc);


        int result = JOptionPane.showConfirmDialog(this, panel, "➕ Create Instructor", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;
        
        // Retrieve password correctly from JPasswordField
        String password = new String(tfPassword.getPassword());

        try {
            createInstructorUser(
                tfUsername.getText().trim(),
                password,
                tfName.getText().trim(),
                tfEmail.getText().trim(),
                tfDept.getText().trim()
            );
            loadUsers(cbRoleFilter.getSelectedItem().toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to create instructor: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Inserts into users_auth + instructors table.
     */
    private void createInstructorUser(String username, String password, String name, String email, String dept) throws Exception {

        if (username.isEmpty() || password.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username, password, and name are required fields.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 1) Create auth entry
        String userId = UUID.randomUUID().toString();
        // Assuming PasswordUtil is available and correctly imported
        String pwHash = PasswordUtil.hash(password); 

        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO users_auth(user_id, username, role, password_hash, status) VALUES (?, ?, 'INSTRUCTOR', ?, 'active')"
             )) {
            ps.setString(1, userId);
            ps.setString(2, username);
            ps.setString(3, pwHash);
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException ex) {
            // Handle unique username constraint violation
             throw new Exception("Username '" + username + "' already exists.", ex);
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

        JOptionPane.showMessageDialog(this, "Instructor user created successfully (User ID: " + userId + ").");
    }
}