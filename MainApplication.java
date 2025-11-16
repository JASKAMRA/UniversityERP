package edu.univ.erp; // Yeh main package hai

// Modern Look & Feel ke liye import
import com.formdev.flatlaf.FlatLightLaf;

// UI panels ko import karna (yeh hum agle steps mein banayenge)
// ABHI YEH IMPORTS ERROR DIKHAYENGE, chinta mat karna.
import edu.univ.erp.ui.auth.LoginPanel;
import edu.univ.erp.ui.student.StudentDashboardPanel;
import edu.univ.erp.ui.instructor.InstructorDashboardPanel;
import edu.univ.erp.ui.admin.AdminDashboardPanel;

import javax.swing.*;
import java.awt.*; // CardLayout aur JPanel ke liye

/**
 * University ERP ki Main Application Class.
 * Yeh Look & Feel set karti hai, main window (JFrame) banati hai,
 * aur CardLayout ka use karke Login/Dashboard panels ko switch karti hai.
 * [Project Brief Ref: 1, 5, 231]
 */
public class MainApplication {

    // Inhein static rakha hai taaki poori app se access kar sakein
    private static JPanel mainContainerPanel;
    private static CardLayout cardLayout;

    public static void main(String[] args) {
        
        // Step 1: Modern Look & Feel (FlatLaf) set karna 
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf. Using default.");
        }

        // Step 2: Swing UI ko hamesha Event Dispatch Thread (EDT) par run karna
        SwingUtilities.invokeLater(() -> {
            
            // Main application window (frame) banana
            JFrame mainFrame = new JFrame("University ERP");
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(1000, 700); // Thoda bada size
            mainFrame.setLocationRelativeTo(null); // Center mein start ho

            // Step 3: CardLayout set karna
            // Yeh panels ko switch karne mein help karega (jaise Login se Student)
            cardLayout = new CardLayout();
            mainContainerPanel = new JPanel(cardLayout);

            // --- Step 4: Saare UI Panels banana ---
            // Hum in panels ko abhi banayenge (agle steps mein)
            // Abhi ke liye, hum assume kar rahe hain ki yeh classes maujood hain.

            // 1. Login Panel (yeh ui/auth/ mein banega)
            // LoginPanel loginPanel = new LoginPanel();

            // 2. Student Panel (yeh ui/student/ mein banega)
            // StudentDashboardPanel studentPanel = new StudentDashboardPanel();

            // 3. Instructor Panel (yeh ui/instructor/ mein banega)
            // InstructorDashboardPanel instructorPanel = new InstructorDashboardPanel();

            // 4. Admin Panel (yeh ui/admin/ mein banega)
            // AdminDashboardPanel adminPanel = new AdminDashboardPanel();

            // !!! --- ABHI KE LIYE PLACEHOLDERS --- !!!
            // Jab tak hum asli panel nahi banate, tab tak ke liye:
            JPanel loginPanel = new JPanel();
            loginPanel.add(new JLabel("Login Panel Yahaan Banega (ui/auth)"));
            
            JPanel studentPanel = new JPanel();
            studentPanel.add(new JLabel("Student Dashboard Yahaan Banega (ui/student)"));
            
            JPanel instructorPanel = new JPanel();
            instructorPanel.add(new JLabel("Instructor Dashboard Yahaan Banega (ui/instructor)"));
            
            JPanel adminPanel = new JPanel();
            adminPanel.add(new JLabel("Admin Dashboard Yahaan Banega (ui/admin)"));
            // !!! --- PLACEHOLDERS END --- !!!


            // --- Step 5: Panels ko CardLayout mein add karna ---
            // Har panel ko ek unique naam (String) dena zaroori hai
            mainContainerPanel.add(loginPanel, "LOGIN");
            mainContainerPanel.add(studentPanel, "STUDENT_DASHBOARD");
            mainContainerPanel.add(instructorPanel, "INSTRUCTOR_DASHBOARD");
            mainContainerPanel.add(adminPanel, "ADMIN_DASHBOARD");

            // Step 6: Frame mein main container ko add karna
            mainFrame.add(mainContainerPanel);
            
            // Step 7: By default, "LOGIN" card (panel) dikhana
            cardLayout.show(mainContainerPanel, "LOGIN");

            // Step 8: Window ko dikhana
            mainFrame.setVisible(true);
        });
    }
    
    /**
     * Yeh ek helper method hai jo poori application mein kahin se bhi
     * panel ko switch karne ke kaam aayega.
     * Jaise: login successful hone par switchToPanel("STUDENT_DASHBOARD");
     * @param panelName "LOGIN", "STUDENT_DASHBOARD", etc.
     */
    public static void switchToPanel(String panelName) {
        cardLayout.show(mainContainerPanel, panelName);
    }
}