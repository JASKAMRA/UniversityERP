package edu.univ.erp.ui.auth;
import edu.univ.erp.data.DBConnection;
import edu.univ.erp.auth.PasswordUtil;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChangePasswordDialog extends JDialog {
    private final JTextField tfUsername=new JTextField(20);
    private final JPasswordField pfOld=new JPasswordField(20);
    private final JPasswordField pfNew=new JPasswordField(20);
    private final JPasswordField pfConfirm= new JPasswordField(20);
    private final JButton btnChange=new JButton("Change Password");
    private final JButton btnCancel=new JButton("Cancel");

    public ChangePasswordDialog(Window owner) {
        super(owner, "Change Password", ModalityType.APPLICATION_MODAL);
        init();
        pack();
    }

    private void init() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(12,12,12,12));
        GridBagConstraints c=new GridBagConstraints();
        c.insets=new Insets(6,6,6,6);
        c.fill=GridBagConstraints.HORIZONTAL;
        c.anchor=GridBagConstraints.WEST;

        int r = 0;
        c.gridx = 0; c.gridy = r; panel.add(new JLabel("Username:"), c);
        c.gridx = 1; panel.add(tfUsername, c); r++;

        c.gridx = 0; c.gridy = r; panel.add(new JLabel("Existing password:"), c);
        c.gridx = 1; panel.add(pfOld, c); r++;

        c.gridx = 0; c.gridy = r; panel.add(new JLabel("New password:"), c);
        c.gridx = 1; panel.add(pfNew, c); r++;

        c.gridx = 0; c.gridy = r; panel.add(new JLabel("Confirm new password:"), c);
        c.gridx = 1; panel.add(pfConfirm, c); r++;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnChange);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> {
            setVisible(false);
            dispose();
        });

        btnChange.addActionListener(e -> doChangePassword());
        getRootPane().setDefaultButton(btnChange);
    }

        private void setString(PreparedStatement prepStatement, int index, String value) throws SQLException {
        setString(prepStatement ,index, value);
    }

    private void doChangePassword() {
        String username = tfUsername.getText().trim();
        String oldPass = new String(pfOld.getPassword());
        String newPass = new String(pfNew.getPassword());
        String confirmPass = new String(pfConfirm.getPassword());  

        if (oldPass.isEmpty() || username.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.","Validation",JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "New password and confirmation do not match.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (newPass.length()<6) {
            JOptionPane.showMessageDialog(this, "New password must be at least 6 characters.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
                
        try (Connection connect=DBConnection.getAuthConnection()){
            String q = "SELECT password_hash FROM users_auth WHERE username = ? LIMIT 1";
            try (PreparedStatement prepStatement=connect.prepareStatement(q)) {
                setString(prepStatement,1, username);
                try (ResultSet resultSet=prepStatement.executeQuery()) {
                    if (!resultSet.next()){
                        JOptionPane.showMessageDialog(this, "Username not found.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String storedHash=resultSet.getString("password_hash");
                    if (storedHash==null || storedHash.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "No password set on the account. Contact admin.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    boolean ok=PasswordUtil.verify(oldPass, storedHash);
                    if (!ok){
                        JOptionPane.showMessageDialog(this, "Existing password does not match.", "Authentication failed", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
    
                }
            }
 
            String newHash=PasswordUtil.hash(newPass); 
            String u="UPDATE users_auth SET password_hash = ? WHERE username = ?";
            try (PreparedStatement prepStatement = connect.prepareStatement(u)){
                setString(prepStatement,1, newHash);
                setString(prepStatement,2, username);
                int row=prepStatement.executeUpdate();
                if (row==1){
                    JOptionPane.showMessageDialog(this, "Password changed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    setVisible(false);
                    dispose();
                } 
                else{
                    JOptionPane.showMessageDialog(this, "Failed to update password. Contact admin.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

