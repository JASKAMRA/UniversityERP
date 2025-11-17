package edu.univ.erp.auth;

import edu.univ.erp.data.Dao.UserDao;
import edu.univ.erp.data.Dao.StudentDAO;
import edu.univ.erp.data.Dao.InstructorDAO;
import edu.univ.erp.data.Dao.AdminDao;
import edu.univ.erp.domain.User;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Admin;

import java.util.Optional;

/**
 * Backend service for authentication & profile loading.
 * UI should NOT call DAOs directly; call this service.
 */
public class AuthServiceBackend {

    private final UserDao userDao = new UserDao();
    private final StudentDAO studentDao = new StudentDAO();
    private final InstructorDAO instructorDao = new InstructorDAO();
    private final AdminDao adminDao = new AdminDao();

    public static class LoginResult {
        public final boolean success;
        public final String message;
        public final User user;        // domain User (null on failure)
        public final Object profile;   // Student or Instructor or Admin (nullable)

        public LoginResult(boolean ok, String msg, User user, Object profile) {
            this.success = ok; this.message = msg; this.user = user; this.profile = profile;
        }

        public static LoginResult ok(User user, Object profile) { return new LoginResult(true, "OK", user, profile); }
        public static LoginResult fail(String msg) { return new LoginResult(false, msg, null, null); }
    }

    /**
     * Authenticate username/password and load profile (Student/Instructor/Admin) if available.
     * Returns LoginResult (success + domain objects).
     */
    public LoginResult login(String username, String plainPassword) {
        try {
            User u = userDao.findByUsername(username);
            if (u == null) return LoginResult.fail("Invalid username or password");

            // status check
            String status = u.GetStatus();
            if (status != null && !"ACTIVE".equalsIgnoreCase(status)) {
                return LoginResult.fail("Account status: " + status);
            }

            // verify password
            boolean ok = PasswordUtil.verify(plainPassword, u.GetHashPass());
            if (!ok) return LoginResult.fail("Invalid username or password");

            // load richer profile if present
            Object profile = null;
            try {
                if (u.GetRole() != null) {
                    switch (u.GetRole()) {
                        case STUDENT:
                            Optional<Student> st = studentDao.findByUserId(u.GetID());
                            if (st.isPresent()) profile = st.get();
                            break;
                        case INSTRUCTOR:
                            Optional<Instructor> ins = instructorDao.findByUserId(u.GetID());
                            if (ins.isPresent()) profile = ins.get();
                            break;
                        case ADMIN:
                            // load admin profile from ERP DB (admins table)
                            Admin admin = adminDao.findByUserId(u.GetID());
                            if (admin != null) profile = admin;
                            break;
                        default:
                            profile = null;
                    }
                }
            } catch (Exception ignored) {}

            return LoginResult.ok(u, profile);
        } catch (Exception ex) {
            ex.printStackTrace();
            return LoginResult.fail("Authentication error: " + ex.getMessage());
        }
    }
}
