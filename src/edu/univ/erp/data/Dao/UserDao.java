package edu.univ.erp.data.Dao;

import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Role;
import edu.univ.erp.domain.User;

import java.sql.*;

public class UserDao {

    // ---------- CREATE USER ----------
    public void createUser(User u) throws SQLException {
        String sql = "INSERT INTO users_auth (user_id, username, role, password_hash) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, u.GetID());
            ps.setString(2, u.GetUsername());
            ps.setString(3, u.GetRole().name().toLowerCase());
            ps.setString(4, u.GetHashPass());

            ps.executeUpdate();
        }
    }

    // ---------- FIND BY USERNAME ----------
   public User findByUsername(String username) throws SQLException {
    String sql = "SELECT user_id, username, role, password_hash, status FROM users_auth WHERE username = ?";

    try (Connection conn = DBConnection.getAuthConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, username);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                User u = new User();
                u.SetID(rs.getString("user_id"));
                u.SetUsername(rs.getString("username"));
                u.SetHashPass(rs.getString("password_hash"));
                u.SetRole(Role.valueOf(rs.getString("role").toUpperCase()));
                u.SetStatus(rs.getString("status")); // NEW
                return u;
            }
        }
    }
    return null;
}

    // ---------- FIND BY ID ----------
  public User findById(String userId) throws SQLException {
    String sql = "SELECT user_id, username, role, password_hash, status FROM users_auth WHERE user_id = ?";

    try (Connection conn = DBConnection.getAuthConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, userId);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                User u = new User();
                u.SetID(rs.getString("user_id"));
                u.SetUsername(rs.getString("username"));
                u.SetHashPass(rs.getString("password_hash"));
                u.SetRole(Role.valueOf(rs.getString("role").toUpperCase()));
                u.SetStatus(rs.getString("status")); // NEW
                return u;
            }
        }
    }
    return null;
}

    // ---------- UPDATE PASSWORD ----------
    public void updatePassword(String userId, String newHash) throws SQLException {
        String sql = "UPDATE users_auth SET password_hash = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newHash);
            ps.setString(2, userId);

            ps.executeUpdate();
        }
    }
}
