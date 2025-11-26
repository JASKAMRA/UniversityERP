package edu.univ.erp.data.Dao;
import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Course;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CourseDao {
    private void setString(PreparedStatement prepStatement, int index, String value) throws SQLException {
        prepStatement.setString(index, value);
    }
    // Creates a new course 
    // public void createCourse(Course c) throws SQLException {
    //     String sql = "INSERT INTO courses (course_id, title, credits, department_id) VALUES (?, ?, ?, ?)";
    //     try (Connection connect=DBConnection.getStudentConnection();
    //          PreparedStatement prepStatement=connect.prepareStatement(sql)) {
    //         setString(prepStatement,1, c.GetCourseID());
    //         prepStatement.setString(2, c.GetTitle());
    //         if (c.GetCredits()!=null){ 
    //             prepStatement.setInt(3, c.GetCredits());
    //         } 
    //         else{ 
    //             prepStatement.setNull(3, Types.INTEGER);
    //         }    
    //         setString(prepStatement,4, c.GetDepartmentID());
    //         prepStatement.executeUpdate();
    //     }
    // }

    //retruns the course object with the help of course_id
    public Course findById(String courseId) throws SQLException {
        String sql = "SELECT course_id, title, credits, department_id FROM courses WHERE course_id = ?";
        try (Connection connect=DBConnection.getStudentConnection();
             PreparedStatement prepStatement=connect.prepareStatement(sql)) {
            setString(prepStatement,1, courseId);
            try (ResultSet resultSet=prepStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Course(resultSet.getString("department_id"),resultSet.getString("title"),resultSet.getString("course_id"),resultSet.getInt("credits"));
                }}}
        return null;}

    
    //finds and return all courses from the courses table
    public List<Course> findAll() throws SQLException {
        String sql = "select course_id, title, credits, department_id FROM courses";
        List<Course> out=new ArrayList<>();
        try (Connection connect=DBConnection.getStudentConnection();
             PreparedStatement prepStatement=connect.prepareStatement(sql);
             ResultSet resultSet=prepStatement.executeQuery()) {
            while (resultSet.next()){
                out.add(new Course(resultSet.getString("department_id"),resultSet.getString("title"),resultSet.getString("course_id"),resultSet.getInt("credits")));
            }}return(out);
    }

    // update the coourse using course_id
    // public void updateCourse(Course c) throws SQLException {
    //     String sql = "UPDATE courses SET title = ?, credits = ?, department_id = ? WHERE course_id = ?";
    //     try (Connection connect=DBConnection.getStudentConnection();
    //          PreparedStatement prepStatement=connect.prepareStatement(sql)) {
    //         setString(prepStatement,1, c.GetTitle());
    //         if (c.GetCredits()!= null){ 
    //             prepStatement.setInt(2, c.GetCredits());
    //         }
    //         else{ 
    //             prepStatement.setNull(2, Types.INTEGER);
    //         }    
    //         setString(prepStatement,3, c.GetDepartmentID());
    //         setString(prepStatement,4, c.GetCourseID());
    //         prepStatement.executeUpdate();
    //     }
    // }


     //deletes a course using course_id
    // public void deleteCourse(String courseId) throws SQLException {
    //     String sql = "DELETE FROM courses WHERE course_id = ?";
    //     try (Connection connect=DBConnection.getStudentConnection();
    //          PreparedStatement prepStatement=connect.prepareStatement(sql)) {
    //         setString(prepStatement,1, courseId);
    //         prepStatement.executeUpdate();
    //     }
    // }
}
