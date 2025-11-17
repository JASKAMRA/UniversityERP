package edu.univ.erp.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // ---------- AUTH DATABASE ----------
    private static final String AUTH_URL =
            "jdbc:mysql://localhost:3306/erp_auth?serverTimezone=UTC";

    // ---------- STUDENT DATABASE ----------
    private static final String STUDENT_URL =
            "jdbc:mysql://localhost:3306/erp_student?serverTimezone=UTC";

    // ---------- MYSQL USERNAME & PASSWORD ----------
    private static final String USER = "root";     // ya 'root'
    private static final String PASS = "JASKAMRA-011"; // jo tumne banaya

    // ---------- GET CONNECTION TO AUTH DB ----------
    public static Connection getAuthConnection() throws SQLException {
        return DriverManager.getConnection(AUTH_URL, USER, PASS);
    }

    // ---------- GET CONNECTION TO STUDENT DB ----------
    public static Connection getStudentConnection() throws SQLException {
        return DriverManager.getConnection(STUDENT_URL, USER, PASS);
    }
}
