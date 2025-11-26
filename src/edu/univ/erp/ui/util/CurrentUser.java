package edu.univ.erp.ui.util;

import edu.univ.erp.domain.Role;

public class CurrentUser {
    private final String userID;   
    private final UserProfile prof;   
    private final Role ROLE;
    

    public CurrentUser(String userId, Role role, UserProfile profile) {
        this.userID = userId;
        this.ROLE = role;
        this.prof = profile;
    }

    public String GetUserID() {
        return userID;
    }
    public UserProfile GetProf() {
        return prof;
    }
    public Role GetRole() {
        return ROLE;
    }

    
}
