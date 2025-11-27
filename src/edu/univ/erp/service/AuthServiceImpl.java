package edu.univ.erp.service;

import edu.univ.erp.data.Dao.UserDao;
import edu.univ.erp.domain.User;
import edu.univ.erp.auth.PasswordUtil;

import java.sql.SQLException;

public class AuthServiceImpl implements AuthService {

    private final UserDao userDao;

    public AuthServiceImpl() {
        this.userDao=new UserDao();
    }
    public AuthServiceImpl(UserDao userDao) {
        this.userDao=userDao;
    }

    @Override
    public boolean changePassword(String username, String oldPassword, String newPassword) throws Exception {
        if (username==null||username.trim().isEmpty()) {
            return false;
        }User user;
        try {
            user = userDao.Find_From_Username(username);
        }catch (SQLException ex) {
            throw ex;
        }if(user==null){
            return false;
        }
        String Store_hash=user.GetHashPass();
        if (Store_hash==null||Store_hash.isEmpty()) {
            throw new IllegalStateException("No password set on the account. Contact admin.");
        }
        boolean Result=PasswordUtil.verify(oldPassword, Store_hash);
        if (!Result) {
            return false;
        }
        String newHash=PasswordUtil.hash(newPassword);
        try {
            userDao.UPDATE_PASS(user.GetID(), newHash);
            return (true); 
        } catch (SQLException ex) {
            throw ex;
        }
    }
}
