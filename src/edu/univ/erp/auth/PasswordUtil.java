package edu.univ.erp.auth;
import org.mindrot.jbcrypt.BCrypt;
public class PasswordUtil {
    public static String hash(String password_dehash) {
        return(BCrypt.hashpw(password_dehash,BCrypt.gensalt(12)));
    }
    public static boolean verify(String password_dehash,String hashed_pass) {
        return(BCrypt.checkpw(password_dehash,hashed_pass));
    }
}
