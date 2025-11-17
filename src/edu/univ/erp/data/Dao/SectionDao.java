package edu.univ.erp.data.Dao;

import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Section;

import java.sql.*;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;

/**
 * Sections DAO. stores day as DayOfWeek.name() in DB (VARCHAR).
 */
public class SectionDao {

    public void createSection(Section s) throws SQLException {
        String sql = "INSERT INTO sections (course_id, instructor_id, day, capacity, semester, year) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.GetCourseID());
            ps.setString(2, s.GetInstructorID());
            ps.setString(3, s.GetDay() == null ? null : s.GetDay().name());
            if (s.GetCapacity() == null) ps.setNull(4, Types.INTEGER);
            else ps.setInt(4, s.GetCapacity());
            ps.setString(5, s.GetSemester());
            if (s.GetYear() == null) ps.setNull(6, Types.INTEGER);
            else ps.setInt(6, s.GetYear());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) s.SetSectionID(keys.getInt(1));
            }
        }
    }

    public Section findById(int id) throws SQLException {
        String sql = "SELECT section_id, course_id, instructor_id, day, capacity, semester, year FROM sections WHERE section_id = ?";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Section s = new Section();
                    s.SetSectionID(rs.getInt("section_id"));
                    s.SetCourseID(rs.getString("course_id"));
                    s.SetInstructorID(rs.getString("instructor_id"));
                    String day = rs.getString("day");
                    if (day != null && !day.isEmpty()) s.SetDay(DayOfWeek.valueOf(day));
                    s.SetCapacity(rs.getInt("capacity"));
                    s.SetSemester(rs.getString("semester"));
                    s.SetYear(rs.getInt("year"));
                    return s;
                }
            }
        }
        return null;
    }

    public List<Section> findByCourse(String courseId) throws SQLException {
        String sql = "SELECT section_id, course_id, instructor_id, day, capacity, semester, year FROM sections WHERE course_id = ?";
        List<Section> out = new ArrayList<>();
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Section s = new Section();
                    s.SetSectionID(rs.getInt("section_id"));
                    s.SetCourseID(rs.getString("course_id"));
                    s.SetInstructorID(rs.getString("instructor_id"));
                    String day = rs.getString("day");
                    if (day != null && !day.isEmpty()) s.SetDay(DayOfWeek.valueOf(day));
                    s.SetCapacity(rs.getInt("capacity"));
                    s.SetSemester(rs.getString("semester"));
                    s.SetYear(rs.getInt("year"));
                    out.add(s);
                }
            }
        }
        return out;
    }

    public void updateSection(Section s) throws SQLException {
        String sql = "UPDATE sections SET course_id = ?, instructor_id = ?, day = ?, capacity = ?, semester = ?, year = ? WHERE section_id = ?";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.GetCourseID());
            ps.setString(2, s.GetInstructorID());
            ps.setString(3, s.GetDay() == null ? null : s.GetDay().name());
            if (s.GetCapacity() == null) ps.setNull(4, Types.INTEGER);
            else ps.setInt(4, s.GetCapacity());
            ps.setString(5, s.GetSemester());
            if (s.GetYear() == null) ps.setNull(6, Types.INTEGER);
            else ps.setInt(6, s.GetYear());
            ps.setInt(7, s.GetSectionID());
            ps.executeUpdate();
        }
    }

    public void deleteSection(int sectionId) throws SQLException {
        String sql = "DELETE FROM sections WHERE section_id = ?";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ps.executeUpdate();
        }
    }
}
