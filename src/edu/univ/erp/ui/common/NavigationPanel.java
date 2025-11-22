package edu.univ.erp.ui.common;

import edu.univ.erp.domain.Role;
import edu.univ.erp.ui.util.CurrentSession;
import edu.univ.erp.ui.util.CurrentUser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class NavigationPanel extends JPanel {
    private MainFrame main;
    private JPanel menu;

    // --- Aesthetic constants ---
    private static final int NAV_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 35;
    private static final int GAP = 8;
    private static final Color NAV_BACKGROUND = new Color(245, 245, 245); // Light gray background
    private static final Color BUTTON_COLOR = new Color(70, 130, 180); // Steel blue
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 12);

    public NavigationPanel(MainFrame main) {
        this.main = main;
        
        // 1. Panel Setup
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(NAV_WIDTH, 0)); // Fixed width, flexible height
        setBackground(NAV_BACKGROUND);
        setBorder(new EmptyBorder(GAP * 2, GAP, GAP * 2, GAP));

        // 2. Menu Container
        menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(NAV_BACKGROUND);

        add(menu, BorderLayout.NORTH);
        // Retaining the flexible space label for layout purposes
        add(new JLabel(" "), BorderLayout.CENTER); 
        
        // Initial load
        loadMenuForRole(null);
    }

    public void loadMenuForRole(Role role) {
        menu.removeAll();

        // Add a role title or separator at the very top
        JLabel roleTitle = new JLabel(getRoleTitle(role));
        roleTitle.setFont(new Font("Arial", Font.BOLD, 14));
        roleTitle.setBorder(new EmptyBorder(0, 0, GAP * 2, 0));
        roleTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        menu.add(roleTitle);
        
        menu.add(Box.createVerticalStrut(5));

        if (role == null) {
            addButton("🔑 Login", MainFrame.CARD_LOGIN, () -> main.showCard(MainFrame.CARD_LOGIN), false);

        } else if (role == Role.STUDENT) {
            addButton("🏠 Dashboard", MainFrame.CARD_STUDENT_DASH, () -> main.showCard(MainFrame.CARD_STUDENT_DASH));
            addButton("📚 Course Catalog", "STUDENT_CATALOG", () -> main.showStudentCatalog());
            addButton("📝 My Registrations", "STUDENT_REGS", () -> main.showCard("STUDENT_REGS"));
            addButton("⏰ Timetable", "STUDENT_TIMETABLE", () -> main.showCard("STUDENT_TIMETABLE"));
            addButton("💯 Grades", "STUDENT_GRADES", () -> main.showCard("STUDENT_GRADES"));
            addButton("📜 Transcript", "STUDENT_TRANSCRIPT", () -> main.showCard("STUDENT_TRANSCRIPT"));
            addLogoutButton();

        } else if (role == Role.INSTRUCTOR) {
            addButton("🏠 Dashboard", MainFrame.CARD_INSTR_DASH, () -> main.showInstructorDashboard());
            addButton("🗓️ My Sections", "INSTR_SECTIONS", () -> main.showInstructorSections());
            addButton("⚙️ Enable Grade Actions", "INSTR_ACTIONS", () -> main.enableInstructorActions());
            addLogoutButton();

        } else if (role == Role.ADMIN) {
            addButton("🏠 Dashboard", MainFrame.CARD_ADMIN_DASH, () -> main.showCard(MainFrame.CARD_ADMIN_DASH));
            addButton("👤 Users", "ADMIN_USERS", () -> main.showCard("ADMIN_USERS"));
            addButton("🎓 Courses", "ADMIN_COURSES", () -> main.showCard("ADMIN_COURSES"));
            addButton("🗓️ Sections", "ADMIN_SECTIONS", () -> main.showCard("ADMIN_SECTIONS"));
            addButton("🧑‍🏫 Assign Instructor", "ADMIN_ASSIGN", () -> main.showCard("ADMIN_ASSIGN"));
            // The maintenance action opens the dashboard card where MaintenancePanel is typically dialog-launched
            addButton("🛠️ Maintenance Toggle", MainFrame.CARD_ADMIN_DASH, () -> main.showCard(MainFrame.CARD_ADMIN_DASH)); 
            addButton("💾 Backup/Restore", "ADMIN_BACKUP", () -> main.showCard("ADMIN_BACKUP"));
            addLogoutButton();
        }
        revalidate();
        repaint();
    }
    
    private String getRoleTitle(Role role) {
        if (role == null) return "Access Required";
        return role.toString() + " Menu";
    }

    /**
     * Helper method to create and style a navigation button.
     */
    private void addButton(String title, String cardName, Runnable action) {
        addButton(title, cardName, action, true);
    }
    
    private void addButton(String title, String cardName, Runnable action, boolean styled) {
        JButton b = new JButton(title);
        
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addActionListener((ActionEvent e) -> action.run());
        
        if (styled) {
            b.setFont(BUTTON_FONT);
            b.setMaximumSize(new Dimension(NAV_WIDTH - GAP * 2, BUTTON_HEIGHT)); // Full width minus padding
            b.setPreferredSize(new Dimension(NAV_WIDTH - GAP * 2, BUTTON_HEIGHT));
            b.setFocusPainted(false);
            b.setBackground(Color.WHITE);
            b.setForeground(Color.BLACK);
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            // Add margin below button
            menu.add(b);
            menu.add(Box.createVerticalStrut(GAP / 2));
        } else {
            menu.add(b);
        }
    }
    
    private void addLogoutButton() {
        // Add glue to push the logout button to the bottom of the NORTH panel
        menu.add(Box.createVerticalGlue());
        
        JButton b = new JButton("🚪 Logout");
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addActionListener((ActionEvent e) -> logout());
        
        b.setFont(BUTTON_FONT);
        b.setMaximumSize(new Dimension(NAV_WIDTH - GAP * 2, BUTTON_HEIGHT));
        b.setPreferredSize(new Dimension(NAV_WIDTH - GAP * 2, BUTTON_HEIGHT));
        b.setFocusPainted(false);
        b.setBackground(new Color(255, 100, 100)); // Reddish background
        b.setForeground(Color.WHITE);
        b.setHorizontalAlignment(SwingConstants.CENTER);

        menu.add(Box.createVerticalStrut(GAP * 2)); // Add a large strut above logout
        menu.add(b);
        menu.add(Box.createVerticalStrut(GAP)); // Add padding at the very end
    }

    private void logout() {
        // Reset session state
        CurrentSession.get().setUser(null);
        CurrentSession.get().setMaintenance(false);
        // Update UI
        main.setBannerMaintenance(false); // Manually update banner
        loadMenuForRole(null);
        main.showCard(MainFrame.CARD_LOGIN);
    }
}