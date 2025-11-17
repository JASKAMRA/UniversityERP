package edu.univ.erp.ui.common;

import edu.univ.erp.ui.auth.LoginPanel;
import edu.univ.erp.ui.student.*;
import edu.univ.erp.ui.instructor.*;
import edu.univ.erp.ui.admin.*;
import edu.univ.erp.ui.util.CurrentSession;
import edu.univ.erp.ui.util.CurrentUser;
import edu.univ.erp.domain.Role;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    private static MainFrame instance;
    private BannerPanel banner;
    private NavigationPanel nav;
    private JPanel contentArea; // CardLayout

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
contentArea.add(new StudentDashboardPanel(), "STUDENT_DASH");
contentArea.add(new CourseCatalogPanel(), "STUDENT_CATALOG");
contentArea.add(new MyRegistrationsPanel(), "STUDENT_REGS");
contentArea.add(new TimetablePanel(), "STUDENT_TIMETABLE");
contentArea.add(new GradesPanel(), "STUDENT_GRADES");
contentArea.add(new TranscriptPanel(), "STUDENT_TRANSCRIPT");

// Instructor panels
contentArea.add(new InstructorDashboardPanel(), "INSTR_DASH");
contentArea.add(new MySectionsPanel(), "INSTR_SECTIONS");
contentArea.add(new GradebookPanel(), "INSTR_GRADEBOOK");
contentArea.add(new ClassStatsPanel(), "INSTR_STATS");

// Admin panels
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

    public void showForUser(CurrentUser user) {
        // update banner maintenance state
        banner.setMaintenance(CurrentSession.get().isMaintenance());

        // configure nav for role
        nav.loadMenuForRole(user.getRole());

        // load role specific dashboard and show
        if (user.getRole() == Role.STUDENT) {
            StudentDashboardPanel p = getPanel(StudentDashboardPanel.class, CARD_STUDENT_DASH);
            p.loadData(user.getProfile());
            showCard(CARD_STUDENT_DASH);
        } else if (user.getRole() == Role.INSTRUCTOR) {
            InstructorDashboardPanel p = getPanel(InstructorDashboardPanel.class, CARD_INSTR_DASH);
            p.loadData(user.getProfile());
            showCard(CARD_INSTR_DASH);
        } else if (user.getRole() == Role.ADMIN) {
            AdminDashboardPanel p = getPanel(AdminDashboardPanel.class, CARD_ADMIN_DASH);
            p.loadData(null);
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
        // fallback: create new (should not happen)
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            MainFrame m = MainFrame.getInstance();
            m.setVisible(true);
        });
    }
}
