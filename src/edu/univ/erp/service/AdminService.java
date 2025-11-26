package edu.univ.erp.service;

import java.sql.SQLException;

public interface AdminService {
    boolean IS_Maintenance_on() throws SQLException;
    String CreateStuUser(String user, String pass, String Name, String Email, String Roll_no, Integer Year, String Pro) throws SQLException;
    int CreateCandS(String Id, String Title, Integer Credit, String DepID,int capacity, String day, String Sem, int year,String InstID) throws SQLException;
    boolean Set_Maintenance(boolean on) throws SQLException;  
}
