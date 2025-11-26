package edu.univ.erp.data.Dao;
import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Role;
import edu.univ.erp.domain.User;
import java.sql.*;


public class UserDao {

public void setStringg(PreparedStatement p,String s,int i)throws SQLException{
    p.setString(i, s);
}
public void executeUpdate(PreparedStatement p)throws SQLException{
    p.executeUpdate();
}

    public void createUser(User u) throws SQLException {
        String sql = "insert into users_auth (user_id, username, role, password_hash) VALUES (?, ?, ?, ?)";
        try (Connection connect = DBConnection.getAuthConnection();                                             //i have thrown SQL exception everywhere to catch errors
             PreparedStatement prepStatement = connect.prepareStatement(sql)) {
                setStringg(prepStatement, u.GetID(),1);
                setStringg(prepStatement, u.GetUsername(),2);
                setStringg(prepStatement, u.GetRole().name().toLowerCase(),3);
                setStringg(prepStatement, u.GetHashPass(),4);
                executeUpdate(prepStatement);
        }
    }


public void UPDATE_PASS(String ID, String New_Hash_recieved) throws SQLException {
        String sql = "update users_auth SET password_hash = ? WHERE user_id = ?";
        try (Connection connect = DBConnection.getAuthConnection();
             PreparedStatement prepStatement = connect.prepareStatement(sql)) {
                setStringg(prepStatement, New_Hash_recieved,1);
                setStringg(prepStatement, ID,2);
                executeUpdate(prepStatement);
                
        }
    }


   public User Find_From_Username(String username) throws SQLException {
    String sql = "select User_id, Username, Role, Password_Hash, status From users_auth where Username = ?";

    try (Connection connect = DBConnection.getAuthConnection();
         PreparedStatement prepStatement = connect.prepareStatement(sql)) {
            setStringg(prepStatement, username,1);
        try (ResultSet resultSet = prepStatement.executeQuery()) {
            if (resultSet.next()) {
                User u = new User();
                u.SetID(resultSet.getString("user_id"));
                u.SetUsername(resultSet.getString("username"));
                u.SetHashPass(resultSet.getString("password_hash"));
                u.SetRole(Role.valueOf(resultSet.getString("role").toUpperCase()));
                u.SetStatus(resultSet.getString("status")); // NEW
                return u;
            }
        }
    }
    return null;
}


  public User Find_From_ID(String userId) throws SQLException {
    String sql = "select user_id, username, role, password_hash, status from Users_auth WHERE user_id = ?";

    try (Connection connect = DBConnection.getAuthConnection();
        PreparedStatement prepStatement = connect.prepareStatement(sql)) {
        setStringg(prepStatement, userId,1);
        try (ResultSet resultSet = prepStatement.executeQuery()) {
            if (resultSet.next()) {
                User u = new User();
                u.SetID(resultSet.getString("user_id"));
                u.SetUsername(resultSet.getString("username"));
                u.SetHashPass(resultSet.getString("password_hash"));
                u.SetRole(Role.valueOf(resultSet.getString("role").toUpperCase()));
                u.SetStatus(resultSet.getString("status")); // NEW
                return u;
            }
        }
    }
    return null;
}
}
