package edu.univ.erp.data.Dao;

import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Course CRUD operations against erp_student DB.
 */
public class CourseDao {

    public void createCourse(Course c) throws SQLException {
        String sql = "INSERT INTO courses (course_id, title, credits, department_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.GetCourseID());
            ps.setString(2, c.GetTitle());
            if (c.GetCredits() == null) ps.setNull(3, Types.INTEGER);
            else ps.setInt(3, c.GetCredits());
            ps.setString(4, c.GetDepartmentID());
            ps.executeUpdate();
        }
    }

    public Course findById(String courseId) throws SQLException {
        String sql = "SELECT course_id, title, credits, department_id FROM courses WHERE course_id = ?";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Course(
                            rs.getString("department_id"),
                            rs.getString("title"),
                            rs.getString("course_id"),
                            rs.getInt("credits")
                            
                    );
                }
            }
        }
        return null;
    }

    public List<Course> findAll() throws SQLException {
        String sql = "SELECT course_id, title, credits, department_id FROM courses";
        List<Course> out = new ArrayList<>();
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(new Course(
                            rs.getString("department_id"),
                            rs.getString("title"),
                            rs.getString("course_id"),
                            rs.getInt("credits")
                ));
            }
        }
        return out;
    }

    public void updateCourse(Course c) throws SQLException {
        String sql = "UPDATE courses SET title = ?, credits = ?, department_id = ? WHERE course_id = ?";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.GetTitle());
            if (c.GetCredits() == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, c.GetCredits());
            ps.setString(3, c.GetDepartmentID());
            ps.setString(4, c.GetCourseID());
            ps.executeUpdate();
        }
    }

    public void deleteCourse(String courseId) throws SQLException {
        String sql = "DELETE FROM courses WHERE course_id = ?";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseId);
            ps.executeUpdate();
        }
    }
}
