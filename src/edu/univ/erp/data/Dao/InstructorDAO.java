package edu.univ.erp.data.Dao;
import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Instructor;
import java.sql.*;

public class InstructorDAO {

    private void setString(PreparedStatement prepStatement, int index, String value) throws SQLException {
        prepStatement.setString(index, value);
    }
    private void setINT(PreparedStatement prepStatement, int index, int value)throws SQLException{
        prepStatement.setInt(index, value);
    }

    public boolean insertInstructor(Instructor ins) throws SQLException {
    String sql = "INSERT INTO instructors (user_id, department, name, email) VALUES (?, ?, ?, ?)";
    try (Connection connect = DBConnection.getStudentConnection();
         PreparedStatement prepStatement = connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        prepStatement.setString(1, ins.GetUserID());
        prepStatement.setString(2, ins.GetDepartment());
        prepStatement.setString(3, ins.GetName());
        prepStatement.setString(4, ins.GetEmail());
        int rows = prepStatement.executeUpdate();
        if (rows == 1) {
            try (ResultSet rs = prepStatement.getGeneratedKeys()) {
                if (rs.next()) {
                    ins.SetID(String.valueOf(rs.getInt(1)));
                }
            }
            return true;
        }
        return false;
    } catch (SQLIntegrityConstraintViolationException e) {
        System.err.println("Instructor insert failed - constraint: " + e.getMessage());
        return false;
    }
}

  
    
    public Instructor findByUserId(String userId) {
        String sql="select instructor_id, user_id, department, name, email FROM instructors WHERE user_id = ?";
        try (Connection connect=DBConnection.getAuthConnection();
                PreparedStatement prepStatement=connect.prepareStatement(sql)) {
                setString(prepStatement,1, userId);
            try (ResultSet resultSet=prepStatement.executeQuery()) {
                if (resultSet.next()) {
                    Instructor instructor=Map_TO_instructor(resultSet);
                    instructor.SetID(String.valueOf(resultSet.getInt("instructor_id")));
                    return instructor;
                }}} catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

  

    public boolean deleteById(int instructorId) {
        String sql = "DELETE FROM instructors WHERE instructor_id = ?";                 //delete by using id 
        try (Connection connect=DBConnection.getAuthConnection();
             PreparedStatement prepStatement=connect.prepareStatement(sql)) {
            prepStatement.setInt(1, instructorId);
            int row=prepStatement.executeUpdate();
            return row==1;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

public String Find_InstnameFROMid_user(String instructor_val) throws SQLException {
    if (instructor_val==null){
        return "";
    }
    try (Connection Connect=DBConnection.getStudentConnection()) {
        try {
            int instrucor_ID=Integer.parseInt(instructor_val);
            String q="SELECT name FROM instructors WHERE instructor_id = ?";
            try (PreparedStatement prepStatement = Connect.prepareStatement(q)) {
                setINT(prepStatement, 1, instrucor_ID);
                try (ResultSet ResultSet=prepStatement.executeQuery()) {
                    if (ResultSet.next()) return ResultSet.getString("name");
                }
            }
        } catch (NumberFormatException ex) {
            String q2="SELECT name FROM instructors WHERE user_id = ?";
            try (PreparedStatement prepStatement=Connect.prepareStatement(q2)) {
                setString(prepStatement, 1, instructor_val);
                try (ResultSet resultSet = prepStatement.executeQuery()) {
                    if (resultSet.next()) {
                    return resultSet.getString("name");
                    }}}}
    }return "";
}

    private Instructor Map_TO_instructor(ResultSet resultSet) throws SQLException {         //mapping to instructor and setting their values
        Instructor Instructor1=new Instructor();
        Instructor1.SetUserID(resultSet.getString("user_id"));
        Instructor1.Setdepartment(resultSet.getString("department"));
        Instructor1.SetName(resultSet.getString("name"));
        Instructor1.SetEmail(resultSet.getString("email"));
        return Instructor1;
    }

}
