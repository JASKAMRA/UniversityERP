package edu.univ.erp.auth;
import edu.univ.erp.data.Dao.UserDao;
import edu.univ.erp.data.Dao.StudentDAO;
import edu.univ.erp.data.Dao.InstructorDAO;
import edu.univ.erp.data.Dao.AdminDao;
import edu.univ.erp.domain.User;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Admin;

public class AuthServiceBackend {
    private final UserDao userDao = new UserDao();
    private final StudentDAO Stu_DAO = new StudentDAO();
    private final InstructorDAO I_dao = new InstructorDAO();
    private final AdminDao a_Dao = new AdminDao();

    public static class LoginResult {
        public final boolean success;
        public final String message;
        public final User user;
        public final Object profile;

        public LoginResult(boolean login_res,String msg,User user,Object prof) {
            this.success = login_res;
            this.message = msg;
            this.user = user;
            this.profile = prof;
        }
    }

public LoginResult login(String username, String plainPassword) {

        try {
            User user_ = userDao.Find_From_Username(username);              
            if (user_==null){
                return(new LoginResult(false,"Invalid Pass or username entered",null,null));}               
                
            String status = user_.GetStatus();
            if (!"ACTIVE".equalsIgnoreCase(status)&&status!=null) {            
                return(new LoginResult(false,"Account Not active!",null,null));
            }                                                       
            boolean login_res = PasswordUtil.verify(plainPassword, user_.GetHashPass());            
            if (!login_res)
                return(new LoginResult(false,"Invalid username or password!",null,null));            
            Object profile=null;
            try {if (user_.GetRole()!=null) {
                 switch (user_.GetRole()) {
                     case STUDENT:{
                            Student st=Stu_DAO.findByUserId(user_.GetID());
                            if (st!=null)
                                profile=st;
                            break;
                        }
                        case INSTRUCTOR:{
                            Instructor ins=I_dao.findByUserId(user_.GetID());
                            if (ins!=null)
                                profile=ins;
                            break;
                        }
                        case ADMIN:{
                            Admin admin=a_Dao.FindFromUserID(user_.GetID());
                            if (admin != null){
                                profile = admin;
                            }
                            break;
                        }
                        default:
                            profile = null;
                    }
                }
            } catch (Exception ignored) {}
            return new LoginResult(true,"OK",user_,profile);
        } catch (Exception ex) {
            ex.printStackTrace();
            return(new LoginResult(false,"Authentication error: " + ex.getMessage(),null,null));              
        }
    }
}
