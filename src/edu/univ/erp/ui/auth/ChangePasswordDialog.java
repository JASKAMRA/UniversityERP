package edu.univ.erp.ui.auth;
import edu.univ.erp.service.AuthService;
import edu.univ.erp.service.AuthServiceImpl;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {
    private final JTextField Username=new JTextField(20);
    private final JPasswordField pass_old=new JPasswordField(20);
    private final JPasswordField pass_new=new JPasswordField(20);
    private final JPasswordField pass_confirm=new JPasswordField(20);
    private final JButton change_button=new JButton("🔑-Change Password");
    private final JButton cancel_button=new JButton("❌-Cancel");

    private final AuthService authService;
    public ChangePasswordDialog(Window Master) {
        this(Master,new AuthServiceImpl());
    }
    public ChangePasswordDialog(Window Master, AuthService authService) {
        super(Master, "Change Password", ModalityType.APPLICATION_MODAL);
        this.authService = authService;
        init();
        pack();
    }
    private void ChangePassword() {
        String username=Username.getText().trim();
        String Old_password=new String(pass_old.getPassword());
        String New_password=new String(pass_new.getPassword());
        String Confirm_password=new String(pass_confirm.getPassword());

        if (Confirm_password.isEmpty() || Old_password.isEmpty()  || username.isEmpty() || New_password.isEmpty() ) {
            JOptionPane.showMessageDialog(this, "Please fill all", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if ( New_password.length()<6 || !New_password.equals(Confirm_password) ){
            if (!New_password.equals(Confirm_password)){
            JOptionPane.showMessageDialog(this, "New Pass and Confirm Pass are not same", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
            }
            JOptionPane.showMessageDialog(this, "New Pass must be 6 letters", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        
        try {
            boolean c=authService.changePassword(username, Old_password, New_password);
            if (!c) {
                JOptionPane.showMessageDialog(this, "Username not found or existing password incorrect.", "Error",JOptionPane.ERROR_MESSAGE);
            }
            else{
                    JOptionPane.showMessageDialog(this, "Password Changed Succesfully", "Success",JOptionPane.INFORMATION_MESSAGE);
                    setVisible(false);
                    dispose();
                }
        } 
        catch(IllegalStateException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } 
        catch (Exception exception2) {
            exception2.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: "+exception2.getMessage(), "Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private void init(){
        JPanel panel=new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints c=new GridBagConstraints();
        c.insets=new Insets(6, 6, 6, 6);
        c.fill=GridBagConstraints.HORIZONTAL; 
        c.anchor=GridBagConstraints.WEST;

        int w = 0; 
        c.gridx = 0; 
        c.gridy = w; 
        panel.add(new JLabel("Username:"), c);
        c.gridx = 1; 
        panel.add(Username, c); 
        w++;
        c.gridx = 0; 
        c.gridy = w; 
        panel.add(new JLabel("Existing password:"),c);
        c.gridx = 1; 
        panel.add(pass_old, c); 
        w++;
        c.gridx = 0; 
        c.gridy = w; 
        panel.add(new JLabel("New password:"),c);
        c.gridx = 1; 
        panel.add(pass_new, c); 
        w++;
        c.gridx = 0; 
        c.gridy = w; 
        panel.add(new JLabel("Confirm new password:"),c);
        c.gridx = 1; 
        panel.add(pass_confirm, c); 
        w++;

        JPanel Btn_Panel=new JPanel(new FlowLayout(FlowLayout.RIGHT));
        Btn_Panel.add(cancel_button);
        Btn_Panel.add(change_button);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(Btn_Panel, BorderLayout.SOUTH);
        cancel_button.addActionListener(e->{setVisible(false);dispose();});
        change_button.addActionListener(e->ChangePassword());
        getRootPane().setDefaultButton(change_button);
    }    
}
