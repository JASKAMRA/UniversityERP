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
    private JButton CreateStudent_btn;
    private JLabel LabelStatus;    
    private JButton CreateCourse_btn;
    private JButton Maintenance_btn;
   
    public AdminDashboardPanel() {
        INIT();
        LoadData(); 
    }

    private void styleButton(JButton btn) {
        btn.setFont(new Font("Arial", Font.PLAIN, 14));
        btn.setMinimumSize(new Dimension(250, 40));
        btn.setPreferredSize(new Dimension(250, 40));
        btn.setFocusPainted(false); 
        btn.setForeground(Color.BLACK);
        btn.setBackground(new Color(230, 240, 255)); 
    
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(150, 180, 255), 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void INIT() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel title=new JLabel("Administrator Dashboard🎓");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(new EmptyBorder(0, 0, 20, 0)); 
        title.setHorizontalAlignment(SwingConstants.LEFT);
        add(title, BorderLayout.NORTH);
        JPanel Panel_centre=new JPanel();
        Panel_centre.setLayout(new GridBagLayout());
        Panel_centre.setBackground(Color.WHITE);

        GridBagConstraints Constraint = new GridBagConstraints();
        Constraint.insets=new Insets(15 / 2, 15, 15 / 2, 15);
        Constraint.fill=GridBagConstraints.HORIZONTAL;
        Constraint.gridx=0;
        Constraint.weightx=1.0;

        CreateStudent_btn=new JButton("Create Student user");
        Maintenance_btn=new JButton("Toggle Maintanence");
        CreateCourse_btn=new JButton("Create Section and course"); 
        styleButton(CreateStudent_btn);
        styleButton(CreateCourse_btn);
        styleButton(Maintenance_btn);
        Constraint.gridy=0;Panel_centre.add(CreateStudent_btn, Constraint);
        Constraint.gridy++;Panel_centre.add(CreateCourse_btn, Constraint);
        Constraint.gridy++;Panel_centre.add(Maintenance_btn, Constraint);
     
        JPanel Panel_consisting_all=new JPanel(new FlowLayout(FlowLayout.CENTER));
        Panel_consisting_all.add(Panel_centre);
        Panel_consisting_all.setBackground(Color.WHITE);
        add(Panel_consisting_all, BorderLayout.CENTER);
        LabelStatus=new JLabel("Status: ready");
        LabelStatus.setFont(LabelStatus.getFont().deriveFont(Font.ITALIC, 12f));
        LabelStatus.setBorder(new EmptyBorder(20, 0, 0, 0));
        add(LabelStatus, BorderLayout.SOUTH);  
        CreateStudent_btn.addActionListener(e -> Create_Student_DIALOG());
        CreateCourse_btn.addActionListener(e -> Create_Course_Section_DALOG());
        Maintenance_btn.addActionListener(e -> MaintenancePanel());
    }

    private void Create_Student_DIALOG() { // check 
        Window owner = SwingUtilities.getWindowAncestor(this);
        CreateStudentDialog dialog = new CreateStudentDialog(owner, adminService);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        if (!dialog.isSucceeded()) {
            return;
        }
        LabelStatus.setText("Created student user: " + dialog.getCreatedUserId());
    }
    
    private void Create_Course_Section_DALOG() { // check
        Window owner=SwingUtilities.getWindowAncestor(this);
        CreateCourseSectionDialog dialogue=new CreateCourseSectionDialog(owner, adminService);
        dialogue.setLocationRelativeTo(this);
        dialogue.setVisible(true);
        if (!dialogue.isSucceeded()) {
            return;
        }
        LabelStatus.setText("Created section id: " + dialogue.getCreatedSectionId());
    }



    private void MaintenancePanel() {    // check
        Window owner=SwingUtilities.getWindowAncestor(this);
        MaintenancePanel Maintenance_Panel=new MaintenancePanel(adminService);
        JDialog dialogue=new JDialog(owner, "Maintenance", Dialog.ModalityType.APPLICATION_MODAL);
        dialogue.getContentPane().add(Maintenance_Panel);
        dialogue.pack();
        dialogue.setLocationRelativeTo(this);
        dialogue.setVisible(true);
        try {
            boolean On=adminService.is_Maintenance_on();
            CurrentSession.get().SetMant(On);
            MainFrame frame = MainFrame.getInstance();
            if (frame!=null){
                frame.togglemantainenceON(On);
            }
            if(!On){
                LabelStatus.setText("Maintenance now OFF");
            }
            else{
                LabelStatus.setText("Maintenance now ON");
            }
        }
        catch(Exception exception){
            LabelStatus.setText("Maintenance: (Unknown)");
        }
    }

    public void LoadData() {
        try{
            boolean On=adminService.is_Maintenance_on();
            if(!On){
                LabelStatus.setText("Maintenance now OFF");
            }
            else{
                LabelStatus.setText("Maintenance now ON");
            }

        }
        catch(Exception exception) {
            LabelStatus.setText("Maintenance: [ERROR]");
        }
    }
}