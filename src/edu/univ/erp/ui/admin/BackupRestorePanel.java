package edu.univ.erp.ui.admin;

import edu.univ.erp.data.DBConnection;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple CSV backup & restore for core tables.
 * Backup: export selected table to CSV.
 * Restore: import CSV to the selected table (best-effort; expects matching columns).
 *
 * NOTE: This is a basic utility — for full DB dumps use mysqldump.
 */
public class BackupRestorePanel extends JPanel {
    private JComboBox<String> cbTables;
    private JButton btnExport, btnImport, btnRefresh;
    private JTextArea taLog;
    private JFileChooser fc;

    private final String[] allowedTables = new String[] {
            "courses", "sections", "students", "instructors", "enrollments", "grades", "admins"
    };

    public BackupRestorePanel() {
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(8,8));
        JLabel title = new JLabel("Backup / Restore (CSV)");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        add(title, BorderLayout.NORTH);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        cbTables = new JComboBox<>(allowedTables);
        btnExport = new JButton("Export CSV");
        btnImport = new JButton("Import CSV");
        btnRefresh = new JButton("Refresh List");
        top.add(new JLabel("Table:")); top.add(cbTables);
        top.add(btnExport); top.add(btnImport); top.add(btnRefresh);
        add(top, BorderLayout.NORTH);

        taLog = new JTextArea(12,80);
        taLog.setEditable(false);
        add(new JScrollPane(taLog), BorderLayout.CENTER);

        fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));

        btnExport.addActionListener(e -> doExport());
        btnImport.addActionListener(e -> doImport());
        btnRefresh.addActionListener(e -> taLog.append("Tables: " + String.join(", ", allowedTables) + "\n"));
    }

    private void doExport() {
        String table = (String) cbTables.getSelectedItem();
        if (table == null) return;
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".csv")) f = new File(f.getAbsolutePath() + ".csv");

        try (Connection conn = DBConnection.getStudentConnection()) {
            // build simple select * query
            String sql = "SELECT * FROM " + table;
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery();
                 PrintWriter pw = new PrintWriter(new FileWriter(f))) {

                ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();
                // header
                for (int i = 1; i <= cols; i++) {
                    pw.print(md.getColumnName(i));
                    if (i < cols) pw.print(",");
                }
                pw.println();

                while (rs.next()) {
                    for (int i = 1; i <= cols; i++) {
                        Object o = rs.getObject(i);
                        String cell = o == null ? "" : o.toString();
                        pw.print(escape(cell));
                        if (i < cols) pw.print(",");
                    }
                    pw.println();
                }
                taLog.append("Exported " + table + " -> " + f.getAbsolutePath() + "\n");
                JOptionPane.showMessageDialog(this, "Export complete.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            taLog.append("Export error: " + ex.getMessage() + "\n");
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doImport() {
        String table = (String) cbTables.getSelectedItem();
        if (table == null) return;
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();

        int confirm = JOptionPane.showConfirmDialog(this, "Import CSV into " + table + " — this may fail if columns mismatch. Continue?", "Confirm Import", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (BufferedReader br = new BufferedReader(new FileReader(f));
             Connection conn = DBConnection.getStudentConnection()) {

            // read header
            String header = br.readLine();
            if (header == null) { JOptionPane.showMessageDialog(this, "Empty file."); return; }
            String[] cols = parseCsvLine(header);

            // prepare insert SQL
            StringBuilder sbCols = new StringBuilder();
            StringBuilder sbVals = new StringBuilder();
            for (int i = 0; i < cols.length; i++) {
                if (i > 0) { sbCols.append(","); sbVals.append(","); }
                sbCols.append("`").append(cols[i]).append("`");
                sbVals.append("?");
            }
            String sql = "INSERT INTO " + table + " (" + sbCols + ") VALUES (" + sbVals + ")";

            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                String line;
                int imported = 0;
                while ((line = br.readLine()) != null) {
                    String[] vals = parseCsvLine(line);
                    for (int i = 0; i < cols.length; i++) {
                        String v = i < vals.length ? vals[i] : null;
                        if (v == null || v.isEmpty()) ps.setNull(i+1, Types.VARCHAR);
                        else ps.setString(i+1, v);
                    }
                    try {
                        ps.executeUpdate();
                        imported++;
                    } catch (SQLException ex) {
                        // log and continue
                        taLog.append("Row import failed: " + ex.getMessage() + "\n");
                    }
                }
                conn.commit();
                taLog.append("Imported " + imported + " rows into " + table + "\n");
                JOptionPane.showMessageDialog(this, "Import finished. Imported rows: " + imported);
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            taLog.append("Import error: " + ex.getMessage() + "\n");
            JOptionPane.showMessageDialog(this, "Import failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }

    private String[] parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean q = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') q = !q;
            else if (c == ',' && !q) { out.add(sb.toString()); sb.setLength(0); }
            else sb.append(c);
        }
        out.add(sb.toString());
        return out.toArray(new String[0]);
    }
}
