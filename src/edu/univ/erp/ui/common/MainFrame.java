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

public class MainFrame extends JFrame {
    private static MainFrame instance;
    private BannerPanel banner;
    private NavigationPanel nav;
    private JPanel contentArea; // CardLayout

    private final StudentService studentService = new StudentServiceImpl();
    private final InstructorService instructorService = new InstructorServiceImpl();
    private final AdminService adminService = new AdminServiceImpl();

    public static final String CARD_LOGIN = "CARD_LOGIN";
    public static final String CARD_STUDENT_DASH = "STUDENT_DASH";
    public static final String CARD_INSTR_DASH = "INSTR_DASH";
    public static final String CARD_ADMIN_DASH = "ADMIN_DASH";

    private MainFrame() {
        super("University ERP");
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

        // static cards
        contentArea.add(new LoginPanel(this), CARD_LOGIN);
        contentArea.add(new StudentDashboardPanel(studentService), CARD_STUDENT_DASH);
        contentArea.add(new AdminDashboardPanel(), CARD_ADMIN_DASH);

        // register admin management cards
        contentArea.add(new CourseManagementPanel(), "ADMIN_COURSES");
        contentArea.add(new SectionManagementPanel(), "ADMIN_SECTIONS");
        contentArea.add(new AssignInstructorPanel(), "ADMIN_ASSIGN");
        contentArea.add(new BackupRestorePanel(), "ADMIN_BACKUP");
        contentArea.add(new UserManagementPanel(), "ADMIN_USERS");

        setLayout(new BorderLayout());
        add(banner, BorderLayout.NORTH);
        add(nav, BorderLayout.WEST);
        add(contentArea, BorderLayout.CENTER);

        setSize(1100, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        showCard(CARD_LOGIN);
    }

    public void showCard(String name) {
        CardLayout cl = (CardLayout) contentArea.getLayout();
        cl.show(contentArea, name);
    }

    public void showForUser(CurrentUser user) {
        banner.setMaintenance(CurrentSession.get().isMaintenance());
        nav.loadMenuForRole(user.getRole());

        if (user.getRole() == Role.STUDENT) {
            StudentDashboardPanel dash = getPanel(StudentDashboardPanel.class, CARD_STUDENT_DASH);
            if (dash != null) dash.loadData(user.getProfile());
            showCard(CARD_STUDENT_DASH);

            if (getPanel(MyRegistrationsPanel.class, "STUDENT_REGS") == null) {
                MyRegistrationsPanel regs = new MyRegistrationsPanel(studentService, user.getUserId());
                contentArea.add(regs, "STUDENT_REGS");
            }
            if (getPanel(TimetablePanel.class, "STUDENT_TIMETABLE") == null) {
                TimetablePanel tt = new TimetablePanel(studentService, user.getUserId());
                contentArea.add(tt, "STUDENT_TIMETABLE");
            }
            if (getPanel(GradesPanel.class, "STUDENT_GRADES") == null) {
                GradesPanel gp = new GradesPanel(studentService, user.getUserId());
                contentArea.add(gp, "STUDENT_GRADES");
            }
            if (getPanel(TranscriptPanel.class, "STUDENT_TRANSCRIPT") == null) {
                TranscriptPanel tp = new TranscriptPanel(studentService, user.getUserId());
                contentArea.add(tp, "STUDENT_TRANSCRIPT");
            }

        } else if (user.getRole() == Role.INSTRUCTOR) {
            for (Component c : contentArea.getComponents()) {
                if (c instanceof InstructorDashboardPanel) contentArea.remove(c);
            }

            InstructorDashboardPanel dash = new InstructorDashboardPanel(instructorService, user.getUserId());
            String displayName = (user.getProfile() != null && user.getProfile().getName() != null)
                    ? user.getProfile().getName() : user.getUserId();
            dash.loadData(user.getUserId(), displayName);
            contentArea.add(dash, CARD_INSTR_DASH);
            revalidate();
            repaint();
            showCard(CARD_INSTR_DASH);

        } else if (user.getRole() == Role.ADMIN) {
            AdminDashboardPanel p = getPanel(AdminDashboardPanel.class, CARD_ADMIN_DASH);
            if (p != null) p.loadData();
            showCard(CARD_ADMIN_DASH);
        } else {
            showCard(CARD_LOGIN);
        }
    }

    public void showStudentCatalog() {
        StudentDashboardPanel p = getPanel(StudentDashboardPanel.class, CARD_STUDENT_DASH);
        if (p == null) { showCard(CARD_STUDENT_DASH); return; }
        try {
            Method m = p.getClass().getMethod("showCatalog");
            m.invoke(p);
        } catch (NoSuchMethodException nsme1) {
            try {
                Method m2 = p.getClass().getMethod("openCatalog");
                m2.invoke(p);
            } catch (Exception ex) {
                try {
                    Method dm = p.getClass().getDeclaredMethod("showCatalog");
                    dm.setAccessible(true);
                    dm.invoke(p);
                } catch (Exception ignored) { ignored.printStackTrace(); }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
        for (Component c : contentArea.getComponents()) {
            if (c instanceof InstructorDashboardPanel) contentArea.remove(c);
        }
        InstructorDashboardPanel dash = new InstructorDashboardPanel(instructorService, cu.getUserId());
        String displayName = (cu.getProfile() != null && cu.getProfile().getName() != null) ? cu.getProfile().getName() : cu.getUserId();
        dash.loadData(cu.getUserId(), displayName);
        contentArea.add(dash, CARD_INSTR_DASH);
        revalidate();
        repaint();
        showCard(CARD_INSTR_DASH);
    }

    public void showInstructorSections() {
        showInstructorDashboard();
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
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            MainFrame m = MainFrame.getInstance();
            m.setVisible(true);
        });
    }
}
