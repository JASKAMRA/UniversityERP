package edu.univ.erp.auth;

import edu.univ.erp.domain.User;

public class AuthApi {
    // Placeholder auth API - wire with real DAO in assignment
    public static User authenticate(String username, String password) {
        // Dummy: accept any non-empty
        if (username != null && !username.isEmpty()) {
            User u = new User();
            u.setId(1);
            u.setUsername(username);
            u.setFullname("Demo User");
            u.setRole("student");
            return u;
        }
        return null;
    }
}
