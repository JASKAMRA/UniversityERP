package edu.univ.erp.service;

import edu.univ.erp.data.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class InstructorServiceImpl implements InstructorService {

    @Override
    public List<Map<String, Object>> getAssignedSections(String instructorUserId) {
        List<Map<String,Object>> out = new ArrayList<>();
        String sql =
            "SELECT s.section_id, s.course_id, c.title AS course_title, s.day, s.capacity, s.semester, s.year " +
            "FROM sections s " +
            "JOIN courses c ON s.course_id = c.course_id " +
            "JOIN instructors i ON s.instructor_id = i.instructor_id " +
            "WHERE i.user_id = ?";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, instructorUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,Object> m = new HashMap<>();
                    m.put("section_id", rs.getInt("section_id"));
                    m.put("course_id", rs.getString("course_id"));
                    m.put("course_title", rs.getString("course_title"));
                    m.put("day", rs.getString("day"));
                    m.put("capacity", rs.getInt("capacity"));
                    m.put("semester", rs.getString("semester"));
                    m.put("year", rs.getInt("year"));
                    out.add(m);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return out;
    }

    @Override
    public List<Map<String, Object>> getStudentsInSection(int sectionId) {
        List<Map<String,Object>> out = new ArrayList<>();
        String sql =
            "SELECT e.enrollment_id, st.student_id, st.roll_no, st.name, e.status " +
            "FROM enrollments e " +
            "JOIN students st ON e.student_id = st.student_id " +
            "WHERE e.section_id = ? " +
            "ORDER BY st.roll_no";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,Object> m = new HashMap<>();
                    m.put("enrollment_id", rs.getInt("enrollment_id"));
                    m.put("student_id", rs.getInt("student_id"));
                    m.put("roll_no", rs.getString("roll_no"));
                    m.put("name", rs.getString("name"));
                    m.put("status", rs.getString("status"));
                    out.add(m);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return out;
    }

  

    @Override
    public boolean finalizeGrades(int sectionId) {
        // For every enrollment in the section compute AVG(score) from grades (exclude component='FINAL' to avoid recursion)
        String enrollSql = "SELECT e.enrollment_id FROM enrollments e WHERE e.section_id = ?";
        String avgSql = "SELECT AVG(score) AS avg_score FROM grades g WHERE g.enrollment_id = ? AND g.component <> 'FINAL'";
        String selectFinalSql = "SELECT grade_id FROM grades WHERE enrollment_id = ? AND component = 'FINAL' LIMIT 1";
        String insertFinalSql = "INSERT INTO grades (enrollment_id, component, score, final_grade) VALUES (?, 'FINAL', ?, ?)";
        String updateFinalSql = "UPDATE grades SET score = ?, final_grade = ? WHERE grade_id = ?";

        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement pEnroll = conn.prepareStatement(enrollSql);
             PreparedStatement pAvg = conn.prepareStatement(avgSql);
             PreparedStatement pSelectFinal = conn.prepareStatement(selectFinalSql);
             PreparedStatement pInsertFinal = conn.prepareStatement(insertFinalSql);
             PreparedStatement pUpdateFinal = conn.prepareStatement(updateFinalSql)) {

            conn.setAutoCommit(false);

            pEnroll.setInt(1, sectionId);
            try (ResultSet rsEnroll = pEnroll.executeQuery()) {
                while (rsEnroll.next()) {
                    int enrollmentId = rsEnroll.getInt("enrollment_id");
                    pAvg.setInt(1, enrollmentId);
                    BigDecimal avg = BigDecimal.ZERO;
                    try (ResultSet rsAvg = pAvg.executeQuery()) {
                        if (rsAvg.next()) {
                            avg = rsAvg.getBigDecimal("avg_score");
                            if (avg == null) avg = BigDecimal.ZERO;
                        }
                    }

                    String letter = numericToLetter(avg);

                    // Does FINAL exist?
                    pSelectFinal.setInt(1, enrollmentId);
                    try (ResultSet rsFinal = pSelectFinal.executeQuery()) {
                        if (rsFinal.next()) {
                            int gradeId = rsFinal.getInt("grade_id");
                            pUpdateFinal.setBigDecimal(1, avg);
                            pUpdateFinal.setString(2, letter);
                            pUpdateFinal.setInt(3, gradeId);
                            pUpdateFinal.executeUpdate();
                        } else {
                            pInsertFinal.setInt(1, enrollmentId);
                            pInsertFinal.setBigDecimal(2, avg);
                            pInsertFinal.setString(3, letter);
                            pInsertFinal.executeUpdate();
                        }
                    }
                }
            }

            conn.commit();
            conn.setAutoCommit(true);
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private String numericToLetter(BigDecimal avg) {
        if (avg == null) return "F";
        double v = avg.doubleValue();
        if (v >= 90.0) return "A";
        if (v >= 80.0) return "B";
        if (v >= 70.0) return "C";
        if (v >= 60.0) return "D";
        return "F";
    }

        @Override
    public boolean isInstructorOfSection(String instructorUserId, int sectionId) {
        String sql = "SELECT 1 FROM sections s JOIN instructors i ON s.instructor_id = i.instructor_id WHERE s.section_id = ? AND i.user_id = ? LIMIT 1";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ps.setString(2, instructorUserId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isEnrollmentInSection(int enrollmentId, int sectionId) {
        String sql = "SELECT 1 FROM enrollments WHERE enrollment_id = ? AND section_id = ? LIMIT 1";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            ps.setInt(2, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // inside InstructorServiceImpl

@Override
public List<Map<String, Object>> getAllSections() {
    List<Map<String,Object>> out = new ArrayList<>();
    String sql =
        "SELECT s.section_id, s.course_id, c.title AS course_title, s.day, s.capacity, s.semester, s.year, i.user_id AS instructor_user_id " +
        "FROM sections s " +
        "LEFT JOIN courses c ON s.course_id = c.course_id " +
        "LEFT JOIN instructors i ON s.instructor_id = i.instructor_id " +
        "ORDER BY s.course_id, s.section_id";
    try (Connection conn = DBConnection.getStudentConnection();
         PreparedStatement ps = conn.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
            Map<String,Object> m = new HashMap<>();
            m.put("section_id", rs.getInt("section_id"));
            m.put("course_id", rs.getString("course_id"));
            m.put("course_title", rs.getString("course_title"));
            m.put("day", rs.getString("day"));
            m.put("capacity", rs.getInt("capacity"));
            m.put("semester", rs.getString("semester"));
            m.put("year", rs.getInt("year"));
            m.put("instructor_user_id", rs.getString("instructor_user_id"));
            out.add(m);
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
    return out;
}

@Override
public boolean saveGrade(int enrollmentId, String component, BigDecimal score) {
    // refuse to save if FINAL already exists for this enrollment
    String checkFinal = "SELECT 1 FROM grades WHERE enrollment_id = ? AND component = 'FINAL' LIMIT 1";
    String insertSql = "INSERT INTO grades (enrollment_id, component, score, final_grade) VALUES (?, ?, ?, NULL)";
    try (Connection conn = DBConnection.getStudentConnection();
         PreparedStatement pCheck = conn.prepareStatement(checkFinal)) {

        pCheck.setInt(1, enrollmentId);
        try (ResultSet rs = pCheck.executeQuery()) {
            if (rs.next()) {
                // final exists -> no further component grade allowed
                System.err.println("Attempt to save grade after FINAL exists for enrollment " + enrollmentId);
                return false;
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setInt(1, enrollmentId);
            ps.setString(2, component);
            ps.setBigDecimal(3, score);
            int rows = ps.executeUpdate();
            return rows == 1;
        }
    } catch (SQLIntegrityConstraintViolationException ex) {
        System.err.println("Save grade constraint: " + ex.getMessage());
        return false;
    } catch (SQLException ex) {
        ex.printStackTrace();
        return false;
    }
}

}
