package edu.univ.erp.ui.auth;
import edu.univ.erp.ui.common.MainFrame;
import edu.univ.erp.ui.common.MessageDialog;
import edu.univ.erp.auth.AuthServiceBackend;
import edu.univ.erp.auth.AuthServiceBackend.LoginResult;
import edu.univ.erp.domain.User;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Admin;
import edu.univ.erp.domain.Role;
import edu.univ.erp.ui.util.CurrentSession;
import edu.univ.erp.ui.util.CurrentUser;
import edu.univ.erp.access.*;
import edu.univ.erp.ui.util.UserProfile;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


public class Login_Panel extends JPanel {
    private MainFrame main;
    private JPasswordField Password_txt=new JPasswordField(20);
    private JTextField Username_txt=new JTextField(20);
    private JButton Login_btn=new JButton("Login");


    public Login_Panel(MainFrame main) {
        this.main=main;
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        JPanel Wrapper=new JPanel(new BorderLayout());
        Wrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(30,30,30,30)
        ));
        Wrapper.setBackground(Color.WHITE);
    
        JPanel formPanel=new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints constraint=new GridBagConstraints();
        constraint.insets=new Insets(15/2, 15, 15 / 2, 15);
        constraint.anchor=GridBagConstraints.WEST;
        constraint.fill=GridBagConstraints.HORIZONTAL;

        JLabel title=new JLabel("ERP PORTAL ");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(new Color(30, 80, 130));
        title.setBorder(new EmptyBorder(0, 0, 30, 0));
        constraint.gridx=0;
        constraint.gridy=0; 
        constraint.anchor=GridBagConstraints.CENTER;
        constraint.gridwidth=2;
        formPanel.add(title, constraint);

        constraint.gridx=0; 
        constraint.gridy=1; 
        constraint.gridwidth=1; 
        constraint.weightx=0; 
        constraint.anchor=GridBagConstraints.WEST;
        JLabel userLabel=new JLabel("Username:");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(userLabel, constraint);
        
        constraint.gridx=1; 
        constraint.gridy=1; 
        constraint.weightx=1.0;
        Username_txt.setPreferredSize(new Dimension(280, 35));
        formPanel.add(Username_txt, constraint);


        constraint.gridx= 0;
        constraint.gridy= 2; 
        constraint.weightx= 0;
        JLabel passLabel= new JLabel("Password:");
        passLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(passLabel, constraint);
        
        constraint.gridx=1; 
        constraint.gridy=2;
        constraint.weightx=1.0;
        Password_txt.setPreferredSize(new Dimension(280, 35));
        formPanel.add(Password_txt, constraint);

        Login_btn.setFont(Login_btn.getFont().deriveFont(Font.BOLD, 16f));
        Login_btn.setBackground(new Color(30, 80, 130));
        Login_btn.setForeground(Color.WHITE);
        Login_btn.setFocusPainted(false);
        Login_btn.setPreferredSize(new Dimension(new Dimension(280, 35).width, 45)); // Larger button
        
        constraint.gridx= 1; constraint.gridy= 3; constraint.gridwidth= 1; constraint.anchor= GridBagConstraints.EAST; 
        constraint.fill= GridBagConstraints.NONE;
        constraint.insets= new Insets(30,30,0,30); 
        formPanel.add(Login_btn, constraint);

        JButton btnChangePass=new JButton("Change Password");
        btnChangePass.setToolTipText("Click to change your password (provide username and existing password)");
        formPanel.add(btnChangePass);
        
  
        Wrapper.add(formPanel, BorderLayout.CENTER);
        add(Wrapper, new GridBagConstraints());
        Login_btn.addActionListener(e -> AfterLoginButton());
        btnChangePass.addActionListener(e -> {
        ChangePasswordDialog dialog = new ChangePasswordDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
});
    }

    private void AfterLoginButton() {
        String u = Username_txt.getText().trim();
        String p = new String(Password_txt.getPassword());
        if (u.isEmpty()){
            MessageDialog.showError(this, "Enter username and password");
            return;
        }
        if(p.isEmpty()){
            MessageDialog.showError(this, "Enter username and password");
            return;
        }
        try {
            AuthServiceBackend backend=new AuthServiceBackend();
            LoginResult LoginResult=backend.login(u, p);
            if (LoginResult==null) {
                MessageDialog.showError(this, "Authentication error ");
                return;
            }
            if (!LoginResult.success) {
                MessageDialog.showError(this, LoginResult.message);
                return;
            }

            boolean maintenance= AccessControl.isMaintenance();
            UserProfile Profile_on_UI;
            Object Profile_recieved= LoginResult.profile;
            if(Profile_recieved instanceof Student){
                Student Stu=(Student) Profile_recieved;
                Profile_on_UI=new UserProfile(Stu.GetName(),Stu.GetEmail());
            }                                                                      
            else if(LoginResult.user!=null){
                User user =LoginResult.user;
                Profile_on_UI=new UserProfile(user.GetUsername(),user.GetEmail());
            }
            else if(Profile_recieved instanceof Admin){
                Admin Stu=(Admin) Profile_recieved;
                Profile_on_UI=new UserProfile(Stu.GetName(),Stu.GetEmail());
            }
            else if(Profile_recieved instanceof Instructor){
                Instructor Stu=(Instructor) Profile_recieved;
                Profile_on_UI=new UserProfile(Stu.GetName(),Stu.GetEmail());
            }
            else{
                Profile_on_UI=new UserProfile(u,"");
            }

    String UserId; 
    Role Role;
            if (LoginResult.user== null) {
                UserId= null;
                Role= null;
            } 
            else {
                UserId= LoginResult.user.GetID();
                Role= LoginResult.user.GetRole();
            }
            CurrentUser current_user=new CurrentUser(UserId, Role, Profile_on_UI);
            CurrentSession.get().SetMant(maintenance);
            CurrentSession.get().setUsr(current_user);
            main.show_to_user(current_user);
        
        } 
        catch (Exception exception) {                  
            exception.printStackTrace();
            MessageDialog.showError(this,"Login error"+ exception.getMessage());
        }}
}