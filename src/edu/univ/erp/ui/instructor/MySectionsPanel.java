package edu.univ.erp.ui.instructor;

import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class MySectionsPanel extends JPanel {
    private final InstructorService instructorService;
    private final String currentUserId;
    private JTable table;
    private DefaultTableModel model;
    private JButton btnRefresh;
    private JButton btnOpenGradebook;

    public MySectionsPanel(InstructorService service, String currentUserId) {
        this.instructorService = service;
        this.currentUserId = currentUserId;
        initComponents();
        loadSections();
    }

    private void initComponents() {
        setLayout(new BorderLayout(8,8));
        model = new DefaultTableModel(new Object[]{"Section ID","Course ID","Course Title","Day","Semester","Year","Capacity"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c){ return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRefresh = new JButton("Refresh");
        btnOpenGradebook = new JButton("Open Gradebook");
        bottom.add(btnRefresh);
        bottom.add(btnOpenGradebook);
        add(bottom, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadSections());
        btnOpenGradebook.addActionListener(e -> openGradebookForSelected());
    }

    public void loadSections() {
        SwingUtilities.invokeLater(() -> {
            model.setRowCount(0);
            List<Map<String,Object>> rows = instructorService.getAssignedSections(currentUserId);
            if (rows == null || rows.isEmpty()) return;
            for (Map<String,Object> r : rows) {
                model.addRow(new Object[]{
                        safeGetIntOrNull(r.get("section_id")),
                        safeToString(r.get("course_id")),
                        safeToString(r.get("course_title")),
                        safeToString(r.get("day")),
                        safeToString(r.get("semester")),
                        safeGetIntOrNull(r.get("year")),
                        safeGetIntOrNull(r.get("capacity"))
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

        GradebookPanel gb = new GradebookPanel(instructorService, sectionId, courseTitle, currentUserId);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Gradebook - " + courseTitle + " (Section " + sectionId + ")", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dlg.getContentPane().add(gb);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        loadSections();
    }

    public JTable getTable() { return table; }
    public DefaultTableModel getModel() { return model; }

    public Integer getSelectedSectionId() {
        int sel = table.getSelectedRow();
        if (sel < 0) return null;
        Object v = model.getValueAt(sel, 0);
        return safeGetIntOrNull(v);
    }
}
