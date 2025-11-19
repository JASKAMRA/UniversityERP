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

    /**
 * Return a list of rows: [ course_id(String), title(String), credits(Integer), instructor_name(String) ]
 * This queries sections JOIN courses LEFT JOIN instructors and uses DISTINCT so we show each course once.
 */
public java.util.List<Object[]> findCourseSummaries() throws SQLException {
    String sql =
        "SELECT DISTINCT s.course_id, c.title, c.credits, COALESCE(i.name, '') AS instructor_name " +
        "FROM sections s " +
        "JOIN courses c ON s.course_id = c.course_id " +
        "LEFT JOIN instructors i ON s.instructor_id = i.user_id";

    java.util.List<Object[]> out = new java.util.ArrayList<>();
    try (Connection conn = DBConnection.getStudentConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            String courseId = rs.getString("course_id");
            String title = rs.getString("title");
            int credits = rs.getInt("credits");
            String instr = rs.getString("instructor_name");
            out.add(new Object[] { courseId, title, credits, instr });
        }
    }
    return out;
}

}
