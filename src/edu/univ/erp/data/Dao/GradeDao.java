package edu.univ.erp.data.Dao;

import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Grade;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GradeDao {

    public void addGrade(Grade g) throws SQLException {
        String sql = "INSERT INTO grades (enrollment_id, component, score, final_grade) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, g.getEnrollmentId());
            ps.setString(2, g.getComponent());
            if (g.getScore() == null) ps.setNull(3, Types.DOUBLE);
            else ps.setDouble(3, g.getScore());
            ps.setString(4, g.getFinalGrade());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) g.setGradeId(keys.getInt(1));
            }
        }
    }

    public List<Grade> findByEnrollment(int enrollmentId) throws SQLException {
        String sql = "SELECT grade_id, enrollment_id, component, score, final_grade FROM grades WHERE enrollment_id = ?";
        List<Grade> out = new ArrayList<>();
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Grade g = new Grade();
                    g.setGradeId(rs.getInt("grade_id"));
                    g.setEnrollmentId(rs.getInt("enrollment_id"));
                    g.setComponent(rs.getString("component"));
                    double sc = rs.getDouble("score");
                    if (rs.wasNull()) g.setScore(null);
                    else g.setScore(sc);
                    g.setFinalGrade(rs.getString("final_grade"));
                    out.add(g);
                }
            }
        }
        return out;
    }

    public void updateFinalGrade(int gradeId, String finalGrade) throws SQLException {
        String sql = "UPDATE grades SET final_grade = ? WHERE grade_id = ?";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, finalGrade);
            ps.setInt(2, gradeId);
            ps.executeUpdate();
        }
    }
}
