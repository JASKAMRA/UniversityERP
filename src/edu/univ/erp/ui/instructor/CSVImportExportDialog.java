package edu.univ.erp.ui.instructor;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Role;
import edu.univ.erp.ui.util.CurrentSession;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV Import/Export Dialog with:
 * ✓ Maintenance mode blocking
 * ✓ Ownership check
 * ✓ Safe CSV parsing
 */
public class CSVImportExportDialog extends JDialog {

    public enum Mode { EXPORT, IMPORT }

    private final InstructorService instructorService;
    private final int sectionId;
    private final String courseTitle;
    private final Mode mode;
    private final String instructorUserId;

    private JButton btnChoose;
    private JButton btnRun;
    private JTextArea taLog;
    private JFileChooser fc;
    private JLabel lblTarget;

    // --- Aesthetic constants ---
    private static final int PADDING = 15;
    private static final int GAP = 10;
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 18);
    private static final Font LOG_FONT = new Font("Monospaced", Font.PLAIN, 12);
    private static final Dimension BUTTON_SIZE = new Dimension(180, 35);


    public CSVImportExportDialog(
            Window owner,
            InstructorService instructorService,
            String instructorUserId,
            int sectionId,
            String courseTitle,
            Mode mode
    ) {
        super(owner, mode == Mode.EXPORT ? "📤 Export Grades" : "📥 Import Grades", ModalityType.APPLICATION_MODAL);
        this.instructorService = instructorService;
        this.instructorUserId = instructorUserId;
        this.sectionId = sectionId;
        this.courseTitle = courseTitle;
        this.mode = mode;

        init();
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void init() {
        setLayout(new BorderLayout(GAP, GAP));
        setBackground(Color.WHITE);

        // 1. Header (North)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        String action = mode == Mode.EXPORT ? "EXPORT" : "IMPORT";
        JLabel title = new JLabel(String.format("File Operation: %s Grades", action));
        title.setFont(TITLE_FONT);
        title.setForeground(PRIMARY_COLOR);
        
        lblTarget = new JLabel("Target: " + courseTitle + " (Section ID: " + sectionId + ")");
        lblTarget.setFont(new Font("Arial", Font.ITALIC, 14));

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(lblTarget, BorderLayout.SOUTH);
        
        add(headerPanel, BorderLayout.NORTH);


        // 2. Control Panel (Top Center)
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, 0));
        controlPanel.setBorder(new EmptyBorder(GAP, 0, GAP, 0));
        controlPanel.setBackground(Color.WHITE);
        
        btnChoose = new JButton(mode == Mode.EXPORT ? "Select Output File" : "Select Input CSV");
        btnRun = new JButton(mode == Mode.EXPORT ? "Run Export" : "Run Import");

        // Style buttons
        styleButton(btnChoose, Color.LIGHT_GRAY, Color.BLACK);
        styleButton(btnRun, mode == Mode.EXPORT ? new Color(180, 220, 255) : new Color(255, 180, 180), mode == Mode.EXPORT ? PRIMARY_COLOR : Color.RED);

        controlPanel.add(btnChoose);
        controlPanel.add(btnRun);
        add(controlPanel, BorderLayout.CENTER);


        // 3. Log Area (South)
        taLog = new JTextArea(10, 60);
        taLog.setEditable(false);
        taLog.setFont(LOG_FONT);
        taLog.setMargin(new Insets(6,6,6,6));
        taLog.setBorder(BorderFactory.createTitledBorder("Operation Log"));
        
        add(new JScrollPane(taLog), BorderLayout.SOUTH);


        fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        if (mode == Mode.EXPORT) {
            fc.setSelectedFile(new File(courseTitle.replace(' ', '_') + "_Section" + sectionId + "_Grades.csv"));
        }


        // 4. Action Listeners
        btnChoose.addActionListener(e -> handleChooseFile());
        btnRun.addActionListener(e -> runAction());
        
        taLog.append("Dialog initialized. Please select file and run the action.\n");
    }
    
    private void styleButton(JButton button, Color background, Color foreground) {
        button.setPreferredSize(BUTTON_SIZE);
        button.setFocusPainted(false);
        button.setBackground(background);
        button.setForeground(foreground);
    }
    
    private void handleChooseFile() {
        if (mode == Mode.EXPORT) {
            fc.setDialogType(JFileChooser.SAVE_DIALOG);
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                if (!f.getName().toLowerCase().endsWith(".csv"))
                    f = new File(f.getAbsolutePath() + ".csv");
                btnChoose.putClientProperty("selectedFile", f);
                btnChoose.setText("Save To: " + f.getName());
                taLog.append("Selected output file: " + f.getName() + "\n");
            }
        } else {
            fc.setDialogType(JFileChooser.OPEN_DIALOG);
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                btnChoose.putClientProperty("selectedFile", f);
                btnChoose.setText("File: " + f.getName());
                taLog.append("Selected input file: " + f.getName() + "\n");
            }
        }
    }

    private void runAction() {
        File file = (File) btnChoose.getClientProperty("selectedFile");
        if (file == null) {
            JOptionPane.showMessageDialog(this, "Please choose a file.", "No file", JOptionPane.WARNING_MESSAGE);
            return;
        }
        taLog.setText("Starting operation...\n");

        // 🔥 Maintenance Check
        Role role = CurrentSession.get().getUser().getRole();
        if (!AccessControl.isActionAllowed(role, true)) {
            taLog.append("Operation denied: System in maintenance mode.\n");
            JOptionPane.showMessageDialog(this,
                    "System is in maintenance mode.\nWrite operations (Import/Export) are disabled.",
                    "Maintenance ON",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 🔥 Ownership Check
        if (!instructorService.isInstructorOfSection(instructorUserId, sectionId)) {
            taLog.append("Operation denied: Instructor does not own this section.\n");
            JOptionPane.showMessageDialog(this, "Not your section!", "Permission denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (mode == Mode.EXPORT) doExport(file);
        else doImport(file);
    }

    // ============================================================
    // EXPORT CSV
    // ============================================================
    private void doExport(File file) {
        int rowsExported = 0;
        try (Connection conn = DBConnection.getStudentConnection()) {
            // Note: Components are not unique, so we export all existing grade records.
            String sql =
                    "SELECT e.enrollment_id, st.roll_no, st.name, g.component, g.score " +
                    "FROM enrollments e " +
                    "JOIN students st ON e.student_id = st.student_id " +
                    "LEFT JOIN grades g ON e.enrollment_id = g.enrollment_id " +
                    "WHERE e.section_id = ? " +
                    "ORDER BY st.roll_no, g.component";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, sectionId);

            ResultSet rs = ps.executeQuery();

            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                // Header (includes data required for import later: enrollment_id, component, score)
                pw.println("enrollment_id,roll_no,name,component,score");

                while (rs.next()) {
                    pw.printf("%s,%s,%s,%s,%s%n",
                            rs.getInt("enrollment_id"),
                            escape(rs.getString("roll_no")),
                            escape(rs.getString("name")),
                            escape(rs.getString("component")),
                            rs.getObject("score")
                    );
                    rowsExported++;
                }
            }

            taLog.append(String.format("Export complete. %d rows written to %s.\n", rowsExported, file.getName()));
            JOptionPane.showMessageDialog(this, String.format("Export successful! %d records exported.", rowsExported));

        } catch (Exception ex) {
            taLog.append("Error during export: " + ex.getMessage() + "\n");
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ============================================================
    // IMPORT CSV
    // ============================================================
    private void doImport(File file) {
        int rowsRead = 0;
        int successfulSaves = 0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(file));
             Connection conn = DBConnection.getStudentConnection()) {

            String header = br.readLine(); // skip header
            if (header == null) {
                taLog.append("Error: Input file is empty.\n");
                JOptionPane.showMessageDialog(this, "Input file is empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Basic check if header contains key fields (enrollment_id, component, score)
            if (!(header.contains("enrollment_id") && header.contains("component") && header.contains("score"))) {
                 taLog.append("Error: CSV header is missing required fields (enrollment_id, component, score).\n");
                 JOptionPane.showMessageDialog(this, "CSV header is invalid. Must contain enrollment_id, component, and score.", "Error", JOptionPane.ERROR_MESSAGE);
                 return;
            }

            String line;
            while ((line = br.readLine()) != null) {
                rowsRead++;
                if (line.trim().isEmpty()) continue;
                
                try {
                    String[] a = split(line);
                    // Expected array indices: 0:enrollment_id, 1:roll_no, 2:name, 3:component, 4:score
                    int enrollmentId = Integer.parseInt(a[0].trim());
                    String comp = a[3].trim();
                    String scoreStr = a[4].trim();
                    
                    BigDecimal score = scoreStr.isEmpty() ? null : new BigDecimal(scoreStr);
                    
                    // The core service call to save/update the grade
                    boolean ok = instructorService.saveGrade(enrollmentId, comp, score);
                    
                    if (ok) {
                        successfulSaves++;
                    } else {
                        taLog.append(String.format("Row %d: Failed to save (EnrollID %d, Comp %s). Check enrollment ID.\n", rowsRead, enrollmentId, comp));
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                    taLog.append(String.format("Row %d: Parsing error or missing data. Skipped line: %s\n", rowsRead, line.substring(0, Math.min(line.length(), 40)) + "..."));
                }
            }

            taLog.append(String.format("Import complete. Read %d rows. Successfully saved/updated %d records.\n", rowsRead, successfulSaves));
            JOptionPane.showMessageDialog(this, String.format("Import finished. Successfully updated %d records.", successfulSaves));

        } catch (Exception ex) {
            taLog.append("Fatal error during import: " + ex.getMessage() + "\n");
            JOptionPane.showMessageDialog(this, "Import failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ============================================================
    // CSV Helper methods (Logic Unchanged)
    // ============================================================
    private String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) // Added newline for safety
            return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }

    private String[] split(String l) {
        // Simple manual CSV parsing based on quoted fields (Logic Unchanged)
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQ = false;

        for (char c : l.toCharArray()) {
            if (c == '"') inQ = !inQ;
            else if (c == ',' && !inQ) {
                out.add(sb.toString().trim());
                sb.setLength(0);
            } else sb.append(c);
        }
        out.add(sb.toString().trim());
        return out.toArray(new String[0]);
    }
}