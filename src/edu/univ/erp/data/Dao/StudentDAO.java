package edu.univ.erp.data.Dao;
import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Student;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

public void setStringg(PreparedStatement p,String s,int i)throws SQLException{
    p.setString(i, s);
}
public void setINT(PreparedStatement p,int s,int i)throws SQLException{
             p.setInt(i, s);
        } 

 public int insertStudent(Student s) throws SQLException {
    String sql = "INSERT INTO students (user_id, roll_no, name, mobile, year, program) VALUES (?, ?, ?, ?, ?, ?)";
    int result = 0;

    try (Connection connect = DBConnection.getStudentConnection();
         PreparedStatement prepStatement = connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        setStringg(prepStatement, s.GetID(), 1);      
        setStringg(prepStatement, s.GetRollNum(), 2); 
        setStringg(prepStatement, s.GetName(), 3);   
        setStringg(prepStatement, s.GetEmail(), 4);   
        if (s.GetYear() == null) {
            prepStatement.setNull(5, Types.INTEGER);
        } else {
            prepStatement.setInt(5, s.GetYear()); 
        }
        setStringg(prepStatement, s.GetProgram(), 6); 
        result = prepStatement.executeUpdate();
        if (result == 1) {
            try (ResultSet resultSet = prepStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    int generatedKey = resultSet.getInt(1);
                    s.SetStudentID(String.valueOf(generatedKey));}}}
    } catch (SQLException e) {
        System.err.println("Database error " + e.getMessage());
        throw e; 
    }return result;
}

public Student findByUserId(String userId) {
        String sql="Select student_id, User_id, Roll_no, Name, mobile, year, program FROM students WHERE user_id = ?";
        try (Connection connect=DBConnection.getStudentConnection();
             PreparedStatement prepStatement=connect.prepareStatement(sql)) {
            setStringg(prepStatement, userId, 1); 
            try (ResultSet resultSet=prepStatement.executeQuery()) {
                if (resultSet.next()) {
                    Student s1=Mapping_to_stu1(resultSet);
                    s1.SetStudentID(String.valueOf(resultSet.getInt("student_id")));
                    return s1;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

public boolean updateStudent(Student s) {
        String sql="update students SET user_id = ?, roll_no = ?, Name = ?, Mobile = ?, Year = ?, program = ? WHERE student_id = ?";
        try (Connection connect=DBConnection.getStudentConnection();
             PreparedStatement prepStatement=connect.prepareStatement(sql)) {
            int Student_id=ParseInt(s.GetStudentID());
            setStringg(prepStatement, s.GetID(), 1);            
            setStringg(prepStatement, s.GetRollNum(), 2);
            setStringg(prepStatement, s.GetName(), 3);          
            setStringg(prepStatement, s.GetEmail(), 4); 
            setINT(prepStatement, s.GetYear(), 5);                    
            setStringg(prepStatement, s.GetProgram(), 6);   
            prepStatement.setInt(7, Student_id);

            int rows=prepStatement.executeUpdate();
            return rows== 1;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

private int ParseInt(String s1) {
            if(s1!=null&&!s1.isEmpty()){
            try {
                return (Integer.parseInt(s1));
            }catch (NumberFormatException ex){
                return (0);}
        }
        else{
            return (0);
        }
    }
private Student Mapping_to_stu1(ResultSet rs) throws SQLException {
        Student s1=new Student();
        s1.SetID(rs.getString("user_id"));
        s1.SetProgram(rs.getString("program"));         
        s1.SetRollNum(rs.getString("roll_no"));  
        s1.SetEmail(rs.getString("mobile"));  
        s1.SetName(rs.getString("name"));          
        s1.SetYear(rs.getInt("year"));
        return s1;
    }

    
    


}