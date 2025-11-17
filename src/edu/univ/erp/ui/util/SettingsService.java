package edu.univ.erp.ui.util;

public class SettingsService {
    // In real app read from DB; here read the CurrentSession flag if set
    public static boolean isMaintenanceOn() {
        return CurrentSession.get().isMaintenance();
    }
}
