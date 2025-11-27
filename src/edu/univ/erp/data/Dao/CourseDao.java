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


    public int createCourse(Connection conn, String courseId, String title, Integer credits, String departmentId) throws SQLException {
    String sql = "insert INTO courses (course_id, title, credits, department_id) VALUES (?, ?, ?, ?)";
    try (PreparedStatement prepStatement = conn.prepareStatement(sql)) {
        setString(prepStatement, 1, courseId);
        setString(prepStatement, 2, title);
        if (credits == null){
            prepStatement.setNull(3, Types.INTEGER);
         }
        else{prepStatement.setInt(3, credits);
        }
        setString(prepStatement, 4, departmentId);
        return prepStatement.executeUpdate();
    }
}
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

}
