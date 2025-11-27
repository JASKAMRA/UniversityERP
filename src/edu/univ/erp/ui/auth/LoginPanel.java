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


public class LoginPanel extends JPanel {
    private MainFrame main;
    private JPasswordField Password_txt = new JPasswordField(20);
    private JTextField Username_txt = new JTextField(20);
    private JButton Login_button = new JButton("Login");

    private static final Color PRIMARY_COLOR = new Color(30, 80, 130); 
  

    public LoginPanel(MainFrame main) {
        this.main = main;
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        JPanel Wrapper=new JPanel(new BorderLayout());
        Wrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(30,30,30,30)
        ));
        Wrapper.setBackground(Color.WHITE);
    
        //creating the form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(15/2, 15, 15 / 2, 15);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;


        //title for the page
        JLabel title = new JLabel("ERP PORTAL ");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(PRIMARY_COLOR);
        title.setBorder(new EmptyBorder(0, 0, 30, 0));
        c.gridx = 0;
        c.gridy = 0; 
        c.anchor = GridBagConstraints.CENTER;
        c.gridwidth = 2;
        formPanel.add(title, c);
        
        //Username Label
        c.gridx = 0; c.gridy = 1; c.gridwidth = 1; c.weightx = 0; c.anchor = GridBagConstraints.WEST;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(userLabel, c);
        
        c.gridx = 1; c.gridy = 1; c.weightx = 1.0;
        Username_txt.setPreferredSize(new Dimension(280, 35));
        formPanel.add(Username_txt, c);


        //Password
        c.gridx= 0; c.gridy= 2; c.weightx= 0;
        JLabel passLabel= new JLabel("Password:");
        passLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(passLabel, c);
        
        c.gridx= 1; c.gridy= 2; c.weightx= 1.0;
        Password_txt.setPreferredSize(new Dimension(280, 35));
        formPanel.add(Password_txt, c);


        //Login
        Login_button.setFont(Login_button.getFont().deriveFont(Font.BOLD, 16f));
        Login_button.setBackground(PRIMARY_COLOR);
        Login_button.setForeground(Color.WHITE);
        Login_button.setFocusPainted(false);
        Login_button.setPreferredSize(new Dimension(new Dimension(280, 35).width, 45)); // Larger button
        
        c.gridx= 1; c.gridy= 3; c.gridwidth= 1; c.anchor= GridBagConstraints.EAST; 
        c.fill= GridBagConstraints.NONE;
        c.insets= new Insets(30,30,0,30); 
        formPanel.add(Login_button, c);

        //Change Password

        JButton btnChangePassword = new JButton("Change Password");
        btnChangePassword.setToolTipText("Click to change your password (provide username and existing password)");
        formPanel.add(btnChangePassword);
        
  
        Wrapper.add(formPanel, BorderLayout.CENTER);
        add(Wrapper, new GridBagConstraints());
        Login_button.addActionListener(e -> After_Login_Button());
        btnChangePassword.addActionListener(e -> {
        ChangePasswordDialog dlg = new ChangePasswordDialog(SwingUtilities.getWindowAncestor(this));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
});
    }

    private void After_Login_Button() {
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
            AuthServiceBackend backend = new AuthServiceBackend();
            LoginResult Login_result = backend.login(u, p);
            if (Login_result == null) {
                MessageDialog.showError(this, "Authentication error ");
                return;
            }if (!Login_result.success) {
                MessageDialog.showError(this, Login_result.message);
                return;
            }

            boolean maintenance= AccessControl.isMaintenance();
            UserProfile Profile_on_UI;
            Object Profile_recieved= Login_result.profile;
            if(Profile_recieved instanceof Student){
                Student Stu=(Student) Profile_recieved;
                Profile_on_UI=new UserProfile(Stu.GetName(),Stu.GetEmail());
            }                                                                      
            else if(Profile_recieved instanceof Admin){
                Admin Stu=(Admin) Profile_recieved;
                Profile_on_UI=new UserProfile(Stu.GetName(),Stu.GetEmail());
            }
            else if(Login_result.user!=null){
                User user =Login_result.user;
                Profile_on_UI=new UserProfile(user.GetUsername(),user.GetEmail());
            }
            else if(Profile_recieved instanceof Instructor){
                Instructor Stu=(Instructor) Profile_recieved;
                Profile_on_UI=new UserProfile(Stu.GetName(),Stu.GetEmail());
            }
            else{
                Profile_on_UI=new UserProfile(u,"");
            }

String UserId; Role Role;
            if (Login_result.user!= null) {
                UserId= Login_result.user.GetID();
                Role= Login_result.user.GetRole();
            } else {
                UserId= null;
                Role= null;
}
            CurrentUser current_user = new CurrentUser(UserId, Role, Profile_on_UI);
            CurrentSession.get().SetMant(maintenance);
            CurrentSession.get().setUsr(current_user);
            main.show_to_user(current_user);
        
        } catch (Exception exception) {                  
            exception.printStackTrace();
            MessageDialog.showError(this,"Login error"+ exception.getMessage());
        }}
}