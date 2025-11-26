package edu.univ.erp.ui.common;
import edu.univ.erp.access.AccessControl;
import edu.univ.erp.domain.Role;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.InstructorServiceImpl;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.service.StudentServiceImpl;
import edu.univ.erp.ui.auth.LoginPanel;
import edu.univ.erp.ui.admin.*;
import edu.univ.erp.ui.instructor.InstructorDashboardPanel;
import edu.univ.erp.ui.student.*;
import edu.univ.erp.ui.util.CurrentSession;
import edu.univ.erp.ui.util.CurrentUser;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;


public class MainFrame extends JFrame {

    private static MainFrame Main_instance;
    private BannerPanel Main_banner;
    private NavigationPanel navigation;
    private JPanel DisplayArea; 

    private final StudentService Student_Service= new StudentServiceImpl();
    private final InstructorService Instructor_Service= new InstructorServiceImpl();
   
    public static final String Card_Instructor_Dashboard = "instructor_dashboard";
    public static final String Card_Admin_Dashboard = "admin_dashboard";
    public static final String Card_Login = "Login_Card";
    public static final String Card_Student_Dashboard = "Student_dashboard";
   
    

    private MainFrame() {
        super("IIITD ERP System🎓"); 
        initUI();
    }

    public static MainFrame getInstance() {
        if (Main_instance==null){
            Main_instance=new MainFrame();
        }
        return Main_instance;
    }

    private void initUI() {
  
        Main_banner=new BannerPanel();
        navigation=new NavigationPanel(this);
        DisplayArea=new JPanel(new CardLayout());
        DisplayArea.setBackground(new Color(240, 240, 240));

        DisplayArea.add(new LoginPanel(this), Card_Login);
        DisplayArea.add(new StudentDashboardPanel(Student_Service), Card_Student_Dashboard);
    
        DisplayArea.add(new InstructorDashboardPanel(Instructor_Service, null), Card_Instructor_Dashboard); 
        DisplayArea.add(new AdminDashboardPanel(), Card_Admin_Dashboard);

        ;

   
        setLayout(new BorderLayout());          
        add(DisplayArea, BorderLayout.CENTER);   
        add(Main_banner, BorderLayout.NORTH);
        add(navigation, BorderLayout.WEST);
        setSize(1200, 700); 
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        Show_any_Card(Card_Login);
    }

    
    public void Show_any_Card(String Card_name) {                            
        CardLayout Card1=(CardLayout) DisplayArea.getLayout();
        Card1.show(DisplayArea,Card_name);
        
        Component current_card=null;
        for (Component c:DisplayArea.getComponents()){
            if (c.isVisible()==true){
                current_card=c;
                break;
            }
        }
        if (current_card!=null) {
            try {
                Method method_Load=current_card.getClass().getMethod("loadData");
                method_Load.invoke(current_card);
            } catch (NoSuchMethodException ignored) {
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void show_to_user(CurrentUser user) {
        
        boolean maintenance=false;

        maintenance=AccessControl.isMaintenance();
        CurrentSession.get().SetMant(maintenance);


        Main_banner.SetMantanence(maintenance);

        navigation.LoadMenu_forRole(user.GetRole());
        Role userRole=user.GetRole();

        if (userRole==Role.INSTRUCTOR) {
            restartDashboard_Instructor(user);
        }
        else if (userRole==Role.STUDENT) {
            ensureStudentCardsExist(user.GetUserID());
            //first we check that if students cards exist or not
            //then we add the dashboard of the student 
            StudentDashboardPanel dashboard=getPanel(StudentDashboardPanel.class, Card_Student_Dashboard);
            if (dashboard!=null){
                dashboard.loadData(user.GetProf());
            }
            Show_any_Card(Card_Student_Dashboard);
        } else if(userRole==Role.ADMIN){
            ensureAdminCardsExist(user.GetUserID());
            //first we check that if admin exist or not
            //then we add the dashboard of the student 
            AdminDashboardPanel Dashboard=getPanel(AdminDashboardPanel.class,Card_Admin_Dashboard);
            if (Dashboard!=null){
                Dashboard.LoadData();
            }
            Show_any_Card(Card_Admin_Dashboard);
        } else {
            Show_any_Card(Card_Login);
        }
    }

    //for ensuring studentscards
    private void ensureStudentCardsExist(String userId) {
        Remove_Panel(TranscriptPanel.class);
        Remove_Panel(GradesPanel.class);
        Remove_Panel(MyRegistrationsPanel.class);
        Remove_Panel(CourseCatalogPanel.class);
        Remove_Panel(TimetablePanel.class);
        if (getPanel(MyRegistrationsPanel.class, "Student_registration")== null) {
            DisplayArea.add(new MyRegistrationsPanel(Student_Service, userId), "Student_registration");
        }
         if (getPanel(CourseCatalogPanel.class, "STUDENT_CATALOG")== null) {
            DisplayArea.add(new CourseCatalogPanel(Student_Service, userId), "STUDENT_CATALOG");
        }
        if (getPanel(TimetablePanel.class, "STUDENT_TIMETABLE")== null) {
            DisplayArea.add(new TimetablePanel(Student_Service, userId), "STUDENT_TIMETABLE");
        }
        
        if (getPanel(TranscriptPanel.class, "STUDENT_TRANSCRIPT")== null) {
            DisplayArea.add(new TranscriptPanel(Student_Service, userId), "STUDENT_TRANSCRIPT");
        }
         if (getPanel(GradesPanel.class, "STUDENT_GRADES")== null) {
            DisplayArea.add(new GradesPanel(Student_Service, userId), "STUDENT_GRADES");
  
}
    }

// for ensuring admin cards
    private void ensureAdminCardsExist(String userId){
        if (getPanel(CourseManagementPanel.class, "Admin_Management")== null) {
            DisplayArea.add(new CourseManagementPanel(), "Admin_Management");
        }
        if (getPanel(SectionManagementPanel.class, "Admin_Section_mng")== null) {
            DisplayArea.add(new SectionManagementPanel(), "Admin_Section_mng");
        }
        if (getPanel(AssignInstructorPanel.class, "Admin_instructor")== null) {
            DisplayArea.add(new AssignInstructorPanel(), "Admin_instructor");
        }
        if (getPanel(BackupRestorePanel.class, "Admin_Backup")== null) {
            DisplayArea.add(new BackupRestorePanel(), "Admin_Backup");
        }
        if (getPanel(UserManagementPanel.class, "Admin_User_Management")== null) {
            DisplayArea.add(new UserManagementPanel(), "Admin_User_Management");
        }
    };

    private void restartDashboard_Instructor(CurrentUser user) {
        for (Component c:DisplayArea.getComponents()) {
            if (c instanceof InstructorDashboardPanel){
                DisplayArea.remove(c);
                break; 
            } } 
        InstructorDashboardPanel instructor_dashboard = new InstructorDashboardPanel(Instructor_Service, user.GetUserID());
        String name_to_displayed;
        if (user.GetProf()==null||user.GetProf().getNAAM()== null) {
            name_to_displayed=user.GetUserID();     
        } else {
            name_to_displayed=user.GetProf().getNAAM(); 
        }
        instructor_dashboard.loadData(user.GetUserID(),name_to_displayed);
        DisplayArea.add(instructor_dashboard, Card_Instructor_Dashboard);
        revalidate();
        repaint();
        Show_any_Card(Card_Instructor_Dashboard);
    }
    public void showInstDashboard() {
        CurrentUser current_user = CurrentSession.get().getUsr();
        if (current_user==null||current_user.GetRole()!=Role.INSTRUCTOR) {
            JOptionPane.showMessageDialog(this, "Instructo was not logged in", "ERROR", JOptionPane.ERROR_MESSAGE);
            Show_any_Card(Card_Login);
            return;
        }
        restartDashboard_Instructor(current_user);
    }

    public void ShowInstforSections() {
        InstructorDashboardPanel dash =getPanel(InstructorDashboardPanel.class,Card_Instructor_Dashboard);
        if (dash != null){
        dash.showSections();
        }
    }

    public void enableInstructorActions() {
        CurrentUser current_user = CurrentSession.get().getUsr();
        if (current_user == null||current_user.GetRole()!=Role.INSTRUCTOR) {
            JOptionPane.showMessageDialog(this, "Instructor was not logged in ", "ERROR", JOptionPane.ERROR_MESSAGE);
            return;
        }
        InstructorDashboardPanel dash = getPanel(InstructorDashboardPanel.class, Card_Instructor_Dashboard);
        if (dash==null) {
            showInstDashboard();
            dash =getPanel(InstructorDashboardPanel.class,Card_Instructor_Dashboard);
        }else{
            dash.enableActions();
            Show_any_Card(Card_Instructor_Dashboard);
        }
    }
    private void Remove_Panel(Class<? extends JPanel> clas) {
    for (Component Component : DisplayArea.getComponents()) {
        if (clas.isInstance(Component)) {
            DisplayArea.remove(Component);
            break;
        }
    }
}

    @SuppressWarnings("unchecked")
    private <T extends JPanel> T getPanel(Class<T> class_searched, String cardName) {
        for (Component c :DisplayArea.getComponents()) {
            if (class_searched.isInstance(c)){
                return((T) c);
            }
        }
        return null;
    }
     public void togglemantainenceON(boolean on) {
        if (Main_banner != null){
            Main_banner.SetMantanence(on);
        }
        if (CurrentSession.get() != null) {
            CurrentSession.get().SetMant(on);
        }
    }

    public static void main(String[] args) {            
            MainFrame m = MainFrame.getInstance();
            m.setVisible(true);
        };
}