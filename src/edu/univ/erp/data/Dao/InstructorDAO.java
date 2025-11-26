package edu.univ.erp.data.Dao;
import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Instructor;
import java.sql.*;

public class InstructorDAO {

    private void setString(PreparedStatement prepStatement, int index, String value) throws SQLException {
        prepStatement.setString(index, value);
    }
    // public boolean insertInstructor(Instructor ins) {
    //     String sql = "INSERT INTO instructors (user_id, department, name, email) VALUES (?, ?, ?, ?)";
    //     try (Connection connect=DBConnection.getAuthConnection();
    //          PreparedStatement prepStatement = connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
    //         setString(prepStatement,1, ins.GetUserID());
    //         setString(prepStatement,2, ins.GetDepartment());
    //         setString(prepStatement,3, ins.GetName());
    //         setString(prepStatement,4, ins.GetEmail());
    //         int rows=prepStatement.executeUpdate();
    //         if (rows==1){
    //             try (ResultSet resultSet=prepStatement.getGeneratedKeys()) {
    //                 if (resultSet.next()){
    //                     int gen=resultSet.getInt(1);
    //                     ins.SetID(String.valueOf(gen)); 
    //                 }
    //             }
    //             return true;
    //         }
    //         return false;
    //     } catch (SQLIntegrityConstraintViolationException exception) {
    //         System.err.println("Instructor insert failed - constraint: " + exception.getMessage());
    //         return false;
    //     } catch (SQLException except) {
    //         except.printStackTrace();
    //         return false;
    //     }
    // }


    // public Instructor findById(int instructorId) {
    //     String sql = "SELECT instructor_id, user_id, department, name, email FROM instructors WHERE instructor_id = ?";
    //     try (Connection connect=DBConnection.getAuthConnection();
    //          PreparedStatement prepStatement=connect.prepareStatement(sql)) {
    //         prepStatement.setInt(1, instructorId);
    //         try (ResultSet resultSet=prepStatement.executeQuery()) {
    //             if (resultSet.next()) {
    //                 Instructor instructor=mapRowToInstructor(resultSet);
    //                 instructor.SetID(String.valueOf(resultSet.getInt("instructor_id")));
    //                 return instructor;
    //             }
    //         }
    //     } catch (SQLException exception) {
    //         exception.printStackTrace();
    //     }
    //     return null;
    // }

    
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

  
    // public boolean updateInstructor(Instructor instructor) {
    //     String sql = "UPDATE instructors SET user_id = ?, department = ?, name = ?, email = ? WHERE instructor_id = ?";
    //     try (Connection connect=DBConnection.getAuthConnection();
    //          PreparedStatement prepStatement=connect.prepareStatement(sql)) {

    //         setString(prepStatement,1, instructor.GetUserID());
    //         setString(prepStatement,2, instructor.GetDepartment());
    //         setString(prepStatement,3, instructor.GetName());
    //         setString(prepStatement,4, instructor.GetEmail());
    //         int instruct_id = parseIntSafe(instructor.GetID());
    //         prepStatement.setInt(5, instruct_id);
    //         int rows=prepStatement.executeUpdate();
    //         return rows==1;
    //     } catch (SQLException exception) {
    //         exception.printStackTrace();
    //         return false;
    //     }
    // }

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

    // public boolean deleteByUserId(String userId) {
    //     String sql = "DELETE FROM instructors WHERE user_id = ?";
    //     try (Connection connect=DBConnection.getAuthConnection();
    //          PreparedStatement prepStatement=connect.prepareStatement(sql)) {
    //         setString(prepStatement,1, userId);
    //         int row=prepStatement.executeUpdate();
    //         return row>= 0;
    //     } catch (SQLException exception) {
    //         exception.printStackTrace();
    //         return false;
    //     }
    // }

    private Instructor Map_TO_instructor(ResultSet resultSet) throws SQLException {         //mapping to instructor and setting their values
        Instructor Instructor1=new Instructor();
        Instructor1.SetUserID(resultSet.getString("user_id"));
        Instructor1.Setdepartment(resultSet.getString("department"));
        Instructor1.SetName(resultSet.getString("name"));
        Instructor1.SetEmail(resultSet.getString("email"));
        return Instructor1;
    }

    // private int parseIntSafe(String s){
    //     if (s.isEmpty() || s == null){
    //         return 0;
    //     }    
    //     try {
    //         return Integer.parseInt(s);
    //     } catch (NumberFormatException exception) {
    //         return 0;
    //     }
    // }
}
