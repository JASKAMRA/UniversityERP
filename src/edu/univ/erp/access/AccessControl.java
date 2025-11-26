package edu.univ.erp.access;

import edu.univ.erp.data.Dao.SettingsDao;
import edu.univ.erp.domain.Role;

public class AccessControl {
    private static final SettingsDao settingsDao = new SettingsDao();

    public static boolean isMaintenance(){
        try {
<<<<<<< HEAD
            return settingsDao.GetBooleanvalue("maintenance.on", false);
        } catch (Exception ex) {
            ex.printStackTrace();
=======
            return settingsDao.getBoolean("maintenance.on", false);
        } 
        catch (Exception except) {
            except.printStackTrace();
>>>>>>> e0ae67f994dd6e7c12018fdfd53df2b834a35722
            return false;
        }
    }

    public static boolean isActionAllowed(Role role, boolean writeOperation) {
        if (!writeOperation) {
            return true;
        }    
        if (!isMaintenance()){ 
            return true;
        }    
        if (role==Role.ADMIN){
             return true;
        }
        return false;
    }
}
