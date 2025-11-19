package edu.univ.erp.service;

import java.sql.SQLException;

public interface AdminService {
    String createStudentUser(String username, String password, String fullName, String email,
                             String rollNo, Integer year, String program) throws SQLException;

    int createCourseAndSection(String courseId, String courseTitle, Integer credits, String departmentId,
                               int capacity, String day, String semester, int year,
                               String instructorUserId) throws SQLException;

    boolean setMaintenance(boolean on) throws SQLException;

    boolean isMaintenanceOn() throws SQLException;
}
