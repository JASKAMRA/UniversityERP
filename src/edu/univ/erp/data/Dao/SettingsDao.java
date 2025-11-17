package edu.univ.erp.data.Dao;

import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Setting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Key/value settings in erp_student DB.
 * table: settings(key VARCHAR PRIMARY KEY, value VARCHAR, updated_on DATETIME OPTIONAL)
 */
public class SettingsDao {

    public Setting findByKey(String key) throws SQLException {
        String sql = "SELECT `key`, `value` FROM settings WHERE `key` = ?";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Setting(rs.getString("key"), rs.getString("value"));
                }
            }
        }
        return null;
    }

    public void upsert(String key, String value) throws SQLException {
        String sql = "INSERT INTO settings(`key`, `value`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `value` = VALUES(`value`)";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) throws SQLException {
        Setting s = findByKey(key);
        if (s == null) return defaultValue;
        return Boolean.parseBoolean(s.getValue());
    }
}
