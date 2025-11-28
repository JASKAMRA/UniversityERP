package edu.univ.erp.ui.admin;
import edu.univ.erp.service.AdminService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class CreateStudentDialog extends JDialog {
    private final AdminService adminService;
    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private String createdUserId;
    private JTextField tfRoll;
    private JTextField tfFullName;
    private JTextField tfEmail;
    private JTextField tfYear;
    private JTextField tfProgram;
    private boolean succeeded = false;
 


    public CreateStudentDialog(Window owner, AdminService adminService) {
        super(owner, "🧑‍🎓 Create Student User", ModalityType.APPLICATION_MODAL);
        this.adminService = adminService;
        init();
        JPanel content=(JPanel)getContentPane();
        content.setBorder(new EmptyBorder(15, 15, 15, 15));
        pack();
        setResizable(false);
    }

    private void init() {
        setLayout(new BorderLayout(10, 10));

        JLabel title=new JLabel("Enter Authentication and Profile Details");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        JPanel mainForm=new JPanel(new GridBagLayout());
        mainForm.setBackground(Color.WHITE);
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.fill=GridBagConstraints.HORIZONTAL;
        gbc.weightx=1.0;
        gbc.insets=new Insets(10 / 2, 0, 10 / 2, 0);

        JPanel authPanel=createTitledPanel("🔑 Login Details");
        
        tfUsername=new JTextField(15);
        pfPassword=new JPasswordField(15);
        
        fillFormPanel(authPanel, new String[]{"Username:", "Password:"}, new JComponent[]{tfUsername, pfPassword});
        
        gbc.gridx = 0; 
        gbc.gridy = 0;
        mainForm.add(authPanel, gbc);

        JPanel profilePanel = createTitledPanel("📄 Student Profile");
        
        tfFullName=new JTextField(15);
        tfEmail=new JTextField(15);
        tfRoll=new JTextField(15);
        tfYear=new JTextField(15);
        tfProgram=new JTextField(15);
        
        fillFormPanel(profilePanel, new String[]{
            "Full Name:", "Email:", "Roll No.:", "Year (int):", "Program:"
        }, new JComponent[]{
            tfFullName, tfEmail, tfRoll, tfYear, tfProgram
        });

        gbc.gridy = 1;
        mainForm.add(profilePanel, gbc);

        add(mainForm, BorderLayout.CENTER);

        JPanel bottom=new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnCancel=new JButton("Cancel");
        JButton btnCreate=new JButton("Create Student");

        btnCreate.setBackground(new Color(70, 130, 180));
        btnCreate.setForeground(Color.WHITE);

        bottom.add(btnCancel); 
        bottom.add(btnCreate);
        add(bottom, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> { succeeded = false; setVisible(false); dispose(); });
        btnCreate.addActionListener(e -> doCreate());
    }

    private JPanel createTitledPanel(String title) {
        JPanel panel=new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),title, TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("Arial", Font.BOLD, 12), 
            new Color(70, 130, 180)
        ));
        return panel;
    }
    
    private void fillFormPanel(JPanel panel, String[] labels, JComponent[] fields) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        for (int row = 0; row < labels.length; row++) {           
            gc.gridx = 0; 
            gc.gridy = row; 
            gc.weightx = 0;
            panel.add(new JLabel(labels[row]), gc);         // dekhliyo
            gc.gridx = 1; 
            gc.weightx = 1.0;
            panel.add(fields[row], gc);
        }
    }

    private void doCreate() {
        String username=tfUsername.getText().trim();
        String roll=tfRoll.getText().trim(); 
        String fullName=tfFullName.getText().trim();
        String email=tfEmail.getText().trim();
        String password=new String(pfPassword.getPassword()); 
        Integer year = null;
        try {
            if (!tfYear.getText().trim().isEmpty()) {
                year=Integer.parseInt(tfYear.getText().trim());
            }
        } 
        catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this, "Year must be a whole number (e.g., 2024).", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String program=tfProgram.getText().trim();

        if (password.isEmpty() || username.isEmpty()|| fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username, password, and full name are required fields.", "Missing Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String userId=adminService.CreateStuUser(username, password, fullName, email, roll, year, program);
            
            if (userId==null) {
                JOptionPane.showMessageDialog(this, "Failed to create student. The username **" + username + "** may already exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            else {
                 succeeded = true;
                 createdUserId = userId;
                 JOptionPane.showMessageDialog(this, "Student user **" + username + "** created successfully (User ID: " + userId + ").", "Success", JOptionPane.INFORMATION_MESSAGE);
                 setVisible(false);
                 dispose();
            }
        } 
        catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSucceeded(){
         return succeeded; 
    }
    public String getCreatedUserId(){ 
        return createdUserId; 
    }
}