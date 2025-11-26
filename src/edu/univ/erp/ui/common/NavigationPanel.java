package edu.univ.erp.ui.common;

import edu.univ.erp.domain.Role;
import edu.univ.erp.ui.util.CurrentSession;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

public class NavigationPanel extends JPanel {
    private MainFrame main;
    private JPanel menu;
    private static final int GAP = 8;

    public NavigationPanel(MainFrame main) {
        this.main = main;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(GAP * 2, GAP, GAP * 2, GAP));
        setPreferredSize(new Dimension(200, 0)); 
        setBackground(new Color(245, 245, 245));

        menu = new JPanel();
        menu.setBackground(new Color(245, 245, 245));
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
       
        add(menu, BorderLayout.NORTH);
        add(new JLabel(" "), BorderLayout.CENTER); 
        LoadMenu_forRole(null);
    }

    public void LoadMenu_forRole(Role role) {
        menu.removeAll();
        JLabel Role_title = new JLabel(GetRoleTitle(role));
        Role_title.setFont(new Font("Arial", Font.BOLD, 14));
        Role_title.setAlignmentX(Component.CENTER_ALIGNMENT);
        Role_title.setBorder(new EmptyBorder(0, 0, GAP * 2, 0));
        menu.add(Role_title); 
        menu.add(Box.createVerticalStrut(5));


        if (role == null) {
            addButton("🔑 Login", MainFrame.Card_Login, ()-> main.Show_any_Card(MainFrame.Card_Login), false);
        }
        else if (role == Role.INSTRUCTOR) {
            addButton("🏠 Dashboard", MainFrame.Card_Instructor_Dashboard, ()-> main.showInstDashboard());
            addButton("🗓️ My Sections", "INSTR_SECTIONS", ()-> main.ShowInstforSections());
            addButton("⚙️ Enable Grade Actions", "INSTR_ACTIONS", ()-> main.enableInstructorActions());
            addLogoutButton();
        } else if (role == Role.STUDENT) {
            addButton("🏠 Dashboard", MainFrame.Card_Student_Dashboard, ()-> main.Show_any_Card(MainFrame.Card_Student_Dashboard));
            addButton("📚 Course Catalog", "STUDENT_CATALOG", ()-> main.Show_any_Card("STUDENT_CATALOG"));
            addButton("📝 My Registrations", "STUDENT_Registration", ()-> main.Show_any_Card("Student_registration"));
            addButton("⏰ Timetable", "STUDENT_TIMETABLE", ()-> main.Show_any_Card("STUDENT_TIMETABLE"));
            addButton("💯 Grades", "STUDENT_GRADES", ()-> main.Show_any_Card("STUDENT_GRADES"));
            addButton("📜 Transcript", "STUDENT_TRANSCRIPT", ()-> main.Show_any_Card("STUDENT_TRANSCRIPT"));
            addLogoutButton();
        } else if (role == Role.ADMIN) {
            addButton("🏠 Dashboard", MainFrame.Card_Admin_Dashboard, ()-> main.Show_any_Card(MainFrame.Card_Admin_Dashboard));
            addButton("👤 Users", "Admin_User_Management", ()-> main.Show_any_Card("Admin_User_Management"));
            addButton("🎓 Courses", "Admin_Management", ()-> main.Show_any_Card("Admin_Management"));
            addButton("🗓️ Sections", "Admin_Section_mng", () -> main.Show_any_Card("Admin_Section_mng"));
            addButton("🧑‍🏫 Assign Instructor", "Admin_instructor", ()-> main.Show_any_Card("Admin_instructor"));
            addButton("💾 Backup/Restore", "Admin_Backup", ()-> main.Show_any_Card("Admin_Backup"));
            addLogoutButton();
        }
        revalidate();
        repaint();
    }
     private void addLogoutButton() {
   
        menu.add(Box.createVerticalGlue());
        JButton button= new JButton("🚪Logout");
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener((ActionEvent e)->logout()); 
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setMaximumSize(new Dimension(300-GAP*2, 35));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(300-GAP*2, 35));
        button.setBackground(new Color(255, 100, 100));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        menu.add(Box.createVerticalStrut(GAP*2));
        menu.add(button); 
    }
   

    private void addButton(String Title, String CardName, Runnable Action) {
        addButton(Title, CardName, Action, true);
    }
    
    private void addButton(String title, String cardName, Runnable action, boolean styled) {
        JButton button= new JButton(title);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener((ActionEvent e)-> action.run());
        
        if (styled) {
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setMaximumSize(new Dimension(300 - GAP * 2, 35)); 
            button.setPreferredSize(new Dimension(300 - GAP * 2, 35));
            button.setFocusPainted(false);
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
         
            menu.add(button);
            menu.add(Box.createVerticalStrut(GAP / 2));
        } else {
            menu.add(button);
        }
    }
 private String GetRoleTitle(Role role) {
        if (role!=null){
            return(role.toString()+" Menu");   
        }
            return "Access Required";
        
    }
    private void logout() {
        CurrentSession.get().SetMant(false);
        CurrentSession.get().setUsr(null);
        main.togglemantainenceON(false); 
        LoadMenu_forRole(null);
        main.Show_any_Card(MainFrame.Card_Login);
    }
}