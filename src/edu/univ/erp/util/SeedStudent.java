package edu.univ.erp.util;

import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Student;
import edu.univ.erp.auth.PasswordUtil;

import java.sql.*;
import java.util.*;
import java.io.*;

/**
 * SeedStudent utility for your project.
 *
 * Inserts:
 *  - erp_auth.users_auth (user_id, username, role, password_hash, status)
 *  - erp_student.students (student_id auto, user_id, roll_no, name, mobile, year, program)
 *
 * Usage:
 *  - Interactive: run with no args
 *  - Single: java ... SeedStudent username password roll_no program year name mobile
 *  - CSV: pass a single arg pointing to CSV file where each line:
 *         username,password,roll_no,program,year,name,mobile
 *
 * Notes:
 *  - Role is inserted as "STUDENT" (uppercase) to match enum checks.
 *  - If a username already exists, we print and continue.
 */
public class SeedStudent {

    private static class Row {
        String username, password, roll, program, name, mobile;
        int year;
        Row(String username, String password, String roll, String program, int year, String name, String mobile) {
            this.username = username; this.password = password; this.roll = roll; this.program = program;
            this.year = year; this.name = name; this.mobile = mobile;
        }
    }

    public static void main(String[] args) {
        List<Row> rows;
        try {
            rows = collectRows(args);
        } catch (Exception e) {
            System.err.println("Input collection failed: " + e.getMessage());
            return;
        }
        if (rows.isEmpty()) {
            System.out.println("No rows to seed. Exiting.");
            return;
        }

        for (Row r : rows) {
            seedSingle(r);
        }
    }

    private static void seedSingle(Row r) {
        // defensive trims
        r.username = safeTrim(r.username);
        r.password = safeTrim(r.password);
        r.roll = safeTrim(r.roll);
        r.program = safeTrim(r.program);
        r.name = safeTrim(r.name);
        r.mobile = safeTrim(r.mobile);

        if (r.username.isEmpty()) {
            System.err.println("Skipping row with empty username (roll=" + r.roll + ")");
            return;
        }
        String userId = UUID.randomUUID().toString();
        String role = "STUDENT"; // use uppercase - matches Role enum checks
        String pwHash;
        try {
            pwHash = PasswordUtil.hash(r.password); // uses your existing BCrypt util
        } catch (Exception ex) {
            System.err.println("Password hashing failed for username=" + r.username + " : " + ex.getMessage());
            return;
        }

        // Insert into auth DB
        final String authSql = "INSERT INTO users_auth (user_id, username, role, password_hash, status) VALUES (?, ?, ?, ?, ?)";
        final String studentSql = "INSERT INTO students (user_id, roll_no, name, mobile, year, program) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection authConn = DBConnection.getAuthConnection();
             PreparedStatement authPs = authConn.prepareStatement(authSql)) {

            authPs.setString(1, userId);
            authPs.setString(2, r.username);
            authPs.setString(3, role);
            authPs.setString(4, pwHash);
            authPs.setString(5, "active");

            int arows = 0;
            try {
                arows = authPs.executeUpdate();
            } catch (SQLIntegrityConstraintViolationException icv) {
                // likely duplicate username or user_id collision
                System.err.println("Skipping username (constraint): " + r.username + " -> " + icv.getMessage());
                return;
            } catch (SQLException sqe) {
                System.err.println("Auth insert failed for username=" + r.username + " : " + sqe.getMessage());
                return;
            }

            if (arows != 1) {
                System.err.println("Auth insert affected " + arows + " rows for username=" + r.username + " - skipping.");
                return;
            }

            // Insert into student DB (separate DB connection)
            try (Connection studConn = DBConnection.getStudentConnection();
                 PreparedStatement studPs = studConn.prepareStatement(studentSql, Statement.RETURN_GENERATED_KEYS)) {

                studPs.setString(1, userId);
                studPs.setString(2, r.roll == null ? "" : r.roll);
                studPs.setString(3, r.name == null ? "" : r.name);
                if (r.mobile == null || r.mobile.isEmpty()) studPs.setNull(4, Types.VARCHAR);
                else studPs.setString(4, r.mobile);
                if (r.year <= 0) studPs.setNull(5, Types.INTEGER);
                else studPs.setInt(5, r.year);
                if (r.program == null || r.program.isEmpty()) studPs.setNull(6, Types.VARCHAR);
                else studPs.setString(6, r.program);

                int srows = 0;
                try {
                    srows = studPs.executeUpdate();
                } catch (SQLException sEx) {
                    System.err.println("Student insert failed for username=" + r.username + " : " + sEx.getMessage());
                    // rollback auth
                    deleteAuthUser(userId);
                    return;
                }

                if (srows != 1) {
                    System.err.println("Student insert affected " + srows + " rows for username=" + r.username + " - rolling back auth.");
                    deleteAuthUser(userId);
                    return;
                }

                // read generated student_id and display
                try (ResultSet gk = studPs.getGeneratedKeys()) {
                    String studentId = "";
                    if (gk != null && gk.next()) {
                        studentId = String.valueOf(gk.getInt(1));
                    }
                    // populate domain Student and show summary
                    Student s = new Student();
                    s.SetID(userId);
                    s.SetStudentID(studentId != null ? studentId : "");
                    s.SetRollNum(r.roll);
                    s.SetName(r.name);
                    s.SetEmail(r.mobile);
                    s.SetYear(r.year);
                    s.SetProgram(r.program);

                    System.out.println("Seeded: username=" + r.username + ", user_id=" + userId + ", student_id=" + s.GetStudentID());
                }

            } catch (SQLException sEx) {
                System.err.println("Student DB error for username=" + r.username + " : " + sEx.getMessage());
                // rollback auth
                deleteAuthUser(userId);
            }

        } catch (SQLException ex) {
            System.err.println("Auth DB error for username=" + r.username + " : " + ex.getMessage());
        }
    }

    private static void deleteAuthUser(String userId) {
        final String del = "DELETE FROM users_auth WHERE user_id = ?";
        try (Connection c = DBConnection.getAuthConnection();
             PreparedStatement ps = c.prepareStatement(del)) {
            ps.setString(1, userId);
            ps.executeUpdate();
            System.out.println("Rolled back auth user " + userId);
        } catch (SQLException ex) {
            System.err.println("Failed to rollback auth user " + userId + " : " + ex.getMessage());
        }
    }

    private static List<Row> collectRows(String[] args) throws Exception {
        List<Row> out = new ArrayList<>();
        if (args != null && args.length == 1 && args[0].toLowerCase().endsWith(".csv")) {
            // CSV: username,password,roll_no,program,year,name,mobile
            File f = new File(args[0]);
            if (!f.exists() || !f.isFile()) {
                throw new FileNotFoundException("CSV file not found: " + args[0]);
            }
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                int lineno = 0;
                while ((line = br.readLine()) != null) {
                    lineno++;
                    if (line.trim().isEmpty()) continue;
                    // skip possible header (if contains the word username)
                    if (lineno == 1 && line.toLowerCase().contains("username") && line.toLowerCase().contains("password")) {
                        continue;
                    }
                    String[] t = line.split(",", -1);
                    // accept either 7 columns (with mobile) or 6 columns (without mobile)
                    if (t.length < 6) {
                        System.err.println("Skipping invalid CSV line " + lineno + ": " + line);
                        continue;
                    }
                    String username = safeTrim(t[0]);
                    String password = safeTrim(t[1]);
                    String roll = safeTrim(t[2]);
                    String program = safeTrim(t[3]);
                    int year = safeParseInt(safeTrim(t[4]), 0);
                    String name = safeTrim(t[5]);
                    String mobile = (t.length >= 7) ? safeTrim(t[6]) : "";
                    out.add(new Row(username, password, roll, program, year, name, mobile));
                }
            }
            return out;
        } else if (args != null && args.length >= 7) {
            out.add(new Row(safeTrim(args[0]), safeTrim(args[1]), safeTrim(args[2]), safeTrim(args[3]),
                    safeParseInt(safeTrim(args[4]), 0), safeTrim(args[5]), safeTrim(args[6])));
            return out;
        } else {
            // interactive prompt for one student
            Scanner sc = new Scanner(System.in);
            System.out.println("Interactive seed (leave username blank to cancel)");
            System.out.print("username: "); String username = sc.nextLine().trim();
            if (username.isEmpty()) return out;
            System.out.print("password: "); String password = sc.nextLine().trim();
            System.out.print("roll_no: "); String roll = sc.nextLine().trim();
            System.out.print("program: "); String program = sc.nextLine().trim();
            System.out.print("year (enter for unknown): ");
            String yearStr = sc.nextLine().trim();
            int year = safeParseInt(yearStr, 0);
            System.out.print("name: "); String name = sc.nextLine().trim();
            System.out.print("mobile (optional): "); String mobile = sc.nextLine().trim();
            out.add(new Row(username, password, roll, program, year, name, mobile));
            return out;
        }
    }

    private static int safeParseInt(String s, int def) {
        if (s == null || s.isEmpty()) return def;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}
