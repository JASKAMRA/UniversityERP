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
    private JFileChooser file_c;

    private final String[] allowedTables=new String[] {
            "courses",
            "sections",
            "students", 
            "instructors", 
            "enrollments", 
            "grades", 
            "admins"
    };

    public BackupRestorePanel() {
        initUI();
    }

    private void initUI() {

        setBorder(new EmptyBorder(20, 20, 20, 20));
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        JLabel title=new JLabel("Database Backup / Restore (CSV)");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        JPanel controlPanel=new JPanel(new GridBagLayout());
        controlPanel.setBackground(new Color(245, 245, 245));
        controlPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Data Operations"));

        GridBagConstraints gbc=new GridBagConstraints();
        gbc.insets=new Insets(10 / 2, 10, 10 / 2, 10);
        gbc.anchor=GridBagConstraints.WEST;

        gbc.gridx = 0;
         gbc.gridy = 0; 
         gbc.weightx = 0;
        JLabel tableLabel = new JLabel("Select Table:");
        controlPanel.add(tableLabel, gbc);

        gbc.gridx=1; 
        gbc.gridy=0; 
        gbc.weightx=0.5; 
        gbc.fill=GridBagConstraints.HORIZONTAL;
        cbTables=new JComboBox<>(allowedTables);
        controlPanel.add(cbTables, gbc);

        gbc.gridx=2; 
        gbc.gridy=0; 
        gbc.weightx=0.5; 
        gbc.fill=GridBagConstraints.NONE;
        btnExport=new JButton("Export CSV");
        styleButton(btnExport, new Color(200, 230, 255));
        controlPanel.add(btnExport, gbc);

        gbc.gridx=3; 
        gbc.gridy=0; 
        gbc.weightx=0.5; 
        gbc.fill=GridBagConstraints.NONE;
        btnImport=new JButton("Import CSV");
        styleButton(btnImport, new Color(255, 230, 200));
        controlPanel.add(btnImport, gbc);

        gbc.gridx=4; 
        gbc.gridy=0; 
        gbc.weightx=0.5; 
        gbc.fill=GridBagConstraints.NONE;
        btnRefresh=new JButton("Refresh List");
        styleButton(btnRefresh, new Color(230, 230, 230));
        controlPanel.add(btnRefresh, gbc);

        add(controlPanel, BorderLayout.NORTH);

        taLog=new JTextArea(12, 80);
        taLog.setEditable(false);
        taLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        taLog.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Operation Log"));
        add(new JScrollPane(taLog), BorderLayout.CENTER);

        file_c=new JFileChooser();
        file_c.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));

        btnExport.addActionListener(e -> doExport());
        btnImport.addActionListener(e -> doImport());
        btnRefresh.addActionListener(e -> taLog.append("Tables: " + String.join(", ", allowedTables) + "\n"));

        taLog.append("System ready. Select a table and operation.\n");
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setMinimumSize(new Dimension(140, 30));
        btn.setPreferredSize(new Dimension(140, 30));
        btn.setBackground(bg);
        btn.setFocusPainted(false);
    }

    private void doExport() {
        String table=(String) cbTables.getSelectedItem();
        if (table==null) {
            return;
        }
        file_c.setDialogTitle("Export Table: " + table + " to CSV");
        file_c.setDialogType(JFileChooser.SAVE_DIALOG);

        file_c.setSelectedFile(new File(table + "_" + System.currentTimeMillis() / 1000 + ".csv"));

        if (file_c.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file=file_c.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file=new File(file.getAbsolutePath() + ".csv");
        }

        try (Connection connect=DBConnection.getStudentConnection()) {

            String sql = "SELECT * FROM " + table;
            taLog.append("Executing query: " + sql + "\n");
            try (PreparedStatement prepStatement = connect.prepareStatement(sql);
                 ResultSet resultSet=prepStatement.executeQuery();
                 PrintWriter printW=new PrintWriter(new FileWriter(file))) {

                ResultSetMetaData meta_d = resultSet.getMetaData();
                int cols = meta_d.getColumnCount();

                StringBuilder header=new StringBuilder();
                for (int i = 1; i <= cols; i++) {
                    header.append(meta_d.getColumnName(i));
                    if (i < cols) {
                        header.append(",");
                    }
                }
                printW.println(header);
                int rowsExported=0;
                while (resultSet.next()) {
                    StringBuilder row=new StringBuilder();
                    for (int i = 1; i <= cols; i++) {
                        Object obj=resultSet.getObject(i);
                        String cell= obj == null ? "" : obj.toString();
                        row.append(escape(cell));
                        if (i < cols) {
                            row.append(",");
                        }
                    }
                    printW.println(row);
                    rowsExported++;
                }
                taLog.append("Exported " + rowsExported + " rows from " + table + " to " + file.getName() + "\n");
                JOptionPane.showMessageDialog(this, "Export complete. Rows exported: " + rowsExported);
            }
        } 
        catch (Exception exception) {
            exception.printStackTrace();
            taLog.append("Export error: " + exception.getMessage() + "\n");
            JOptionPane.showMessageDialog(this, "Export failed: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doImport() {
        String table = (String) cbTables.getSelectedItem();
        if (table == null) {
            return;
        }
        file_c.setDialogTitle("Import CSV into Table: " + table);
        file_c.setDialogType(JFileChooser.OPEN_DIALOG);
        if (file_c.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File f=file_c.getSelectedFile();

        int confirm=JOptionPane.showConfirmDialog(this,
            "Import CSV into " + table + ".\nWARNING: This will attempt to insert rows and may fail if columns mismatch or duplicate keys exist.\nDo you want to continue?",
            "Confirm Import", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        taLog.append("Starting import from file: " + f.getName() + " into " + table + "\n");

        try (BufferedReader buffer_read=new BufferedReader(new FileReader(f));
             Connection connect=DBConnection.getStudentConnection()) {

            String header=buffer_read.readLine();
            if (header==null) { 
                JOptionPane.showMessageDialog(this, "Empty file."); 
                return; 
            }
            String[] cols=parseCsvLine(header);
            taLog.append("Detected columns: " + String.join(", ", cols) + "\n");

            StringBuilder sbCols=new StringBuilder();
            StringBuilder sbVals=new StringBuilder();
            for (int i = 0; i < cols.length; i++) {
                if (i > 0) { 
                    sbCols.append(","); 
                    sbVals.append(","); 
                }
                sbCols.append("`").append(cols[i]).append("`");
                sbVals.append("?");
            }
            String sql = "INSERT INTO " + table + " (" + sbCols + ") VALUES (" + sbVals + ")";
            taLog.append("Prepared SQL: " + sql + "\n");

            connect.setAutoCommit(false);
            try (PreparedStatement prepStatement = connect.prepareStatement(sql)) {
                String line;
                int imported = 0;
                int rowCount = 0;
                while ((line = buffer_read.readLine()) != null) {
                    rowCount++;
                    String[] val=parseCsvLine(line);

                    for (int i = 0; i < cols.length; i++) {
                        String v=i < val.length ? val[i].trim() : null;
                        if (v.isEmpty()||v == null ||v.equalsIgnoreCase("NULL")) {
                            prepStatement.setNull(i+1, Types.VARCHAR);
                        } 
                        else {
                            prepStatement.setString(i+1, v);
                        }
                    }

                    try {
                        prepStatement.executeUpdate();
                        imported++;
                    } 
                    catch (SQLException exception) {
                        taLog.append("Row " + rowCount + " failed: " + exception.getMessage().trim() + "\n");
                        if (rowCount-imported > 5) {
                            taLog.append("... additional row failures suppressed from log.\n");
                            break;
                        }
                    }
                }
                connect.commit();
                taLog.append("Import finished. Total rows read: " + rowCount + ". Rows imported: " + imported + ".\n");
                JOptionPane.showMessageDialog(this, "Import finished. Imported rows: " + imported, "Import Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException exception) {
                connect.rollback();
                throw exception;
            } 
            finally {
                connect.setAutoCommit(true);
            }

        } 
        catch (Exception exception) {
            exception.printStackTrace();
            taLog.append("Fatal import error: " + exception.getMessage() + "\n");
            JOptionPane.showMessageDialog(this, "Import failed: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private String escape(String s) {
        if (s == null) {
            return "";
        }
        if ( s.contains("\"") || s.contains(",")  || s.contains("\r") || s.contains("\n") ) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private String[] parseCsvLine(String line) {

        List<String> out=new ArrayList<>();
        StringBuilder sb=new StringBuilder();
        boolean q=false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if ( line.charAt(i+1) == '"' && i + 1 < line.length()  && q) {
                    sb.append('"');
                    i++;
                } 
                else {
                    q = !q;
                }
            } 
            else if (!q && c == ',') {
                out.add(sb.toString().trim());
                sb.setLength(0);
            } 
            else {
                sb.append(c);
            }
        }
        out.add(sb.toString().trim());
        return out.toArray(new String[0]);
    }
}
