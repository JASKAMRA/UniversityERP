package edu.univ.erp.data.Dao;

import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDao {

    public void enrollStudent(Enrollment e) throws SQLException {
        String sql = "INSERT INTO enrollments(student_id, section_id, status) VALUES (?, ?, ?)";
        try (Connection c = DBConnection.getStudentConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.GetStudentID());
            ps.setInt(2, e.GetSectionID());
            ps.setString(3, e.GetStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) e.SetEnrollmentID(keys.getInt(1));
            }
        }
    }

    public List<Enrollment> findByStudent(String studentId) throws SQLException {
        String sql = "SELECT enrollment_id, student_id, section_id, status FROM enrollments WHERE student_id = ?";
        List<Enrollment> out = new ArrayList<>();
        try (Connection c = DBConnection.getStudentConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Enrollment e = new Enrollment();
                    e.SetEnrollmentID(rs.getInt("enrollment_id"));
                    e.SetStudentID(rs.getString("student_id"));
                    e.SetSectionID(rs.getInt("section_id"));
                    String statusStr = rs.getString("status");
                    if (statusStr != null) {
                        e.SetStatus(Status.valueOf(statusStr.toUpperCase()));
                    }
                    out.add(e);
                }
            }
        }
        return out;
    }

    public Enrollment findById(int id) throws SQLException {
        String sql = "SELECT enrollment_id, student_id, section_id, status FROM enrollments WHERE enrollment_id = ?";
        try (Connection c = DBConnection.getStudentConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Enrollment e = new Enrollment();
                    e.SetEnrollmentID(rs.getInt("enrollment_id"));
                    e.SetStudentID(rs.getString("student_id"));
                    e.SetSectionID(rs.getInt("section_id"));
                    String statusStr = rs.getString("status");
                    if (statusStr != null) {
                        e.SetStatus(Status.valueOf(statusStr.toUpperCase()));
                    }
                    return e;
                }
            }
        }
        return null;
    }

    public void updateStatus(int enrollmentId, String newStatus) throws SQLException {
        String sql = "UPDATE enrollments SET status = ? WHERE enrollment_id = ?";
        try (Connection c = DBConnection.getStudentConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, enrollmentId);
            ps.executeUpdate();
        }
    }

    public void deleteEnrollment(int id) throws SQLException {
    String sql = "DELETE FROM enrollments WHERE enrollment_id = ?";
    try (Connection c = DBConnection.getStudentConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
        ps.setInt(1, id);
        ps.executeUpdate();
    }
}

}
