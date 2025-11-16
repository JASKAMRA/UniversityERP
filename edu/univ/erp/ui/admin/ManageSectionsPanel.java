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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ManageSectionsPanel extends JPanel {

    private static final String[] COLS = {"ID", "Section Code", "Course Code", "Term", "Capacity", "Enrolled", "Assigned Instructor"};
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField searchField;
    private final JLabel recordCount;

    // in-memory store; replace with DB-backed model
    private final List<SectionRow> sections = new ArrayList<>();
    private final List<InstructorStub> instructors = new ArrayList<>(); // to populate assign dialog

    public ManageSectionsPanel() {
        setLayout(new BorderLayout(12,12));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setBackground(Color.WHITE);

        // header + actions
        JPanel top = new JPanel(new BorderLayout(8,8));
        top.setOpaque(false);
        JLabel title = new JLabel("Manage Sections");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        top.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        searchField = new JTextField(26);
        searchField.setToolTipText("Search by section id, course code, term, instructor...");
        actions.add(searchField);

        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton delBtn = new JButton("Delete");
        JButton assignBtn = new JButton("Assign Instructor");
        JButton unassignBtn = new JButton("Unassign");
        JButton importBtn = new JButton("Import CSV");
        JButton exportBtn = new JButton("Export CSV");

        stylePrimary(addBtn);
        editBtn.setBackground(new Color(95,158,160)); editBtn.setForeground(Color.WHITE);
        delBtn.setBackground(new Color(220,80,80)); delBtn.setForeground(Color.WHITE);
        stylePrimary(assignBtn);
        unassignBtn.setBackground(new Color(200,130,50)); unassignBtn.setForeground(Color.WHITE);

        actions.add(addBtn);
        actions.add(editBtn);
        actions.add(delBtn);
        actions.add(assignBtn);
        actions.add(unassignBtn);
        actions.add(importBtn);
        actions.add(exportBtn);

        top.add(actions, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // table
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane sp = new JScrollPane(table);
        add(sp, BorderLayout.CENTER);

        // footer
        recordCount = new JLabel("0 records");
        add(recordCount, BorderLayout.SOUTH);

        // actions wiring
        addBtn.addActionListener(e -> openAddDialog());
        editBtn.addActionListener(e -> openEditDialog());
        delBtn.addActionListener(e -> deleteSelected());
        assignBtn.addActionListener(e -> openAssignDialog());
        unassignBtn.addActionListener(e -> unassignSelected());
        importBtn.addActionListener(e -> importCSV());
        exportBtn.addActionListener(e -> exportCSV());

        // search
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
            private void applyFilter() {
                String text = searchField.getText().trim();
                if (text.isEmpty()) sorter.setRowFilter(null);
                else {
                    String expr = "(?i).*" + Pattern.quote(text) + ".*";
                    sorter.setRowFilter(RowFilter.regexFilter(expr, 0,1,2,3,6)); // id,section,course,term,instructor
                }
                updateCount();
            }
        });

        // double click -> edit
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) openEditDialog();
            }
        });

        // sample data
        loadSampleData();
        refreshTable();
    }

    // ---------- CRUD and actions ----------

    private void openAddDialog() {
        SectionForm form = new SectionForm(null, instructors);
        int res = JOptionPane.showConfirmDialog(this, form.getPanel(), "Add Section", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        SectionRow s = form.toSectionRow();
        if (s == null) return;
        s.id = generateNextId();
        // TODO: persist to DB and set real id
        sections.add(s);
        refreshTable();
        JOptionPane.showMessageDialog(this, "Section added.");
    }

    private void openEditDialog() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a section to edit.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        SectionRow s = findById(id);
        if (s == null) return;

        SectionForm form = new SectionForm(s, instructors);
        int res = JOptionPane.showConfirmDialog(this, form.getPanel(), "Edit Section", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        SectionRow updated = form.toSectionRow();
        if (updated == null) return;
        // TODO: update DB
        s.sectionCode = updated.sectionCode;
        s.courseCode = updated.courseCode;
        s.term = updated.term;
        s.capacity = updated.capacity;
        s.enrolled = Math.min(updated.enrolled, updated.capacity);
        s.assignedInstructorId = updated.assignedInstructorId;
        s.assignedInstructorName = updated.assignedInstructorName;
        refreshTable();
        JOptionPane.showMessageDialog(this, "Section updated.");
    }

    private void deleteSelected() {
        int[] sel = table.getSelectedRows();
        if (sel.length == 0) {
            JOptionPane.showMessageDialog(this, "Select one or more sections to delete.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<Integer> ids = new ArrayList<>();
        for (int viewRow : sel) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            ids.add((Integer) tableModel.getValueAt(modelRow, 0));
        }
        int conf = JOptionPane.showConfirmDialog(this,
                "Delete " + ids.size() + " section(s)? This cannot be undone.",
                "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf != JOptionPane.YES_OPTION) return;
        // TODO: delete from DB
        sections.removeIf(s -> ids.contains(s.id));
        refreshTable();
        JOptionPane.showMessageDialog(this, "Deleted " + ids.size() + " section(s).");
    }

    private void openAssignDialog() {
        int[] sel = table.getSelectedRows();
        if (sel.length == 0) {
            JOptionPane.showMessageDialog(this, "Select one or more sections to assign an instructor to.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // choose instructor from list
        String[] choices = instructors.stream().map(i -> i.id + " - " + i.name).toArray(String[]::new);
        String pick = (String) JOptionPane.showInputDialog(this, "Select Instructor:", "Assign Instructor",
                JOptionPane.PLAIN_MESSAGE, null, choices, choices.length > 0 ? choices[0] : null);
        if (pick == null) return;
        int instrId = Integer.parseInt(pick.split(" - ")[0]);
        InstructorStub ins = instructors.stream().filter(i -> i.id == instrId).findFirst().orElse(null);
        if (ins == null) return;

        String[] types = new String[]{"Primary Instructor", "Co-Instructor", "Guest Lecturer"};
        String type = (String) JOptionPane.showInputDialog(this, "Assignment type:", "Assign Instructor", JOptionPane.PLAIN_MESSAGE, null, types, types[0]);
        if (type == null) return;

        // apply assignment
        List<SectionRow> selected = new ArrayList<>();
        for (int viewRow : sel) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            int sid = (Integer) tableModel.getValueAt(modelRow, 0);
            SectionRow s = findById(sid);
            if (s != null) selected.add(s);
        }

        for (SectionRow s : selected) {
            s.assignedInstructorId = ins.id;
            s.assignedInstructorName = ins.name + " (" + type + ")";
            // TODO: persist assignment to DB
        }
        refreshTable();
        JOptionPane.showMessageDialog(this, "Assigned " + ins.name + " to " + selected.size() + " section(s).");
    }

    private void unassignSelected() {
        int[] sel = table.getSelectedRows();
        if (sel.length == 0) {
            JOptionPane.showMessageDialog(this, "Select one or more sections to unassign.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int conf = JOptionPane.showConfirmDialog(this, "Remove assigned instructor from selected sections?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf != JOptionPane.YES_OPTION) return;

        List<Integer> ids = new ArrayList<>();
        for (int viewRow : sel) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            ids.add((Integer) tableModel.getValueAt(modelRow, 0));
        }
        for (SectionRow s : sections) {
            if (ids.contains(s.id)) {
                s.assignedInstructorId = 0;
                s.assignedInstructorName = "";
                // TODO: persist
            }
        }
        refreshTable();
        JOptionPane.showMessageDialog(this, "Unassigned instructor(s) from selected sections.");
    }

    // ---------- CSV import/export ----------

    private void exportCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export sections CSV");
        chooser.setSelectedFile(new File("sections_export.csv"));
        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File target = chooser.getSelectedFile();
        try (PrintWriter pw = new PrintWriter(target)) {
            pw.println("id,section_code,course_code,term,capacity,enrolled,assigned_instructor_id,assigned_instructor_name");
            for (SectionRow s : sections) {
                pw.printf("%d,%s,%s,%s,%d,%d,%d,%s%n",
                        s.id,
                        escapeCsv(s.sectionCode),
                        escapeCsv(s.courseCode),
                        escapeCsv(s.term),
                        s.capacity,
                        s.enrolled,
                        s.assignedInstructorId,
                        escapeCsv(s.assignedInstructorName));
            }
            JOptionPane.showMessageDialog(this, "Exported " + sections.size() + " sections to " + target.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import sections CSV");
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File src = chooser.getSelectedFile();
        int added = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(src))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                String[] parts = parseCsvLine(line);
                if (parts.length < 8) continue;
                SectionRow s = new SectionRow(
                        0,
                        parts[1].trim(),
                        parts[2].trim(),
                        parts[3].trim(),
                        safeParseInt(parts[4], 30),
                        safeParseInt(parts[5], 0)
                );
                s.assignedInstructorId = safeParseInt(parts[6], 0);
                s.assignedInstructorName = parts[7].trim();
                s.id = generateNextId();
                // TODO: persist
                sections.add(s);
                added++;
            }
            refreshTable();
            JOptionPane.showMessageDialog(this, "Imported " + added + " sections from CSV.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Import failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- Helpers ----------

    private void refreshTable() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (SectionRow s : sections) {
                tableModel.addRow(new Object[]{
                        s.id,
                        s.sectionCode,
                        s.courseCode,
                        s.term,
                        s.capacity,
                        s.enrolled,
                        s.assignedInstructorName == null ? "" : s.assignedInstructorName
                });
            }
            updateCount();
        });
    }

    private void updateCount() {
        recordCount.setText(table.getRowCount() + " records");
    }

    private SectionRow findById(int id) {
        return sections.stream().filter(s -> s.id == id).findFirst().orElse(null);
    }

    private int generateNextId() {
        return sections.stream().mapToInt(s -> s.id).max().orElse(2000) + 1;
    }

    private static int safeParseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private static String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private static String[] parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i+1 < line.length() && line.charAt(i+1) == '"') {
                    cur.append('"'); i++;
                } else inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else cur.append(ch);
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    // ---------- Sample data ----------

    private void loadSampleData() {
        instructors.clear();
        instructors.add(new InstructorStub(3001, "Dr. Aarti Desai", "Computer Science"));
        instructors.add(new InstructorStub(3002, "Prof. Vikram Singh", "Computer Science"));
        instructors.add(new InstructorStub(3003, "Dr. Neha Gupta", "Mathematics"));

        sections.clear();
        sections.add(new SectionRow(2101, "SEC-CS101-A", "CS101", "Fall 2025", 60, 52));
        sections.add(new SectionRow(2102, "SEC-CS201-A", "CS201", "Fall 2025", 45, 40));
        sections.add(new SectionRow(2103, "SEC-MA101-A", "MA101", "Fall 2025", 80, 76));
        sections.add(new SectionRow(2104, "SEC-EC201-A", "EC201", "Fall 2025", 50, 49));
        // pre-assign one
        sections.get(1).assignedInstructorId = 3002;
        sections.get(1).assignedInstructorName = "Prof. Vikram Singh (Primary Instructor)";
    }

    private void stylePrimary(JButton b) {
        b.setBackground(new Color(52, 152, 219));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
    }

    // ---------- Inner classes ----------

    private static class SectionRow {
        int id;
        String sectionCode;
        String courseCode;
        String term;
        int capacity;
        int enrolled;
        int assignedInstructorId;
        String assignedInstructorName;

        SectionRow(int id, String sectionCode, String courseCode, String term, int capacity, int enrolled) {
            this.id = id;
            this.sectionCode = sectionCode;
            this.courseCode = courseCode;
            this.term = term;
            this.capacity = capacity;
            this.enrolled = enrolled;
            this.assignedInstructorId = 0;
            this.assignedInstructorName = "";
        }
    }

    private static class InstructorStub {
        int id;
        String name;
        String dept;
        InstructorStub(int id, String name, String dept) { this.id = id; this.name = name; this.dept = dept; }
    }

    // Form for add/edit
    private static class SectionForm {
        private final JPanel panel;
        private final JTextField sectionCodeField = new JTextField(20);
        private final JTextField courseCodeField = new JTextField(20);
        private final JTextField termField = new JTextField(20);
        private final JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(30, 1, 500, 1));
        private final JSpinner enrolledSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 500, 1));
        private final JComboBox<String> instructorBox;

        SectionForm(SectionRow existing, List<InstructorStub> instructors) {
            panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(6,6,6,6);
            g.anchor = GridBagConstraints.WEST;

            g.gridx = 0; g.gridy = 0;
            panel.add(new JLabel("Section Code:"), g);
            g.gridx = 1;
            panel.add(sectionCodeField, g);

            g.gridx = 0; g.gridy++;
            panel.add(new JLabel("Course Code:"), g);
            g.gridx = 1;
            panel.add(courseCodeField, g);

            g.gridx = 0; g.gridy++;
            panel.add(new JLabel("Term:"), g);
            g.gridx = 1;
            panel.add(termField, g);

            g.gridx = 0; g.gridy++;
            panel.add(new JLabel("Capacity:"), g);
            g.gridx = 1;
            panel.add(capacitySpinner, g);

            g.gridx = 0; g.gridy++;
            panel.add(new JLabel("Enrolled:"), g);
            g.gridx = 1;
            panel.add(enrolledSpinner, g);

            g.gridx = 0; g.gridy++;
            panel.add(new JLabel("Assigned Instructor:"), g);
            g.gridx = 1;
            String[] choices = new String[instructors.size() + 1];
            choices[0] = ""; // none
            for (int i = 0; i < instructors.size(); i++) {
                choices[i+1] = instructors.get(i).id + " - " + instructors.get(i).name;
            }
            instructorBox = new JComboBox<>(choices);
            panel.add(instructorBox, g);

            if (existing != null) {
                sectionCodeField.setText(existing.sectionCode);
                courseCodeField.setText(existing.courseCode);
                termField.setText(existing.term);
                capacitySpinner.setValue(existing.capacity);
                enrolledSpinner.setValue(existing.enrolled);
                if (existing.assignedInstructorId != 0) {
                    String sel = existing.assignedInstructorId + " - " + existing.assignedInstructorName.split(" \\(")[0];
                    instructorBox.setSelectedItem(sel);
                }
            }
        }

        JPanel getPanel() { return panel; }

        SectionRow toSectionRow() {
            String sCode = sectionCodeField.getText().trim();
            String cCode = courseCodeField.getText().trim();
            String term = termField.getText().trim();
            int cap = (int) capacitySpinner.getValue();
            int enrolled = (int) enrolledSpinner.getValue();
            if (sCode.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Section code is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            if (cCode.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Course code is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            if (term.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Term is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            if (enrolled > cap) {
                int conf = JOptionPane.showConfirmDialog(panel, "Enrolled > capacity. Continue?", "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (conf != JOptionPane.YES_OPTION) return null;
            }
            SectionRow out = new SectionRow(0, sCode, cCode, term, cap, enrolled);
            String picked = (String) instructorBox.getSelectedItem();
            if (picked != null && !picked.trim().isEmpty()) {
                try {
                    int id = Integer.parseInt(picked.split(" - ")[0]);
                    out.assignedInstructorId = id;
                    out.assignedInstructorName = picked.split(" - ")[1];
                } catch (Exception ignored) {}
            }
            return out;
        }
    }
}
