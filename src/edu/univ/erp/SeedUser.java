package edu.univ.erp;

import edu.univ.erp.auth.PasswordUtil;
import edu.univ.erp.data.Dao.UserDao;
import edu.univ.erp.domain.Role;
import edu.univ.erp.domain.User;

public class SeedUser {
    public static void main(String[] args) throws Exception {

        String userId = "u1001";           // your string ID
        String username = "admin";         // login username
        String plainPassword = "admin123"; // password you want to use
        String email = "admin@uni.com";    // optional

        String hashed = PasswordUtil.hash(plainPassword);

        User u = new User();
        u.SetID(userId);
        u.SetUsername(username);
        u.SetHashPass(hashed);
        u.SetRole(Role.ADMIN);  // or STUDENT/INSTRUCTOR
        u.SetEmail(email);
        u.SetStatus("ACTIVE");

        UserDao dao = new UserDao();
        dao.createUser(u);

        System.out.println("User created!");
    }
}

