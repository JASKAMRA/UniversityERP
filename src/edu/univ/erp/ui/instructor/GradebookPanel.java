package edu.univ.erp.ui.instructor;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.ui.util.CurrentSession;
import edu.univ.erp.domain.Role;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * GradebookPanel for instructors.
 * - Save component/score rows
 * - Finalize grades for the section
 * - Definalize (if supported by service) using reflection (safe fallback)
 * - Blocks write actions when maintenance mode is ON
 */
public class GradebookPanel extends JPanel {

    private final InstructorService instructorService;
    private final int sectionId;
    private final String courseTitle;
    private final String instructorUserId;

    private JTable table;
    private DefaultTableModel model;
    private JButton btnSave;
    private JButton btnFinalize;
    private JButton btnDefinalize;

    public GradebookPanel(InstructorService service, int sectionId, String courseTitle, String instructorUserId) {
        this.instructorService = service;
        this.sectionId = sectionId;
        this.courseTitle = courseTitle;
        this.instructorUserId = instructorUserId;

        initComponents();
        loadStudents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10,10));

        model = new DefaultTableModel(
                new Object[]{"Enrollment ID","Roll No","Name","Component","Score"},0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 3 || c == 4; // Component & Score editable only
            }
        };

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSave = new JButton("Save Grade");
        btnFinalize = new JButton("Finalize Grades");
        btnDefinalize = new JButton("Definalize Grades");

        bottom.add(btnSave);
        bottom.add(btnFinalize);
        bottom.add(btnDefinalize);
        add(bottom, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> saveSelectedGrade());
        btnFinalize.addActionListener(e -> finalizeSection());
        btnDefinalize.addActionListener(e -> definalizeSection());
    }

    private void loadStudents() {
        model.setRowCount(0);
        List<Map<String,Object>> rows = instructorService.getStudentsInSection(sectionId);
        if (rows == null) return;
        for (Map<String,Object> r : rows) {
            // If your service returns current component/score in the map, include them; else empty.
            Object comp = r.get("component");
            Object score = r.get("score");
            model.addRow(new Object[]{
                    r.get("enrollment_id"),
                    r.get("roll_no"),
                    r.get("name"),
                    comp == null ? "" : comp,
                    score == null ? "" : score
            });
        }
    }

    private boolean checkMaintenanceAndOwnership() {
        Role role = CurrentSession.get().getUser().getRole();
        // Block writes if maintenance ON and user is not allowed
        if (!AccessControl.isActionAllowed(role, true)) {
            JOptionPane.showMessageDialog(this,
                    "System is in maintenance mode. Write operations are disabled.",
                    "Maintenance ON",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Ownership check
        if (!instructorService.isInstructorOfSection(instructorUserId, sectionId)) {
            JOptionPane.showMessageDialog(this, "Not your section.", "Permission denied", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void saveSelectedGrade() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a student row.", "No row", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!checkMaintenanceAndOwnership()) return;

        Object enrollObj = model.getValueAt(row, 0);
        if (enrollObj == null) {
            JOptionPane.showMessageDialog(this, "Invalid enrollment id.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int enrollmentId;
        try {
            enrollmentId = Integer.parseInt(String.valueOf(enrollObj));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid enrollment id.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String comp = String.valueOf(model.getValueAt(row, 3)).trim();
        String scoreStr = String.valueOf(model.getValueAt(row, 4)).trim();

        if (comp.isEmpty() || scoreStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter component and score.", "Missing fields", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal score;
        try {
            score = new BigDecimal(scoreStr);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid score format.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean ok = instructorService.saveGrade(enrollmentId, comp.toUpperCase(), score);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Grade saved.");
            // optionally refresh row or reload table
            loadStudents();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to save grade.\nThe section might be finalized or a final grade already exists.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void finalizeSection() {
        if (!checkMaintenanceAndOwnership()) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure? After this you cannot add/edit grades.",
                "Confirm Finalize",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = instructorService.finalizeGrades(sectionId);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Grades finalized.");
            loadStudents();
        } else {
            JOptionPane.showMessageDialog(this, "Error finalizing grades.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Attempts to definalize grades. Uses reflection to call a supported method on InstructorService if present.
     * Checks (maintenance + ownership) before attempting.
     */
    private void definalizeSection() {
        if (!checkMaintenanceAndOwnership()) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure? This will allow grades to be edited again.",
                "Confirm Definalize",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        // Try common method names via reflection to avoid compile-time dependency:
        String[] candidateNames = new String[]{"definalizeGrades", "unfinalizeGrades", "setFinalized"};
        boolean called = false;
        Exception lastEx = null;

        for (String name : candidateNames) {
            try {
                Method m;
                Object res = null;
                if ("setFinalized".equals(name)) {
                    // signature guess: setFinalized(int sectionId, boolean finalized)
                    try {
                        m = instructorService.getClass().getMethod(name, int.class, boolean.class);
                        res = m.invoke(instructorService, sectionId, false);
                    } catch (NoSuchMethodException ignored) {
                        // try with Integer param
                        try {
                            m = instructorService.getClass().getMethod(name, Integer.class, Boolean.class);
                            res = m.invoke(instructorService, Integer.valueOf(sectionId), Boolean.FALSE);
                        } catch (NoSuchMethodException ignored2) {
                            continue;
                        }
                    }
                } else {
                    // signature guess: boolean name(int sectionId)
                    m = instructorService.getClass().getMethod(name, int.class);
                    res = m.invoke(instructorService, sectionId);
                }

                // interpret result
                if (res instanceof Boolean) {
                    if ((Boolean) res) {
                        JOptionPane.showMessageDialog(this, "Section definalized successfully.");
                        loadStudents();
                        return;
                    } else {
                        JOptionPane.showMessageDialog(this, "Definalize attempted but service returned false.", "Info", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                } else {
                    // If method didn't return boolean we assume success if no exception
                    JOptionPane.showMessageDialog(this, "Definalize completed (no boolean result).");
                    loadStudents();
                    return;
                }
            } catch (NoSuchMethodException nsme) {
                // try next candidate
                continue;
            } catch (Exception ex) {
                lastEx = ex;
                break;
            }
        }

        // If we reach here, we couldn't call any method
        if (lastEx != null) {
            lastEx.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error while attempting to definalize: " + lastEx.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Definalize is not supported by the current InstructorService implementation.\n" +
                            "If you want this feature, add one of the following methods to InstructorService/Impl:\n" +
                            "  - boolean definalizeGrades(int sectionId)\n" +
                            "  - boolean unfinalizeGrades(int sectionId)\n" +
                            "  - boolean setFinalized(int sectionId, boolean finalized)\n",
                    "Not supported",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
