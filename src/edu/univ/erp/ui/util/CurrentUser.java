package edu.univ.erp.ui.util;

import edu.univ.erp.domain.Role;

public class CurrentUser {

    private final String userId;      // CHANGED from long → String
    private final Role role;
    private final UserProfile profile;

    // Updated constructor to match LoginPanel and AuthService
    public CurrentUser(String userId, Role role, UserProfile profile) {
        this.userId = userId;
        this.role = role;
        this.profile = profile;
    }

    public String getUserId() {
        return userId;
    }

    public Role getRole() {
        return role;
    }

    public UserProfile getProfile() {
        return profile;
    }
}
