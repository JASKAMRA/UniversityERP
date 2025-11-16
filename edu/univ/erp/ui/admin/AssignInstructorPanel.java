package edu.univ.erp.ui.admin;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AssignInstructorPanel
 *
 * UI to assign instructors to sections:
 * - Left: Sections table (select one or multiple)
 * - Right: Instructors table (select one)
 * - Assign / Unassign / View assignments
 *
 * Replace TODO sections with DB calls to persist assignments.
 */
public class AssignInstructorPanel extends JPanel {

    private final DefaultTableModel sectionsModel;
    private final JTable sectionsTable;
    private final TableRowSorter<DefaultTableModel> sectionsSorter;
    private final JTextField sectionsSearch;

    private final DefaultTableModel instructorsModel;
    private final JTable instructorsTable;
    private final TableRowSorter<DefaultTableModel> instructorsSorter;
    private final JTextField instructorsSearch;

    // In-memory sample lists; replace with DB/service calls
    private final List<SectionRow> sections = new ArrayList<>();
    private final List<InstructorRow> instructors = new ArrayList<>();

    // columns
    private static final String[] SECTION_COLS = {"ID", "Course", "Term", "Assigned Instructor"};
    private static final String[] INSTRUCTOR_COLS = {"ID", "Name", "Email", "Dept"};

    public AssignInstructorPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Color.WHITE);

        // Header
        JLabel title = new JLabel("Assign Instructor to Sections");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        // Split pane - left: sections, right: instructors
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.6);
        split.setDividerLocation(800);
        split.setBorder(null);
        split.setBackground(Color.WHITE);

        // -- Sections panel
        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setBackground(Color.WHITE);

        JPanel leftTop = new JPanel(new BorderLayout(8, 8));
        leftTop.setOpaque(false);
        sectionsSearch = new JTextField(20);
        sectionsSearch.setToolTipText("Search sections by ID/course/term/assigned instructor...");
        leftTop.add(new JLabel("Sections"), BorderLayout.WEST);
        leftTop.add(sectionsSearch, BorderLayout.EAST);
        left.add(leftTop, BorderLayout.NORTH);

        sectionsModel = new DefaultTableModel(SECTION_COLS, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        sectionsTable = new JTable(sectionsModel);
        sectionsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        sectionsTable.setRowHeight(26);
        sectionsTable.setAutoCreateRowSorter(true);
        sectionsSorter = new TableRowSorter<>(sectionsModel);
        sectionsTable.setRowSorter(sectionsSorter);

        JScrollPane leftScroll = new JScrollPane(sectionsTable);
        left.add(leftScroll, BorderLayout.CENTER);

        JPanel leftBottomBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        leftBottomBtns.setOpaque(false);
        JButton viewAssignmentsBtn = new JButton("View Assignments");
        JButton refreshSectionsBtn = new JButton("Refresh Sections");
        leftBottomBtns.add(viewAssignmentsBtn);
        leftBottomBtns.add(refreshSectionsBtn);
        left.add(leftBottomBtns, BorderLayout.SOUTH);

        // -- Instructors panel
        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setBackground(Color.WHITE);

        JPanel rightTop = new JPanel(new BorderLayout(8, 8));
        rightTop.setOpaque(false);
        instructorsSearch = new JTextField(20);
        instructorsSearch.setToolTipText("Search instructors by name/email/department...");
        rightTop.add(new JLabel("Instructors"), BorderLayout.WEST);
        rightTop.add(instructorsSearch, BorderLayout.EAST);
        right.add(rightTop, BorderLayout.NORTH);

        instructorsModel = new DefaultTableModel(INSTRUCTOR_COLS, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        instructorsTable = new JTable(instructorsModel);
        instructorsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        instructorsTable.setRowHeight(26);
        instructorsTable.setAutoCreateRowSorter(true);
        instructorsSorter = new TableRowSorter<>(instructorsModel);
        instructorsTable.setRowSorter(instructorsSorter);

        JScrollPane rightScroll = new JScrollPane(instructorsTable);
        right.add(rightScroll, BorderLayout.CENTER);

        JPanel rightBottomBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        rightBottomBtns.setOpaque(false);
        JButton assignBtn = new JButton("Assign →");
        JButton unassignBtn = new JButton("Unassign");
        JButton refreshInstructorsBtn = new JButton("Refresh Instructors");

        stylePrimary(assignBtn);
        unassignBtn.setBackground(new Color(230, 80, 80));
        unassignBtn.setForeground(Color.WHITE);

        rightBottomBtns.add(unassignBtn);
        rightBottomBtns.add(assignBtn);
        rightBottomBtns.add(refreshInstructorsBtn);
        right.add(rightBottomBtns, BorderLayout.SOUTH);

        split.setLeftComponent(left);
        split.setRightComponent(right);

        add(split, BorderLayout.CENTER);

        // Wiring search filters
        setupSearchFiltering();

        // Button actions
        assignBtn.addActionListener(e -> onAssignClicked());
        unassignBtn.addActionListener(e -> onUnassignClicked());
        viewAssignmentsBtn.addActionListener(e -> showAssignmentsDialog());
        refreshSectionsBtn.addActionListener(e -> refreshSections());
        refreshInstructorsBtn.addActionListener(e -> refreshInstructors());

        // Double-click instructor to assign to selected sections
        instructorsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onAssignClicked();
                }
            }
        });

        // load sample data & refresh
        loadSampleData();
        refreshSections();
        refreshInstructors();
    }

   private void setupSearchFiltering() {
    sectionsSearch.getDocument().addDocumentListener(new DocumentListener() {
        private void apply() {
            String text = sectionsSearch.getText().trim();
            if (text.isEmpty()) {
                sectionsSorter.setRowFilter(null);
            } else {
                try {
                    sectionsSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
                } catch (java.util.regex.PatternSyntaxException ex) {
                    sectionsSorter.setRowFilter(null);
                }
            }
        }
        public void insertUpdate(DocumentEvent e) { apply(); }
        public void removeUpdate(DocumentEvent e) { apply(); }
        public void changedUpdate(DocumentEvent e) { apply(); }
    });

    instructorsSearch.getDocument().addDocumentListener(new DocumentListener() {
        private void apply() {
            String text = instructorsSearch.getText().trim();
            if (text.isEmpty()) {
                instructorsSorter.setRowFilter(null);
            } else {
                try {
                    instructorsSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
                } catch (java.util.regex.PatternSyntaxException ex) {
                    instructorsSorter.setRowFilter(null);
                }
            }
        }
        public void insertUpdate(DocumentEvent e) { apply(); }
        public void removeUpdate(DocumentEvent e) { apply(); }
        public void changedUpdate(DocumentEvent e) { apply(); }
    });
}


    private void onAssignClicked() {
        int[] selectedSectionRows = sectionsTable.getSelectedRows();
        int selectedInstructorRow = instructorsTable.getSelectedRow();

        if (selectedSectionRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Select at least one section to assign.", "No section selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedInstructorRow == -1) {
            JOptionPane.showMessageDialog(this, "Select an instructor to assign.", "No instructor selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int instrModelRow = instructorsTable.convertRowIndexToModel(selectedInstructorRow);
        int instrId = (int) instructorsModel.getValueAt(instrModelRow, 0);
        InstructorRow instr = findInstructorById(instrId);
        if (instr == null) return;

        // Build list of section objects selected
        List<SectionRow> selectedSections = new ArrayList<>();
        for (int viewRow : selectedSectionRows) {
            int modelRow = sectionsTable.convertRowIndexToModel(viewRow);
            int sid = (int) sectionsModel.getValueAt(modelRow, 0);
            SectionRow s = findSectionById(sid);
            if (s != null) selectedSections.add(s);
        }

        // Confirm dialog with optional assignment type
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.anchor = GridBagConstraints.WEST;

        g.gridx=0; g.gridy=0;
        panel.add(new JLabel("Assign instructor:"), g);
        g.gridx=1;
        panel.add(new JLabel(instr.name + " (" + instr.email + ")"), g);

        g.gridx=0; g.gridy++;
        panel.add(new JLabel("To sections:"), g);
        g.gridx=1;
        panel.add(new JLabel(selectedSections.stream().map(s -> s.id).collect(Collectors.joining(", "))), g);

        g.gridx=0; g.gridy++;
        panel.add(new JLabel("Assignment type:"), g);
        g.gridx=1;
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Primary Instructor", "Co-Instructor", "Guest Lecturer"});
        panel.add(typeBox, g);

        int opt = JOptionPane.showConfirmDialog(this, panel, "Confirm assignment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opt != JOptionPane.OK_OPTION) return;

        String assignmentType = (String) typeBox.getSelectedItem();

        // Perform assignment (in-memory here; replace with DB call)
        for (SectionRow s : selectedSections) {
            s.assignedInstructorId = instr.id;
            s.assignedInstructorName = instr.name + " (" + assignmentType + ")";
            // TODO: persist assignment to DB, e.g. AssignmentService.assignInstructor(sectionId, instrId, assignmentType);
        }

        refreshSections();
        JOptionPane.showMessageDialog(this, "Assigned " + instr.name + " to " + selectedSections.size() + " section(s).");
    }

    private void onUnassignClicked() {
        int[] selectedSectionRows = sectionsTable.getSelectedRows();
        if (selectedSectionRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Select at least one section to unassign.", "No section selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<SectionRow> selectedSections = new ArrayList<>();
        for (int viewRow : selectedSectionRows) {
            int modelRow = sectionsTable.convertRowIndexToModel(viewRow);
            int sid = (int) sectionsModel.getValueAt(modelRow, 0);
            SectionRow s = findSectionById(sid);
            if (s != null) selectedSections.add(s);
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove assigned instructor from " + selectedSections.size() + " section(s)?",
                "Confirm unassign", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        for (SectionRow s : selectedSections) {
            // TODO: perform DB unassign
            s.assignedInstructorId = 0;
            s.assignedInstructorName = "";
        }
        refreshSections();
        JOptionPane.showMessageDialog(this, "Unassigned instructor(s).");
    }

    private void showAssignmentsDialog() {
        List<SectionRow> assigned = sections.stream().filter(s -> s.assignedInstructorId != 0).collect(Collectors.toList());
        if (assigned.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No assignments exist.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (SectionRow s : assigned) {
            sb.append(String.format("%s — %s\n", s.id, s.assignedInstructorName == null || s.assignedInstructorName.isEmpty() ? "<unknown>" : s.assignedInstructorName));
        }
        JTextArea ta = new JTextArea(sb.toString());
        ta.setEditable(false);
        ta.setRows(Math.min(assigned.size() + 1, 20));
        JOptionPane.showMessageDialog(this, new JScrollPane(ta), "Current Assignments", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshSections() {
        SwingUtilities.invokeLater(() -> {
            sectionsModel.setRowCount(0);
            for (SectionRow s : sections) {
                String assigned = (s.assignedInstructorName == null || s.assignedInstructorName.isEmpty()) ? "" : s.assignedInstructorName;
                sectionsModel.addRow(new Object[]{s.id, s.course, s.term, assigned});
            }
        });
    }

    private void refreshInstructors() {
        SwingUtilities.invokeLater(() -> {
            instructorsModel.setRowCount(0);
            for (InstructorRow i : instructors) {
                instructorsModel.addRow(new Object[]{i.id, i.name, i.email, i.department});
            }
        });
    }

    private SectionRow findSectionById(int id) {
        return sections.stream().filter(s -> s.id == id).findFirst().orElse(null);
    }

    private InstructorRow findInstructorById(int id) {
        return instructors.stream().filter(i -> i.id == id).findFirst().orElse(null);
    }

    private void loadSampleData() {
        sections.clear();
        sections.add(new SectionRow(2001, "CS101 - Intro to CS", "Fall 2025"));
        sections.add(new SectionRow(2002, "CS201 - Data Structures", "Fall 2025"));
        sections.add(new SectionRow(2003, "CS305 - Operating Systems", "Spring 2026"));
        sections.add(new SectionRow(2004, "MA101 - Calculus I", "Fall 2025"));
        sections.add(new SectionRow(2005, "EC201 - Microeconomics", "Fall 2025"));

        instructors.clear();
        instructors.add(new InstructorRow(3001, "Dr. Aarti Desai", "aarti.desai@uni.edu", "Computer Science"));
        instructors.add(new InstructorRow(3002, "Prof. Vikram Singh", "vikram.singh@uni.edu", "Computer Science"));
        instructors.add(new InstructorRow(3003, "Dr. Neha Gupta", "neha.gupta@uni.edu", "Mathematics"));
        instructors.add(new InstructorRow(3004, "Mr. Raj Patel", "raj.patel@uni.edu", "Economics"));

        // sample pre-assignment
        sections.get(1).assignedInstructorId = 3002;
        sections.get(1).assignedInstructorName = "Prof. Vikram Singh (Primary Instructor)";
    }

    private void stylePrimary(JButton b) {
        b.setBackground(new Color(52, 152, 219));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
    }

    // ---------- data holders ----------
    private static class SectionRow {
        int id;
        String course;
        String term;
        int assignedInstructorId;
        String assignedInstructorName;

        SectionRow(int id, String course, String term) {
            this.id = id;
            this.course = course;
            this.term = term;
            this.assignedInstructorId = 0;
            this.assignedInstructorName = "";
        }
    }

    private static class InstructorRow {
        int id;
        String name;
        String email;
        String department;

        InstructorRow(int id, String name, String email, String department) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.department = department;
        }
    }

    // Optional public API: allow MainFrame to reload data from DB
    public void loadFromDatabase(List<SectionRow> dbSections, List<InstructorRow> dbInstructors) {
        if (dbSections != null) {
            sections.clear();
            sections.addAll(dbSections);
            refreshSections();
        }
        if (dbInstructors != null) {
            instructors.clear();
            instructors.addAll(dbInstructors);
            refreshInstructors();
        }
    }
}
