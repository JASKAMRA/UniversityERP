package edu.univ.erp.ui.admin;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ManageCoursesPanel
 * - Shows courses in a table
 * - Search / filter by code/title/department
 * - Add / Edit / Delete
 * - Export / Import CSV (simple)
 *
 * Replace TODO sections with real DB calls.
 */
public class ManageCoursesPanel extends JPanel {

    private static final String[] COLS = {"ID", "Course Code", "Title", "Department", "Credits", "Status"};
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField searchField;
    private final JLabel recordCount;

    // in-memory list; replace with DB-backed list
    private final List<CourseRow> courses = new ArrayList<>();

    public ManageCoursesPanel() {
        setLayout(new BorderLayout(12,12));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setBackground(Color.WHITE);

        // Top header + actions
        JPanel top = new JPanel(new BorderLayout(8,8));
        top.setOpaque(false);
        JLabel title = new JLabel("Manage Courses");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        top.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8,0));
        actions.setOpaque(false);

        searchField = new JTextField(24);
        searchField.setToolTipText("Search by code, title or department...");
        actions.add(searchField);

        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton delBtn = new JButton("Delete");
        JButton importBtn = new JButton("Import CSV");
        JButton exportBtn = new JButton("Export CSV");

        stylePrimary(addBtn);
        editBtn.setBackground(new Color(95, 158, 160)); editBtn.setForeground(Color.WHITE);
        delBtn.setBackground(new Color(220,80,80)); delBtn.setForeground(Color.WHITE);

        actions.add(addBtn);
        actions.add(editBtn);
        actions.add(delBtn);
        actions.add(importBtn);
        actions.add(exportBtn);

        top.add(actions, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane sp = new JScrollPane(table);
        add(sp, BorderLayout.CENTER);

        // Footer count
        recordCount = new JLabel("0 records");
        add(recordCount, BorderLayout.SOUTH);

        // Wire events
        addBtn.addActionListener(e -> openAddDialog());
        editBtn.addActionListener(e -> openEditDialog());
        delBtn.addActionListener(e -> deleteSelected());
        importBtn.addActionListener(e -> importCSV());
        exportBtn.addActionListener(e -> exportCSV());

        // search filter
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
            private void applyFilter() {
                String text = searchField.getText().trim();
                if (text.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    String expr = "(?i).*" + Pattern.quote(text) + ".*";
                    sorter.setRowFilter(RowFilter.regexFilter(expr, 1, 2, 3)); // code, title, dept
                }
                updateCount();
            }
        });

        // double-click to edit
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount()==2 && table.getSelectedRow() != -1) openEditDialog();
            }
        });

        // sample data
        loadSampleData();
        refreshTable();
    }

    // ---------- CRUD UI actions ----------
    private void openAddDialog() {
        CourseForm form = new CourseForm(null);
        int res = JOptionPane.showConfirmDialog(this, form.getPanel(), "Add Course", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            CourseRow newC = form.toCourseRow();
            if (newC == null) return; // validation failed
            newC.id = generateNextId();
            // TODO: persist to DB and get real ID
            courses.add(newC);
            refreshTable();
            JOptionPane.showMessageDialog(this, "Course added.");
        }
    }

    private void openEditDialog() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a course to edit.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        CourseRow c = findById(id);
        if (c == null) return;

        CourseForm form = new CourseForm(c);
        int res = JOptionPane.showConfirmDialog(this, form.getPanel(), "Edit Course", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            CourseRow updated = form.toCourseRow();
            if (updated == null) return;
            // TODO: update DB
            c.courseCode = updated.courseCode;
            c.title = updated.title;
            c.department = updated.department;
            c.credits = updated.credits;
            c.status = updated.status;
            refreshTable();
            JOptionPane.showMessageDialog(this, "Course updated.");
        }
    }

    private void deleteSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a course to delete.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        CourseRow c = findById(id);
        if (c == null) return;

        int conf = JOptionPane.showConfirmDialog(this,
                "Delete course '" + c.courseCode + " - " + c.title + "'?",
                "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf == JOptionPane.YES_OPTION) {
            // TODO: delete from DB
            courses.removeIf(x -> x.id == id);
            refreshTable();
            JOptionPane.showMessageDialog(this, "Course deleted.");
        }
    }

    // ---------- CSV import/export ----------
    private void exportCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export courses CSV");
        chooser.setSelectedFile(new File("courses_export.csv"));
        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File target = chooser.getSelectedFile();
        try (PrintWriter pw = new PrintWriter(target)) {
            pw.println("id,course_code,title,department,credits,status");
            for (CourseRow c : courses) {
                pw.printf("%d,%s,%s,%s,%d,%s%n",
                        c.id,
                        escapeCsv(c.courseCode),
                        escapeCsv(c.title),
                        escapeCsv(c.department),
                        c.credits,
                        escapeCsv(c.status));
            }
            JOptionPane.showMessageDialog(this, "Exported " + courses.size() + " courses to " + target.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import courses CSV");
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File src = chooser.getSelectedFile();
        int added = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(src))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                String[] parts = parseCsvLine(line);
                if (parts.length < 6) continue;
                // simple parsing (id may be present but we assign new ids)
                CourseRow c = new CourseRow(0,
                        parts[1].trim(),
                        parts[2].trim(),
                        parts[3].trim(),
                        safeParseInt(parts[4], 3),
                        parts[5].trim());
                c.id = generateNextId();
                // TODO: persist to DB
                courses.add(c);
                added++;
            }
            refreshTable();
            JOptionPane.showMessageDialog(this, "Imported " + added + " courses from CSV.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Import failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- Helpers ----------
    private void refreshTable() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (CourseRow c : courses) {
                tableModel.addRow(new Object[]{c.id, c.courseCode, c.title, c.department, c.credits, c.status});
            }
            updateCount();
        });
    }

    private void updateCount() {
        recordCount.setText(table.getRowCount() + " records");
    }

    private CourseRow findById(int id) {
        return courses.stream().filter(c -> c.id == id).findFirst().orElse(null);
    }

    private int generateNextId() {
        return courses.stream().mapToInt(c -> c.id).max().orElse(5000) + 1;
    }

    private static int safeParseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    // small CSV helpers (very simple; not RFC perfect)
    private static String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private static String[] parseCsvLine(String line) {
        // naive CSV parsing for our simple exported format
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"' ) {
                if (inQuotes && i+1 < line.length() && line.charAt(i+1) == '"') {
                    cur.append('"'); i++; // escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    // ---------- Sample data ----------
    private void loadSampleData() {
        courses.clear();
        courses.add(new CourseRow(5001, "CS101", "Intro to Computer Science", "Computer Science", 4, "Active"));
        courses.add(new CourseRow(5002, "CS201", "Data Structures", "Computer Science", 3, "Active"));
        courses.add(new CourseRow(5003, "MA101", "Calculus I", "Mathematics", 4, "Active"));
        courses.add(new CourseRow(5004, "EC201", "Microeconomics", "Economics", 3, "Inactive"));
    }

    // ---------- UI styling helper ----------
    private void stylePrimary(JButton b) {
        b.setBackground(new Color(52, 152, 219));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
    }

    // ---------- Inner classes ----------
    private static class CourseRow {
        int id;
        String courseCode;
        String title;
        String department;
        int credits;
        String status;

        CourseRow(int id, String courseCode, String title, String department, int credits, String status) {
            this.id = id;
            this.courseCode = courseCode;
            this.title = title;
            this.department = department;
            this.credits = credits;
            this.status = status;
        }
    }

    // Add/Edit form
    private static class CourseForm {
        private final JPanel panel;
        private final JTextField codeField = new JTextField(20);
        private final JTextField titleField = new JTextField(30);
        private final JTextField deptField = new JTextField(20);
        private final JSpinner creditsSpinner = new JSpinner(new SpinnerNumberModel(3, 0, 10, 1));
        private final JComboBox<String> statusBox = new JComboBox<>(new String[]{"Active", "Inactive"});

        CourseForm(CourseRow existing) {
            panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(6,6,6,6);
            g.anchor = GridBagConstraints.WEST;

            g.gridx=0; g.gridy=0;
            panel.add(new JLabel("Course Code:"), g);
            g.gridx=1;
            panel.add(codeField, g);

            g.gridx=0; g.gridy++;
            panel.add(new JLabel("Title:"), g);
            g.gridx=1;
            panel.add(titleField, g);

            g.gridx=0; g.gridy++;
            panel.add(new JLabel("Department:"), g);
            g.gridx=1;
            panel.add(deptField, g);

            g.gridx=0; g.gridy++;
            panel.add(new JLabel("Credits:"), g);
            g.gridx=1;
            panel.add(creditsSpinner, g);

            g.gridx=0; g.gridy++;
            panel.add(new JLabel("Status:"), g);
            g.gridx=1;
            panel.add(statusBox, g);

            if (existing != null) {
                codeField.setText(existing.courseCode);
                titleField.setText(existing.title);
                deptField.setText(existing.department);
                creditsSpinner.setValue(existing.credits);
                statusBox.setSelectedItem(existing.status);
            }
        }

        JPanel getPanel() { return panel; }

        CourseRow toCourseRow() {
            String code = codeField.getText().trim();
            String title = titleField.getText().trim();
            String dept = deptField.getText().trim();
            int credits = (int) creditsSpinner.getValue();
            String status = (String) statusBox.getSelectedItem();

            if (code.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Course code is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Title is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            if (dept.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Department is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            // id = 0 placeholder; caller will set
            return new CourseRow(0, code, title, dept, credits, status);
        }
    }
}
