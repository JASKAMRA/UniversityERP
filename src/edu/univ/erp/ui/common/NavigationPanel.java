package edu.univ.erp.ui.common;

import edu.univ.erp.domain.Role;
import edu.univ.erp.ui.util.CurrentSession;
import edu.univ.erp.ui.util.CurrentUser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class NavigationPanel extends JPanel {
    private MainFrame main;
    private JPanel menu;

    public NavigationPanel(MainFrame main) {
        this.main = main;
        setLayout(new BorderLayout());
        menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        add(menu, BorderLayout.NORTH);
        add(new JLabel(" "), BorderLayout.CENTER);
        loadMenuForRole(null);
    }

    public void loadMenuForRole(Role role) {
        menu.removeAll();
        // if null -> minimal (login)
        if (role == null) {
            JButton loginBtn = new JButton("Login");
            loginBtn.addActionListener(e -> main.showCard(MainFrame.CARD_LOGIN));
            menu.add(loginBtn);
        } else if (role == Role.STUDENT) {
            addButton("Dashboard", () -> main.showCard("STUDENT_DASH"));
            addButton("Course Catalog", () -> main.showStudentCatalog());
            addButton("My Registrations", () -> main.showCard("STUDENT_REGS"));
            addButton("Timetable", () -> main.showCard("STUDENT_TIMETABLE"));
            addButton("Grades", () -> main.showCard("STUDENT_GRADES"));
            addButton("Transcript", () -> main.showCard("STUDENT_TRANSCRIPT"));
            addButton("Logout", this::logout);
        } else if (role == Role.INSTRUCTOR) {
            addButton("Dashboard", () -> main.showCard("INSTR_DASH"));
            addButton("My Sections", () -> main.showCard("INSTR_SECTIONS"));
            addButton("Gradebook", () -> main.showCard("INSTR_GRADEBOOK"));
            addButton("Stats", () -> main.showCard("INSTR_STATS"));
            addButton("Logout", this::logout);
        } else if (role == Role.ADMIN) {
            addButton("Dashboard", () -> main.showCard("ADMIN_DASH"));
            addButton("Users", () -> main.showCard("ADMIN_USERS"));
            addButton("Courses", () -> main.showCard("ADMIN_COURSES"));
            addButton("Sections", () -> main.showCard("ADMIN_SECTIONS"));
            addButton("Assign Instructor", () -> main.showCard("ADMIN_ASSIGN"));
            addButton("Maintenance", () -> main.showCard("ADMIN_MAINT"));
            addButton("Backup", () -> main.showCard("ADMIN_BACKUP"));
            addButton("Logout", this::logout);
        }
        revalidate();
        repaint();
    }

    private void addButton(String title, Runnable action) {
        JButton b = new JButton(title);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addActionListener((ActionEvent e) -> action.run());
        menu.add(b);
    }

    private void logout() {
        CurrentSession.get().setUser(null);
        CurrentSession.get().setMaintenance(false);
        loadMenuForRole(null);
        main.showCard(MainFrame.CARD_LOGIN);
    }
}
