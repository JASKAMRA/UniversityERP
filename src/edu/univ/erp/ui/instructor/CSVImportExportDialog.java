package edu.univ.erp.ui.instructor;

import edu.univ.erp.access.AccessControl;
import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Role;
import edu.univ.erp.ui.util.CurrentSession;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
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

    public CSVImportExportDialog(
            Window owner,
            InstructorService instructorService,
            String instructorUserId,
            int sectionId,
            String courseTitle,
            Mode mode
    ) {
        super(owner, mode == Mode.EXPORT ? "Export CSV" : "Import CSV", ModalityType.APPLICATION_MODAL);
        this.instructorService = instructorService;
        this.instructorUserId = instructorUserId;
        this.sectionId = sectionId;
        this.courseTitle = courseTitle;
        this.mode = mode;

        init();
        pack();
        setLocationRelativeTo(owner);
    }

    private void init() {
        setLayout(new BorderLayout(10,10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnChoose = new JButton(mode == Mode.EXPORT ? "Choose Save Location" : "Choose CSV File");
        btnRun = new JButton(mode == Mode.EXPORT ? "Export" : "Import");

        top.add(btnChoose);
        top.add(btnRun);
        add(top, BorderLayout.NORTH);

        taLog = new JTextArea(12,60);
        taLog.setEditable(false);
        taLog.setMargin(new Insets(6,6,6,6));
        add(new JScrollPane(taLog), BorderLayout.CENTER);

        fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));

        // Choose File Button
        btnChoose.addActionListener(e -> {
            if (mode == Mode.EXPORT) {
                fc.setDialogType(JFileChooser.SAVE_DIALOG);
                if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    if (!f.getName().toLowerCase().endsWith(".csv"))
                        f = new File(f.getAbsolutePath() + ".csv");
                    btnChoose.putClientProperty("selectedFile", f);
                    btnChoose.setText("Save To: " + f.getName());
                }
            } else {
                fc.setDialogType(JFileChooser.OPEN_DIALOG);
                if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    File f = fc.getSelectedFile();
                    btnChoose.putClientProperty("selectedFile", f);
                    btnChoose.setText("File: " + f.getName());
                }
            }
        });

        // Run Button (Import/Export)
        btnRun.addActionListener(e -> runAction());
    }

    private void runAction() {
        File file = (File) btnChoose.getClientProperty("selectedFile");
        if (file == null) {
            JOptionPane.showMessageDialog(this, "Please choose a file.", "No file", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 🔥 Maintenance Check
        Role role = CurrentSession.get().getUser().getRole();
        if (!AccessControl.isActionAllowed(role, true)) {
            JOptionPane.showMessageDialog(this,
                    "System is in maintenance mode.\nWrite operations (Import/Export) are disabled.",
                    "Maintenance ON",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 🔥 Ownership Check
        if (!instructorService.isInstructorOfSection(instructorUserId, sectionId)) {
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
        try (Connection conn = DBConnection.getStudentConnection()) {
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
                pw.println("enrollment_id,roll_no,name,component,score");

                while (rs.next()) {
                    pw.printf("%s,%s,%s,%s,%s%n",
                            rs.getInt("enrollment_id"),
                            escape(rs.getString("roll_no")),
                            escape(rs.getString("name")),
                            escape(rs.getString("component")),
                            rs.getObject("score")
                    );
                }
            }

            taLog.append("Export complete.\n");

        } catch (Exception ex) {
            taLog.append("Error during export: " + ex.getMessage() + "\n");
        }
    }

    // ============================================================
    // IMPORT CSV
    // ============================================================
    private void doImport(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file));
             Connection conn = DBConnection.getStudentConnection()) {

            br.readLine(); // skip header
            String line;

            while ((line = br.readLine()) != null) {
                String[] a = split(line);
                int enrollmentId = Integer.parseInt(a[0]);
                String comp = a[3];
                String scoreStr = a[4];
                BigDecimal score = scoreStr.isEmpty() ? null : new BigDecimal(scoreStr);

                boolean ok = instructorService.saveGrade(enrollmentId, comp, score);
                if (!ok) {
                    taLog.append("Failed for enrollment " + enrollmentId + "\n");
                }
            }

            taLog.append("Import complete.\n");

        } catch (Exception ex) {
            taLog.append("Error during import: " + ex.getMessage() + "\n");
        }
    }

    // CSV Helper methods
    private String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\""))
            return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }

    private String[] split(String l) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQ = false;

        for (char c : l.toCharArray()) {
            if (c == '"') inQ = !inQ;
            else if (c == ',' && !inQ) {
                out.add(sb.toString());
                sb.setLength(0);
            } else sb.append(c);
        }
        out.add(sb.toString());
        return out.toArray(new String[0]);
    }
}
