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

    // --- Aesthetic constants ---
    private static final int PADDING = 20;
    private static final int GAP = 15;
    private static final Font WELCOME_FONT = new Font("Arial", Font.BOLD, 22);
    private static final Dimension BUTTON_SIZE = new Dimension(220, 35);
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);

    public InstructorDashboardPanel(InstructorService instructorService, String currentUserId) {
        this.instructorService = instructorService;
        this.currentUserId = currentUserId;
        initOnce();
    }

    private void initOnce() {
        if (uiInitialized) return;
        uiInitialized = true;

        // 1. Overall Layout & Padding
        setLayout(new BorderLayout(GAP, GAP));
        setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        setBackground(Color.WHITE);

        // 2. Top Header Panel
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        
        welcomeLabel.setFont(WELCOME_FONT);
        welcomeLabel.setForeground(PRIMARY_COLOR);
        welcomeLabel.setText("Hello");
        
        top.add(new JLabel("🧑‍🏫"), BorderLayout.WEST); // Icon placeholder
        top.add(welcomeLabel, BorderLayout.CENTER);
        
        add(top, BorderLayout.NORTH);

        // 3. Center Content Area (for MySectionsPanel, etc.)
        centerContainer.setBackground(Color.WHITE);
        add(centerContainer, BorderLayout.CENTER);

        // 4. Action Sidebar (East)
        rightPanel.setLayout(new GridBagLayout()); // Use GridBag for clean button layout
        rightPanel.setBackground(new Color(245, 245, 245));
        rightPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            "Grade Actions",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14), PRIMARY_COLOR
        ));
        rightPanel.setVisible(false);
        add(rightPanel, BorderLayout.EAST);
    }

    public void setWelcomeName(String name) {
        String base = "Hello";
        if (name == null || name.trim().isEmpty()) {
             welcomeLabel.setText(base);
        } else {
             welcomeLabel.setText(base + ", " + name.trim() + "!");
        }
    }

    public void showSections() {
        centerContainer.removeAll();
        // Instantiate the sections panel with the current user ID
        mySectionsPanel = new MySectionsPanel(instructorService, currentUserId);
        centerContainer.add(mySectionsPanel, BorderLayout.CENTER);
        centerContainer.revalidate();
        centerContainer.repaint();
    }

    /**
     * Populates and displays the action sidebar.
     */
    public void enableActions() {
        rightPanel.removeAll();

        // 1. Initialize Buttons
        JButton btnRefresh = new JButton("🔄 Refresh Sections");
        JButton btnExportCsv = new JButton("📤 Export Gradebook (CSV)");
        JButton btnImportCsv = new JButton("📥 Import Grades (CSV)");
        JButton btnClassStats = new JButton("📊 Open Class Stats");

        // 2. Button Styling
        styleButton(btnRefresh, Color.LIGHT_GRAY, Color.BLACK);
        styleButton(btnExportCsv, new Color(180, 220, 255), PRIMARY_COLOR);
        styleButton(btnImportCsv, new Color(255, 230, 180), new Color(200, 150, 0));
        styleButton(btnClassStats, new Color(220, 255, 220), new Color(50, 160, 50));


        // 3. GridBagLayout Constraints Setup
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(GAP / 2, GAP, GAP / 2, GAP);
        c.gridx = 0; c.weightx = 1.0;

        // 4. Add Components to Sidebar
        int y = 0;
        rightPanel.add(Box.createVerticalStrut(10), c); // Top padding

        c.gridy = ++y; rightPanel.add(btnRefresh, c);
        
        c.gridy = ++y; rightPanel.add(Box.createVerticalStrut(GAP), c); // Separator

        c.gridy = ++y; rightPanel.add(btnExportCsv, c);
        c.gridy = ++y; rightPanel.add(btnImportCsv, c);
        
        c.gridy = ++y; rightPanel.add(Box.createVerticalStrut(GAP), c); // Separator

        c.gridy = ++y; rightPanel.add(btnClassStats, c);
        
        c.gridy = ++y; c.weighty = 1.0; rightPanel.add(Box.createVerticalGlue(), c); // Push buttons up

        // 5. Action Listeners (Logic Unchanged)
        btnRefresh.addActionListener(e -> { if (mySectionsPanel != null) mySectionsPanel.loadSections(); });
        
        btnExportCsv.addActionListener(e -> runGradeAction(CSVImportExportDialog.Mode.EXPORT));
        btnImportCsv.addActionListener(e -> runGradeAction(CSVImportExportDialog.Mode.IMPORT));
        btnClassStats.addActionListener(e -> runClassStats());

        rightPanel.setVisible(true);
        rightPanel.revalidate();
        rightPanel.repaint();
    }
    
    /** Helper to style buttons */
    private void styleButton(JButton button, Color bg, Color fg) {
        button.setPreferredSize(BUTTON_SIZE);
        button.setMinimumSize(BUTTON_SIZE);
        button.setFocusPainted(false);
        button.setBackground(bg);
        button.setForeground(fg);
    }
    
    /** Common logic to check section selection before launching CSV dialogs */
    private void runGradeAction(CSVImportExportDialog.Mode mode) {
        if (mySectionsPanel == null) { JOptionPane.showMessageDialog(this, "Open My Sections first.", "Prerequisite", JOptionPane.WARNING_MESSAGE); return; }
        
        Integer sec = mySectionsPanel.getSelectedSectionId();
        int selectedRow = mySectionsPanel.getTable().getSelectedRow();
        
        if (sec == null || selectedRow < 0) { JOptionPane.showMessageDialog(this, "Select a section from the table.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }
        
        String courseTitle = mySectionsPanel.getModel().getValueAt(selectedRow, 2).toString(); // Assuming column 2 holds Course Title
        
        CSVImportExportDialog dlg = new CSVImportExportDialog(
            SwingUtilities.getWindowAncestor(this), 
            instructorService, 
            currentUserId, 
            sec, 
            courseTitle, 
            mode
        );
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }
    
    /** Common logic to check section selection before launching Class Stats dialog */
    private void runClassStats() {
        if (mySectionsPanel == null) { JOptionPane.showMessageDialog(this, "Open My Sections first.", "Prerequisite", JOptionPane.WARNING_MESSAGE); return; }
        
        Integer sec = mySectionsPanel.getSelectedSectionId();
        int selectedRow = mySectionsPanel.getTable().getSelectedRow();
        
        if (sec == null || selectedRow < 0) { JOptionPane.showMessageDialog(this, "Select a section from the table.", "No Selection", JOptionPane.WARNING_MESSAGE); return; }

        String courseTitle = mySectionsPanel.getModel().getValueAt(selectedRow, 2).toString();
        
        ClassStatsPanel stats = new ClassStatsPanel(instructorService, currentUserId, sec, courseTitle);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "📊 Class Statistics", Dialog.ModalityType.APPLICATION_MODAL);
        
        dlg.getContentPane().add(stats);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    public void loadData(String instructorUserId, String displayName) {
        this.currentUserId = instructorUserId;
        setWelcomeName(displayName);
        // Ensure mySectionsPanel is refreshed/recreated if the user ID changed (though MainFrame typically handles this)
        if (mySectionsPanel != null) {
            mySectionsPanel.loadSections();
        }
    }
}