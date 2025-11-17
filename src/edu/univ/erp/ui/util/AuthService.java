package edu.univ.erp.ui.util;

import edu.univ.erp.data.Dao.UserDao;
import edu.univ.erp.domain.User;
import edu.univ.erp.domain.Role;
import edu.univ.erp.util.PasswordUtil;

public class AuthService {

    public static class AuthResult {
        public boolean success;
        public String message;
        public String userId;
        public Role role;
        public UserProfile profile;

        public AuthResult(boolean s, String m) {
            success = s;
            message = m;
        }
    }

    private static final UserDao userDao = new UserDao();

    public static AuthResult authenticate(String username, String password) {
        try {
            // 1) Get user from DB
            User u = userDao.findByUsername(username);
            if (u == null) {
                return new AuthResult(false, "Invalid username or password");
            }

            // 2) Check status
            if (!"ACTIVE".equalsIgnoreCase(u.GetStatus())) {
                return new AuthResult(false, "Account status: " + u.GetStatus());
            }

            // 3) Verify password using bcrypt
            if (!PasswordUtil.verify(password, u.GetHashPass())) {
                return new AuthResult(false, "Invalid username or password");
            }

            // 4) Build successful response
            AuthResult res = new AuthResult(true, "OK");
            res.userId = u.GetID();
            res.role = u.GetRole();
            res.profile = new UserProfile(u.GetUsername(), u.GetEmail());
            return res;

        } catch (Exception ex) {
            ex.printStackTrace();
            return new AuthResult(false, "Error: " + ex.getMessage());
        }
    }
}
