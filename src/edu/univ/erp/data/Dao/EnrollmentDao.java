package edu.univ.erp.data.Dao;
import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnrollmentDao {
    private void setString(PreparedStatement prepStatement, int index, String value) throws SQLException {
        prepStatement.setString(index, value);
    }
    private void setINT(PreparedStatement prepStatement, int index, int value)throws SQLException{
        prepStatement.setInt(index, value);
    }
    private void executeUpdate(PreparedStatement p)throws SQLException{
             p.executeUpdate();
        }

    // returns enrollment record for a particular student_id
    public List<Enrollment> findByStudent(String studentId) throws SQLException {
        String sql = "SELECT enrollment_id, student_id, section_id, status FROM enrollments WHERE student_id = ?";
        List<Enrollment> out=new ArrayList<>();
        try (Connection connect=DBConnection.getStudentConnection();
             PreparedStatement prepStatement=connect.prepareStatement(sql)) {
            setString(prepStatement,1, studentId);
            try (ResultSet resultSet=prepStatement.executeQuery()) {
                while (resultSet.next()){
                    Enrollment enroll=new Enrollment();
                    enroll.SetEnrollmentID(resultSet.getInt("enrollment_id"));
                    enroll.SetStudentID(resultSet.getString("student_id"));
                    enroll.SetSectionID(resultSet.getInt("section_id"));
                    String statusStr = resultSet.getString("status");
                    if (statusStr!=null) {
                        enroll.SetStatus(Status.valueOf(statusStr.toUpperCase()));
                    }
                    out.add(enroll);
                }}}return(out);
    }
    
//Deletes an enrollment record
public void deleteEnrollment(int id) throws SQLException {
        String sql = "DELETE FROM enrollments WHERE enrollment_id = ?";
        try (Connection connect=DBConnection.getStudentConnection();
            PreparedStatement prepStatement=connect.prepareStatement(sql)) {
            prepStatement.setInt(1, id);
            executeUpdate(prepStatement);
    }
}

public List<Integer> FindEnrollUsingSec(int sectionId) throws SQLException {
    String sql="select enrollment_id FROM enrollments WHERE section_id = ?";
    List<Integer>out=new ArrayList<>();
    try (Connection Connect=DBConnection.getStudentConnection();
        PreparedStatement PrepStatement=Connect.prepareStatement(sql)) {
        setINT(PrepStatement, 1, sectionId);
        try (ResultSet ResultSet = PrepStatement.executeQuery()) {
            while (ResultSet.next()){
                out.add(ResultSet.getInt("enrollment_id"));
        }
        }
    }
    return out;
}

public int num_activeEnrol(Connection Connect, int sectionId) throws SQLException {
    String sql="SELECT COUNT(*) FROM enrollments WHERE section_id = ? AND status IN ('ENROLLED','Confirmed','enrolled','confirmed')";
    try (PreparedStatement Prepstatement=Connect.prepareStatement(sql)) {
        Prepstatement.setInt(1, sectionId);
        try (ResultSet resultSet=Prepstatement.executeQuery()) {
            if (resultSet.next()){
                return resultSet.getInt(1);
            }
            return 0;
        }
    }
}


public boolean hasduplicateEnroll(Connection Connect, String Stu_ID, int SecID) throws SQLException {
    String sql ="SELECT COUNT(*) FROM enrollments e " +"JOIN sections s ON e.section_id = s.section_id " +"WHERE e.student_id = ? AND (e.section_id = ? OR s.course_id = (SELECT course_id FROM sections WHERE section_id = ?))";
    try (PreparedStatement prepStatement = Connect.prepareStatement(sql)) {
        setString(prepStatement, 1, Stu_ID);
        setINT(prepStatement, 2, SecID);
        setINT(prepStatement, 3, SecID);
        try (ResultSet resultSet = prepStatement.executeQuery()) {
            if (resultSet.next()) 
                return resultSet.getInt(1) > 0;
            return false;
        }
    }
}


public int insertingEnroll(Connection Connect, String StuID, int SecID, String status) throws SQLException {
    String sql="INSERT INTO enrollments (student_id, section_id, status) VALUES (?, ?, ?)";
    try (PreparedStatement prepStatement = Connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        setString(prepStatement, 1, StuID);
        setINT(prepStatement, 2, SecID);
        setString(prepStatement, 3, status);
        return prepStatement.executeUpdate();
    }
}


public List<Map<String,Object>>FindEnrollUsingStu(int sectionId) throws SQLException {
    String sql="SELECT e.enrollment_id, st.student_id, st.roll_no, st.name, e.status " +"FROM enrollments e JOIN students st ON e.student_id = st.student_id WHERE e.section_id = ? ORDER BY st.roll_no";
    List<Map<String,Object>> out=new ArrayList<>();
    try (Connection Connect=DBConnection.getStudentConnection();
         PreparedStatement PrepStatement=Connect.prepareStatement(sql)) {
        setINT(PrepStatement, 1, sectionId);
        try (ResultSet ResultSet=PrepStatement.executeQuery()) {
            while (ResultSet.next()) {
                Map<String,Object> Map=new HashMap<>();
                Map.put("enrollment_id", ResultSet.getInt("enrollment_id"));Map.put("student_id", ResultSet.getInt("student_id"));
                Map.put("roll_no", ResultSet.getString("roll_no"));Map.put("name", ResultSet.getString("name"));
                Map.put("status", ResultSet.getString("status"));
                out.add(Map);
            }}}
    return out;
}

public boolean isEnrollmentInSection(int enrollmentId, int sectionId) throws SQLException {
    String sql="SELECT 1 FROM enrollments WHERE enrollment_id = ? AND section_id = ? LIMIT 1";
    try (Connection Connect=DBConnection.getStudentConnection();
         PreparedStatement PrepStatement = Connect.prepareStatement(sql)) {
        setINT(PrepStatement, 1, enrollmentId);
        setINT(PrepStatement, 2, sectionId);
        try (ResultSet rs=PrepStatement.executeQuery()) {
            return rs.next();}}}

 

    
    

}
