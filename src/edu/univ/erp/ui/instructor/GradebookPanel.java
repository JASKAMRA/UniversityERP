package edu.univ.erp.ui.instructor;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Role;
import edu.univ.erp.service.InstructorService;
import edu.univ.erp.ui.util.CurrentSession;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.List;

/**
 * Polished GradebookPanel
 * - fixed component list (dropdown) with enforced maxima
 * - upsert behavior (no duplicates)
 * - sexy UI: toolbar, colored buttons, alternating row striping, right-aligned scores, log
 * - maintenance + ownership checks
 */
public class GradebookPanel extends JPanel {

    private final InstructorService instructorService;
    private final int sectionId;
    private final String courseTitle;
    private final String instructorUserId;

    private JTable table;
    private DefaultTableModel model;
    private JButton btnRefresh;
    private JButton btnSaveSelected;
    private JButton btnSaveAll;
    private JButton btnFinalize;
    private JButton btnDefinalize;
    private JTextArea taLog;
    private JComboBox<String> compEditor;

    // fixed component maxima (from user's list)
    private final LinkedHashMap<String, BigDecimal> componentMax = new LinkedHashMap<>();

    public GradebookPanel(InstructorService service, int sectionId, String courseTitle, String instructorUserId) {
        this.instructorService = service;
        this.sectionId = sectionId;
        this.courseTitle = courseTitle;
        this.instructorUserId = instructorUserId;

        initComponentMax();
        initComponents();
        loadStudents();
    }

    private void initComponentMax() {
        // user's provided component maxima
        componentMax.put("Assignment 1", new BigDecimal("10"));
        componentMax.put("Assignment 2", new BigDecimal("10"));
        componentMax.put("Midsem", new BigDecimal("25"));
        componentMax.put("Endsem", new BigDecimal("30"));
        componentMax.put("Quiz 1", new BigDecimal("10"));
        componentMax.put("Quiz 2", new BigDecimal("10"));
        componentMax.put("Attendance", new BigDecimal("5"));
    }

    private void initComponents() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        // Top toolbar with colored buttons
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        toolbar.setBackground(new Color(245,245,245));

        JLabel title = new JLabel("Gradebook — " + courseTitle + " (Section " + sectionId + ")");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        toolbar.add(title);
        toolbar.addSeparator(new Dimension(12,0));

        btnRefresh = makeButton("Refresh", "Reload student rows");
        btnSaveSelected = makeButton("Save Selected", "Save currently selected component/score");
        btnSaveAll = makeButton("Save All", "Validate & save all rows");
        btnFinalize = makeButton("Finalize", "Lock grades for this section");
        btnDefinalize = makeButton("Definalize", "Unlock grades if supported");

        toolbar.add(btnRefresh);
        toolbar.addSeparator();
        toolbar.add(btnSaveSelected);
        toolbar.add(btnSaveAll);
        toolbar.addSeparator();
        toolbar.add(btnFinalize);
        toolbar.add(btnDefinalize);

        add(toolbar, BorderLayout.NORTH);

        // Table model and table
        model = new DefaultTableModel(new Object[]{"Enrollment ID","Roll No","Name","Component","Score"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                // only component and score editable
                return col == 3 || col == 4;
            }
        };

        table = new JTable(model) {
            // alternating row colors
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                } else {
                    c.setBackground(new Color(220, 235, 255));
                }
                return c;
            }
        };

        // hide enrollment id column width
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // right align score column
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        // component editor = dropdown of allowed components (prevents typos)
        compEditor = new JComboBox<>(componentMax.keySet().toArray(new String[0]));
        compEditor.setEditable(false);
        TableColumn compCol = table.getColumnModel().getColumn(3);
        compCol.setCellEditor(new DefaultCellEditor(compEditor));
        compCol.setPreferredWidth(180);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Log area
        taLog = new JTextArea(6, 80);
        taLog.setEditable(false);
        taLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        taLog.setBackground(new Color(30,30,30));
        taLog.setForeground(new Color(220,220,220));
        taLog.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        add(new JScrollPane(taLog), BorderLayout.SOUTH);

        // action wiring
        btnRefresh.addActionListener(e -> loadStudents());
        btnSaveSelected.addActionListener(e -> saveSelectedGrade());
        btnSaveAll.addActionListener(e -> saveAllGrades());
        btnFinalize.addActionListener(e -> finalizeSection());
        btnDefinalize.addActionListener(e -> definalizeSection());

        // tooltip hints
        table.setToolTipText("Select a student row. Edit Component (choose) and Score then Save.");

        // small UX: double-click component cell focuses editor
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = table.rowAtPoint(e.getPoint());
                    int c = table.columnAtPoint(e.getPoint());
                    if (c == 3) table.editCellAt(r, c);
                }
            }
        });
    }

    private JButton makeButton(String text, String tooltip) {
        JButton b = new JButton(text);
        b.setFocusable(false);
        b.setToolTipText(tooltip);
        b.setBackground(new Color(60,120,200));
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
        return b;
    }

    private void appendLog(String s) {
    taLog.append("[" 
            + new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()) 
            + "] " + s + "\n");

    taLog.setCaretPosition(taLog.getDocument().getLength());
}


    private void loadStudents() {
        model.setRowCount(0);
        taLog.setText("");
        try {
            List<Map<String,Object>> rows = instructorService.getStudentsInSection(sectionId);
            if (rows == null) {
                appendLog("No student rows returned.");
                return;
            }
            for (Map<String,Object> r : rows) {
                model.addRow(new Object[]{
                        r.get("enrollment_id"),
                        r.get("roll_no"),
                        r.get("name"),
                        // If DB provided a component that doesn't match exactly, try to map to a known component (case-insensitive)
                        mapToKnownComponent(r.get("component")),
                        r.get("score") == null ? "" : r.get("score")
                });
            }
            appendLog("Loaded " + model.getRowCount() + " rows.");
        } catch (Exception ex) {
            ex.printStackTrace();
            appendLog("Error loading students: " + ex.getMessage());
        }
    }

    private String mapToKnownComponent(Object compObj) {
        if (compObj == null) return "";
        String s = String.valueOf(compObj).trim();
        if (s.isEmpty()) return "";
        for (String k : componentMax.keySet()) {
            if (k.equalsIgnoreCase(s)) return k;
        }
        // unknown -> return raw for visibility
        return s;
    }

    private boolean checkMaintenanceAndOwnership() {
        Role role = CurrentSession.get().getUser().getRole();
        if (!AccessControl.isActionAllowed(role, true)) {
            JOptionPane.showMessageDialog(this,
                    "System is in maintenance mode. Write operations are disabled.",
                    "Maintenance ON", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!instructorService.isInstructorOfSection(instructorUserId, sectionId)) {
            JOptionPane.showMessageDialog(this,
                    "Not your section.", "Permission denied", JOptionPane.ERROR_MESSAGE);
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
        try { enrollmentId = Integer.parseInt(String.valueOf(enrollObj)); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid enrollment id.", "Error", JOptionPane.ERROR_MESSAGE); return; }

        String component = String.valueOf(model.getValueAt(row, 3)).trim();
        String scoreStr = String.valueOf(model.getValueAt(row, 4)).trim();

        if (component.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Choose a component.", "Missing", JOptionPane.WARNING_MESSAGE);
            return;
        }
        BigDecimal score;
        try { score = new BigDecimal(scoreStr); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this, "Invalid score format.", "Error", JOptionPane.ERROR_MESSAGE); return; }

        BigDecimal compMax = componentMax.getOrDefault(component, new BigDecimal("100"));
        if (score.compareTo(compMax) > 0) {
            JOptionPane.showMessageDialog(this,
                    "Score exceeds allowed maximum for " + component + " (" + compMax.toPlainString() + ").",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean ok = upsertGradeInDb(enrollmentId, component, score);
        if (ok) {
            appendLog("Saved: enrollment " + enrollmentId + " — " + component + " = " + score);
            loadStudents();
        } else {
            appendLog("Save failed for enrollment " + enrollmentId);
            JOptionPane.showMessageDialog(this, "Failed to save grade.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAllGrades() {
        if (!checkMaintenanceAndOwnership()) return;
        int rows = model.getRowCount();
        int saved = 0, skipped = 0;
        for (int r = 0; r < rows; r++) {
            Object enrollObj = model.getValueAt(r, 0);
            if (enrollObj == null) { skipped++; continue; }
            int enrollmentId;
            try { enrollmentId = Integer.parseInt(String.valueOf(enrollObj)); }
            catch (Exception ex) { skipped++; continue; }

            String component = String.valueOf(model.getValueAt(r, 3)).trim();
            String scoreStr = String.valueOf(model.getValueAt(r, 4)).trim();
            if (component.isEmpty() || scoreStr.isEmpty()) { skipped++; continue; }

            BigDecimal score;
            try { score = new BigDecimal(scoreStr); } catch (Exception ex) { skipped++; continue; }

            BigDecimal compMax = componentMax.getOrDefault(component, new BigDecimal("100"));
            if (score.compareTo(compMax) > 0) { appendLog("Row " + (r+1) + " skipped: > max (" + compMax + ")"); skipped++; continue; }

            boolean ok = upsertGradeInDb(enrollmentId, component, score);
            if (ok) saved++; else skipped++;
        }
        appendLog("Save All: saved=" + saved + " skipped=" + skipped);
        loadStudents();
    }

    /**
     * Upsert: if grade row exists for (enrollment_id, component) -> UPDATE else INSERT.
     */
    private boolean upsertGradeInDb(int enrollmentId, String component, BigDecimal score) {
        String selectSql = "SELECT grade_id FROM grades WHERE enrollment_id = ? AND component = ?";
        String insertSql = "INSERT INTO grades (enrollment_id, component, score) VALUES (?, ?, ?)";
        String updateSql = "UPDATE grades SET score = ? WHERE grade_id = ?";
        try (Connection conn = DBConnection.getStudentConnection()) {
            Integer gradeId = null;
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, enrollmentId);
                ps.setString(2, component);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) gradeId = rs.getInt("grade_id");
                }
            }

            if (gradeId == null) {
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, enrollmentId);
                    ps.setString(2, component);
                    ps.setBigDecimal(3, score);
                    return ps.executeUpdate() == 1;
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setBigDecimal(1, score);
                    ps.setInt(2, gradeId);
                    return ps.executeUpdate() == 1;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void finalizeSection() {
        if (!checkMaintenanceAndOwnership()) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure? Finalizing will prevent further edits.",
                "Confirm Finalize", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        boolean ok = instructorService.finalizeGrades(sectionId);
        if (ok) { appendLog("Section finalized."); loadStudents(); }
        else { appendLog("Finalize failed."); JOptionPane.showMessageDialog(this, "Error finalizing.", "Error", JOptionPane.ERROR_MESSAGE); }
    }

    private void definalizeSection() {
        if (!checkMaintenanceAndOwnership()) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure? This will allow edits again.",
                "Confirm Definalize", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        // try service methods via reflection or direct call if available
        String[] names = new String[]{"definalizeGrades","unfinalizeGrades","setFinalized"};
        Exception lastEx = null;
        for (String n : names) {
            try {
                if ("setFinalized".equals(n)) {
                    try {
                        java.lang.reflect.Method m = instructorService.getClass().getMethod(n, int.class, boolean.class);
                        Object res = m.invoke(instructorService, sectionId, false);
                        if (res instanceof Boolean && (Boolean) res) { appendLog("Definalized via " + n); loadStudents(); return; }
                    } catch (NoSuchMethodException ignored) {
                        try {
                            java.lang.reflect.Method m2 = instructorService.getClass().getMethod(n, Integer.class, Boolean.class);
                            Object res = m2.invoke(instructorService, Integer.valueOf(sectionId), Boolean.FALSE);
                            if (res instanceof Boolean && (Boolean) res) { appendLog("Definalized via " + n); loadStudents(); return; }
                        } catch (NoSuchMethodException ignored2) { continue; }
                    }
                } else {
                    java.lang.reflect.Method m = instructorService.getClass().getMethod(n, int.class);
                    Object res = m.invoke(instructorService, sectionId);
                    if (res instanceof Boolean && (Boolean) res) { appendLog("Definalized via " + n); loadStudents(); return; }
                }
            } catch (NoSuchMethodException nsme) {
                continue;
            } catch (Exception ex) {
                lastEx = ex;
                break;
            }
        }

        if (lastEx != null) {
            lastEx.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error during definalize: " + lastEx.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Definalize not supported by InstructorService. Implement definalizeGrades/unfinalizeGrades/setFinalized.",
                    "Not supported", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
