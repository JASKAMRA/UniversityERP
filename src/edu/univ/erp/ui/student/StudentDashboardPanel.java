package edu.univ.erp.ui.student;
import edu.univ.erp.ui.util.UserProfile;
import edu.univ.erp.ui.util.CurrentUser;
import edu.univ.erp.ui.util.CurrentSession;
import edu.univ.erp.service.StudentService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class StudentDashboardPanel extends JPanel {

    private final StudentService studentService;
    private UserProfile profile;
    private JLabel lblWelcome;
    private JPanel contentPanel;
    private CardLayout contentLayout;
    private static final String CARD_EMPTY = "EMPTY";
    private static final String CARD_CATALOG = "CATALOG";
    private CourseCatalogPanel catalogPanel;

    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);

    public StudentDashboardPanel(StudentService studentService) {
        this.studentService=studentService;
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel top=new JPanel(new BorderLayout());
        top.setPreferredSize(new Dimension(this.getWidth(), 50));
        top.setBackground(new Color(235, 245, 255));
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY)); 

        lblWelcome=new JLabel("🎓 Student Dashboard");
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 22));
        lblWelcome.setForeground(PRIMARY_COLOR);
        lblWelcome.setBorder(new EmptyBorder(0, 20, 0, 20));

        top.add(lblWelcome, BorderLayout.WEST);
        add(top, BorderLayout.NORTH);

        contentLayout=new CardLayout();
        contentPanel=new JPanel(contentLayout);
        contentPanel.setBackground(Color.WHITE);

        JPanel emptyPanel=new JPanel(new GridBagLayout());
        emptyPanel.setBackground(Color.WHITE);

        JPanel messgPanel=new JPanel();
        messgPanel.setLayout(new BoxLayout(messgPanel, BoxLayout.Y_AXIS));
        messgPanel.setBackground(Color.WHITE);

        JLabel l1=new JLabel("Welcome to your Student Dashboard.");
        l1.setFont(new Font("Arial", Font.PLAIN, 16));
        l1.setForeground(Color.GRAY);
        l1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel l2=new JLabel("Please use the menu on the left to navigate and access your academic records.");
        l2.setFont(new Font("Arial", Font.PLAIN, 14));
        l2.setForeground(Color.GRAY);
        l2.setAlignmentX(Component.CENTER_ALIGNMENT);

        messgPanel.add(l1);
        messgPanel.add(Box.createVerticalStrut(6));
        messgPanel.add(l2);

        emptyPanel.add(messgPanel);
        contentPanel.add(emptyPanel, CARD_EMPTY);
        add(contentPanel, BorderLayout.CENTER);
    }


    public void loadData(UserProfile profile) {
        this.profile=profile;
        if (profile!=null) {
            lblWelcome.setText("Welcome, " + profile.getNAAM() + "!");
            ensureCatalogPanel();
            contentLayout.show(contentPanel, CARD_EMPTY); 
        }
    }

    private void ensureCatalogPanel() {
        if (catalogPanel==null) {
            CurrentUser current_user=CurrentSession.get().getUsr();
            if (current_user==null) {
                return;
            }
            String userId=current_user.GetUserID();
            catalogPanel=new CourseCatalogPanel(studentService, userId);
            contentPanel.add(catalogPanel, CARD_CATALOG);
        }
    }

    public void showCatalog() {
        if (profile==null) {
            JOptionPane.showMessageDialog(this, "Profile not loaded yet.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ensureCatalogPanel();
        contentLayout.show(contentPanel, CARD_CATALOG);
    }
}
