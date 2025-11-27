package edu.univ.erp.ui.admin;

import edu.univ.erp.data.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BackupRestorePanel extends JPanel {
    private JComboBox<String> cbTables;
    private JButton btnExport, btnImport, btnRefresh;
    private JTextArea taLog;
    private JFileChooser fc;

    private final String[] allowedTables = new String[] {
            "courses", "sections", "students", "instructors", "enrollments", "grades", "admins"
    };

    private static final int PADDING = 20;
    private static final int GAP = 10;
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 20);
    private static final Dimension BUTTON_SIZE = new Dimension(140, 30);

    public BackupRestorePanel() {
        initUI();
    }

    private void initUI() {

        setLayout(new BorderLayout(GAP, GAP));
        setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Database Backup / Restore (CSV)");
        title.setFont(TITLE_FONT);
        title.setBorder(new EmptyBorder(0, 0, GAP, 0));
        add(title, BorderLayout.NORTH);

        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBackground(new Color(245, 245, 245));
        controlPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Data Operations"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(GAP / 2, GAP, GAP / 2, GAP);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        JLabel tableLabel = new JLabel("Select Table:");
        controlPanel.add(tableLabel, c);

        c.gridx = 1; c.gridy = 0; c.weightx = 0.5; c.fill = GridBagConstraints.HORIZONTAL;
        cbTables = new JComboBox<>(allowedTables);
        controlPanel.add(cbTables, c);

        c.gridx = 2; c.gridy = 0; c.weightx = 0.5; c.fill = GridBagConstraints.NONE;
        btnExport = new JButton("Export CSV");
        styleButton(btnExport, new Color(200, 230, 255));
        controlPanel.add(btnExport, c);

        c.gridx = 3; c.gridy = 0; c.weightx = 0.5; c.fill = GridBagConstraints.NONE;
        btnImport = new JButton("Import CSV");
        styleButton(btnImport, new Color(255, 230, 200));
        controlPanel.add(btnImport, c);

        c.gridx = 4; c.gridy = 0; c.weightx = 0.5; c.fill = GridBagConstraints.NONE;
        btnRefresh = new JButton("Refresh List");
        styleButton(btnRefresh, new Color(230, 230, 230));
        controlPanel.add(btnRefresh, c);

        add(controlPanel, BorderLayout.NORTH);

        taLog = new JTextArea(12, 80);
        taLog.setEditable(false);
        taLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        taLog.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Operation Log"));
        add(new JScrollPane(taLog), BorderLayout.CENTER);

        fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));

        btnExport.addActionListener(e -> doExport());
        btnImport.addActionListener(e -> doImport());
        btnRefresh.addActionListener(e -> taLog.append("Tables: " + String.join(", ", allowedTables) + "\n"));

        taLog.append("System ready. Select a table and operation.\n");
    }

    private void styleButton(JButton button, Color background) {
        button.setPreferredSize(BUTTON_SIZE);
        button.setMinimumSize(BUTTON_SIZE);
        button.setFocusPainted(false);
        button.setBackground(background);
    }

    private void doExport() {
        String table = (String) cbTables.getSelectedItem();
        if (table == null) return;
        fc.setDialogTitle("Export Table: " + table + " to CSV");
        fc.setDialogType(JFileChooser.SAVE_DIALOG);

        fc.setSelectedFile(new File(table + "_" + System.currentTimeMillis() / 1000 + ".csv"));

        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".csv")) f = new File(f.getAbsolutePath() + ".csv");

        try (Connection conn = DBConnection.getStudentConnection()) {

            String sql = "SELECT * FROM " + table;
            taLog.append("Executing query: " + sql + "\n");
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery();
                 PrintWriter pw = new PrintWriter(new FileWriter(f))) {

                ResultSetMetaData md = rs.getMetaData();
                int cols = md.getColumnCount();

                StringBuilder header = new StringBuilder();
                for (int i = 1; i <= cols; i++) {
                    header.append(md.getColumnName(i));
                    if (i < cols) header.append(",");
                }
                pw.println(header);

                int rowsExported = 0;
                while (rs.next()) {
                    StringBuilder row = new StringBuilder();
                    for (int i = 1; i <= cols; i++) {
                        Object o = rs.getObject(i);
                        String cell = o == null ? "" : o.toString();
                        row.append(escape(cell));
                        if (i < cols) row.append(",");
                    }
                    pw.println(row);
                    rowsExported++;
                }
                taLog.append("Exported " + rowsExported + " rows from " + table + " to " + f.getName() + "\n");
                JOptionPane.showMessageDialog(this, "Export complete. Rows exported: " + rowsExported);
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
        fc.setDialogTitle("Import CSV into Table: " + table);
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File f = fc.getSelectedFile();

        int confirm = JOptionPane.showConfirmDialog(this,
            "Import CSV into " + table + ".\nWARNING: This will attempt to insert rows and may fail if columns mismatch or duplicate keys exist.\nDo you want to continue?",
            "Confirm Import", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;
        taLog.append("Starting import from file: " + f.getName() + " into " + table + "\n");

        try (BufferedReader br = new BufferedReader(new FileReader(f));
             Connection conn = DBConnection.getStudentConnection()) {

            String header = br.readLine();
            if (header == null) { JOptionPane.showMessageDialog(this, "Empty file."); return; }
            String[] cols = parseCsvLine(header);
            taLog.append("Detected columns: " + String.join(", ", cols) + "\n");

            StringBuilder sbCols = new StringBuilder();
            StringBuilder sbVals = new StringBuilder();
            for (int i = 0; i < cols.length; i++) {
                if (i > 0) { sbCols.append(","); sbVals.append(","); }
                sbCols.append("`").append(cols[i]).append("`");
                sbVals.append("?");
            }
            String sql = "INSERT INTO " + table + " (" + sbCols + ") VALUES (" + sbVals + ")";
            taLog.append("Prepared SQL: " + sql + "\n");

            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                String line;
                int imported = 0;
                int rowCount = 0;
                while ((line = br.readLine()) != null) {
                    rowCount++;
                    String[] vals = parseCsvLine(line);

                    // Bind values
                    for (int i = 0; i < cols.length; i++) {
                        String v = i < vals.length ? vals[i].trim() : null;
                        if (v == null || v.isEmpty() || v.equalsIgnoreCase("NULL")) {
                            // Default to VARCHAR type for simplicity when setting NULL
                            ps.setNull(i+1, Types.VARCHAR);
                        } else {
                            ps.setString(i+1, v);
                        }
                    }

                    try {
                        ps.executeUpdate();
                        imported++;
                    } catch (SQLException ex) {
                        taLog.append("Row " + rowCount + " failed: " + ex.getMessage().trim() + "\n");
                        if (rowCount - imported > 5) {
                            taLog.append("... additional row failures suppressed from log.\n");
                            break;
                        }
                    }
                }
                conn.commit();
                taLog.append("Import finished. Total rows read: " + rowCount + ". Rows imported: " + imported + ".\n");
                JOptionPane.showMessageDialog(this, "Import finished. Imported rows: " + imported, "Import Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            taLog.append("Fatal import error: " + ex.getMessage() + "\n");
            JOptionPane.showMessageDialog(this, "Import failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String escape(String s) {
        if (s == null) return "";

        if (s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private String[] parseCsvLine(String line) {

        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean q = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (i + 1 < line.length() && line.charAt(i+1) == '"' && q) {
                    sb.append('"');
                    i++;
                } else {
                    q = !q;
                }
            } else if (c == ',' && !q) {
                out.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        out.add(sb.toString().trim());
        return out.toArray(new String[0]);
    }
}
