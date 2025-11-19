package edu.univ.erp.ui.instructor;

import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class InstructorDashboardPanel extends JPanel {
    private final InstructorService instructorService;
    private String currentUserId;

    private MySectionsPanel mySectionsPanel;
    private JPanel rightPanel;
    private JButton btnExportCsv;
    private JButton btnImportCsv;
    private JButton btnClassStats;
    private JButton btnRefresh;

    public InstructorDashboardPanel(InstructorService instructorService, String currentUserId) {
        this.instructorService = instructorService;
        this.currentUserId = currentUserId;
        init();
    }

    private void init() {
        setLayout(new BorderLayout(8,8));
        mySectionsPanel = new MySectionsPanel(instructorService, currentUserId);
        add(mySectionsPanel, BorderLayout.CENTER);

        rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Actions"));

        btnRefresh = new JButton("Refresh Sections");
        btnExportCsv = new JButton("Export Gradebook (CSV)");
        btnImportCsv = new JButton("Import Grades (CSV)");
        btnClassStats = new JButton("Open Class Stats");

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

        add(rightPanel, BorderLayout.EAST);

        // Wire actions
        btnRefresh.addActionListener(e -> mySectionsPanel.loadSections());
        btnExportCsv.addActionListener(e -> doExport());
        btnImportCsv.addActionListener(e -> doImport());
        btnClassStats.addActionListener(e -> openClassStats());
    }

    /**
     * Called when MainFrame wants to (re)load data for a specific instructor
     */
    public void loadData(String instructorUserId) {
        this.currentUserId = instructorUserId;
        // recreate or refresh MySectionsPanel with new userId
        remove(mySectionsPanel);
        mySectionsPanel = new MySectionsPanel(instructorService, currentUserId);
        add(mySectionsPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private Integer getSelectedSectionId() {
        return mySectionsPanel.getSelectedSectionId();
    }

    private String getSelectedCourseTitle() {
        DefaultTableModel model = mySectionsPanel.getModel();
        JTable table = mySectionsPanel.getTable();
        if (model == null || table == null) return null;
        int sel = table.getSelectedRow();
        if (sel < 0) return null;
        Object v = model.getValueAt(sel, 2);
        return v == null ? null : v.toString();
    }

    private void doExport() {
        Integer secId = getSelectedSectionId();
        String title = getSelectedCourseTitle();
        if (secId == null) {
            JOptionPane.showMessageDialog(this, "Select a section.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // ownership check
        if (!instructorService.isInstructorOfSection(currentUserId, secId)) {
            JOptionPane.showMessageDialog(this, "Not your section.", "Permission denied", JOptionPane.ERROR_MESSAGE);
            return;
        }
        CSVImportExportDialog dlg = new CSVImportExportDialog(SwingUtilities.getWindowAncestor(this), instructorService, currentUserId, secId, title, CSVImportExportDialog.Mode.EXPORT);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void doImport() {
        Integer secId = getSelectedSectionId();
        String title = getSelectedCourseTitle();
        if (secId == null) {
            JOptionPane.showMessageDialog(this, "Select a section.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // ownership check
        if (!instructorService.isInstructorOfSection(currentUserId, secId)) {
            JOptionPane.showMessageDialog(this, "Not your section.", "Permission denied", JOptionPane.ERROR_MESSAGE);
            return;
        }
        CSVImportExportDialog dlg = new CSVImportExportDialog(SwingUtilities.getWindowAncestor(this), instructorService, currentUserId, secId, title, CSVImportExportDialog.Mode.IMPORT);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void openClassStats() {
        Integer secId = getSelectedSectionId();
        String title = getSelectedCourseTitle();
        if (secId == null) {
            JOptionPane.showMessageDialog(this, "Select a section.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!instructorService.isInstructorOfSection(currentUserId, secId)) {
            JOptionPane.showMessageDialog(this, "Not your section.", "Permission denied", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ClassStatsPanel statsPanel = new ClassStatsPanel(instructorService, currentUserId, secId, title);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Class Stats - " + title + " (Section " + secId + ")", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.getContentPane().add(statsPanel);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }
}
