package edu.univ.erp.data.Dao;
import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Setting;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsDao {
    public void setStringg(PreparedStatement p,String s,int i)throws SQLException{
             p.setString(i, s);
        }  
    public void executeUpdate(PreparedStatement p)throws SQLException{
             p.executeUpdate();
        }
    public void setINT(PreparedStatement p,int s,int i)throws SQLException{
             p.setInt(i, s);
        } 

    public void insert_into_settings(String key, String value) throws SQLException {
        String sql = "insert INTO settings(`key`, `value`) values (?, ?) On Duplicate key update `value` = Values(`value`)";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement prepStatement = conn.prepareStatement(sql)) {
            setStringg(prepStatement, key, 1);
            setStringg(prepStatement, value, 2);
            executeUpdate(prepStatement);
        }
    }  

    public Setting FindKey(String key) throws SQLException {
        String sql="Select `key`, `value` from settings WHERE `key` = ?";
        try (Connection conn=DBConnection.getStudentConnection();
            PreparedStatement prepStatement=conn.prepareStatement(sql)) {
            setStringg(prepStatement, key, 1);
            try (ResultSet resultSet=prepStatement.executeQuery()) {
                if (resultSet.next()) {
                    return new Setting(resultSet.getString("key"), resultSet.getString("value"));
                }
            }
        }
        return null;
    }
    public int updateMaintenance(boolean on) throws SQLException {
    String sql = "update settings SET maintenance_on = ?, `value` = ? WHERE `key` = 'maintenance.on'";
    try (Connection Connect=DBConnection.getStudentConnection();
        PreparedStatement prepStatement=Connect.prepareStatement(sql)){
        prepStatement.setInt(1, on ? 1 : 0);
        setStringg(prepStatement,Boolean.toString(on),2);
        return prepStatement.executeUpdate();
    }
}

    public boolean GetBooleanvalue(String key, boolean defaultValue)throws SQLException {
        Setting s=FindKey(key);
        if (s==null){
            return defaultValue;
        }
        return(Boolean.parseBoolean(s.getValue()));
    }
}
