package edu.univ.erp.ui.auth;

import edu.univ.erp.service.AuthService;
import edu.univ.erp.service.AuthServiceImpl;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {
    private final JTextField Username = new JTextField(20);
    private final JPasswordField pass_old = new JPasswordField(20);
    private final JPasswordField pass_new = new JPasswordField(20);
    private final JPasswordField pass_confirm = new JPasswordField(20);
    private final JButton change_button = new JButton("🔑-Change Password");
    private final JButton cancel_button = new JButton("❌-Cancel");

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
    private void ChangePass() {
        String username = Username.getText().trim();
        String Old_pass = new String(pass_old.getPassword());
        String New_Pass = new String(pass_new.getPassword());
        String Confirm_Pass = new String(pass_confirm.getPassword());

        if (Old_pass.isEmpty()||Confirm_Pass.isEmpty()||New_Pass.isEmpty()||username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all", "Validation", JOptionPane.WARNING_MESSAGE);
            return;}
        if (New_Pass.length()<6 || !New_Pass.equals(Confirm_Pass)) {
            if (!New_Pass.equals(Confirm_Pass)) {
            JOptionPane.showMessageDialog(this, "New Pass and Confirm Pass are not same", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
            JOptionPane.showMessageDialog(this, "New Pass must be 6 letters", "Validation", JOptionPane.WARNING_MESSAGE);
            return;}
        
        
        try {
            boolean c=authService.changePassword(username, Old_pass, New_Pass);
            if (c) {JOptionPane.showMessageDialog(this, "Password Changed Succesfully", "Success",JOptionPane.INFORMATION_MESSAGE);
                    setVisible(false);
                    dispose();
                }else{JOptionPane.showMessageDialog(this, "Username not found or existing password incorrect.", "Error",JOptionPane.ERROR_MESSAGE);}
        } catch(IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex2) {
            ex2.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: "+ex2.getMessage(), "Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private void init() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints Constraints = new GridBagConstraints();
        Constraints.insets = new Insets(6, 6, 6, 6); Constraints.fill = GridBagConstraints.HORIZONTAL; Constraints.anchor = GridBagConstraints.WEST;

        int w = 0; 
        Constraints.gridx = 0; Constraints.gridy = w; panel.add(new JLabel("Username:"), Constraints);
        Constraints.gridx = 1; panel.add(Username, Constraints); w++;

        Constraints.gridx = 0; Constraints.gridy = w; panel.add(new JLabel("Existing password:"),Constraints);
        Constraints.gridx = 1; panel.add(pass_old, Constraints); w++;
        Constraints.gridx = 0; Constraints.gridy = w; panel.add(new JLabel("New password:"),Constraints);
        Constraints.gridx = 1; panel.add(pass_new, Constraints); w++;
        Constraints.gridx = 0; Constraints.gridy = w; panel.add(new JLabel("Confirm new password:"),Constraints);
        Constraints.gridx = 1; panel.add(pass_confirm, Constraints); w++;

        JPanel Button_Panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        Button_Panel.add(cancel_button);Button_Panel.add(change_button);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(Button_Panel, BorderLayout.SOUTH);
        cancel_button.addActionListener(e->{setVisible(false);dispose();});
        change_button.addActionListener(e->ChangePass());
        getRootPane().setDefaultButton(change_button);
    }

    
}
