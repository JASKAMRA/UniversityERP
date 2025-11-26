package edu.univ.erp.data.Dao;
import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDao {
    private void setString(PreparedStatement prepStatement, int index, String value) throws SQLException {
        prepStatement.setString(index, value);
    }
    //Add a new enrollment  
    // public void enrollStudent(Enrollment enroll) throws SQLException {
    //     String sql = "INSERT INTO enrollments(student_id, section_id, status) VALUES (?, ?, ?)";
    //     try (Connection connect=DBConnection.getStudentConnection();
    //          PreparedStatement prepStatement=connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
    //         setString(prepStatement,1, enroll.GetStudentID());
    //         prepStatement.setInt(2, enroll.GetSectionID());
    //         setString(prepStatement,3, enroll.GetStatus().name());
    //         prepStatement.executeUpdate();
    //         try (ResultSet key=prepStatement.getGeneratedKeys()){
    //             if (key.next()){
    //                 enroll.SetEnrollmentID(key.getInt(1));
    //             }
    //         }
    //     }
    // }

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
                }
            }
        }
        return out;
    }


    //fetches and return single enrollment with its ID
    // public Enrollment findById(int id) throws SQLException {
    //     String sql = "SELECT enrollment_id, student_id, section_id, status FROM enrollments WHERE enrollment_id = ?";
    //     try (Connection connect=DBConnection.getStudentConnection();
    //          PreparedStatement prepStatement=connect.prepareStatement(sql)) {
    //         prepStatement.setInt(1, id);
    //         try (ResultSet resultSet=prepStatement.executeQuery()) {
    //             if (resultSet.next()) {
    //                 Enrollment enroll=new Enrollment();
    //                 enroll.SetEnrollmentID(resultSet.getInt("enrollment_id"));
    //                 enroll.SetStudentID(resultSet.getString("student_id"));
    //                 enroll.SetSectionID(resultSet.getInt("section_id"));
    //                 String statusStr=resultSet.getString("status");
    //                 if (statusStr!=null) {
    //                     enroll.SetStatus(Status.valueOf(statusStr.toUpperCase()));
    //                 }
    //                 return enroll;
    //             }
    //         }
    //     }
    //     return null;
    // }

    //updates status column for a enrollment_id
    // public void updateStatus(int enrollmentId, String newStatus) throws SQLException {
    //     String sql = "UPDATE enrollments SET status = ? WHERE enrollment_id = ?";
    //     try (Connection connect=DBConnection.getStudentConnection();
    //          PreparedStatement prepStatement=connect.prepareStatement(sql)) {
    //         setString(prepStatement,1, newStatus);
    //         prepStatement.setInt(2, enrollmentId);
    //         prepStatement.executeUpdate();
    //     }
    // }

    //Deletes an enrollment record
    public void deleteEnrollment(int id) throws SQLException {
    String sql = "DELETE FROM enrollments WHERE enrollment_id = ?";
    try (Connection connect=DBConnection.getStudentConnection();
         PreparedStatement prepStatement=connect.prepareStatement(sql)) {
        prepStatement.setInt(1, id);
        prepStatement.executeUpdate();
    }
}

}
