package edu.univ.erp.ui.instructor;

import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class MySectionsPanel extends JPanel {
    private final InstructorService instructorService;
    private final String currentUserId; // instructor's user_id from auth DB
    private JTable table;
    private DefaultTableModel model;
    private JButton btnRefresh;
    private JButton btnOpenGradebook;
    private JCheckBox cbShowAll;

    public MySectionsPanel(InstructorService service, String currentUserId) {
        this.instructorService = service;
        this.currentUserId = currentUserId;
        initComponents();
        loadSections(); // initial load (default: only mine)
    }

    private void initComponents() {
        setLayout(new BorderLayout(8,8));
        model = new DefaultTableModel(new Object[]{"Section ID","Course ID","Course Title","Day","Semester","Year","Capacity","Instructor"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c){ return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRefresh = new JButton("Refresh");
        btnOpenGradebook = new JButton("Open Gradebook");
        cbShowAll = new JCheckBox("Show all sections (read-only for others)");
        bottom.add(cbShowAll);
        bottom.add(btnRefresh);
        bottom.add(btnOpenGradebook);
        add(bottom, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadSections());
        btnOpenGradebook.addActionListener(e -> openGradebookForSelected());
        cbShowAll.addActionListener(e -> loadSections());
    }

    /**
     * Public so callers (e.g. dashboard) can refresh the list.
     * If checkbox selected -> load all sections, else load only instructor's assigned sections
     */
    public void loadSections() {
        SwingUtilities.invokeLater(() -> {
            model.setRowCount(0);
            List<Map<String,Object>> rows;
            if (cbShowAll.isSelected()) {
                rows = instructorService.getAllSections();
            } else {
                rows = instructorService.getAssignedSections(currentUserId);
            }
            if (rows == null || rows.isEmpty()) return;
            for (Map<String,Object> r : rows) {
                model.addRow(new Object[]{
                        safeGetIntOrNull(r.get("section_id")),
                        safeToString(r.get("course_id")),
                        safeToString(r.get("course_title")),
                        safeToString(r.get("day")),
                        safeToString(r.get("semester")),
                        safeGetIntOrNull(r.get("year")),
                        safeGetIntOrNull(r.get("capacity")),
                        safeToString(r.get("instructor_user_id"))
                });
            }
        });
    }

    private Integer safeGetIntOrNull(Object o) {
        if (o == null) return null;
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception ex) {
            return null;
        }
    }

    private String safeToString(Object o) {
        return o == null ? "" : o.toString();
    }

    private void openGradebookForSelected() {
        int sel = table.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Select a section first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer sectionIdObj = getSelectedSectionId();
        if (sectionIdObj == null) {
            JOptionPane.showMessageDialog(this, "Selected section id is invalid.", "Invalid data", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int sectionId = sectionIdObj;
        String courseTitle = String.valueOf(model.getValueAt(sel, 2));
        // Open gradebook dialog - pass instructor user id to gradebook
        GradebookPanel gb = new GradebookPanel(instructorService, sectionId, courseTitle, currentUserId);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Gradebook - " + courseTitle + " (Section " + sectionId + ")", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dlg.getContentPane().add(gb);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        // On close we may refresh (optional)
        loadSections();
    }

    /**
     * Safe getter for the underlying JTable (for external panels).
     */
    public JTable getTable() {
        return table;
    }

    /**
     * Safe getter for the table model.
     */
    public DefaultTableModel getModel() {
        return model;
    }

    /**
     * Returns the selected section id (or null if none / invalid).
     */
    public Integer getSelectedSectionId() {
        int sel = table.getSelectedRow();
        if (sel < 0) return null;
        Object v = model.getValueAt(sel, 0);
        return safeGetIntOrNull(v);
    }
}
