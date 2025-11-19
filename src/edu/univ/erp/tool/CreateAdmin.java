package edu.univ.erp.tool;

import edu.univ.erp.data.DBConnection;
import edu.univ.erp.data.Dao.AdminDao;
import edu.univ.erp.auth.PasswordUtil;
import edu.univ.erp.domain.Admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Scanner;
import java.util.UUID;

/**
 * Interactive admin creation tool.
 * Asks for username, password, full name, email
 * Inserts into:
 *  - erp_auth.users_auth
 *  - erp_student.admins
 */
public class CreateAdmin {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("=== Create New ADMIN User ===");

        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();

        System.out.print("Enter password: ");
        String password = sc.nextLine().trim();

        System.out.print("Enter full name: ");
        String fullName = sc.nextLine().trim();

        System.out.print("Enter email: ");
        String email = sc.nextLine().trim();

        try {
            // Generate random UUID for users_auth.user_id
            String userId = UUID.randomUUID().toString();

            System.out.println("\nCreating admin...");
            System.out.println("Generated User ID = " + userId);

            // Hash password with BCrypt using your PasswordUtil
            String passwordHash = PasswordUtil.hash(password);

            // 1) Insert into erp_auth.users_auth
            String sqlAuth = "INSERT INTO users_auth (user_id, username, role, password_hash, status) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getAuthConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlAuth)) {

                ps.setString(1, userId);
                ps.setString(2, username);
                ps.setString(3, "ADMIN");   // ensures admin role
                ps.setString(4, passwordHash);
                ps.setString(5, "active");

                int rows = ps.executeUpdate();
                if (rows != 1) {
                    System.err.println("Failed to insert into users_auth! Code: " + rows);
                    return;
                }
            }

            System.out.println("✔ Inserted into users_auth");

            // 2) Insert profile into erp_student.admins
            Admin admin = new Admin();
            admin.setUserId(userId);
            admin.setName(fullName);
            admin.setEmail(email);

            AdminDao dao = new AdminDao();
            boolean ok = dao.insert(admin);

            if (!ok) {
                System.err.println("Failed to insert into admins table!");
                return;
            }

            System.out.println("✔ Inserted into admins table (admin_id = " + admin.getAdminId() + ")");

            System.out.println("\n=== ADMIN CREATED SUCCESSFULLY ===");
            System.out.println("Login Username : " + username);
            System.out.println("Login Password : " + password + "  (hashed internally)");
            System.out.println("Role           : ADMIN");
            System.out.println("user_id        : " + userId);
            System.out.println("admin_id       : " + admin.getAdminId());
            System.out.println("====================================");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("!! ERROR CREATING ADMIN !!");
        }
    }
}
