package edu.univ.erp.data.Dao;

import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Instructor;

import java.sql.*;

public class InstructorDAO {

    // Insert instructor; DB generates instructor_id (INT). Domain stores as String.
    public boolean insertInstructor(Instructor ins) {
        String sql = "INSERT INTO instructors (user_id, department, name, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, ins.GetUserID());
            ps.setString(2, ins.GetDepartment());
            ps.setString(3, ins.GetName());
            ps.setString(4, ins.GetEmail());

            int rows = ps.executeUpdate();
            if (rows == 1) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int gen = rs.getInt(1);
                        ins.SetID(String.valueOf(gen)); // domain's instructor_id stored as String
                    }
                }
                return true;
            }
            return false;
        } catch (SQLIntegrityConstraintViolationException ex) {
            System.err.println("Instructor insert failed - constraint: " + ex.getMessage());
            return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Find by PK instructor_id (int). Returns Instructor or null if not found
    public Instructor findById(int instructorId) {
        String sql = "SELECT instructor_id, user_id, department, name, email FROM instructors WHERE instructor_id = ?";
        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, instructorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Instructor ins = mapRowToInstructor(rs);
                    ins.SetID(String.valueOf(rs.getInt("instructor_id")));
                    return ins;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // Find by user_id. Returns Instructor or null if not found
    public Instructor findByUserId(String userId) {
        String sql = "SELECT instructor_id, user_id, department, name, email FROM instructors WHERE user_id = ?";
        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Instructor ins = mapRowToInstructor(rs);
                    ins.SetID(String.valueOf(rs.getInt("instructor_id")));
                    return ins;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // Update by PK (instructor_id)
    public boolean updateInstructor(Instructor ins) {
        String sql = "UPDATE instructors SET user_id = ?, department = ?, name = ?, email = ? WHERE instructor_id = ?";
        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, ins.GetUserID());
            ps.setString(2, ins.GetDepartment());
            ps.setString(3, ins.GetName());
            ps.setString(4, ins.GetEmail());

            int iid = parseIntSafe(ins.GetID());
            ps.setInt(5, iid);

            int rows = ps.executeUpdate();
            return rows == 1;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Delete by PK
    public boolean deleteById(int instructorId) {
        String sql = "DELETE FROM instructors WHERE instructor_id = ?";
        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, instructorId);
            int rows = ps.executeUpdate();
            return rows == 1;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Delete by user_id helper
    public boolean deleteByUserId(String userId) {
        String sql = "DELETE FROM instructors WHERE user_id = ?";
        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            int rows = ps.executeUpdate();
            return rows >= 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private Instructor mapRowToInstructor(ResultSet rs) throws SQLException {
        Instructor ins = new Instructor();
        ins.SetUserID(rs.getString("user_id"));
        ins.Setdepartment(rs.getString("department"));
        ins.SetName(rs.getString("name"));
        ins.SetEmail(rs.getString("email"));
        return ins;
    }

    private int parseIntSafe(String s) {
        if (s == null || s.isEmpty()) return 0;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
