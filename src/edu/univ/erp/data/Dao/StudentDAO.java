package edu.univ.erp.data.Dao;

import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Student;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StudentDAO {

    // Insert student. DB student_id is INT AUTO_INCREMENT, domain wants String student_id,
    // so after insert we set domain.setStudentId(String.valueOf(generatedKey))
    public boolean insertStudent(Student s) {
        String sql = "INSERT INTO students (user_id, roll_num, name, mobile, year, program) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, s.GetID());              // domain GetID() => user_id
            ps.setString(2, s.GetRollNum());         // roll_num
            ps.setString(3, s.GetName());            // name
            ps.setString(4, s.GetEmail());           // you kept email field in domain as email_id
            ps.setInt(5, s.GetYear());               // year
            ps.setString(6, s.GetProgram());         // program

            int rows = ps.executeUpdate();
            if (rows == 1) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int gen = rs.getInt(1);
                        s.SetStudentID(String.valueOf(gen)); // store generated int as String
                    }
                }
                return true;
            }
            return false;
        } catch (SQLIntegrityConstraintViolationException ex) {
            System.err.println("Student insert failed - constraint: " + ex.getMessage());
            return false;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Find by PK student_id (int). Maps to domain by setting student_id String.
    public Optional<Student> findById(int studentId) {
        String sql = "SELECT student_id, user_id, roll_num, name, mobile, year, program FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student s = mapRowToStudent(rs);
                    s.SetStudentID(String.valueOf(rs.getInt("student_id")));
                    return Optional.of(s);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }

    // Find by user_id (String)
    public Optional<Student> findByUserId(String userId) {
        String sql = "SELECT student_id, user_id, roll_num, name, mobile, year, program FROM students WHERE user_id = ?";
        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Student s = mapRowToStudent(rs);
                    s.SetStudentID(String.valueOf(rs.getInt("student_id")));
                    return Optional.of(s);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }

    // Update by PK (student_id). Domain stores student_id as String so convert to int.
    public boolean updateStudent(Student s) {
        String sql = "UPDATE students SET user_id = ?, roll_num = ?, name = ?, mobile = ?, year = ?, program = ? WHERE student_id = ?";
        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, s.GetID());
            ps.setString(2, s.GetRollNum());
            ps.setString(3, s.GetName());
            ps.setString(4, s.GetEmail());
            ps.setInt(5, s.GetYear());
            ps.setString(6, s.GetProgram());

            // convert domain student_id string to int; handle null/empty defensively
            int sid = parseIntSafe(s.GetStudentID());
            ps.setInt(7, sid);

            int rows = ps.executeUpdate();
            return rows == 1;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Delete by PK
    public boolean deleteById(int studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            int rows = ps.executeUpdate();
            return rows == 1;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // Delete by user_id (useful for TestDao cleanup)
    public boolean deleteByUserId(String userId) {
        String sql = "DELETE FROM students WHERE user_id = ?";
        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            int rows = ps.executeUpdate();
            return rows >= 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // List all
    public List<Student> findAll() {
        String sql = "SELECT student_id, user_id, roll_num, name, mobile, year, program FROM students";
        List<Student> out = new ArrayList<>();
        try (Connection conn = DBConnection.getAuthConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Student s = mapRowToStudent(rs);
                s.SetStudentID(String.valueOf(rs.getInt("student_id")));
                out.add(s);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return out;
    }

    // helper
    private Student mapRowToStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.SetID(rs.getString("user_id"));         // domain SetID -> user_id
        s.SetRollNum(rs.getString("roll_num"));   // Roll_num
        s.SetName(rs.getString("name"));          // name
        s.SetEmail(rs.getString("mobile"));       // WARNING: your domain uses email_id, DB column mobile -> you earlier listed mobile; adjust if domain expects email_id
        // I assigned mobile into email field because domain has email_id; if you want separate mobile field in domain, update domain accordingly.
        s.SetYear(rs.getInt("year"));
        s.SetProgram(rs.getString("program"));
        return s;
    }

    // parse domain-stored PK safely
    private int parseIntSafe(String s) {
        if (s == null || s.isEmpty()) return 0;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
