package edu.univ.erp.data.Dao;

import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Section;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Sections DAO.
 *
 * Changes:
 *  - new columns handled: start_time (VARCHAR), end_time (VARCHAR), days (VARCHAR CSV)
 *  - backwards-compatible: existing single 'day' column still read and converted to days if 'days' is null
 *  - added getAllSections() to support instructor listing
 */
public class SectionDao {

    /**
     * Create a section. Expects Section to contain:
     *  - GetCourseID(), GetInstructorID(), GetDays() (CSV) or GetDay() enum,
     *  - GetStartTime(), GetEndTime() which are Strings ("HH:mm") or null,
     *  - GetCapacity(), GetSemester(), GetYear()
     */
    public void createSection(Section s) throws SQLException {
        String sql = "INSERT INTO sections (course_id, instructor_id, day, days, start_time, end_time, capacity, semester, year) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int idx = 1;
            ps.setString(idx++, s.GetCourseID());
            ps.setString(idx++, s.GetInstructorID());

            // legacy single day column (to keep compatibility) - store primary day name
            if (s.GetDay() == null) ps.setNull(idx++, Types.VARCHAR);
            else ps.setString(idx++, s.GetDay().name());

            // new CSV days column (may be null)
            if (s.GetDays() == null) ps.setNull(idx++, Types.VARCHAR);
            else ps.setString(idx++, s.GetDays());

            // start_time, end_time as strings (HH:mm) - store as VARCHAR in DB
            if (s.GetStartTime() == null) ps.setNull(idx++, Types.VARCHAR);
            else ps.setString(idx++, s.GetStartTime());

            if (s.GetEndTime() == null) ps.setNull(idx++, Types.VARCHAR);
            else ps.setString(idx++, s.GetEndTime());

            if (s.GetCapacity() == null) ps.setNull(idx++, Types.INTEGER);
            else ps.setInt(idx++, s.GetCapacity());

            ps.setString(idx++, s.GetSemester());

            if (s.GetYear() == null) ps.setNull(idx++, Types.INTEGER);
            else ps.setInt(idx++, s.GetYear());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) s.SetSectionID(keys.getInt(1));
            }
        }
    }

    /**
     * Finds a section by PK and maps new fields.
     */
    public Section findById(int id) throws SQLException {
        String sql = "SELECT section_id, course_id, instructor_id, day, days, start_time, end_time, capacity, semester, year " +
                     "FROM sections WHERE section_id = ?";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToSection(rs);
                }
            }
        }
        return null;
    }

    /**
     * Finds sections by course (used in CourseCatalog).
     */
    public List<Section> findByCourse(String courseId) throws SQLException {
        String sql = "SELECT section_id, course_id, instructor_id, day, days, start_time, end_time, capacity, semester, year " +
                     "FROM sections WHERE course_id = ?";
        List<Section> out = new ArrayList<>();
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapRowToSection(rs));
                }
            }
        }
        return out;
    }

    /**
     * Update section: includes new fields (days/start_time/end_time).
     */
    public void updateSection(Section s) throws SQLException {
        String sql = "UPDATE sections SET course_id = ?, instructor_id = ?, day = ?, days = ?, start_time = ?, end_time = ?, " +
                     "capacity = ?, semester = ?, year = ? WHERE section_id = ?";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int idx = 1;
            ps.setString(idx++, s.GetCourseID());
            ps.setString(idx++, s.GetInstructorID());
            if (s.GetDay() == null) ps.setNull(idx++, Types.VARCHAR);
            else ps.setString(idx++, s.GetDay().name());

            if (s.GetDays() == null) ps.setNull(idx++, Types.VARCHAR);
            else ps.setString(idx++, s.GetDays());

            if (s.GetStartTime() == null) ps.setNull(idx++, Types.VARCHAR);
            else ps.setString(idx++, s.GetStartTime());

            if (s.GetEndTime() == null) ps.setNull(idx++, Types.VARCHAR);
            else ps.setString(idx++, s.GetEndTime());

            if (s.GetCapacity() == null) ps.setNull(idx++, Types.INTEGER);
            else ps.setInt(idx++, s.GetCapacity());

            ps.setString(idx++, s.GetSemester());

            if (s.GetYear() == null) ps.setNull(idx++, Types.INTEGER);
            else ps.setInt(idx++, s.GetYear());

            ps.setInt(idx++, s.GetSectionID());
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
     * Return course summaries (unchanged) — keep as is but include instructor lookup by either user_id or instructor_id.
     */
    public java.util.List<Object[]> findCourseSummaries() throws SQLException {
        String sql =
            "SELECT DISTINCT s.course_id, c.title, c.credits, COALESCE(i.name, '') AS instructor_name " +
            "FROM sections s " +
            "JOIN courses c ON s.course_id = c.course_id " +
            "LEFT JOIN instructors i ON ( " +
            "   (i.user_id IS NOT NULL AND i.user_id = s.instructor_id) OR " +
            "   (CAST(i.instructor_id AS CHAR) = s.instructor_id) " +
            ")";
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

    /**
     * New: return all sections for admin/instructor listing.
     * Returns Section domain objects with days/start/end mapped.
     */
    public List<Section> getAllSections() throws SQLException {
        String sql = "SELECT section_id, course_id, instructor_id, day, days, start_time, end_time, capacity, semester, year FROM sections ORDER BY course_id, section_id";
        List<Section> out = new ArrayList<>();
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(mapRowToSection(rs));
            }
        }
        return out;
    }

    // -------------------
    // Helper: map ResultSet row to Section (centralized)
    // -------------------
    private Section mapRowToSection(ResultSet rs) throws SQLException {
        Section s = new Section();
        s.SetSectionID(rs.getInt("section_id"));
        s.SetCourseID(rs.getString("course_id"));
        s.SetInstructorID(rs.getString("instructor_id"));

        // legacy single day (enum) -> try to set Day field and also set Days CSV for compatibility
        String day = rs.getString("day");
        if (day != null && !day.isEmpty()) {
            try {
                s.SetDay(DayOfWeek.valueOf(day));
            } catch (IllegalArgumentException iae) {
                // ignore invalid enum value
            }
        }

        // new days CSV column (preferred)
        String daysCsv = rs.getString("days");
        if (daysCsv != null && !daysCsv.isEmpty()) {
            s.SetDays(daysCsv);
        } else {
            // fallback: if days not present but legacy day exists, set days from day
            if (s.GetDay() != null) {
                s.SetDays(s.GetDay().name());
            } else {
                s.SetDays(null);
            }
        }

        // times stored as strings "HH:mm" (or null)
        String st = rs.getString("start_time");
        String et = rs.getString("end_time");
        s.SetStartTime(st);
        s.SetEndTime(et);

        // capacity/semester/year
        int cap = rs.getInt("capacity");
        if (rs.wasNull()) s.SetCapacity(null);
        else s.SetCapacity(cap);

        s.SetSemester(rs.getString("semester"));

        int yr = rs.getInt("year");
        if (rs.wasNull()) s.SetYear(null);
        else s.SetYear(yr);

        return s;
    }
}
