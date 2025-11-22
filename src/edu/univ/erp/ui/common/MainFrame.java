package edu.univ.erp.ui.common;

import edu.univ.erp.domain.Role;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.InstructorServiceImpl;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.service.StudentServiceImpl;
import edu.univ.erp.service.AdminService;
import edu.univ.erp.service.AdminServiceImpl;
import edu.univ.erp.ui.auth.LoginPanel;
import edu.univ.erp.ui.admin.*;
import edu.univ.erp.ui.instructor.InstructorDashboardPanel;
import edu.univ.erp.ui.student.*;
import edu.univ.erp.ui.util.CurrentSession;
import edu.univ.erp.ui.util.CurrentUser;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.Enumeration;

public class MainFrame extends JFrame {
    private static MainFrame instance;
    private BannerPanel banner;
    private NavigationPanel nav;
    private JPanel contentArea; // CardLayout

    // Service Instances (Dependencies)
    private final StudentService studentService = new StudentServiceImpl();
    private final InstructorService instructorService = new InstructorServiceImpl();
    private final AdminService adminService = new AdminServiceImpl();

    public static final String CARD_LOGIN = "CARD_LOGIN";
    public static final String CARD_STUDENT_DASH = "STUDENT_DASH";
    public static final String CARD_INSTR_DASH = "INSTR_DASH";
    public static final String CARD_ADMIN_DASH = "ADMIN_DASH";

    // --- Aesthetic Constants ---
    private static final int DEFAULT_WIDTH = 1200; // Increased width slightly
    private static final int DEFAULT_HEIGHT = 780; // Increased height slightly
    private static final Color FRAME_BACKGROUND = new Color(240, 240, 240); // Light background for separation

    private MainFrame() {
        super("🏛️ University ERP System"); // Improved Title
        initUI();
    }

    public static MainFrame getInstance() {
        if (instance == null) instance = new MainFrame();
        return instance;
    }

    private void initUI() {
        // Initialize Core Components
        banner = new BannerPanel();
        nav = new NavigationPanel(this);
        contentArea = new JPanel(new CardLayout());
        contentArea.setBackground(FRAME_BACKGROUND);

        // === Register Main Cards ===
        contentArea.add(new LoginPanel(this), CARD_LOGIN);
        contentArea.add(new StudentDashboardPanel(studentService), CARD_STUDENT_DASH);
        // Note: Initial InstructorDashboardPanel is a placeholder/template
        contentArea.add(new InstructorDashboardPanel(instructorService, null), CARD_INSTR_DASH); 
        contentArea.add(new AdminDashboardPanel(), CARD_ADMIN_DASH);

        // === Register Admin Management Cards ===
        contentArea.add(new CourseManagementPanel(), "ADMIN_COURSES");
        contentArea.add(new SectionManagementPanel(), "ADMIN_SECTIONS");
        contentArea.add(new AssignInstructorPanel(), "ADMIN_ASSIGN");
        contentArea.add(new BackupRestorePanel(), "ADMIN_BACKUP");
        contentArea.add(new UserManagementPanel(), "ADMIN_USERS");

        // === Frame Layout ===
        setLayout(new BorderLayout());
        add(banner, BorderLayout.NORTH);
        add(nav, BorderLayout.WEST);
        add(contentArea, BorderLayout.CENTER);

        // === Frame Properties ===
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        showCard(CARD_LOGIN);
    }

    public void showCard(String name) {
        CardLayout cl = (CardLayout) contentArea.getLayout();
        cl.show(contentArea, name);
        
        // Attempt to call loadData if the panel supports it (useful for refreshing tables)
        Component currentCard = null;
        for (Component comp : contentArea.getComponents()) {
            if (comp.isVisible()) {
                currentCard = comp;
                break;
            }
        }
        
        if (currentCard != null) {
            try {
                // Use reflection to call loadData on the newly visible panel
                Method m = currentCard.getClass().getMethod("loadData");
                m.invoke(currentCard);
            } catch (NoSuchMethodException ignored) {
                // Method does not exist, which is fine for most panels
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void showForUser(CurrentUser user) {
        // --- 1. Maintenance Check (Always first) ---
        boolean maintenance = false;
        try {
            maintenance = adminService.isMaintenanceOn();
        } catch (Exception ex) {
            ex.printStackTrace();
            maintenance = CurrentSession.get() != null && CurrentSession.get().isMaintenance();
        }
        CurrentSession.get().setMaintenance(maintenance);
        banner.setMaintenance(maintenance);

        // --- 2. Navigation Update ---
        nav.loadMenuForRole(user.getRole());
        
        // --- 3. Route to Dashboard and Setup Role-Specific Panels ---
        Role userRole = user.getRole();

        if (userRole == Role.STUDENT) {
            // Setup dynamic Student cards if they don't exist
            ensureStudentCardsExist(user.getUserId());
            
            StudentDashboardPanel dash = getPanel(StudentDashboardPanel.class, CARD_STUDENT_DASH);
            if (dash != null) dash.loadData(user.getProfile());
            showCard(CARD_STUDENT_DASH);

        } else if (userRole == Role.INSTRUCTOR) {
            // Refresh Instructor Dashboard (since it's user-specific)
            refreshInstructorDashboard(user);

        } else if (userRole == Role.ADMIN) {
            AdminDashboardPanel p = getPanel(AdminDashboardPanel.class, CARD_ADMIN_DASH);
            if (p != null) p.loadData();
            showCard(CARD_ADMIN_DASH);
        } else {
            showCard(CARD_LOGIN);
        }
    }
    
    /**
     * Instantiates necessary student panels if they haven't been added yet.
     */
    private void ensureStudentCardsExist(String userId) {
        if (getPanel(MyRegistrationsPanel.class, "STUDENT_REGS") == null) {
            MyRegistrationsPanel regs = new MyRegistrationsPanel(studentService, userId);
            contentArea.add(regs, "STUDENT_REGS");
        }
        if (getPanel(TimetablePanel.class, "STUDENT_TIMETABLE") == null) {
            TimetablePanel tt = new TimetablePanel(studentService, userId);
            contentArea.add(tt, "STUDENT_TIMETABLE");
        }
        if (getPanel(GradesPanel.class, "STUDENT_GRADES") == null) {
            GradesPanel gp = new GradesPanel(studentService, userId);
            contentArea.add(gp, "STUDENT_GRADES");
        }
        if (getPanel(TranscriptPanel.class, "STUDENT_TRANSCRIPT") == null) {
            TranscriptPanel tp = new TranscriptPanel(studentService, userId);
            contentArea.add(tp, "STUDENT_TRANSCRIPT");
        }
    }
    
    /**
     * Recreates the Instructor Dashboard as it relies on the current user's ID.
     * FIX: Simplified logic to avoid calling .elements() on an int.
     */
    private void refreshInstructorDashboard(CurrentUser user) {
        // Find existing dash panel by component type and remove it
        for (Component c : contentArea.getComponents()) {
            if (c instanceof InstructorDashboardPanel) {
                contentArea.remove(c);
                break; // Assuming only one dash panel of this type exists
            }
        }
        
        InstructorDashboardPanel dash = new InstructorDashboardPanel(instructorService, user.getUserId());
        String displayName = (user.getProfile() != null && user.getProfile().getName() != null)
            ? user.getProfile().getName() : user.getUserId();
            
        dash.loadData(user.getUserId(), displayName);
        
        // Re-add using the standard card name
        contentArea.add(dash, CARD_INSTR_DASH);
        revalidate();
        repaint();
        showCard(CARD_INSTR_DASH);
    }

    public void showStudentCatalog() {
        StudentDashboardPanel p = getPanel(StudentDashboardPanel.class, CARD_STUDENT_DASH);
        if (p == null) { showCard(CARD_STUDENT_DASH); return; }
        
        // Simplified reflection block (Logic Unchanged)
        try {
            Method m = p.getClass().getMethod("showCatalog");
            m.invoke(p);
        } catch (Exception ex) {
            // Fallback attempts
            try {
                Method m2 = p.getClass().getMethod("openCatalog");
                m2.invoke(p);
            } catch (Exception ignored) {
                // Last resort private reflection attempt
                try {
                    Method dm = p.getClass().getDeclaredMethod("showCatalog");
                    dm.setAccessible(true);
                    dm.invoke(p);
                } catch (Exception e) {
                    System.err.println("Failed to open student catalog using reflection.");
                    e.printStackTrace();
                }
            }
        }
        showCard(CARD_STUDENT_DASH);
    }

    public void showInstructorDashboard() {
        CurrentUser cu = CurrentSession.get().getUser();
        if (cu == null || cu.getRole() != Role.INSTRUCTOR) {
            JOptionPane.showMessageDialog(this, "Instructor not logged in.", "Error", JOptionPane.ERROR_MESSAGE);
            showCard(CARD_LOGIN);
            return;
        }
        refreshInstructorDashboard(cu);
    }

    public void showInstructorSections() {
        showInstructorDashboard(); // Ensure dashboard is loaded/refreshed
        InstructorDashboardPanel dash = getPanel(InstructorDashboardPanel.class, CARD_INSTR_DASH);
        if (dash != null) dash.showSections();
    }

    public void enableInstructorActions() {
        CurrentUser cu = CurrentSession.get().getUser();
        if (cu == null || cu.getRole() != Role.INSTRUCTOR) {
            JOptionPane.showMessageDialog(this, "Instructor not logged in.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        InstructorDashboardPanel dash = getPanel(InstructorDashboardPanel.class, CARD_INSTR_DASH);
        if (dash == null) {
            showInstructorDashboard();
            dash = getPanel(InstructorDashboardPanel.class, CARD_INSTR_DASH);
        }
        if (dash != null) {
            dash.enableActions();
            showCard(CARD_INSTR_DASH);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends JPanel> T getPanel(Class<T> cls, String cardName) {
        for (Component c : contentArea.getComponents()) {
            if (cls.isInstance(c)) return (T) c;
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { 
                // Use a modern look and feel if available, otherwise fallback
                String lf = UIManager.getSystemLookAndFeelClassName();
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        lf = info.getClassName();
                        break;
                    }
                }
                UIManager.setLookAndFeel(lf);
            } catch (Exception ignored) {}
            
            MainFrame m = MainFrame.getInstance();
            m.setVisible(true);
        });
    }
    
    /**
     * Sets the maintenance flag on the banner and in the session.
     */
    public void setBannerMaintenance(boolean on) {
        if (banner != null) banner.setMaintenance(on);
        if (CurrentSession.get() != null) {
            CurrentSession.get().setMaintenance(on);
        }
    }
}