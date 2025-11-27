package edu.univ.erp.service;

import edu.univ.erp.data.Dao.*;
import edu.univ.erp.domain.User;
import edu.univ.erp.domain.Student;
import edu.univ.erp.access.*;
import edu.univ.erp.domain.Role;

import java.sql.SQLException;

import edu.univ.erp.auth.PasswordUtil;

public class AdminServiceImpl implements AdminService {

    private final SettingsDao settingsDao = new SettingsDao();
    private final UserDao userDao = new UserDao();
    private final StudentDAO studentDao = new StudentDAO();
    private final CourseDao courseDao = new CourseDao();
    private final InstructorDAO instructorDao = new InstructorDAO();
    private final SectionDao sectionDao = new SectionDao();

    @Override
    public String CreateStuUser(String User_Name, String pass,String name,String email,String rollNo,Integer year,String program) throws SQLException{
        if(User_Name==null||pass==null){
            return null;
        }
        User existing=userDao.Find_From_Username(User_Name);
        if(existing != null){
            return null;
        }
        String userId = java.util.UUID.randomUUID().toString();
        String hash = PasswordUtil.hash(pass);
        
        User u = new User();
        u.SetID(userId);u.SetUsername(User_Name);u.SetRole(Role.STUDENT);u.SetHashPass(hash);u.SetStatus("active");

        Student s = new Student();
        s.SetID(userId);s.SetRollNum(rollNo);s.SetName(name);s.SetEmail(email);s.SetYear(year);s.SetProgram(program);

        try {userDao.createUser(u); 
        } 
        catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
        int rows=studentDao.insertStudent(s); 
        if (rows!=1) {
            try { 
                userDao.deleteById(userId);
         }
        catch (Exception ex) { 
            ex.printStackTrace();
        }return null;
        }return userId;
    }
    @Override
    public boolean IS_Maintenance_on()throws SQLException {
        return AccessControl.isMaintenance();
    }

    @Override
    public int CreateCandS(String courseId, String courseTitle, Integer credits, String departmentId, int capacity, String day, String semester, int year, String instructorUserId) throws SQLException {
        if (courseId == null || instructorUserId == null) return -1;
        java.sql.Connection Connect = null;
        try {
            Connect = edu.univ.erp.data.DBConnection.getStudentConnection();Connect.setAutoCommit(false);
            if (courseDao.findById(courseId) == null) {
                int Crows=courseDao.createCourse(Connect, courseId, courseTitle, credits, departmentId);
                if (Crows != 1){
                    Connect.rollback();
                    return -1;
                }
            }
    //creating instructor id
            Integer instructorId = null;    
            try {
                instructorId = instructorDao.findByUserId(instructorUserId) != null ?
                        Integer.parseInt(instructorDao.findByUserId(instructorUserId).GetID()) : null;
            } catch (NumberFormatException nfe) {
                instructorId = null;
            }
            if (instructorId == null) { Connect.rollback(); return -1; }
//creatting section
            int sectionId = sectionDao.createSection(Connect, courseId, instructorId, day, capacity, semester, year);
            if (sectionId <= 0) { Connect.rollback(); return -1; }

            Connect.commit();
            return sectionId;
        } catch (Exception ex) {
            if (Connect != null) try {
                Connect.rollback(); }
            catch (Exception ignored) {}
            ex.printStackTrace();
            return -1;
        } finally {
            if (Connect != null) 
            try { Connect.setAutoCommit(true); Connect.close(); } 
            catch (Exception ignored) {}
        }
    }

    @Override
    public boolean Set_Maintenance(boolean on) throws SQLException {
        settingsDao.insert_into_settings("maintenance.on",Boolean.toString(on));
        settingsDao.updateMaintenance(on);
        return true;
    }

    
}
