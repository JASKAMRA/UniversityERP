package edu.univ.erp.data.Dao;
import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Grade;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GradeDao {
    public void setString(PreparedStatement prepStatement, int index, String value) throws SQLException {
        prepStatement.setString(index, value);
    }

    // adds a new grade
    // public void addGrade(Grade grade) throws SQLException {
    //     String sql = "INSERT INTO grades (enrollment_id, component, score, final_grade) VALUES (?, ?, ?, ?)";
    //     try (Connection connect=DBConnection.getStudentConnection();
    //          PreparedStatement prepStatement=connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
    //         prepStatement.setInt(1, grade.getEnrollmentId());
    //         setString(prepStatement,2, grade.getComponent());
    //         if (grade.getScore()!=null){
    //             prepStatement.setDouble(3, grade.getScore());
    //         }
    //         else{
    //             prepStatement.setNull(3, Types.DOUBLE);
    //         }
    //         setString(prepStatement,4, grade.getFinalGrade());
    //         prepStatement.executeUpdate();
    //         try (ResultSet keys=prepStatement.getGeneratedKeys()) {
    //             if(keys.next()){
    //                 grade.setGradeId(keys.getInt(1));
    //             }
    //             }
    //     }
    // }

    // returns grade components for a particular enrollment
    public List<Grade> findByEnrollment(int enrollmentId) throws SQLException {
        String sql = "SELECT grade_id, enrollment_id, component, score, final_grade FROM grades WHERE enrollment_id = ?";
        List<Grade> out=new ArrayList<>();
        try (Connection connect=DBConnection.getStudentConnection();
             PreparedStatement prepStatement=connect.prepareStatement(sql)) {
            prepStatement.setInt(1, enrollmentId);
            try (ResultSet resultSet=prepStatement.executeQuery()) {
                while (resultSet.next()){
                    Grade grade=new Grade();
                    grade.setGradeId(resultSet.getInt("grade_id"));
                    grade.setEnrollmentId(resultSet.getInt("enrollment_id"));
                    grade.setComponent(resultSet.getString("component"));
                    double score=resultSet.getDouble("score");
                    if (!resultSet.wasNull()){ 
                        grade.setScore(score);
                    }
                    else {
                        grade.setScore(null);
                    }
                    grade.setFinalGrade(resultSet.getString("final_grade"));
                    out.add(grade);
                }
            }
        }
        return out;
    }
  
    //updates final_grade
    // public void updateFinalGrade(int gradeId, String finalGrade) throws SQLException {
    //     String sql = "UPDATE grades SET final_grade = ? WHERE grade_id = ?";
    //     try (Connection connect = DBConnection.getStudentConnection();
    //          PreparedStatement prepStatement = connect.prepareStatement(sql)) {
    //         setString(prepStatement,1, finalGrade);
    //         prepStatement.setInt(2, gradeId);
    //         prepStatement.executeUpdate();
    //     }
    // }
}
