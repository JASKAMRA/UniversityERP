package edu.univ.erp.ui.student;

import edu.univ.erp.ui.util.UserProfile;
import edu.univ.erp.ui.util.CurrentUser;
import edu.univ.erp.ui.util.CurrentSession;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

    // --- Aesthetic constants ---
    private static final int PADDING = 20;
    private static final int HEADER_HEIGHT = 50;
    private static final Font WELCOME_FONT = new Font("Arial", Font.BOLD, 22);
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204); // Deep Blue

    public StudentDashboardPanel(StudentService studentService) {
        this.studentService = studentService;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 1. TOP PANEL (Welcome Message)
        JPanel top = new JPanel(new BorderLayout());
        top.setPreferredSize(new Dimension(this.getWidth(), HEADER_HEIGHT));
        top.setBackground(new Color(235, 245, 255)); // Light background for header
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY)); // Separator line

        lblWelcome = new JLabel("🎓 Student Dashboard");
        lblWelcome.setFont(WELCOME_FONT);
        lblWelcome.setForeground(PRIMARY_COLOR);
        lblWelcome.setBorder(new EmptyBorder(0, PADDING, 0, PADDING));

        top.add(lblWelcome, BorderLayout.WEST);
        add(top, BorderLayout.NORTH);

        // 2. CENTER PANEL (The "Cards" area)
        contentLayout = new CardLayout();
        contentPanel = new JPanel(contentLayout);
        contentPanel.setBackground(Color.WHITE);

        // Add a default "Empty" screen without HTML
        JPanel emptyPanel = new JPanel(new GridBagLayout());
        emptyPanel.setBackground(Color.WHITE);

        // Create two plain JLabels stacked vertically and center-aligned
        JPanel msgPanel = new JPanel();
        msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.Y_AXIS));
        msgPanel.setBackground(Color.WHITE);

        JLabel line1 = new JLabel("Welcome to your Student Dashboard.");
        line1.setFont(new Font("Arial", Font.PLAIN, 16));
        line1.setForeground(Color.GRAY);
        line1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel line2 = new JLabel("Please use the menu on the left to navigate and access your academic records.");
        line2.setFont(new Font("Arial", Font.PLAIN, 14));
        line2.setForeground(Color.GRAY);
        line2.setAlignmentX(Component.CENTER_ALIGNMENT);

        msgPanel.add(line1);
        msgPanel.add(Box.createVerticalStrut(6));
        msgPanel.add(line2);

        emptyPanel.add(msgPanel);

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
            lblWelcome.setText("Welcome, " + profile.getNAAM() + "!");
            ensureCatalogPanel();
            contentLayout.show(contentPanel, CARD_EMPTY); // Show default card on initial load
        }
    }

    private void ensureCatalogPanel() {
        if (catalogPanel == null) {
            // Get current user id from CurrentSession
            CurrentUser cu = CurrentSession.get().getUsr();
            if (cu == null) {
                // Should not happen after successful login
                return;
            }
            String userId = cu.GetUserID();
            catalogPanel = new CourseCatalogPanel(studentService, userId);
            contentPanel.add(catalogPanel, CARD_CATALOG);
        }
    }

    // public API for MainFrame to ask dashboard to show the catalog
    public void showCatalog() {
        if (profile == null) {
            JOptionPane.showMessageDialog(this, "Profile not loaded yet.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ensureCatalogPanel();
        contentLayout.show(contentPanel, CARD_CATALOG);
    }
}
