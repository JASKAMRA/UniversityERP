package edu.univ.erp.ui.instructor;

import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import java.awt.*;

public class InstructorDashboardPanel extends JPanel {
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
        if (uiInitialized) return;
        uiInitialized = true;

        setLayout(new BorderLayout(8,8));
        JPanel top = new JPanel(new BorderLayout());
        welcomeLabel.setFont(welcomeLabel.getFont().deriveFont(Font.BOLD, 18f));
        welcomeLabel.setText("Hello");
        top.add(welcomeLabel, BorderLayout.WEST);
        add(top, BorderLayout.NORTH);

        add(centerContainer, BorderLayout.CENTER);

        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        rightPanel.setVisible(false);
        add(rightPanel, BorderLayout.EAST);
    }

    public void setWelcomeName(String name) {
        if (name == null || name.trim().isEmpty()) welcomeLabel.setText("Hello");
        else welcomeLabel.setText("Hello, " + name.trim());
    }

    public void showSections() {
        centerContainer.removeAll();
        mySectionsPanel = new MySectionsPanel(instructorService, currentUserId);
        centerContainer.add(mySectionsPanel, BorderLayout.CENTER);
        centerContainer.revalidate();
        centerContainer.repaint();
    }

    public void enableActions() {
        rightPanel.removeAll();

        JButton btnRefresh = new JButton("Refresh Sections");
        JButton btnExportCsv = new JButton("Export Gradebook (CSV)");
        JButton btnImportCsv = new JButton("Import Grades (CSV)");
        JButton btnClassStats = new JButton("Open Class Stats");

        btnRefresh.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnExportCsv.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnImportCsv.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnClassStats.setAlignmentX(Component.CENTER_ALIGNMENT);

        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(btnRefresh);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(btnExportCsv);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(btnImportCsv);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(btnClassStats);
        rightPanel.add(Box.createVerticalGlue());

        btnRefresh.addActionListener(e -> { if (mySectionsPanel != null) mySectionsPanel.loadSections(); });
        btnExportCsv.addActionListener(e -> {
            if (mySectionsPanel == null) { JOptionPane.showMessageDialog(this, "Open sections first."); return; }
            Integer sec = mySectionsPanel.getSelectedSectionId();
            if (sec == null) { JOptionPane.showMessageDialog(this, "Select a section."); return; }
            CSVImportExportDialog dlg = new CSVImportExportDialog(SwingUtilities.getWindowAncestor(this), instructorService, currentUserId, sec, mySectionsPanel.getModel().getValueAt(mySectionsPanel.getTable().getSelectedRow(), 2).toString(), CSVImportExportDialog.Mode.EXPORT);
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
        });
        btnImportCsv.addActionListener(e -> {
            if (mySectionsPanel == null) { JOptionPane.showMessageDialog(this, "Open sections first."); return; }
            Integer sec = mySectionsPanel.getSelectedSectionId();
            if (sec == null) { JOptionPane.showMessageDialog(this, "Select a section."); return; }
            CSVImportExportDialog dlg = new CSVImportExportDialog(SwingUtilities.getWindowAncestor(this), instructorService, currentUserId, sec, mySectionsPanel.getModel().getValueAt(mySectionsPanel.getTable().getSelectedRow(), 2).toString(), CSVImportExportDialog.Mode.IMPORT);
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
        });
        btnClassStats.addActionListener(e -> {
            if (mySectionsPanel == null) { JOptionPane.showMessageDialog(this, "Open sections first."); return; }
            Integer sec = mySectionsPanel.getSelectedSectionId();
            if (sec == null) { JOptionPane.showMessageDialog(this, "Select a section."); return; }
            ClassStatsPanel stats = new ClassStatsPanel(instructorService, currentUserId, sec, mySectionsPanel.getModel().getValueAt(mySectionsPanel.getTable().getSelectedRow(), 2).toString());
            JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Class Stats", Dialog.ModalityType.APPLICATION_MODAL);
            dlg.getContentPane().add(stats);
            dlg.pack();
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
        });

        rightPanel.setVisible(true);
        revalidate();
        repaint();
    }

    public void loadData(String instructorUserId, String displayName) {
        this.currentUserId = instructorUserId;
        setWelcomeName(displayName);
    }
}
