package edu.univ.erp;

import edu.univ.erp.data.DBConnection;
import java.sql.Connection;

public class MainApplication {
    public static void main(String[] args) {
        try (Connection conn = DBConnection.getAuthConnection()) {
            System.out.println("CONNECTED TO erp_auth DB SUCCESSFULLY!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
