package edu.univ.erp.ui.common;

import javax.swing.*;

import edu.univ.erp.ui.admin.AdminDashboardPanel;
import edu.univ.erp.ui.auth.LoginPanel;
import edu.univ.erp.ui.instructor.InstructorDashboardPanel;
import edu.univ.erp.ui.student.StudentDashboardPanel;

import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cards;
    private JPanel cardPanel;

    public MainFrame() {
        setTitle("UniversityERP");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        init();
    }

    private void init() {
        cards = new CardLayout();
        cardPanel = new JPanel(cards);

        // instantiate main panels (placeholders)
        cardPanel.add(new LoginPanel(), "login");
        cardPanel.add(new StudentDashboardPanel(), "student_dashboard");
        cardPanel.add(new InstructorDashboardPanel(), "instructor_dashboard");
        cardPanel.add(new AdminDashboardPanel(), "admin_dashboard");

        add(cardPanel, BorderLayout.CENTER);
    }

    public void showCard(String name) {
        cards.show(cardPanel, name);
    }
}
