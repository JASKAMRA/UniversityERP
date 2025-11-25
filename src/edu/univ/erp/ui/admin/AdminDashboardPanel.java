package edu.univ.erp.ui.admin;

import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.AdminServiceImpl;
import edu.univ.erp.ui.common.MainFrame;
import edu.univ.erp.ui.util.CurrentSession; 

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminDashboardPanel extends JPanel {
    private final AdminService adminService = new AdminServiceImpl();
    private JButton CreateStudent_button;private JButton CreateCourse_Button;private JButton Maintenance_button;private JLabel LabelStatus;    
   
    public AdminDashboardPanel() {
        INIT();LoadData(); 
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setPreferredSize(new Dimension(250, 40));
        button.setMinimumSize(new Dimension(250, 40));
        button.setFocusPainted(false); 
        button.setBackground(new Color(230, 240, 255)); 
        button.setForeground(Color.BLACK);
    
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 180, 255), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void INIT() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
//
        JLabel title = new JLabel("Administrator Dashboard🎓");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(new EmptyBorder(0, 0, 20, 0)); 
        title.setHorizontalAlignment(SwingConstants.LEFT);
        add(title, BorderLayout.NORTH);
        JPanel Panel_centre = new JPanel();
        Panel_centre.setLayout(new GridBagLayout());
        Panel_centre.setBackground(Color.WHITE);

        GridBagConstraints Constraint = new GridBagConstraints();
        Constraint.insets = new Insets(15 / 2, 15, 15 / 2, 15);
        Constraint.fill = GridBagConstraints.HORIZONTAL;
        Constraint.gridx = 0;
        Constraint.weightx = 1.0;

        CreateStudent_button = new JButton("Create Student user");
        Maintenance_button = new JButton("Toggle Maintanence");
        CreateCourse_Button = new JButton("Create Section and course"); 
        styleButton(CreateStudent_button);
        styleButton(CreateCourse_Button);
        styleButton(Maintenance_button);
        Constraint.gridy = 0;Panel_centre.add(CreateStudent_button, Constraint);
        Constraint.gridy++;Panel_centre.add(CreateCourse_Button, Constraint);
        Constraint.gridy++;Panel_centre.add(Maintenance_button, Constraint);
     
        JPanel Panel_consisting_all = new JPanel(new FlowLayout(FlowLayout.CENTER));
        Panel_consisting_all.add(Panel_centre);
        Panel_consisting_all.setBackground(Color.WHITE);
        add(Panel_consisting_all, BorderLayout.CENTER);
        LabelStatus = new JLabel("Status: ready");
        LabelStatus.setFont(LabelStatus.getFont().deriveFont(Font.ITALIC, 12f));
        LabelStatus.setBorder(new EmptyBorder(20, 0, 0, 0));
        add(LabelStatus, BorderLayout.SOUTH);  
        CreateStudent_button.addActionListener(e -> Create_Student_DIALOG());
        CreateCourse_Button.addActionListener(e -> Create_Course_Section_DALOG());
        Maintenance_button.addActionListener(e -> MaintenancePanel());
    }

    
    private void Create_Course_Section_DALOG() {
     
        Window owner = SwingUtilities.getWindowAncestor(this);
        CreateCourseSectionDialog dialogue = new CreateCourseSectionDialog(owner, adminService);
        dialogue.setLocationRelativeTo(this);
        dialogue.setVisible(true);
        if (dialogue.isSucceeded()) {
            LabelStatus.setText("Created section id: " + dialogue.getCreatedSectionId());
        }
    }

    private void Create_Student_DIALOG() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        CreateStudentDialog dlg = new CreateStudentDialog(owner, adminService);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        if (dlg.isSucceeded()) {
            LabelStatus.setText("Created student user: " + dlg.getCreatedUserId());
        }
    }


    private void MaintenancePanel() {
   
        Window owner=SwingUtilities.getWindowAncestor(this);
        MaintenancePanel Maintenance_Panel=new MaintenancePanel(adminService);
        JDialog dialogue=new JDialog(owner, "Maintenance", Dialog.ModalityType.APPLICATION_MODAL);
        dialogue.getContentPane().add(Maintenance_Panel);
        dialogue.pack();
        dialogue.setLocationRelativeTo(this);
        dialogue.setVisible(true);
        try {
            boolean On=adminService.isMaintenanceOn();
            CurrentSession.get().SetMantanence(On);
            if (MainFrame.getInstance()!=null) {
                MainFrame.getInstance().togglemantainenceON(On);
            }
            if(On){
                LabelStatus.setText("Maintenance now ON");
            }else{
                LabelStatus.setText("Maintenance now OFF");
            }
        }catch(Exception ex){
            LabelStatus.setText("Maintenance: (Unknown)");
        }
    }

    public void LoadData() {
 
        try{
            boolean On=adminService.isMaintenanceOn();
            if(On){
                LabelStatus.setText("Maintenance now ON");
            }else{
                LabelStatus.setText("Maintenance now OFF");
            }

        }catch(Exception ex) {
            LabelStatus.setText("Maintenance: [ERROR]");
        }
    }
}