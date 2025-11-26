package edu.univ.erp.data.Dao;
import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Admin;
import java.sql.*;

public class AdminDao {
    private void setString(PreparedStatement prepStatement, int index, String value) throws SQLException {
        prepStatement.setString(index, value);
    }

    public boolean insert(Admin admn) throws SQLException {
        String sql="Insert INTO Admins (User_id, NAME, email) Values (?, ?, ?)";                              //Admin profile koh erp_student.admins mein insert krna  
        try (Connection connect=DBConnection.getStudentConnection();
             PreparedStatement prepstatement=connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                setString(prepstatement,1, admn.GetUserId());
                setString(prepstatement,2, admn.GetName());
                setString(prepstatement,3, admn.GetEmail());
                int row=prepstatement.executeUpdate();
                if (row==1) {
                    try (ResultSet resultSet=prepstatement.getGeneratedKeys()) {
                        if (resultSet.next()){
                            admn.SetAdminId(resultSet.getInt(1));
                        }}return (true);
                    }return false;
             }}
    public Admin findByUserId(String userId) throws SQLException {
        String sql="Select admin_id, user_id, name, email from ADMINS where user_id = ?";                    //by using user_id we will find admin profile
        try (Connection connect=DBConnection.getStudentConnection();
             PreparedStatement prepstatement=connect.prepareStatement(sql)){
             setString(prepstatement,1, userId);
            try (ResultSet resultSet=prepstatement.executeQuery()) {
                if (resultSet.next()){
                    Admin admn=new Admin();
                    admn.SetAdminId(resultSet.getInt("admin_id"));
                    admn.SetName(resultSet.getString("name"));
                    admn.SetEmail(resultSet.getString("email"));
                    admn.SetUserId(resultSet.getString("user_id"));
                    return admn;
                }}}return null;
    }
    
     // Delete admin row by user_id    
    // public boolean deleteByUserId(String userId) throws SQLException {
    //     String sql = "DELETE FROM admins WHERE user_id = ?";
    //     try (Connection connect=DBConnection.getStudentConnection();
    //          PreparedStatement prepstatement=connect.prepareStatement(sql)) {
    //         setString(prepstatement,1, userId);
    //         int row=prepstatement.executeUpdate();
    //         return row>= 0;
    //     }
    // }
}
