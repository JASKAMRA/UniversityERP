package edu.univ.erp.ui.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * ReportsPanel
 *
 * Features:
 * - Report type selector (Enrollment, Registrations, Revenue, Attendance, Custom)
 * - Date range pickers (JSpinner with Date model)
 * - Filters (department, course, instructor) with live search
 * - Generate button runs a SwingWorker and fills table with sample data
 * - Export CSV of shown table
 *
 * Replace TODO blocks with real DB/report-service calls.
 */
public class ReportsPanel extends JPanel {

    private final JComboBox<String> reportTypeBox;
    private final JSpinner fromDateSpinner;
    private final JSpinner toDateSpinner;
    private final JTextField filterCourse;
    private final JTextField filterDept;
    private final JTextField filterInstructor;

    private final JButton generateBtn;
    private final JButton exportCsvBtn;
    private final JButton clearBtn;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JLabel statusLabel;

    private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    public ReportsPanel() {
        setLayout(new BorderLayout(12,12));
        setBorder(new EmptyBorder(12,12,12,12));
        setBackground(Color.WHITE);

        // Header
        JLabel title = new JLabel("Reports");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        // Controls panel (top)
        JPanel controls = new JPanel(new GridBagLayout());
        controls.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        // Report types
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        controls.add(new JLabel("Report:"), g);
        reportTypeBox = new JComboBox<>(new String[]{
                "Enrollment Summary", "Registrations", "Revenue (fees)", "Attendance Summary", "Custom Query"
        });
        g.gridx = 1; g.weightx = 1.0;
        controls.add(reportTypeBox, g);

        // Date range
        g.gridx = 0; g.gridy++;
        controls.add(new JLabel("From:"), g);
        fromDateSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        fromDateSpinner.setEditor(new JSpinner.DateEditor(fromDateSpinner, "yyyy-MM-dd"));
        g.gridx = 1;
        controls.add(fromDateSpinner, g);

        g.gridx = 0; g.gridy++;
        controls.add(new JLabel("To:"), g);
        toDateSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        toDateSpinner.setEditor(new JSpinner.DateEditor(toDateSpinner, "yyyy-MM-dd"));
        g.gridx = 1;
        controls.add(toDateSpinner, g);

        // Filters (course, dept, instructor)
        g.gridx = 0; g.gridy++;
        controls.add(new JLabel("Course:"), g);
        filterCourse = new JTextField(20);
        filterCourse.setToolTipText("Filter by course code or title");
        g.gridx = 1;
        controls.add(filterCourse, g);

        g.gridx = 0; g.gridy++;
        controls.add(new JLabel("Department:"), g);
        filterDept = new JTextField(20);
        g.gridx = 1;
        controls.add(filterDept, g);

        g.gridx = 0; g.gridy++;
        controls.add(new JLabel("Instructor:"), g);
        filterInstructor = new JTextField(20);
        g.gridx = 1;
        controls.add(filterInstructor, g);

        // Buttons row
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        generateBtn = new JButton("Generate");
        exportCsvBtn = new JButton("Export CSV");
        clearBtn = new JButton("Clear Filters");

        stylePrimary(generateBtn);
        exportCsvBtn.setBackground(new Color(80,160,120)); exportCsvBtn.setForeground(Color.WHITE);
        clearBtn.setBackground(new Color(200,120,80)); clearBtn.setForeground(Color.WHITE);

        btnRow.add(clearBtn);
        btnRow.add(exportCsvBtn);
        btnRow.add(generateBtn);

        g.gridx = 0; g.gridy++; g.gridwidth = 2; g.weightx = 1.0;
        controls.add(btnRow, g);
        g.gridwidth = 1;

        add(controls, BorderLayout.WEST);

        // Table (center)
        String[] cols = {"Col1","Col2","Col3","Col4","Col5"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        table.setFillsViewportHeight(true);
        table.setRowHeight(26);

        JScrollPane sp = new JScrollPane(table);
        add(sp, BorderLayout.CENTER);

        // Status bar bottom
        JPanel bottom = new JPanel(new BorderLayout(8,8));
        bottom.setOpaque(false);
        statusLabel = new JLabel("Ready");
        bottom.add(statusLabel, BorderLayout.WEST);
        add(bottom, BorderLayout.SOUTH);

        // Wire actions
        generateBtn.addActionListener(this::onGenerate);
        exportCsvBtn.addActionListener(e -> exportCsv());
        clearBtn.addActionListener(e -> clearFilters());

        // live-search helpers: pressing Enter on a filter triggers generate
        DocumentListener genOnChange = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { /* no */ }
            public void removeUpdate(DocumentEvent e) { /* no */ }
            public void changedUpdate(DocumentEvent e) { /* no */ }
        };
        filterCourse.getDocument().addDocumentListener(genOnChange);
        filterDept.getDocument().addDocumentListener(genOnChange);
        filterInstructor.getDocument().addDocumentListener(genOnChange);

        // populate initial sample report
        generateSampleReport();
    }

    // ---------------- Actions ----------------

    private void onGenerate(ActionEvent ev) {
        Date from = (Date) fromDateSpinner.getValue();
        Date to = (Date) toDateSpinner.getValue();
        if (from.after(to)) {
            JOptionPane.showMessageDialog(this, "'From' date must be before or equal to 'To' date.", "Invalid range", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String reportType = (String) reportTypeBox.getSelectedItem();
        String course = filterCourse.getText().trim();
        String dept = filterDept.getText().trim();
        String instr = filterInstructor.getText().trim();

        // disable UI while generating
        setControlsEnabled(false);
        status("Generating " + reportType + " (" + df.format(from) + " → " + df.format(to) + ") ...");

        // Use SwingWorker to avoid freezing UI
        SwingWorker<List<String[]>, Void> worker = new SwingWorker<List<String[]>, Void>() {
            @Override
            protected List<String[]> doInBackground() throws Exception {
                // Simulated delay; replace with DB/query logic
                Thread.sleep(600);

                // TODO: replace this switch with actual DB queries that return rows
                List<String[]> rows = new ArrayList<>();
                if ("Enrollment Summary".equals(reportType)) {
                    rows.add(new String[]{"Course", "Term", "Capacity", "Enrolled", "Utilization"});
                    rows.add(new String[]{"CS101", "Fall 2025", "60", "52", "86.7%"});
                    rows.add(new String[]{"CS201", "Fall 2025", "45", "40", "88.9%"});
                    rows.add(new String[]{"MA101", "Fall 2025", "80", "76", "95.0%"});
                } else if ("Registrations".equals(reportType)) {
                    rows.add(new String[]{"RegistrationID", "Student", "Course", "Date", "Status"});
                    rows.add(new String[]{"R-1001", "Sana Khan", "CS101", df.format(new Date()), "Completed"});
                    rows.add(new String[]{"R-1002", "Vikram Iyer", "CS201", df.format(new Date()), "Pending"});
                } else if ("Revenue (fees)".equals(reportType)) {
                    rows.add(new String[]{"Date", "Source", "Amount", "Payment Method", "Receipt"});
                    rows.add(new String[]{df.format(new Date()), "Course Fee - CS101", "₹12,000", "Online", "REC-2001"});
                    rows.add(new String[]{df.format(new Date()), "Late Fee", "₹200", "Offline", "REC-2002"});
                } else if ("Attendance Summary".equals(reportType)) {
                    rows.add(new String[]{"Course", "Section", "Sessions", "Avg Attendance"});
                    rows.add(new String[]{"CS101", "A", "12", "85%"});
                    rows.add(new String[]{"CS201", "A", "10", "80%"});
                } else { // Custom Query
                    rows.add(new String[]{"Result", "Col2", "Col3"});
                    rows.add(new String[]{"Custom row 1", "-", "-"});
                }

                // Apply simple filters (client-side sample filtering)
                if (!course.isEmpty()) {
                    rows.removeIf(r -> r.length > 0 && !r[0].toLowerCase().contains(course.toLowerCase()));
                }
                if (!dept.isEmpty()) {
                    rows.removeIf(r -> r.length > 1 && !r[1].toLowerCase().contains(dept.toLowerCase()));
                }
                if (!instr.isEmpty()) {
                    rows.removeIf(r -> r.length > 1 && !r[1].toLowerCase().contains(instr.toLowerCase()));
                }

                return rows;
            }

            @Override
            protected void done() {
                try {
                    List<String[]> rows = get();
                    populateTable(rows);
                    status("Report generated: " + rows.size() + " rows (including header if present).");
                } catch (Exception ex) {
                    status("Failed to generate report: " + ex.getMessage());
                    JOptionPane.showMessageDialog(ReportsPanel.this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    setControlsEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void exportCsv() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data to export.", "Empty", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export report CSV");
        chooser.setSelectedFile(new File("report_export.csv"));
        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        try (PrintWriter pw = new PrintWriter(f)) {
            // header
            int cols = tableModel.getColumnCount();
            for (int c = 0; c < cols; c++) {
                if (c > 0) pw.print(",");
                pw.print(escapeCsv(tableModel.getColumnName(c)));
            }
            pw.println();
            // rows
            for (int r = 0; r < tableModel.getRowCount(); r++) {
                for (int c = 0; c < cols; c++) {
                    if (c > 0) pw.print(",");
                    Object val = tableModel.getValueAt(r, c);
                    pw.print(escapeCsv(val == null ? "" : val.toString()));
                }
                pw.println();
            }
            JOptionPane.showMessageDialog(this, "Report exported to " + f.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFilters() {
        filterCourse.setText("");
        filterDept.setText("");
        filterInstructor.setText("");
        reportTypeBox.setSelectedIndex(0);
        fromDateSpinner.setValue(new Date());
        toDateSpinner.setValue(new Date());
        generateSampleReport();
        status("Filters cleared.");
    }

    // ---------------- UI helpers ----------------

    private void populateTable(List<String[]> rows) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            if (rows == null || rows.isEmpty()) return;
            // If first row looks like header (non-numeric or contains letters and there are >=2 columns), use it as header
            String[] first = rows.get(0);
            boolean useAsHeader = rows.size() > 1 && looksLikeHeader(first);
            if (useAsHeader) {
                // set columns to header values
                String[] hdr = first;
                // adjust model columns
                tableModel.setColumnCount(hdr.length);
                for (int i = 0; i < hdr.length; i++) tableModel.setColumnIdentifiers(hdr);
                // add remaining rows
                for (int i = 1; i < rows.size(); i++) tableModel.addRow(rows.get(i));
            } else {
                // use default columns or expand to required count
                int cols = Math.max(tableModel.getColumnCount(), first.length);
                tableModel.setColumnCount(cols);
                for (String[] r : rows) {
                    // ensure row length matches model
                    Object[] vals = new Object[cols];
                    for (int i = 0; i < cols; i++) vals[i] = i < r.length ? r[i] : "";
                    tableModel.addRow(vals);
                }
            }
        });
    }

    private boolean looksLikeHeader(String[] row) {
        int letters = 0;
        for (String s : row) {
            if (s == null) continue;
            if (s.matches(".*[A-Za-z].*")) letters++;
        }
        return letters >= Math.max(1, row.length / 2);
    }

    private void setControlsEnabled(boolean enabled) {
        generateBtn.setEnabled(enabled);
        exportCsvBtn.setEnabled(enabled);
        clearBtn.setEnabled(enabled);
        reportTypeBox.setEnabled(enabled);
        fromDateSpinner.setEnabled(enabled);
        toDateSpinner.setEnabled(enabled);
        filterCourse.setEnabled(enabled);
        filterDept.setEnabled(enabled);
        filterInstructor.setEnabled(enabled);
    }

    private void status(String s) {
        statusLabel.setText(s);
    }

    private void generateSampleReport() {
        // default initial sample
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Course", "Term", "Capacity", "Enrolled", "Utilization"});
        rows.add(new String[]{"CS101", "Fall 2025", "60", "52", "86.7%"});
        rows.add(new String[]{"CS201", "Fall 2025", "45", "40", "88.9%"});
        populateTable(rows);
    }

    private static String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private static String sanitizeFileName(String s) {
        return s.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
    }

    private void stylePrimary(JButton b) {
        b.setBackground(new Color(52, 152, 219));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
    }
}
