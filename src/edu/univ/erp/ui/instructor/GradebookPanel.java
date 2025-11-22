package edu.univ.erp.ui.instructor;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.ui.util.CurrentSession;
import edu.univ.erp.domain.Role;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
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

    // --- Aesthetic constants ---
    private static final int PADDING = 15;
    private static final int GAP = 10;
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180); // Blue
    private static final Color SUCCESS_COLOR = new Color(50, 160, 50); // Green
    private static final Color WARNING_COLOR = new Color(255, 165, 0); // Orange

    public GradebookPanel(InstructorService service, int sectionId, String courseTitle, String instructorUserId) {
        this.instructorService = service;
        this.sectionId = sectionId;
        this.courseTitle = courseTitle;
        this.instructorUserId = instructorUserId;

        initComponents();
        loadStudents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(GAP, GAP));
        setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        setBackground(Color.WHITE);
        
        // 1. Header (North)
        JLabel header = new JLabel("📚 Gradebook: " + courseTitle + " (Section " + sectionId + ")");
        header.setFont(new Font("Arial", Font.BOLD, 22));
        header.setForeground(PRIMARY_COLOR);
        add(header, BorderLayout.NORTH);

        // 2. Gradebook Table (Center)
        model = new DefaultTableModel(
                new Object[]{"Enrollment ID", "Roll No", "Name", "Component", "Score"},0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                // Component (col 3) & Score (col 4) are editable
                return c == 3 || c == 4; 
            }
            // Ensure data types are handled correctly (especially for Score)
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 4) return BigDecimal.class;
                return Object.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        
        // Style editable columns slightly differently
        // Note: This requires a custom renderer/editor usually, but basic table appearance is set here.
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        add(scrollPane, BorderLayout.CENTER);

        // 3. Action Buttons (South)
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP, GAP));
        bottom.setBackground(Color.WHITE);
        
        btnSave = new JButton("📝 Save Selected Grade");
        btnFinalize = new JButton("🔒 Finalize All Grades");
        btnDefinalize = new JButton("🔓 Definalize Grades");

        // Style buttons
        styleButton(btnSave, PRIMARY_COLOR, Color.WHITE);
        styleButton(btnFinalize, SUCCESS_COLOR, Color.WHITE);
        styleButton(btnDefinalize, WARNING_COLOR, Color.BLACK);

        bottom.add(btnSave);
        bottom.add(btnFinalize);
        bottom.add(btnDefinalize);
        add(bottom, BorderLayout.SOUTH);

        // 4. Action Listeners
        btnSave.addActionListener(e -> saveSelectedGrade());
        btnFinalize.addActionListener(e -> finalizeSection());
        btnDefinalize.addActionListener(e -> definalizeSection());
    }

    private void styleButton(JButton button, Color bg, Color fg) {
        button.setFocusPainted(false);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setPreferredSize(new Dimension(180, 35));
    }

    private void loadStudents() {
        model.setRowCount(0);
        
        // Note: This assumes getStudentsInSection returns rows with: 
        // {enrollment_id, roll_no, name, component (optional), score (optional)}
        List<Map<String,Object>> rows = instructorService.getStudentsInSection(sectionId);
        if (rows == null) return;
        
        for (Map<String,Object> r : rows) {
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

    /** Checks for Maintenance Mode and Section Ownership before allowing writes. */
    private boolean checkMaintenanceAndOwnership() {
        Role role = CurrentSession.get().getUser().getRole();
        
        // Block writes if maintenance ON
        if (!AccessControl.isActionAllowed(role, true)) {
            JOptionPane.showMessageDialog(this,
                    "System is in maintenance mode. Write operations are disabled.",
                    "Maintenance ON",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Ownership check
        if (!instructorService.isInstructorOfSection(instructorUserId, sectionId)) {
            JOptionPane.showMessageDialog(this, "You do not have permission to modify this section's grades.", "Permission denied", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void saveSelectedGrade() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a student row to save the grade.", "No row selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!checkMaintenanceAndOwnership()) return;

        Object enrollObj = model.getValueAt(row, 0);
        if (enrollObj == null) {
            JOptionPane.showMessageDialog(this, "Invalid enrollment ID in the table.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int enrollmentId;
        try {
            enrollmentId = Integer.parseInt(String.valueOf(enrollObj));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid enrollment ID format.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String comp = String.valueOf(model.getValueAt(row, 3)).trim();
        String scoreStr = String.valueOf(model.getValueAt(row, 4)).trim();

        if (comp.isEmpty() || scoreStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter both Grade Component (e.g., Midterm, Final) and Score.", "Missing fields", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Ensure component is uppercase as per common convention (if service expects it)
        comp = comp.toUpperCase();

        BigDecimal score;
        try {
            score = new BigDecimal(scoreStr);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Score must be a valid number (e.g., 85.5).", "Invalid score format", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean ok = instructorService.saveGrade(enrollmentId, comp, score);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Grade for Component **" + comp + "** saved successfully.");
            // Update table cell in case comp/score was fixed/normalized by service
            model.setValueAt(comp, row, 3);
            model.setValueAt(score, row, 4);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to save grade. The section might be finalized or enrollment ID is invalid.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void finalizeSection() {
        if (!checkMaintenanceAndOwnership()) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "<html>Are you sure you want to **FINALIZE** grades for Section " + sectionId + "?<br/>" +
                "This action typically prevents further editing.</html>",
                "⚠️ Confirm Finalize",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = instructorService.finalizeGrades(sectionId);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Grades successfully finalized.");
            loadStudents(); // Reload to show final state
        } else {
            JOptionPane.showMessageDialog(this, "Error finalizing grades. Check if all required grades are entered.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Attempts to definalize grades. Uses reflection as a safe fallback.
     */
    private void definalizeSection() {
        if (!checkMaintenanceAndOwnership()) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "<html>Are you sure you want to **DEFINALIZE** grades for Section " + sectionId + "?<br/>" +
                "This will allow grades to be edited again.</html>",
                "🔓 Confirm Definalize",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        // Try common method names via reflection (Logic Unchanged)
        String[] candidateNames = new String[]{"definalizeGrades", "unfinalizeGrades", "setFinalized"};
        Exception lastEx = null;

        for (String name : candidateNames) {
            try {
                Method m;
                Object res = null;
                
                if ("setFinalized".equals(name)) {
                    // Try: setFinalized(int sectionId, boolean finalized)
                    try {
                        m = instructorService.getClass().getMethod(name, int.class, boolean.class);
                        res = m.invoke(instructorService, sectionId, false);
                    } catch (NoSuchMethodException ignored) {
                        // Try with wrapper types: setFinalized(Integer sectionId, Boolean finalized)
                        try {
                            m = instructorService.getClass().getMethod(name, Integer.class, Boolean.class);
                            res = m.invoke(instructorService, Integer.valueOf(sectionId), Boolean.FALSE);
                        } catch (NoSuchMethodException ignored2) {
                            continue; // Try next candidate
                        }
                    }
                } else {
                    // Try: boolean name(int sectionId)
                    m = instructorService.getClass().getMethod(name, int.class);
                    res = m.invoke(instructorService, sectionId);
                }

                // Interpret result
                if (res instanceof Boolean) {
                    if ((Boolean) res) {
                        JOptionPane.showMessageDialog(this, "Section definalized successfully.");
                        loadStudents();
                        return;
                    } else {
                        JOptionPane.showMessageDialog(this, "Definalize attempted but service returned false (operation may not be permitted).", "Info", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                } else {
                    // Assume success if method was called without exception and didn't return boolean
                    JOptionPane.showMessageDialog(this, "Definalize completed (service returned non-boolean value).");
                    loadStudents();
                    return;
                }
            } catch (NoSuchMethodException nsme) {
                continue; // Try next candidate
            } catch (Exception ex) {
                lastEx = ex;
                break;
            }
        }

        // If we reach here, we couldn't call any supported method
        if (lastEx != null) {
            lastEx.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error while attempting to definalize: " + lastEx.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Definalize method not found in InstructorService. Please update the service implementation.",
                    "Not supported",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}