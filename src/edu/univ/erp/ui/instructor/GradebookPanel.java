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
    private JTextArea tLog;
    private JComboBox<String> compEditor;

    private final LinkedHashMap<String, BigDecimal> componentMax=new LinkedHashMap<>();

    public GradebookPanel(InstructorService service, int sectionId, String courseTitle, String instructorUserId) {
        this.instructorService = service;
        this.instructorUserId = instructorUserId;
        this.sectionId = sectionId;
        this.courseTitle = courseTitle;
        initComponentMax();
        initComponents();
        loadStudents();
    }

    private void initComponentMax() {
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

        model = new DefaultTableModel(new Object[]{"Enrollment ID","Roll No","Name","Component","Score"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 4 || col == 3;
            }
        };

        table = new JTable(model) {
            
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
               if (isRowSelected(row)) {      // check krliyo yeh 
                    c.setBackground(new Color(220, 235, 255));
                } 
                else {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 250));
                }
                return c;
            }
        };

        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(26);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);

        compEditor = new JComboBox<>(componentMax.keySet().toArray(new String[0]));
        compEditor.setEditable(false);
        TableColumn compColumn = table.getColumnModel().getColumn(3);
        compColumn.setCellEditor(new DefaultCellEditor(compEditor));
        compColumn.setPreferredWidth(180);

        add(new JScrollPane(table), BorderLayout.CENTER);

        tLog = new JTextArea(6, 80);
        tLog.setEditable(false);
        tLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        tLog.setBackground(new Color(30,30,30));
        tLog.setForeground(new Color(220,220,220));
        tLog.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        add(new JScrollPane(tLog), BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadStudents());
        btnSaveSelected.addActionListener(e -> saveSelectedGrade());
        btnSaveAll.addActionListener(e -> saveAllGrades());
        btnFinalize.addActionListener(e -> finalizeSection());
        btnDefinalize.addActionListener(e -> definalizeSection());

        table.setToolTipText("Select a student row. Edit Component (choose) and Score then Save.");

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = table.rowAtPoint(e.getPoint());
                    int c = table.columnAtPoint(e.getPoint());
                    if (c == 3) {
                        table.editCellAt(r, c);
                    }
                }
            }
        });
    }

    private JButton makeButton(String text, String tooltip) {
        JButton but=new JButton(text);
        but.setFocusable(false);
        but.setToolTipText(tooltip);
        but.setBackground(new Color(60,120,200));
        but.setForeground(Color.WHITE);
        but.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
        return but;
    }

    private void appendLog(String s) {
    tLog.append("[" 
            + new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()) 
            + "] " + s + "\n");

    tLog.setCaretPosition(tLog.getDocument().getLength());
}


    private void loadStudents() {
        model.setRowCount(0);
        tLog.setText("");
        try {
            List<Map<String,Object>> rows = instructorService.GetstuInSec(sectionId);
            if (rows==null) {
                appendLog("No student rows returned.");
                return;
            }
            for (Map<String,Object> r : rows) {
                model.addRow(new Object[]{
                        r.get("enrollment_id"),
                        r.get("roll_no"),
                        r.get("name"),
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
        if (compObj == null) {
            return "";
        }
        String s = String.valueOf(compObj).trim();
        if (s.isEmpty()){
             return "";
        }     
        for (String k : componentMax.keySet()) {
            if (k.equalsIgnoreCase(s)) {
                return k;
            }    
        }
        return s;
    }

    private boolean checkMaintenanceAndOwnership() {
        Role role=CurrentSession.get().getUsr().GetRole();
        if (!AccessControl.isActionAllowed(role, true)) {
            JOptionPane.showMessageDialog(this,
                    "System is in maintenance mode. Write operations are disabled.",
                    "Maintenance ON", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!instructorService.IsInstructorIn(instructorUserId, sectionId)) {
            JOptionPane.showMessageDialog(this,
                    "Not your section.", "Permission denied", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void saveSelectedGrade() {
        int r=table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Select a student row.", "No row", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!checkMaintenanceAndOwnership()){
            return;
        }
        Object enrollObject=model.getValueAt(r, 0);
        if (enrollObject==null) {
            JOptionPane.showMessageDialog(this, "Invalid enrollment id.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int enrollmentId;
        try { 
            enrollmentId = Integer.parseInt(String.valueOf(enrollObject)); 
        }
        catch (Exception ex) { 
            JOptionPane.showMessageDialog(this, "Invalid enrollment id.", "Error", JOptionPane.ERROR_MESSAGE); 
            return; 
        }
        String component = String.valueOf(model.getValueAt(r, 3)).trim();
        String scoreStr = String.valueOf(model.getValueAt(r, 4)).trim();

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
        if (!ok) {
            appendLog("Save failed for enrollment " + enrollmentId);
            JOptionPane.showMessageDialog(this, "Failed to save grade.", "Error", JOptionPane.ERROR_MESSAGE);
        } 
        else {
            appendLog("Saved: enrollment " + enrollmentId + " — " + component + " = " + score);
            loadStudents();
        }
    }

    private void saveAllGrades() {
        if (!checkMaintenanceAndOwnership()) return;
        int row= model.getRowCount();
        int saved=0; 
        int skip=0;
        for (int r=0; r < row; r++) {
            Object enrollObj=model.getValueAt(r, 0);
            if (enrollObj == null) { skip++; continue; }
            int enrollmentId;
            try { enrollmentId = Integer.parseInt(String.valueOf(enrollObj)); }
            catch (Exception exception) { 
                skip++; continue; 
            }

            String component = String.valueOf(model.getValueAt(r, 3)).trim();
            String scoreStr = String.valueOf(model.getValueAt(r, 4)).trim();
            if (component.isEmpty() || scoreStr.isEmpty()) { skip++; continue; }

            BigDecimal score;
            try { 
                score = new BigDecimal(scoreStr); 
            } 
                catch (Exception exception) { 
                    skip++; continue; 
                }

            BigDecimal compMax = componentMax.getOrDefault(component, new BigDecimal("100"));
            if (score.compareTo(compMax) > 0) { 
                appendLog("Row " + (r+1) + " skipped: > max (" + compMax + ")"); skip++; 
                continue; 
            }

            boolean ok = upsertGradeInDb(enrollmentId, component, score);
            if (!ok){ 
                skip++;
            } 
            else{ 
                 saved++;
             }
        }
        appendLog("Save All: saved=" + saved + " skipped=" + skip);
        loadStudents();
    }

    private boolean upsertGradeInDb(int enrollmentId, String component, BigDecimal score) {
    String checkFinalSql = "SELECT 1 FROM grades WHERE enrollment_id = ? AND component = 'FINAL' LIMIT 1";
    String selectSql = "SELECT grade_id FROM grades WHERE enrollment_id = ? AND component = ?";
    String insertSql = "INSERT INTO grades (enrollment_id, component, score) VALUES (?, ?, ?)";
    String updateSql = "UPDATE grades SET score = ? WHERE grade_id = ?";

    try (Connection conn = DBConnection.getStudentConnection()) {
        // 0) check if final exists for this enrollment -> disallow edits if present
        try (PreparedStatement pCheckFinal = conn.prepareStatement(checkFinalSql)) {
            pCheckFinal.setInt(1, enrollmentId);
            try (ResultSet rs = pCheckFinal.executeQuery()) {
                if (rs.next()) {
                    // final exists -> disallow
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(GradebookPanel.this,
                            "Grades are finalized for this student. Edit not allowed unless you definalize the section.",
                            "Finalized", JOptionPane.ERROR_MESSAGE);
                    });
                    return false;
                }
            }
        }

        // proceed with upsert
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
    } catch (SQLException exception) {
        exception.printStackTrace();
        return false;
    }
}


    private void finalizeSection() {
        if (!checkMaintenanceAndOwnership()) {
            return;
        }
        int confirm=JOptionPane.showConfirmDialog(this,
                "Are you sure? Finalizing will prevent further edits.",
                "Confirm Finalize", JOptionPane.YES_NO_OPTION);
        if (confirm!=JOptionPane.YES_OPTION){ 
            return;
        }

        boolean ok=instructorService.Finalize_Grade(sectionId);
        if (!ok) { 
            appendLog("Finalize failed."); 
            JOptionPane.showMessageDialog(this, "Error finalizing.", "Error", JOptionPane.ERROR_MESSAGE); 
        }
        else { 
            appendLog("Section finalized."); 
            loadStudents(); 
        }
    }

    private void definalizeSection() {
        if (!checkMaintenanceAndOwnership()) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure? This will allow edits again.",
                "Confirm Definalize", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }    
        String[] names=new String[]{"definalizeGrades","unfinalizeGrades","setFinalized"};
        Exception lastEx=null;
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
                } 
                else {
                    java.lang.reflect.Method m = instructorService.getClass().getMethod(n, int.class);
                    Object res = m.invoke(instructorService, sectionId);
                    if (res instanceof Boolean && (Boolean) res) { appendLog("Definalized via " + n); loadStudents(); return; }
                }
            } 
            catch (NoSuchMethodException nsme) {
                continue;
            } 
            catch (Exception exception) {
                lastEx = exception;
                break;
            }
        }
        if (lastEx == null) {
            JOptionPane.showMessageDialog(this,
                    "Definalize not supported by InstructorService. Implement definalizeGrades/unfinalizeGrades/setFinalized.",
                    "Not supported", JOptionPane.INFORMATION_MESSAGE);
        } 
        else {
            lastEx.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error during definalize: " + lastEx.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
