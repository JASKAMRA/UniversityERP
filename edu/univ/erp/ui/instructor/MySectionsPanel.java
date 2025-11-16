package edu.univ.erp.ui.instructor;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * MySectionsPanel
 * - Lists sections assigned to the instructor
 * - Search / filter
 * - Double-click -> opens a simple Section Detail dialog (students)
 * - Export roster CSV
 */
public class MySectionsPanel extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField searchField;
    private final JLabel countLabel;

    // sample data
    private final List<SectionRow> sections = new ArrayList<>();

    public MySectionsPanel() {
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("My Sections");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        JPanel top = new JPanel(new BorderLayout(8,8));
        top.setOpaque(false);
        searchField = new JTextField(24);
        searchField.setToolTipText("Search by section ID, course or term...");
        top.add(searchField, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton viewBtn = new JButton("View Students");
        JButton exportBtn = new JButton("Export Roster");
        JButton refreshBtn = new JButton("Refresh");
        stylePrimary(viewBtn);
        stylePrimary(exportBtn);
        actions.add(viewBtn);
        actions.add(exportBtn);
        actions.add(refreshBtn);
        top.add(actions, BorderLayout.EAST);

        add(top, BorderLayout.SOUTH);

        String[] cols = {"Section ID","Course","Term","Enrolled","Status"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        add(new JScrollPane(table), BorderLayout.CENTER);

        countLabel = new JLabel("0 sections");
        add(countLabel, BorderLayout.PAGE_END);

        // wire actions
        refreshBtn.addActionListener(e -> refreshData());
        viewBtn.addActionListener(e -> openSelectedSectionDetail());
        exportBtn.addActionListener(e -> exportRoster());

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openSelectedSectionDetail();
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
            private void applyFilter() {
                String t = searchField.getText().trim();
                if (t.isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(t), 0,1,2));
                countLabel.setText(table.getRowCount() + " sections");
            }
        });

        loadSampleData();
        refreshData();
    }

    private void loadSampleData() {
        sections.clear();
        sections.add(new SectionRow("SEC101","CS101 - Intro to CS","Fall 2025",42,"Active"));
        sections.add(new SectionRow("SEC202","CS201 - Data Structures","Fall 2025",38,"Active"));
        sections.add(new SectionRow("SEC303","CS305 - Operating Systems","Spring 2026",29,"Planned"));
    }

    private void refreshData() {
        SwingUtilities.invokeLater(() -> {
            model.setRowCount(0);
            for (SectionRow s : sections) {
                model.addRow(new Object[]{s.id, s.course, s.term, s.enrolled, s.status});
            }
            countLabel.setText(model.getRowCount() + " sections");
        });
    }

    private void openSelectedSectionDetail() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a section first.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        String sectionId = (String) model.getValueAt(modelRow, 0);
        SectionRow s = sections.stream().filter(x -> x.id.equals(sectionId)).findFirst().orElse(null);
        if (s == null) return;

        // Show a simple dialog with students (reuse SectionDetailPanel class below)
        SectionDetailDialog dlg = new SectionDetailDialog(SwingUtilities.getWindowAncestor(this), s);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void exportRoster() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a section to export roster.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        String sectionId = (String) model.getValueAt(modelRow, 0);
        SectionRow s = sections.stream().filter(x -> x.id.equals(sectionId)).findFirst().orElse(null);
        if (s == null) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(s.id + "_roster.csv"));
        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        try (PrintWriter pw = new PrintWriter(chooser.getSelectedFile())) {
            pw.println("student_id,name,email");
            for (StudentRow st : s.students) {
                pw.printf("%s,%s,%s%n", st.id, escapeCsv(st.name), escapeCsv(st.email));
            }
            JOptionPane.showMessageDialog(this, "Roster exported.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stylePrimary(JButton b) {
        b.setBackground(new Color(52,152,219));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
    }

    // simple dialog that shows student list and quick actions
    private static class SectionDetailDialog extends JDialog {
        public SectionDetailDialog(Window owner, SectionRow s) {
            super(owner, "Section: " + s.id, ModalityType.APPLICATION_MODAL);
            setLayout(new BorderLayout(8,8));
            setSize(700, 480);

            JLabel header = new JLabel(s.course + " — " + s.term);
            header.setFont(new Font("Segoe UI", Font.BOLD, 18));
            add(header, BorderLayout.NORTH);

            String[] cols = {"Student ID","Name","Email","Attendance %"};
            DefaultTableModel m = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            JTable t = new JTable(m);
            for (StudentRow st : s.students) m.addRow(new Object[]{st.id, st.name, st.email, st.attendancePercent + "%"});

            add(new JScrollPane(t), BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton export = new JButton("Export CSV");
            JButton close = new JButton("Close");
            bottom.add(export);
            bottom.add(close);
            add(bottom, BorderLayout.SOUTH);

            export.addActionListener(e -> {
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File(s.id + "_roster.csv"));
                int res = chooser.showSaveDialog(SectionDetailDialog.this);
                if (res != JFileChooser.APPROVE_OPTION) return;
                try (PrintWriter pw = new PrintWriter(chooser.getSelectedFile())) {
                    pw.println("student_id,name,email,attendance_percent");
                    for (StudentRow sr : s.students) pw.printf("%s,%s,%s,%d%%\n", sr.id, sr.name, sr.email, sr.attendancePercent);
                    JOptionPane.showMessageDialog(SectionDetailDialog.this, "Exported.");
                } catch (Exception ex) { JOptionPane.showMessageDialog(SectionDetailDialog.this, "Failed: " + ex.getMessage()); }
            });

            close.addActionListener(e -> dispose());
        }
    }

    // Helpers / simple data
    private static String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }

    private static class SectionRow {
        String id, course, term, status;
        int enrolled;
        List<StudentRow> students = new ArrayList<>();
        SectionRow(String id, String course, String term, int enrolled, String status) {
            this.id = id; this.course = course; this.term = term; this.enrolled = enrolled; this.status = status;
            // demo students
            for (int i = 1; i <= Math.min(8, enrolled); i++) {
                students.add(new StudentRow(id + "-S" + i, "Student " + i, "student"+i+"@uni.edu", 80 + i % 10));
            }
        }
    }

    private static class StudentRow {
        String id, name, email;
        int attendancePercent;
        StudentRow(String id, String name, String email, int att) { this.id=id; this.name=name; this.email=email; this.attendancePercent=att; }
    }
}
