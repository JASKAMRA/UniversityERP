package edu.univ.erp.ui.common;

import edu.univ.erp.ui.auth.LoginPanel;
import edu.univ.erp.ui.student.*;
import edu.univ.erp.ui.instructor.*;
import edu.univ.erp.ui.admin.*;
import edu.univ.erp.ui.util.CurrentSession;
import edu.univ.erp.ui.util.CurrentUser;
import edu.univ.erp.domain.Role;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.service.StudentServiceImpl;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.service.InstructorServiceImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Method;

public class MainFrame extends JFrame {
    private static MainFrame instance;
    private BannerPanel banner;
    private NavigationPanel nav;
    private JPanel contentArea; // CardLayout

    // application-scoped service instances
    private final StudentService studentService = new StudentServiceImpl();
    private final InstructorService instructorService = new InstructorServiceImpl();

    public static final String CARD_LOGIN = "CARD_LOGIN";
    public static final String CARD_STUDENT_DASH = "STUDENT_DASH";
    public static final String CARD_INSTR_DASH = "INSTR_DASH";
    public static final String CARD_ADMIN_DASH = "ADMIN_DASH";

    private MainFrame() {
        super("University ERP - Skeleton");
        initUI();
    }

    public static MainFrame getInstance() {
        if (instance == null) instance = new MainFrame();
        return instance;
    }

    private void initUI() {
        banner = new BannerPanel();
        nav = new NavigationPanel(this);
        contentArea = new JPanel(new CardLayout());

        // register cards (add these panels)
        contentArea.add(new LoginPanel(this), CARD_LOGIN);

        // Student panels
        // pass studentService into StudentDashboardPanel so it can create the catalog and call service methods
        contentArea.add(new StudentDashboardPanel(studentService), CARD_STUDENT_DASH);

        // DO NOT add standalone MyRegistrations / Timetable / Grades / Transcript panels here.
        // They require userId (available only after login) and will be created lazily in showForUser().

        // NOTE: Instructor & Admin panels are created lazily in showForUser() because they require user context.
        // (This avoids constructing instructor panels with missing userId at startup.)

        // Admin panels (static) - these don't require per-user creation, keep skeletons ready
        contentArea.add(new AdminDashboardPanel(), "ADMIN_DASH");
        contentArea.add(new UserManagementPanel(), "ADMIN_USERS");
        contentArea.add(new CourseManagementPanel(), "ADMIN_COURSES");
        contentArea.add(new SectionManagementPanel(), "ADMIN_SECTIONS");
        contentArea.add(new AssignInstructorPanel(), "ADMIN_ASSIGN");
        contentArea.add(new MaintenancePanel(), "ADMIN_MAINT");
        contentArea.add(new BackupRestorePanel(), "ADMIN_BACKUP");

        setLayout(new BorderLayout());
        add(banner, BorderLayout.NORTH);
        add(nav, BorderLayout.WEST);
        add(contentArea, BorderLayout.CENTER);

        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // initially show login
        showCard(CARD_LOGIN);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    public void showCard(String name) {
        CardLayout cl = (CardLayout) contentArea.getLayout();
        cl.show(contentArea, name);
    }

    /**
     * Called after successful login. This will configure nav and show the correct dashboard.
     * It will also lazily create panels that require user-specific data (like MyRegistrations, Timetable).
     */
    public void showForUser(CurrentUser user) {
        // update banner maintenance state
        banner.setMaintenance(CurrentSession.get().isMaintenance());

        // configure nav for role
        nav.loadMenuForRole(user.getRole());

        // load role specific dashboard and show
        if (user.getRole() == Role.STUDENT) {
            StudentDashboardPanel dash = getPanel(StudentDashboardPanel.class, CARD_STUDENT_DASH);
            if (dash != null) dash.loadData(user.getProfile());
            showCard(CARD_STUDENT_DASH);

            // Lazy-create MyRegistrationsPanel (requires userId)
            MyRegistrationsPanel regs = getPanel(MyRegistrationsPanel.class, "STUDENT_REGS");
            if (regs == null) {
                try {
                    regs = new MyRegistrationsPanel(studentService, user.getUserId());
                    contentArea.add(regs, "STUDENT_REGS");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            // Lazy-create TimetablePanel (requires userId)
            TimetablePanel tt = getPanel(TimetablePanel.class, "STUDENT_TIMETABLE");
            if (tt == null) {
                try {
                    tt = new TimetablePanel(studentService, user.getUserId());
                    contentArea.add(tt, "STUDENT_TIMETABLE");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            // Lazy-create GradesPanel (requires userId)
            GradesPanel gp = getPanel(GradesPanel.class, "STUDENT_GRADES");
            if (gp == null) {
                try {
                    gp = new GradesPanel(studentService, user.getUserId());
                    contentArea.add(gp, "STUDENT_GRADES");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            // Lazy-create TranscriptPanel (requires userId)
            TranscriptPanel tp = getPanel(TranscriptPanel.class, "STUDENT_TRANSCRIPT");
            if (tp == null) {
                try {
                    tp = new TranscriptPanel(studentService, user.getUserId());
                    contentArea.add(tp, "STUDENT_TRANSCRIPT");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        } else if (user.getRole() == Role.INSTRUCTOR) {
            // Lazily create or reuse the Instructor dashboard for the logged-in instructor
            InstructorDashboardPanel dash = getPanel(InstructorDashboardPanel.class, CARD_INSTR_DASH);
            if (dash == null) {
                try {
                    // create dashboard with instructorService and the instructor's auth user_id
                    dash = new InstructorDashboardPanel(instructorService, user.getUserId());
                    contentArea.add(dash, CARD_INSTR_DASH);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                // reload data for this instructor (in case another instructor previously used the same card)
                dash.loadData(user.getUserId());
            }
            showCard(CARD_INSTR_DASH);

        } else if (user.getRole() == Role.ADMIN) {
            AdminDashboardPanel p = getPanel(AdminDashboardPanel.class, CARD_ADMIN_DASH);
            if (p != null) p.loadData(null);
            showCard(CARD_ADMIN_DASH);
        } else {
            showCard(CARD_LOGIN);
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends JPanel> T getPanel(Class<T> cls, String cardName) {
        for (Component c : contentArea.getComponents()) {
            if (cls.isInstance(c)) return (T) c;
        }
        // fallback: not found
        return null;
    }

    /**
     * Helper used by NavigationPanel to open the student catalog inside the student dashboard.
     * This implementation tries several ways to invoke the dashboard's method so it works
     * whether the dashboard exposes showCatalog() publicly or via openCatalog().
     */
    public void showStudentCatalog() {
        StudentDashboardPanel p = getPanel(StudentDashboardPanel.class, CARD_STUDENT_DASH);
        if (p == null) {
            showCard(CARD_STUDENT_DASH);
            return;
        }

        // try direct methods first
        try {
            // try public showCatalog()
            Method m = p.getClass().getMethod("showCatalog");
            m.invoke(p);
        } catch (NoSuchMethodException nsme1) {
            try {
                // try public openCatalog()
                Method m2 = p.getClass().getMethod("openCatalog");
                m2.invoke(p);
            } catch (NoSuchMethodException nsme2) {
                try {
                    // fallback: call private showCatalog() via reflection (set accessible)
                    Method dm = p.getClass().getDeclaredMethod("showCatalog");
                    dm.setAccessible(true);
                    dm.invoke(p);
                } catch (Exception ex) {
                    // give up gracefully and just show the dashboard main card
                    ex.printStackTrace();
                }
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // ensure the student dashboard card is visible
        showCard(CARD_STUDENT_DASH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            MainFrame m = MainFrame.getInstance();
            m.setVisible(true);
        });
    }
}
