package edu.univ.erp.data.Dao;

import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Admin;

import java.sql.*;

/**
 * DAO for minimal admins table stored in erp_student DB.
 */
public class AdminDao {

    /**
     * Insert admin profile into erp_student.admins.
     * On success sets generated adminId on the Admin object and returns true.
     */
    public boolean insert(Admin a) throws SQLException {
        String sql = "INSERT INTO admins (user_id, name, email) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, a.GetUserId());
            ps.setString(2, a.GetName());
            ps.setString(3, a.GetEmail());

            int rows = ps.executeUpdate();
            if (rows == 1) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) a.SetAdminId(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    /**
     * Find admin profile by user_id.
     * Returns Admin object or null if not found.
     */
    public Admin findByUserId(String userId) throws SQLException {
        String sql = "SELECT admin_id, user_id, name, email FROM admins WHERE user_id = ?";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Admin a = new Admin();
                    a.SetAdminId(rs.getInt("admin_id"));
                    a.SetUserId(rs.getString("user_id"));
                    a.SetName(rs.getString("name"));
                    a.SetEmail(rs.getString("email"));
                    return a;
                }
            }
        }
        return null;
    }

    /**
     * Delete admin row by user_id. Returns true if deletion executed (rows >= 0).
     */
    public boolean deleteByUserId(String userId) throws SQLException {
        String sql = "DELETE FROM admins WHERE user_id = ?";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            int rows = ps.executeUpdate();
            return rows >= 0;
        }
    }
}
