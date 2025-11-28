package edu.univ.erp.ui.admin;
import edu.univ.erp.data.DBConnection;
import edu.univ.erp.service.AdminServiceImpl;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class UserManagementPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;

    private JComboBox<String> cbRoleFilter;
    private JButton btnRefresh;
    private JButton btnCreateInstructor;
    private JButton btnActivate;
    private JButton btnDeactivate;
    private JButton btnDeleteUser;

    private final AdminServiceImpl adminService = new AdminServiceImpl();

    public UserManagementPanel() {
        initUI();
        loadUsers("ALL");
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        JLabel title=new JLabel("System User Management");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBorder(new EmptyBorder(0, 0, 12, 0));
        add(title, BorderLayout.NORTH);

        model=new DefaultTableModel(
                new Object[]{"User ID", "Username", "Role", "Status"}, 0
        ) 
        {
            @Override public boolean isCellEditable(int r, int c) { 
                return false; 
            }
        };

        table=new JTable(model);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        add(new JScrollPane(table), 
        BorderLayout.CENTER);

        JPanel r=new JPanel(new GridBagLayout());
        r.setBackground(Color.WHITE);
        r.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "User Actions",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 12),
                new Color(70, 130, 180)));

        GridBagConstraints gbc=new GridBagConstraints();
        gbc.insets=new Insets(5, 5, 5, 5);
        gbc.fill=GridBagConstraints.HORIZONTAL;
        gbc.gridx=0;

        cbRoleFilter=new JComboBox<>(new String[]{"ALL", "STUDENT", "INSTRUCTOR", "ADMIN"});
        cbRoleFilter.setPreferredSize(new Dimension(170, 35));
        gbc.gridy=0;
        r.add(cbRoleFilter, gbc);

        btnRefresh=new JButton("Refresh List");
        styleButton(btnRefresh, Color.LIGHT_GRAY, Color.BLACK);
        gbc.gridy=1;
        r.add(btnRefresh, gbc);

        gbc.gridy=2;
        r.add(Box.createVerticalStrut(12), gbc);

        btnCreateInstructor=new JButton("Create Instructor");
        styleButton(btnCreateInstructor, new Color(180, 220, 255), new Color(70, 130, 180));
        gbc.gridy = 3;
        r.add(btnCreateInstructor, gbc);

        btnActivate=new JButton("Activate User");
        styleButton(btnActivate, new Color(220, 255, 220), Color.GREEN.darker());
        gbc.gridy=4;
        r.add(btnActivate, gbc);

        btnDeactivate=new JButton("Deactivate User");
        styleButton(btnDeactivate, new Color(255, 220, 220), Color.RED.darker());
        gbc.gridy=5;
        r.add(btnDeactivate, gbc);

        gbc.gridy=6;
        r.add(Box.createVerticalStrut(12), gbc);

        btnDeleteUser=new JButton("Delete User");
        styleButton(btnDeleteUser, new Color(255, 180, 180), Color.RED);
        gbc.gridy=7;
        r.add(btnDeleteUser, gbc);

        gbc.gridy=8;
        gbc.weighty=1;
        r.add(Box.createVerticalGlue(), gbc);

        add(r, BorderLayout.EAST);

        cbRoleFilter.addActionListener(e -> loadUsers(cbRoleFilter.getSelectedItem().toString()));
        btnRefresh.addActionListener(e -> loadUsers(cbRoleFilter.getSelectedItem().toString()));
        btnCreateInstructor.addActionListener(e -> openCreateInstructorDialog());
        btnActivate.addActionListener(e -> changeStatus("active"));
        btnDeactivate.addActionListener(e -> changeStatus("inactive"));
        btnDeleteUser.addActionListener(e -> deleteSelectedUser());
    }

    private void styleButton(JButton b, Color bg, Color fg) {
        b.setPreferredSize(new Dimension(170, 35));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
    }


   
    private void loadUsers(String roleFilter) {
        model.setRowCount(0);

        String sql = "SELECT user_id, username, role, status FROM users_auth";
        if (!roleFilter.equals("ALL"))
            sql += " WHERE role = ?";

        sql += " ORDER BY username";

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
        } 
        catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load users.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String selectedUserId() {
        int r = table.getSelectedRow();
        return (r < 0) ? null : model.getValueAt(r, 0).toString();
    }
    private String selectedUsername() {
        int r = table.getSelectedRow();
        return (r < 0) ? null : model.getValueAt(r, 1).toString();
    }
    private String selectedRole() {
        int r = table.getSelectedRow();
        return (r < 0) ? null : model.getValueAt(r, 2).toString();
    }



    private void changeStatus(String newStatus) {
        String uid = selectedUserId();
        String role = selectedRole();

        if (uid == null) {
            JOptionPane.showMessageDialog(this, "Select a user.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("ADMIN".equals(role) && "inactive".equals(newStatus)) {
            JOptionPane.showMessageDialog(this, "Admin users cannot be deactivated.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE users_auth SET status = ? WHERE user_id = ?")) {

            ps.setString(1, newStatus);
            ps.setString(2, uid);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "User status updated to: " + newStatus.toUpperCase());

            loadUsers(cbRoleFilter.getSelectedItem().toString());

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Status update failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void deleteSelectedUser() {
        String uid = selectedUserId();
        String username = selectedUsername();
        String role = selectedRole();

        if (uid == null) {
            JOptionPane.showMessageDialog(this, "Select a user first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("ADMIN".equals(role)) {
            JOptionPane.showMessageDialog(this, "Admin account deletion is restricted.", "Access Denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete user \"" + username + "\" (" + role + ")?\nThis action is permanent.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            deleteProfile(uid);

            try (Connection connect= DBConnection.getAuthConnection();
                 PreparedStatement prepStatement= connect.prepareStatement(
                         "DELETE FROM users_auth WHERE user_id = ?")) {
                prepStatement.setString(1, uid);
                prepStatement.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "User deleted: " + username);
            loadUsers(cbRoleFilter.getSelectedItem().toString());

        } 
        catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this, "Delete failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteProfile(String uid) {
        try (Connection connect = DBConnection.getStudentConnection()) {

            try (PreparedStatement prepStatement = connect.prepareStatement("DELETE FROM students WHERE user_id = ?")) {
                prepStatement.setString(1, uid);
                prepStatement.executeUpdate();
            }
            try (PreparedStatement prepStatement = connect.prepareStatement("DELETE FROM instructors WHERE user_id = ?")) {
                prepStatement.setString(1, uid);
                prepStatement.executeUpdate();
            }
            try (PreparedStatement prepStatement = connect.prepareStatement("DELETE FROM admins WHERE user_id = ?")) {
                prepStatement.setString(1, uid);
                prepStatement.executeUpdate();
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void openCreateInstructorDialog() {

        JTextField tfUser = new JTextField(15);
        JPasswordField tfPass = new JPasswordField(15);
        JTextField tfName = new JTextField(15);
        JTextField tfEmail = new JTextField(15);
        JTextField tfDept = new JTextField(15);

        JPanel p=new JPanel(new GridBagLayout());
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.insets=new Insets(6, 6, 6, 6);
        gbc.fill=GridBagConstraints.HORIZONTAL;

        int row=0;

        gbc.gridx = 0; gbc.gridy = row++; p.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; p.add(tfUser, gbc);

        gbc.gridx = 0; gbc.gridy = row++; p.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; p.add(tfPass, gbc);

        gbc.gridx = 0; gbc.gridy = row++; p.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; p.add(tfName, gbc);

        gbc.gridx = 0; gbc.gridy = row++; p.add(new JLabel("Email (Optional):"), gbc);
        gbc.gridx = 1; p.add(tfEmail, gbc);

        gbc.gridx = 0; gbc.gridy = row++; p.add(new JLabel("Department (Optional):"), gbc);
        gbc.gridx = 1; p.add(tfDept, gbc);

        int res = JOptionPane.showConfirmDialog(
                this,
                p,
                "Create Instructor",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (res != JOptionPane.OK_OPTION) {
            return;
        }

        String username = tfUser.getText().trim();
        String password = new String(tfPass.getPassword());
        String name = tfName.getText().trim();
        String email = tfEmail.getText().trim();
        String dept = tfDept.getText().trim();

        if ( password.isEmpty() || username.isEmpty()  || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username, password & name are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnCreateInstructor.setEnabled(false);

        SwingWorker<Boolean, Void> w = new SwingWorker<>() {
            Exception thrown;

            @Override
            protected Boolean doInBackground() {
                try {
                    return adminService.createInstructor(username, password, name, email, dept);
                } 
                catch (Exception exception) {
                    thrown = exception;
                    return false;
                }
            }

            @Override
            protected void done() {
                btnCreateInstructor.setEnabled(true);
                try {
                    boolean ok=get();
                    if (!ok) {
                        JOptionPane.showMessageDialog(UserManagementPanel.this,
                                "Failed: " + (thrown != null ? thrown.getMessage() : "Unknown error."),
                                "Error", JOptionPane.ERROR_MESSAGE);
                            } 
                            else {
                                JOptionPane.showMessageDialog(UserManagementPanel.this,
                                        "Instructor created successfully.");
                                loadUsers(cbRoleFilter.getSelectedItem().toString());
                            }
                } 
                catch (Exception exception) {
                    exception.printStackTrace();
                    JOptionPane.showMessageDialog(UserManagementPanel.this,
                            "Error: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        w.execute();
    }
}
