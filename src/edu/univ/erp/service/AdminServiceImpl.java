package edu.univ.erp.service;

import edu.univ.erp.data.DBConnection;
import edu.univ.erp.data.Dao.SettingsDao;

import edu.univ.erp.auth.PasswordUtil;

import java.sql.*;

public class AdminServiceImpl implements AdminService {

    private final SettingsDao settingsDao = new SettingsDao();

    @Override
    public String createStudentUser(String username, String password, String fullName, String email,
                                    String rollNo, Integer year, String program) throws SQLException {
        if (username == null || password == null) return null;

        String userId = null;

        String insertAuth = "INSERT INTO users_auth (user_id, username, role, password_hash, status) VALUES (?, ?, ?, ?, ?)";
        String insertStudent = "INSERT INTO students (user_id, roll_no, name, mobile, year, program) VALUES (?, ?, ?, ?, ?, ?)";

        Connection authConn = null;
        Connection studentConn = null;
        PreparedStatement psAuth = null;
        PreparedStatement psStudent = null;

        try {
            authConn = DBConnection.getAuthConnection();
            studentConn = DBConnection.getStudentConnection();

            try (PreparedStatement ch = authConn.prepareStatement("SELECT 1 FROM users_auth WHERE username = ? LIMIT 1")) {
                ch.setString(1, username);
                try (ResultSet rs = ch.executeQuery()) {
                    if (rs.next()) {
                        return null;
                    }
                }
            }

            String generatedId = java.util.UUID.randomUUID().toString();
            String pwHash = PasswordUtil.hash(password);

            psAuth = authConn.prepareStatement(insertAuth);
            psAuth.setString(1, generatedId);
            psAuth.setString(2, username);
            psAuth.setString(3, "STUDENT");
            psAuth.setString(4, pwHash);
            psAuth.setString(5, "active");

            int r = psAuth.executeUpdate();
            if (r != 1) {
                return null;
            }

            psStudent = studentConn.prepareStatement(insertStudent, Statement.RETURN_GENERATED_KEYS);
            psStudent.setString(1, generatedId);
            psStudent.setString(2, rollNo);
            psStudent.setString(3, fullName);
            psStudent.setString(4, null);
            if (year == null) psStudent.setNull(5, Types.INTEGER); else psStudent.setInt(5, year);
            psStudent.setString(6, program);

            int sr = psStudent.executeUpdate();
            if (sr != 1) {
                try (PreparedStatement del = authConn.prepareStatement("DELETE FROM users_auth WHERE user_id = ?")) {
                    del.setString(1, generatedId);
                    del.executeUpdate();
                } catch (Exception ex) { ex.printStackTrace(); }
                return null;
            }

            userId = generatedId;
            return userId;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (psAuth != null) try { psAuth.close(); } catch (Exception ignored) {}
            if (psStudent != null) try { psStudent.close(); } catch (Exception ignored) {}
            if (authConn != null) try { authConn.close(); } catch (Exception ignored) {}
            if (studentConn != null) try { studentConn.close(); } catch (Exception ignored) {}
        }
    }

    @Override
    public int createCourseAndSection(String courseId, String courseTitle, Integer credits, String departmentId,
                                      int capacity, String day, String semester, int year,
                                      String instructorUserId) throws SQLException {

        if (courseId == null || instructorUserId == null) return -1;

        Connection conn = null;
        PreparedStatement psCourse = null;
        PreparedStatement psSection = null;
        try {
            conn = DBConnection.getStudentConnection();
            conn.setAutoCommit(false);

            String existsCourse = "SELECT 1 FROM courses WHERE course_id = ? LIMIT 1";
            try (PreparedStatement ch = conn.prepareStatement(existsCourse)) {
                ch.setString(1, courseId);
                try (ResultSet rs = ch.executeQuery()) {
                    if (!rs.next()) {
                        String insertCourse = "INSERT INTO courses (course_id, title, credits, department_id) VALUES (?, ?, ?, ?)";
                        psCourse = conn.prepareStatement(insertCourse);
                        psCourse.setString(1, courseId);
                        psCourse.setString(2, courseTitle);
                        if (credits == null) psCourse.setNull(3, Types.INTEGER); else psCourse.setInt(3, credits);
                        psCourse.setString(4, departmentId);
                        psCourse.executeUpdate();
                        if (psCourse != null) { psCourse.close(); psCourse = null; }
                    }
                }
            }

            Integer instructorId = null;
            String findInstructor = "SELECT instructor_id FROM instructors WHERE user_id = ? LIMIT 1";
            try (PreparedStatement pFindInst = conn.prepareStatement(findInstructor)) {
                pFindInst.setString(1, instructorUserId);
                try (ResultSet rs = pFindInst.executeQuery()) {
                    if (rs.next()) instructorId = rs.getInt("instructor_id");
                }
            }
            if (instructorId == null) {
                conn.rollback();
                return -1;
            }

            String insertSection = "INSERT INTO sections (course_id, instructor_id, day, capacity, semester, year, registration_deadline) VALUES (?, ?, ?, ?, ?, ?, NULL)";
            psSection = conn.prepareStatement(insertSection, Statement.RETURN_GENERATED_KEYS);
            psSection.setString(1, courseId);
            psSection.setInt(2, instructorId);
            psSection.setString(3, day);
            psSection.setInt(4, capacity);
            psSection.setString(5, semester);
            psSection.setInt(6, year);

            int rr = psSection.executeUpdate();
            if (rr != 1) {
                conn.rollback();
                return -1;
            }

            try (ResultSet rs = psSection.getGeneratedKeys()) {
                if (rs.next()) {
                    int sectionId = rs.getInt(1);
                    conn.commit();
                    return sectionId;
                } else {
                    conn.rollback();
                    return -1;
                }
            }

        } catch (SQLException ex) {
            if (conn != null) try { conn.rollback(); } catch (Exception ignored) {}
            ex.printStackTrace();
            return -1;
        } finally {
            if (psCourse != null) try { psCourse.close(); } catch (Exception ignored) {}
            if (psSection != null) try { psSection.close(); } catch (Exception ignored) {}
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (Exception ignored) {}
        }
    }

    @Override
    public boolean setMaintenance(boolean on) throws SQLException {
        try {
            settingsDao.upsert("maintenance.on", Boolean.toString(on));
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }

        Connection conn = null;
        PreparedStatement pUpd = null;
        PreparedStatement pIns = null;
        try {
            conn = DBConnection.getStudentConnection();
            String upd = "UPDATE settings SET maintenance_on = ?, `value` = ? WHERE `key` = 'maintenance.on'";
            pUpd = conn.prepareStatement(upd);
            pUpd.setInt(1, on ? 1 : 0);
            pUpd.setString(2, Boolean.toString(on));
            int u = pUpd.executeUpdate();
            if (u == 0) {
                String ins = "INSERT INTO settings(`key`, `value`, maintenance_on) VALUES ('maintenance.on', ?, ?)";
                pIns = conn.prepareStatement(ins);
                pIns.setString(1, Boolean.toString(on));
                pIns.setInt(2, on ? 1 : 0);
                pIns.executeUpdate();
            }
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        } finally {
            if (pUpd != null) try { pUpd.close(); } catch (Exception ignored) {}
            if (pIns != null) try { pIns.close(); } catch (Exception ignored) {}
            if (conn != null) try { conn.close(); } catch (Exception ignored) {}
        }
    }

    @Override
    public boolean isMaintenanceOn() throws SQLException {
        return settingsDao.getBoolean("maintenance.on", false);
    }
}
