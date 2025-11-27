package edu.univ.erp.data.Dao;
import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Grade;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GradeDao {
    public void setString(PreparedStatement prepStatement, int index, String value) throws SQLException {
        prepStatement.setString(index, value);
    }
    private void setINT(PreparedStatement prepStatement, int index, int value)throws SQLException{
        prepStatement.setInt(index, value);
    }

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
public BigDecimal GetAvgForEnroll(int enrollID) throws SQLException {
    String sql="SELECT AVG(score) AS avg_score FROM grades WHERE enrollment_id = ? AND component <> 'FINAL'";
    try (Connection Connect=DBConnection.getStudentConnection();
        PreparedStatement PrepStatement=Connect.prepareStatement(sql)) {
        setINT(PrepStatement, 1, enrollID);
        try (ResultSet ResultSet = PrepStatement.executeQuery()) {
            if (ResultSet.next()) {
                BigDecimal Average=ResultSet.getBigDecimal("avg_score");
                return Average;
            }
        }
    }
    return null;
}


public Integer SearchFinalGrade(int enrollID)throws SQLException {
    String sql="SELECT grade_id FROM grades WHERE enrollment_id = ? AND component = 'FINAL' LIMIT 1";
    try (Connection Connect=DBConnection.getStudentConnection();
         PreparedStatement PrepStatement=Connect.prepareStatement(sql)) {
        setINT(PrepStatement, 1, enrollID);
        try (ResultSet ResultSet=PrepStatement.executeQuery()) {
            if (ResultSet.next()) {
                return ResultSet.getInt("grade_id");
            }
        }
    }
    return null;
}


public int ChangeFinalGrade(int GradeID, BigDecimal SCORE, String Final_Grade) throws SQLException {
    String sql="UPDATE grades SET score = ?, final_grade = ? WHERE grade_id = ?";
    try (Connection Connect=DBConnection.getStudentConnection();
        PreparedStatement PrepStatement=Connect.prepareStatement(sql)) {
        PrepStatement.setBigDecimal(1, SCORE);
        setINT(PrepStatement, 3, GradeID);
        setString(PrepStatement, 2, Final_Grade);
        return PrepStatement.executeUpdate();
    }
}


public int insertingFINAL_GRADE(int EnrollID, BigDecimal SCORE, String FINAL_GRADE) throws SQLException {
    String sql="INSERT INTO grades (enrollment_id, component, score, final_grade) VALUES (?, 'FINAL', ?, ?)";
    try (Connection Connect=DBConnection.getStudentConnection();
         PreparedStatement PrepStatement=Connect.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
        setINT(PrepStatement, 1, EnrollID);
        PrepStatement.setBigDecimal(2, SCORE);
        setString(PrepStatement, 3, FINAL_GRADE);
        return PrepStatement.executeUpdate();
    }
}

public boolean finalExistsForEnrollment(int enrollmentId) throws SQLException {
    String sql="SELECT 1 FROM grades WHERE enrollment_id = ? AND component = 'FINAL' LIMIT 1";
    try (Connection Connect=DBConnection.getStudentConnection();
         PreparedStatement PrepStatement=Connect.prepareStatement(sql)) {
            setINT(PrepStatement, 1, enrollmentId);
        try (ResultSet ResultSet=PrepStatement.executeQuery()) {
            return ResultSet.next();
        }}}

public boolean GradingInserting(int EnrollID, String comp, BigDecimal SCORE) throws SQLException {
    String sql = "INSERT INTO grades (enrollment_id, component, score, final_grade) VALUES (?, ?, ?, NULL)";
    try (Connection Connect=DBConnection.getStudentConnection();
         PreparedStatement PrepStatement=Connect.prepareStatement(sql)) {
        setINT(PrepStatement, 1, EnrollID);
        setString(PrepStatement, 2, comp);
        if (SCORE==null){
            PrepStatement.setBigDecimal(3, SCORE);    
        }else {
            PrepStatement.setNull(3, Types.DECIMAL);
        }int a=PrepStatement.executeUpdate();
        return a==1;
    }
}
//creating an inner class
public static class GradeRow {
    public final String Comp;
    public final String Final_grd;
    public final String CourseID;
    public final int SectionID;
    public final BigDecimal SCORE;
    
    public GradeRow(String component, BigDecimal score, String finalGrade, String courseId, int sectionId) {
        this.Comp = component; 
        this.SCORE = score;this.Final_grd = finalGrade;
        this.CourseID = courseId;this.SectionID = sectionId;
    }
}

public List<GradeRow> getGradesForEnrollment(int enrollmentId) throws SQLException {
    String sql ="SELECT g.component, g.score, g.final_grade, s.course_id, s.section_id " +"FROM grades g " +"JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +"JOIN sections s ON e.section_id = s.section_id " +
        "WHERE e.enrollment_id = ? ORDER BY g.grade_id";
    List<GradeRow> OUT = new ArrayList<>();
    try (Connection Connect = DBConnection.getStudentConnection();
         PreparedStatement PrepStatement = Connect.prepareStatement(sql)) {
          setINT(PrepStatement, 1, enrollmentId);
        try (ResultSet resultSet = PrepStatement.executeQuery()) {
            while (resultSet.next()) {
                OUT.add(new GradeRow(
                    resultSet.getString("component"),
                    resultSet.getBigDecimal("score"),
                    resultSet.getString("final_grade"),
                    resultSet.getString("course_id"),
                    resultSet.getInt("section_id")
                ));}}}
    return OUT;
}}
