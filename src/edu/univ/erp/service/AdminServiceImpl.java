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
    private final SectionDao sectionDao = new SectionDao();
    private final StudentDAO studentDao = new StudentDAO();
    private final CourseDao courseDao = new CourseDao();
    private final InstructorDAO instructorDao = new InstructorDAO();

    @Override
    public String CreateStuUser(String User_Name, String pass,String name,String email,String rollNo,Integer year,String program) throws SQLException{
        if(pass==null||User_Name==null){
            return null;
        }

        User existing=userDao.Find_From_Username(User_Name);

        if(existing!=null){
            return null;
        }

        String userId=java.util.UUID.randomUUID().toString();
        String hash=PasswordUtil.hash(pass);
        
        User user=new User();
        user.SetID(userId);
        user.SetUsername(User_Name);
        user.SetRole(Role.STUDENT);
        user.SetStatus("active");
        user.SetHashPass(hash);

        Student student=new Student();
        student.SetID(userId);
        student.SetRollNum(rollNo);
        student.SetName(name);
        student.SetEmail(email);
        student.SetYear(year);
        student.SetProgram(program);

        try {
            userDao.createUser(user); 
        } 
        catch (Exception exception){
            exception.printStackTrace();
            return null;
        }
        int row=studentDao.insertStudent(student);

        if (row!=1) {
            try { 
                userDao.deleteById(userId);
            }
        catch (Exception exception) { 
            exception.printStackTrace();
        }
        return null;
        }
        return userId;
    }
    @Override
    public boolean is_Maintenance_on() throws SQLException {
        return AccessControl.isMaintenance();
    }

    @Override
    public int CreateCandS(String courseId, String courseTitle, Integer credits, String departmentId, int capacity, String day, String semester, int year, String instructorUserId) throws SQLException {
        if (instructorUserId==null || courseId==null) {
            return -1;
        }
        java.sql.Connection Connect=null;
        try {
            Connect=edu.univ.erp.data.DBConnection.getStudentConnection();Connect.setAutoCommit(false);
            if (courseDao.findById(courseId)==null) {
                int Crows=courseDao.createCourse(Connect, courseId, courseTitle, credits, departmentId);
                if (Crows != 1){
                    Connect.rollback();
                    return -1;
                }
            }

            Integer instructorId=null;    
            try {
                instructorId=instructorDao.findByUserId(instructorUserId) != null ?
                        Integer.parseInt(instructorDao.findByUserId(instructorUserId).GetID()) : null;
            }
            catch (NumberFormatException numformatException) {
                instructorId = null;
            }
            if (instructorId == null) { 
                Connect.rollback();
                 return -1; 
            }

            int sectionId=sectionDao.createSection(Connect, courseId, instructorId, day, capacity, semester, year);
            if (sectionId <= 0) { 
                Connect.rollback(); 
                return -1; 
            }
            Connect.commit();
            return sectionId;
        } 
        catch (Exception exception) {
            if (Connect!=null){ 
                try {
                Connect.rollback(); 
            }
            catch (Exception ignored) {}
        }
            exception.printStackTrace();
            return -1;
        }
         finally {
            if (Connect != null) 
            try {
             Connect.setAutoCommit(true); 
             Connect.close(); 
            } 
            catch (Exception ignored) {}
        }
    }

    @Override
    public boolean Set_Maintenance(boolean on) throws SQLException {
        settingsDao.insert_into_settings("maintenance.on",Boolean.toString(on));
        settingsDao.updateMaintenance(on);
        return true;
    }
    public boolean createInstructor(String username, String rawPassword, String name, String email, String department) {
    UserDao userDao=new UserDao();           
    InstructorDAO instructorDao=new InstructorDAO();

    String newUserId=java.util.UUID.randomUUID().toString();
    String passHash=PasswordUtil.hash(rawPassword);

    User user=new User();
    user.SetID(newUserId);
    user.SetUsername(username);
    user.SetHashPass(passHash);
    user.SetRole(Role.INSTRUCTOR); 

    try {
        userDao.createUser(user);
    } 
    catch (SQLException exception) {
        exception.printStackTrace();
        return false;
    }

    edu.univ.erp.domain.Instructor ins=new edu.univ.erp.domain.Instructor();
    ins.SetUserID(newUserId);
    ins.SetEmail(email);
    ins.Setdepartment(department);
    ins.SetName(name);

    try {
        boolean ok = instructorDao.insertInstructor(ins);
        if (ok){ 
            return true;
        }
        try { 
            userDao.deleteById(newUserId); 
        } 
        catch (Exception ignore) {}
        return false;
    } 
    catch (SQLException exceptionb) {
        exceptionb.printStackTrace();
        try {
             userDao.deleteById(newUserId); 
        } 
        catch (Exception ignore) {}
        return false;
    }
}   
}
