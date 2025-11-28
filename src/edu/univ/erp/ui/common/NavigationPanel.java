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

    public NavigationPanel(MainFrame main) {
        this.main=main;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(8 * 2, 8, 8 * 2, 8));
        setPreferredSize(new Dimension(200, 0)); 
        setBackground(new Color(245, 245, 245));

        menu=new JPanel();
        menu.setBackground(new Color(245, 245, 245));
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
       
        add(menu, BorderLayout.NORTH);
        add(new JLabel(" "), BorderLayout.CENTER); 
        LoadMenuforRole(null);
    }

    public void LoadMenuforRole(Role r) {
        menu.removeAll();
        JLabel RoleTitle=new JLabel(GetRoleTitle(r));
        RoleTitle.setFont(new Font("Arial", Font.BOLD, 14));
        RoleTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        RoleTitle.setBorder(new EmptyBorder(0, 0, 8 * 2, 0));
        menu.add(RoleTitle); 
        menu.add(Box.createVerticalStrut(5));

        if (r==null) {
            addButton("🔑 Login", MainFrame.Card_Login, ()-> main.Show_anyCard(MainFrame.Card_Login), false);
        }
        else if (r==Role.INSTRUCTOR) {
            addButton("🏠 Dashboard", MainFrame.Card_Instructor_Dashboard, ()-> main.showInstDashboard());
            addButton("🗓️ My Sections", "INSTR_SECTIONS", ()-> main.ShowInstforSections());
            addButton("⚙️ Enable Grade Actions", "INSTR_ACTIONS", ()-> main.enableInstructorActions());
            addLogoutButton();
        } 
        else if (r==Role.ADMIN) {
            addButton("🏠 Dashboard", MainFrame.Card_Admin_Dashboard, ()-> main.Show_anyCard(MainFrame.Card_Admin_Dashboard));
            addButton("👤 Users", "Admin_User_Management", ()-> main.Show_anyCard("Admin_User_Management"));
            addButton("🎓 Courses", "Admin_Management", ()-> main.Show_anyCard("Admin_Management"));
            addButton("🗓️ Sections", "Admin_Section_mng", () -> main.Show_anyCard("Admin_Section_mng"));
            addButton("🧑‍🏫 Assign Instructor", "Admin_instructor", ()-> main.Show_anyCard("Admin_instructor"));
            addButton("💾 Backup/Restore", "Admin_Backup", ()-> main.Show_anyCard("Admin_Backup"));
            addLogoutButton();
        }
        else if (r==Role.STUDENT) {
            addButton("🏠 Dashboard", MainFrame.Card_Student_Dashboard, ()-> main.Show_anyCard(MainFrame.Card_Student_Dashboard));
            addButton("📚 Course Catalog", "STUDENT_CATALOG", ()-> main.Show_anyCard("STUDENT_CATALOG"));
            addButton("📝 My Registrations", "STUDENT_Registration", ()-> main.Show_anyCard("Student_registration"));
            addButton("⏰ Timetable", "STUDENT_TIMETABLE", ()-> main.Show_anyCard("STUDENT_TIMETABLE"));
            addButton("💯 Grades", "STUDENT_GRADES", ()-> main.Show_anyCard("STUDENT_GRADES"));
            addButton("📜 Transcript", "STUDENT_TRANSCRIPT", ()-> main.Show_anyCard("STUDENT_TRANSCRIPT"));
            addLogoutButton();
        } 
        revalidate();
        repaint();
    }

     private void addLogoutButton(){   
        menu.add(Box.createVerticalGlue());
        JButton button=new JButton("🚪Logout");
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener((ActionEvent e)->logout()); 
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setMaximumSize(new Dimension(300-8*2, 35));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(300-8*2, 35));
        button.setBackground(new Color(255, 100, 100));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        menu.add(Box.createVerticalStrut(8*2));
        menu.add(button); 
    }
   

    private void addButton(String Title, String CardName, Runnable Action) {
        addButton(Title, CardName, Action, true);
    }
    
    private void addButton(String title, String cardName, Runnable action, boolean styled) {
        JButton button= new JButton(title);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener((ActionEvent e)-> action.run());
        
        if (!styled){
            menu.add(button);
        } 
        else{
            button.setFont(new Font("Arial", Font.BOLD, 12));
            button.setMaximumSize(new Dimension(300 - 8 * 2, 35)); 
            button.setPreferredSize(new Dimension(300 - 8 * 2, 35));
            button.setFocusPainted(false);
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            menu.add(button);
            menu.add(Box.createVerticalStrut(8 / 2));
        }
    }
    
    private void logout() {
        CurrentSession.get().SetMant(false);
        CurrentSession.get().setUsr(null);
        main.togglemantainenceON(false); 
        LoadMenuforRole(null);
        main.Show_anyCard(MainFrame.Card_Login);
    }
    private String GetRoleTitle(Role r) {
        if (r!=null){
            return(r.toString()+" Menu");   
        }
            return "Access Required";     
    }
}