package edu.univ.erp;

import edu.univ.erp.data.DBConnection;
import edu.univ.erp.data.Dao.*;
import edu.univ.erp.domain.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.DayOfWeek;
import java.util.List;

public class TestDao {
    public static void main(String[] args) {
        // change these constants if you want different test ids
        final String TEST_USER_ID = "u1001";
        final String TEST_USERNAME = "testuser";
        final String TEST_EMAIL = "abc@gmail";

        // keep references so we can delete later
        User u = null;
        Course c = null;
        Section s = null;
        Enrollment e = null;

        try {
            // ---------- 1. TEST USER DAO (auth DB) ----------
            UserDao userDao = new UserDao();
            // use the constructor you already used
            u = new User(TEST_USER_ID, TEST_USERNAME, TEST_EMAIL, "hashed123", Role.STUDENT);
            userDao.createUser(u);
            System.out.println("User Inserted Successfully!");

            User fetched = userDao.findByUsername(TEST_USERNAME);
            System.out.println("Fetched User = " + fetched);

            // ---------- 1.5 Insert STUDENT and INSTRUCTOR rows in student DB (so FKs succeed) ----------
            // Insert a student row for TEST_USER_ID
            try (Connection conn = DBConnection.getStudentConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO students (user_id, name, program, year) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, TEST_USER_ID);
                ps.setString(2, "Test Student");
                ps.setString(3, TEST_EMAIL);
                ps.setString(4, "B.Tech");    // sample program
                ps.setInt(5, 2);              // sample year
                ps.executeUpdate();
                System.out.println("Student row inserted for user_id=" + TEST_USER_ID);
            } catch (SQLException ex) {
                // if row already exists, ignore (but print)
                System.out.println("Student insert skipped / error: " + ex.getMessage());
            }

            // Insert an instructor row for TEST_USER_ID (so sections.instructor_id FK passes)
            try (Connection conn = DBConnection.getStudentConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO instructors (user_id, name, email, department) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, TEST_USER_ID);
                ps.setString(2, "Demo Instructor");
                ps.setString(3, "demo@" + TEST_USERNAME + ".com");
                ps.setString(4, "CSE");
                ps.executeUpdate();
                System.out.println("Instructor row inserted for user_id=" + TEST_USER_ID);
            } catch (SQLException ex) {
                System.out.println("Instructor insert skipped / error: " + ex.getMessage());
            }

            // ---------- 2. TEST COURSE DAO (student DB) ----------
            CourseDao courseDao = new CourseDao();
            // keep using your constructor/order — adjust if your Course constructor differs
            c = new Course("CSE", "Intro to CS", "CSE101", 4);
            courseDao.createCourse(c);
            System.out.println("Course Inserted Successfully!");

            Course fetchedCourse = courseDao.findById("CSE101");
            System.out.println("Fetched Course = " + fetchedCourse);

            // ---------- 3. TEST SECTION DAO ----------
            SectionDao sectionDao = new SectionDao();
            s = new Section(
                    null,                             // section_id (auto)
                    "Spring",                         // semester
                    "CSE101",                         // course_id
                    TEST_USER_ID,                     // instructor_id (must exist in instructors.user_id)
                    60,                               // capacity
                    DayOfWeek.MONDAY,                 // day
                    2025                              // year
            );

            sectionDao.createSection(s);
            System.out.println("Section Created! ID = " + s.GetSectionID());

            // ---------- 4. TEST ENROLLMENT DAO ----------
            EnrollmentDao enrollDao = new EnrollmentDao();
            e = new Enrollment(
                    0,                        // enrollment_id placeholder (auto)
                    TEST_USER_ID,             // student_id (must exist in students.user_id)
                    s.GetSectionID(),         // section_id
                    Status.Confirmed          // enum value (use exact enum name from your Status.java)
            );

            enrollDao.enrollStudent(e);
            System.out.println("Enrollment Done! ID = " + e.GetEnrollmentID());

            List<Enrollment> enrollments = enrollDao.findByStudent(TEST_USER_ID);
            System.out.println("Student enrollments: " + enrollments);

            // ---------- 5. TEST SETTINGS DAO ----------
            SettingsDao settingsDao = new SettingsDao();
            settingsDao.upsert("maintenance_mode", "true");
            System.out.println("Setting upserted.");
            System.out.println("Setting fetched: " + settingsDao.findByKey("maintenance_mode"));

            // ----------------- NOW DELETE IN REVERSE/SAFE ORDER -----------------
            System.out.println("\n--- Deleting inserted test data now ---");

            // 1) delete enrollment (student DB)
            if (e != null && e.GetEnrollmentID() != null && e.GetEnrollmentID() > 0) {
                try (Connection conn = DBConnection.getStudentConnection();
                     PreparedStatement ps = conn.prepareStatement("DELETE FROM enrollments WHERE enrollment_id = ?")) {
                    ps.setInt(1, e.GetEnrollmentID());
                    int deleted = ps.executeUpdate();
                    System.out.println("Enrollment deletion rows: " + deleted);
                }
            }

            // 2) delete section (student DB)
            if (s != null && s.GetSectionID() != null && s.GetSectionID() > 0) {
                sectionDao.deleteSection(s.GetSectionID());
                System.out.println("Section deleted: id=" + s.GetSectionID());
            }

            // 3) delete course (student DB)
            if (c != null) {
                try {
                    courseDao.deleteCourse(c.GetCourseID());
                    System.out.println("Course deleted: id=" + c.GetCourseID());
                } catch (Exception ex) {
                    System.out.println("Course delete skipped/error: " + ex.getMessage());
                }
            }

            // 4) delete setting (student DB)
            try (Connection conn = DBConnection.getStudentConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM settings WHERE `key` = ?")) {
                ps.setString(1, "maintenance_mode");
                int deleted = ps.executeUpdate();
                System.out.println("Settings deletion rows: " + deleted);
            } catch (SQLException ex) {
                System.out.println("Settings delete error: " + ex.getMessage());
            }

            // 5) delete student row (student DB)
            try (Connection conn = DBConnection.getStudentConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM students WHERE user_id = ?")) {
                ps.setString(1, TEST_USER_ID);
                int deleted = ps.executeUpdate();
                System.out.println("Student deletion rows: " + deleted);
            } catch (SQLException ex) {
                System.out.println("Student delete error: " + ex.getMessage());
            }

            // 6) delete instructor row (student DB)
            try (Connection conn = DBConnection.getStudentConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM instructors WHERE user_id = ?")) {
                ps.setString(1, TEST_USER_ID);
                int deleted = ps.executeUpdate();
                System.out.println("Instructor deletion rows: " + deleted);
            } catch (SQLException ex) {
                System.out.println("Instructor delete error: " + ex.getMessage());
            }

            // 7) delete user from auth DB (users_auth)
            try (Connection conn = DBConnection.getAuthConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM users_auth WHERE user_id = ?")) {
                ps.setString(1, TEST_USER_ID);
                int deleted = ps.executeUpdate();
                System.out.println("Auth user deletion rows: " + deleted);
            } catch (SQLException ex) {
                System.out.println("Auth user delete error: " + ex.getMessage());
            }

            System.out.println("--- All cleanup done ---");

        } catch (SQLException ex) {
            System.err.println("SQL Error: ");
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
