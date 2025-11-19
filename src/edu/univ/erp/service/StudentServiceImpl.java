package edu.univ.erp.service;

import edu.univ.erp.data.DBConnection;
import edu.univ.erp.data.Dao.CourseDao;
import edu.univ.erp.data.Dao.EnrollmentDao;
import edu.univ.erp.data.Dao.SectionDao;
import edu.univ.erp.data.Dao.StudentDAO;

import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.Status;

import java.io.File;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/**
 * StudentService implementation — handles catalog, registration, registrations listing and drop.
 */
public class StudentServiceImpl implements StudentService {

    private final CourseDao courseDao = new CourseDao();
    private final SectionDao sectionDao = new SectionDao();
    private final EnrollmentDao enrollmentDao = new EnrollmentDao();
    private final StudentDAO studentDao = new StudentDAO();

    // ------------------------------------------------------
    // COURSE CATALOG
    // ------------------------------------------------------
    @Override
    public List<Course> getCourseCatalog() throws Exception {
        return courseDao.findAll();
    }

    @Override
    public List<Section> getSectionsForCourse(String courseId) throws Exception {
        return sectionDao.findByCourse(courseId);
    }

    // ------------------------------------------------------
    // REGISTER FOR SECTION (transactional, capacity + duplicate + deadline checks)
    // ------------------------------------------------------
    @Override
    public boolean registerForSection(String userId, int sectionId) throws Exception {
        // resolve user -> student_id
        Optional<Student> stOpt = studentDao.findByUserId(userId);
        if (!stOpt.isPresent()) {
            System.err.println("[register] no student profile for userId=" + userId);
            return false;
        }
        String studentId = stOpt.get().GetStudentID();

        // transactional register with row lock to prevent oversubscribe
        try (Connection conn = DBConnection.getStudentConnection()) {
            try {
                conn.setAutoCommit(false);

                // 1) lock section row to read capacity & deadline
                String secSql = "SELECT capacity, registration_deadline FROM sections WHERE section_id = ? FOR UPDATE";
                Integer capacity = null;
                Timestamp regDeadline = null;
                try (PreparedStatement ps = conn.prepareStatement(secSql)) {
                    ps.setInt(1, sectionId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            System.err.println("[register] section not found: " + sectionId);
                            return false;
                        }
                        capacity = rs.getInt("capacity");
                        regDeadline = rs.getTimestamp("registration_deadline");
                    }
                }

                // 2) check registration deadline if set
                if (regDeadline != null) {
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    if (now.after(regDeadline)) {
                        conn.rollback();
                        System.err.println("[register] past registration deadline for section " + sectionId);
                        return false;
                    }
                }

                // 3) count current enrolled (only count active statuses)
                String countSql = "SELECT COUNT(*) FROM enrollments WHERE section_id = ? AND status IN ('ENROLLED','Confirmed','enrolled','confirmed')";
                int enrolledCount = 0;
                try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                    ps.setInt(1, sectionId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) enrolledCount = rs.getInt(1);
                    }
                }

                if (capacity != null && enrolledCount >= capacity) {
                    conn.rollback();
                    System.err.println("[register] no seats left (capacity=" + capacity + ", enrolled=" + enrolledCount + ")");
                    return false;
                }

                // 4) duplicate check (same section or already in same course)
                String dupSql =
                        "SELECT COUNT(*) FROM enrollments e " +
                                "JOIN sections s ON e.section_id = s.section_id " +
                                "WHERE e.student_id = ? AND (e.section_id = ? OR s.course_id = (SELECT course_id FROM sections WHERE section_id = ?))";
                try (PreparedStatement ps = conn.prepareStatement(dupSql)) {
                    ps.setString(1, studentId);
                    ps.setInt(2, sectionId);
                    ps.setInt(3, sectionId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            conn.rollback();
                            System.err.println("[register] duplicate enrollment detected for student " + studentId);
                            return false;
                        }
                    }
                }

                // 5) insert enrollment (in same transaction/connection)
                String insSql = "INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, studentId);
                    ps.setInt(2, sectionId);
                    ps.setString(3, "ENROLLED"); // adapt to your enum string if different
                    ps.executeUpdate();
                }

                conn.commit();
                return true;

            } catch (Exception ex) {
                try { conn.rollback(); } catch (Exception ignore) {}
                throw ex;
            } finally {
                try { conn.setAutoCommit(true); } catch (Exception ignore) {}
            }
        }
    }

    // ------------------------------------------------------
    // MY REGISTRATIONS TABLE (UI)
    // ------------------------------------------------------
    /**
     * Returns rows as:
     * [ 0: enrollment_id (Integer),
     *   1: course_id     (String),
     *   2: section_id    (Integer),
     *   3: day           (String),
     *   4: semester      (String),
     *   5: status        (String) ]
     */
    @Override
    public List<Object[]> getMyRegistrations(String userId) throws Exception {
        // resolve userId -> student record
        Optional<Student> stOpt = studentDao.findByUserId(userId);
        if (!stOpt.isPresent()) {
            return Collections.emptyList();
        }

        String studentId = stOpt.get().GetStudentID();

        List<Enrollment> enrollments = enrollmentDao.findByStudent(studentId);
        if (enrollments == null || enrollments.isEmpty()) return Collections.emptyList();

        List<Object[]> rows = new ArrayList<>();
        for (Enrollment e : enrollments) {
            // load section
            Section sec = sectionDao.findById(e.GetSectionID());
            if (sec == null) continue;

            // load course
            Course c = courseDao.findById(sec.GetCourseID());
            String courseId = (c != null) ? c.GetCourseID() : sec.GetCourseID();

            Object[] row = new Object[]{
                    e.GetEnrollmentID(),                                      // Integer
                    courseId,                                                 // String
                    sec.GetSectionID(),                                       // Integer
                    sec.GetDay() == null ? "" : sec.GetDay().name(),          // String
                    sec.GetSemester(),                                        // String
                    e.GetStatus() == null ? "" : e.GetStatus().name()         // String
            };
            rows.add(row);
        }
        return rows;
    }

    // ------------------------------------------------------
    // DROP REGISTRATION
    // ------------------------------------------------------
    @Override
    public boolean dropEnrollment(int enrollmentId) throws Exception {
        // simple delete (you may choose to set status='Dropped' instead)
        enrollmentDao.deleteEnrollment(enrollmentId);
        return true;
    }
    @Override
public List<Section> getTimetable(String userId) throws Exception {
    // find student record by auth user id
    Optional<Student> stOpt = studentDao.findByUserId(userId);
    if (!stOpt.isPresent()) return Collections.emptyList();

    String studentId = stOpt.get().GetStudentID();

    // find enrollments
    List<Enrollment> enrolls = enrollmentDao.findByStudent(studentId);
    if (enrolls == null || enrolls.isEmpty()) return Collections.emptyList();

    List<Section> out = new ArrayList<>();
    for (Enrollment e : enrolls) {
        Section s = sectionDao.findById(e.GetSectionID());
        if (s != null) out.add(s);
    }

    // optional: sort by day then by section_id
    out.sort(Comparator.comparing((Section s) -> s.GetDay() == null ? "" : s.GetDay().name())
                       .thenComparingInt(Section::GetSectionID));
    return out;
}
@Override
public File generateTranscriptCsv(String userId) throws Exception {
    // get registrations (we already implemented getMyRegistrations)
    List<Object[]> regs = getMyRegistrations(userId); // [enrollId, courseId, sectionId, day, semester, status]

    // create temp file
    File out = File.createTempFile("transcript_" + userId + "_", ".csv");
    try (PrintWriter pw = new PrintWriter(out)) {
        pw.println("Course,Section,Semester,Year,Component,Score,FinalGrade");
        for (Object[] r : regs) {
            Integer enrollId = (Integer) r[0];
            String courseId = String.valueOf(r[1]);
            Integer sectionId = (Integer) r[2];
            String semester = String.valueOf(r[4]);
            // try to find final grade from grades table
            // you can add a helper in GradeDao; for now we do a simple query here using DBConnection
            String finalGrade = "";
            // get grade rows for this enrollment
            String sql = "SELECT component, score, final_grade FROM grades WHERE enrollment_id = ?";
            try (Connection conn = DBConnection.getStudentConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, enrollId);
                try (ResultSet rs = ps.executeQuery()) {
                    boolean foundAny = false;
                    while (rs.next()) {
                        foundAny = true;
                        String comp = rs.getString("component");
                        BigDecimal score = rs.getBigDecimal("score");
                        String fg = rs.getString("final_grade");
                        pw.printf("%s,%d,%s,%s,%s,%s,%s%n",
                                escapeCsv(courseId),
                                sectionId,
                                escapeCsv(semester),
                                "", // year not fetched here; add if you want
                                escapeCsv(comp),
                                score == null ? "" : score.toPlainString(),
                                escapeCsv(fg)
                        );
                    }
                    if (!foundAny) {
                        // no grade rows — still write an empty row for course
                        pw.printf("%s,%d,%s,%s,%s,%s,%s%n",
                                escapeCsv(courseId),
                                sectionId,
                                escapeCsv(semester),
                                "",
                                "",
                                "",
                                ""
                        );
                    }
                }
            }
        }
    }
    return out;
}

private String escapeCsv(String s) {
    if (s == null) return "";
    if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
        s = s.replace("\"", "\"\"");
        return "\"" + s + "\"";
    }
    return s;
}

@Override
public List<Object[]> getGrades(String userId) throws Exception {
    // resolve user -> studentId
    Optional<Student> stOpt = studentDao.findByUserId(userId);
    if (!stOpt.isPresent()) return Collections.emptyList();
    String studentId = stOpt.get().GetStudentID();

    // get enrollments for student
    List<Enrollment> enrolls = enrollmentDao.findByStudent(studentId);
    if (enrolls == null || enrolls.isEmpty()) return Collections.emptyList();

    List<Object[]> rows = new ArrayList<>();

    // We'll query grades for each enrollment and also include the course id
    String sql = "SELECT g.component, g.score, g.final_grade, s.course_id, s.section_id " +
                 "FROM grades g " +
                 "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                 "JOIN sections s ON e.section_id = s.section_id " +
                 "WHERE e.enrollment_id = ? " +
                 "ORDER BY g.grade_id";

    try (Connection conn = DBConnection.getStudentConnection()) {
        for (Enrollment e : enrolls) {
            boolean any = false;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, e.GetEnrollmentID());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        any = true;
                        String component = rs.getString("component");
                        BigDecimal score = rs.getBigDecimal("score");
                        String finalGrade = rs.getString("final_grade");
                        String courseId = rs.getString("course_id");
                        Integer sectionId = rs.getInt("section_id");
                        rows.add(new Object[]{ courseId, sectionId, component, score, finalGrade });
                    }
                }
            }
            if (!any) {
                // no component rows for this enrollment — still add an empty row so course shows up
                Section sec = sectionDao.findById(e.GetSectionID());
                String courseId = sec == null ? "" : sec.GetCourseID();
                rows.add(new Object[]{ courseId, e.GetSectionID(), "", null, "" });
            }
        }
    }

    return rows;
}

@Override
public String getInstructorNameForSection(edu.univ.erp.domain.Section section) throws Exception {
    if (section == null) return "";

    // instructor id value from Section may be stored as VARCHAR (user_id) or as numeric PK (instructor_id).
    String instrVal = section.GetInstructorID();
    if (instrVal == null || instrVal.trim().isEmpty()) return "";

    // try numeric (instructor_id INT)
    try (Connection conn = DBConnection.getStudentConnection()) {
        // if numeric -> query by instructor_id (INT)
        try {
            int iid = Integer.parseInt(instrVal);
            String q = "SELECT name FROM instructors WHERE instructor_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(q)) {
                ps.setInt(1, iid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getString("name");
                }
            }
        } catch (NumberFormatException nfe) {
            // not an integer — try lookup by user_id (VARCHAR)
            String q2 = "SELECT name FROM instructors WHERE user_id = ?";
            try (PreparedStatement ps2 = conn.prepareStatement(q2)) {
                ps2.setString(1, instrVal);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    if (rs2.next()) return rs2.getString("name");
                }
            }
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
        // don't throw fatal — return empty (caller will show blank)
        return "";
    }
    return "";
}


}
