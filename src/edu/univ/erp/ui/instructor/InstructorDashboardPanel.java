package edu.univ.erp.ui.instructor;
import edu.univ.erp.service.InstructorService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class InstructorDashboardPanel extends JPanel{
    private final InstructorService instructorService;
    private String currentUserId;

    private final JLabel welcomeLabel = new JLabel();
    private final JPanel centerContainer = new JPanel(new BorderLayout());
    private MySectionsPanel mySectionsPanel;

    private final JPanel rightPanel = new JPanel();
    private boolean uiInitialized = false;

    public InstructorDashboardPanel(InstructorService instructorService, String currentUserId) {
        this.instructorService = instructorService;
        this.currentUserId = currentUserId;
        initOnce();
    }

    private void initOnce() {
        if (uiInitialized) {
            return;
        }
        uiInitialized = true;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 22));
        welcomeLabel.setForeground(new Color(70, 130, 180));
        welcomeLabel.setText("Hello");
        
        top.add(new JLabel("🧑‍🏫"), BorderLayout.WEST);
        top.add(welcomeLabel, BorderLayout.CENTER);
        
        add(top, BorderLayout.NORTH);

        centerContainer.setBackground(Color.WHITE);
        add(centerContainer, BorderLayout.CENTER);

        rightPanel.setLayout(new GridBagLayout()); 
        rightPanel.setBackground(new Color(245, 245, 245));
        rightPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            "Grade Actions",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14), new Color(70, 130, 180)
        ));
        rightPanel.setVisible(false);
        add(rightPanel, BorderLayout.EAST);
    }

    public void setWelcomeName(String name) {
        String base = "Hello";
        if (name.trim().isEmpty() || name == null) {
             welcomeLabel.setText(base);
        }
        else {
             welcomeLabel.setText(base + ", " + name.trim() + "!");
        }
    }

    public void showSections(){
        mySectionsPanel=new MySectionsPanel(instructorService, currentUserId);
        centerContainer.removeAll();
        centerContainer.repaint();
        centerContainer.add(mySectionsPanel, BorderLayout.CENTER);
        centerContainer.revalidate();
    }

    public void enableActions() {
        rightPanel.removeAll();

        JButton btnRefresh=new JButton("🔄 Refresh Sections");
        JButton btnClassStats=new JButton("📊 Open Class Stats");
        JButton btnImportCsv=new JButton("📥 Import Grades (CSV)");
        JButton btnExportCsv=new JButton("📤 Export Gradebook (CSV)");

        styleButton(btnRefresh, Color.LIGHT_GRAY, Color.BLACK);
        styleButton(btnExportCsv, new Color(180, 220, 255),new Color(70, 130, 180) );
        styleButton(btnImportCsv, new Color(255, 230, 180), new Color(200, 150, 0));
        styleButton(btnClassStats, new Color(220, 255, 220), new Color(50, 160, 50));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(15 / 2, 15, 15 / 2, 15);
        constraints.gridx = 0; constraints.weightx = 1.0;

        int y = 0;
        rightPanel.add(Box.createVerticalStrut(10), constraints); 

        constraints.gridy = ++y; rightPanel.add(btnRefresh, constraints);
        
        constraints.gridy = ++y; rightPanel.add(Box.createVerticalStrut(15), constraints); 

        constraints.gridy = ++y; rightPanel.add(btnExportCsv, constraints);
        constraints.gridy = ++y; rightPanel.add(btnImportCsv, constraints);
        
        constraints.gridy = ++y; rightPanel.add(Box.createVerticalStrut(15), constraints); 

        constraints.gridy = ++y; rightPanel.add(btnClassStats, constraints);
        
        constraints.gridy = ++y; constraints.weighty = 1.0; rightPanel.add(Box.createVerticalGlue(), constraints); 

        btnRefresh.addActionListener(e -> { if (mySectionsPanel != null) mySectionsPanel.loadSections(); });
        
        btnExportCsv.addActionListener(e -> runGradeAction(CSVImportExportDialog.Mode.EXPORT));
        btnImportCsv.addActionListener(e -> runGradeAction(CSVImportExportDialog.Mode.IMPORT));
        btnClassStats.addActionListener(e -> runClassStats());

        rightPanel.setVisible(true);
        rightPanel.revalidate();
        rightPanel.repaint();
    }
    
    
    private void runGradeAction(CSVImportExportDialog.Mode mode) {
        if (mySectionsPanel == null){ JOptionPane.showMessageDialog(this, "Open My Sections first.", "Prerequisite", JOptionPane.WARNING_MESSAGE); return; }
        Integer sec = mySectionsPanel.getSelectedSectionId();
        int selectedRow = mySectionsPanel.getTable().getSelectedRow();       
        if (sec == null || selectedRow < 0){ JOptionPane.showMessageDialog(this, "Select a section from the table.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }        
        String courseTitle = mySectionsPanel.getModel().getValueAt(selectedRow, 2).toString(); // Assuming column 2 holds Course Title
        
        CSVImportExportDialog dialog = new CSVImportExportDialog(
            SwingUtilities.getWindowAncestor(this), 
            instructorService, 
            currentUserId, 
            sec, 
            courseTitle, 
            mode
        );
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    
    private void styleButton(JButton button, Color bg, Color fg) {
        button.setPreferredSize(new Dimension(220, 35));
        button.setMinimumSize(new Dimension(220, 35));
        button.setFocusPainted(false);
        button.setBackground(bg);
        button.setForeground(fg);
    }

    public void loadData(String instructorUserId, String displayName) {
        this.currentUserId = instructorUserId;
        setWelcomeName(displayName);
        if (mySectionsPanel != null) {
            mySectionsPanel.loadSections();
        }
    }

    private void runClassStats() {
        if (mySectionsPanel == null) { 
            JOptionPane.showMessageDialog(this, "Open My Sections first.", "Prerequisite", JOptionPane.WARNING_MESSAGE); 
            return; 
        }
        Integer section = mySectionsPanel.getSelectedSectionId();
        int selectedRow = mySectionsPanel.getTable().getSelectedRow();
        if (section == null || selectedRow < 0){
             JOptionPane.showMessageDialog(this, "Select a section from the table.", "No Selection", JOptionPane.WARNING_MESSAGE); 
             return; 
        }
        String courseTitle=mySectionsPanel.getModel().getValueAt(selectedRow, 2).toString();
        ClassStatsPanel stats=new ClassStatsPanel(instructorService, currentUserId, section, courseTitle);
        JDialog dialog=new JDialog(SwingUtilities.getWindowAncestor(this), "📊 Class Statistics", Dialog.ModalityType.APPLICATION_MODAL);
        
        dialog.getContentPane().add(stats);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

}