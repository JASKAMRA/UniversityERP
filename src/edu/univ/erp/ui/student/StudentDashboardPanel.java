package edu.univ.erp.ui.student;

import edu.univ.erp.ui.util.UserProfile;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import java.awt.*;

/**
 * Student dashboard - shows welcome, left menu and a center area that hosts cards
 * (Course Catalog, My Registrations etc).
 *
 * Construct with a StudentService and call loadData(profile) after login.
 */
public class StudentDashboardPanel extends JPanel {

    private final StudentService studentService;
    private UserProfile profile;

    private JLabel lblWelcome;
    private JPanel contentPanel;
    private CardLayout contentLayout;

    private static final String CARD_EMPTY = "EMPTY";
    private static final String CARD_CATALOG = "CATALOG";
    private CourseCatalogPanel catalogPanel;

    public StudentDashboardPanel(StudentService studentService) {
        this.studentService = studentService;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 1. TOP PANEL (Welcome Message)
        JPanel top = new JPanel(new BorderLayout());
        lblWelcome = new JLabel("Student Dashboard");
        lblWelcome.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        top.add(lblWelcome, BorderLayout.WEST);
        add(top, BorderLayout.NORTH);

        // 2. CENTER PANEL (The "Cards" area)
        // This block was likely missing or deleted by accident
        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);

        // Add a default "Empty" screen
        JPanel emptyPanel = new JPanel(new BorderLayout());
        emptyPanel.add(new JLabel("Welcome! Please select an option from the main menu.", SwingConstants.CENTER), BorderLayout.CENTER);
        contentPanel.add(emptyPanel, CARD_EMPTY);

        // Add the content panel to the main layout
        add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * Call after login to populate UI with current user info.
     */
    public void loadData(UserProfile profile) {
        this.profile = profile;
        if (profile != null) {
            lblWelcome.setText("Welcome, " + profile.getName());
            ensureCatalogPanel();
        }
    }

   private void ensureCatalogPanel() {
    if (catalogPanel == null) {
        // get current user id from CurrentSession (set in LoginPanel)
        edu.univ.erp.ui.util.CurrentUser cu = edu.univ.erp.ui.util.CurrentSession.get().getUser();
        if (cu == null) {
            // can't create catalog without a user
            return;
        }
        String userId = cu.getUserId(); // CurrentUser has userId
        catalogPanel = new CourseCatalogPanel(studentService, userId);
        contentPanel.add(catalogPanel, CARD_CATALOG);
    }
}
    // public API for MainFrame to ask dashboard to show the catalog


    public void showCatalog() {
        if (profile == null) {
            JOptionPane.showMessageDialog(this, "Profile not loaded yet.");
            return;
        }
        ensureCatalogPanel();
        contentLayout.show(contentPanel, CARD_CATALOG);
    }
}
