package edu.univ.erp.ui.instructor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * InstructorDashboardPanel
 * - Header with welcome and stats
 * - Sidebar quick actions
 * - Center tabs: My Sections (table) and Gradebook (sample)
 * - Dummy data + TODO hooks for DB integration
 */
public class InstructorDashboardPanel extends JPanel {
    // sample instructor info (replace with real user data)
    private final String instructorName = "Dr. Rahul Mehta";

    // UI components used outside ctor
    private final JLabel lblSectionsCount = new JLabel("0");
    private final JLabel lblStudentsCount = new JLabel("0");
    private final JLabel lblPendingGradeCount = new JLabel("0");

    private final DefaultTableModel sectionsModel;
    private final JTable sectionsTable;

    // sample in-memory data
    private final List<SectionRow> sections = new ArrayList<>();

    public InstructorDashboardPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(Color.WHITE);

        // header
        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setOpaque(false);

        JLabel title = new JLabel("Instructor Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.add(title, BorderLayout.WEST);

        JLabel welcome = new JLabel("Welcome, " + instructorName + "  ");
        welcome.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        header.add(welcome, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // top stats strip
        JPanel statsStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        statsStrip.setOpaque(false);

        statsStrip.add(createStatCard("Sections", lblSectionsCount));
        statsStrip.add(createStatCard("Students", lblStudentsCount));
        statsStrip.add(createStatCard("Pending Grades", lblPendingGradeCount));

        add(statsStrip, BorderLayout.PAGE_START);

        // main split: sidebar + center
        JSplitPane split = new JSplitPane();
        split.setDividerLocation(200);
        split.setResizeWeight(0);
        split.setBorder(null);

        // sidebar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        sidebar.add(makeSidebarButton("My Sections", e -> selectSectionsTab()));
        sidebar.add(makeSidebarButton("Gradebook", e -> selectGradebookTab()));
        sidebar.add(makeSidebarButton("Take Attendance", e -> takeAttendance()));
        sidebar.add(makeSidebarButton("Upload Grades", e -> uploadGrades()));
        sidebar.add(Box.createVerticalStrut(12));
        sidebar.add(makeSidebarButton("Profile", e -> openProfile()));
        sidebar.add(Box.createVerticalGlue());

        split.setLeftComponent(sidebar);

        // center: tabs
        JTabbedPane tabs = new JTabbedPane();

        // -- My Sections tab
        sectionsModel = new DefaultTableModel(new String[]{"Section ID", "Course", "Term", "Enrolled"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        sectionsTable = new JTable(sectionsModel);
        sectionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane secScroll = new JScrollPane(sectionsTable);
        tabs.addTab("My Sections", secScroll);

        // -- Gradebook quick tab
        JPanel gradebookPanel = new JPanel(new BorderLayout(8, 8));
        gradebookPanel.setBackground(Color.WHITE);
        gradebookPanel.add(new JLabel("Recent grade submissions / quick actions:"), BorderLayout.NORTH);

        JTextArea gbArea = new JTextArea(8, 40);
        gbArea.setEditable(false);
        gbArea.setText("No recent submissions (sample data).");
        gradebookPanel.add(new JScrollPane(gbArea), BorderLayout.CENTER);

        tabs.addTab("Gradebook", gradebookPanel);

        // wire double-click on section -> open section detail
        sectionsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) openSectionDetail();
            }
        });

        split.setRightComponent(tabs);
        add(split, BorderLayout.CENTER);

        // bottom quick actions
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refreshData());
        bottom.add(refresh);
        add(bottom, BorderLayout.SOUTH);

        // load dummy data and refresh UI
        loadSampleData();
        refreshData();
    }

    private JPanel createStatCard(String label, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(160, 60));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        card.setBackground(Color.WHITE);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(lbl, BorderLayout.SOUTH);
        return card;
    }

    private JButton makeSidebarButton(String text, ActionListener action) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setFocusPainted(false);
        b.addActionListener(action);
        b.setBackground(new Color(52, 152, 219));
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return b;
    }

    // ---------- Actions (TODO: wire to real services) ----------
    private void selectSectionsTab() {
        Component c = ((JSplitPane) getComponent(2)).getRightComponent();
        if (c instanceof JTabbedPane) ((JTabbedPane) c).setSelectedIndex(0);
    }

    private void selectGradebookTab() {
        Component c = ((JSplitPane) getComponent(2)).getRightComponent();
        if (c instanceof JTabbedPane) ((JTabbedPane) c).setSelectedIndex(1);
    }

    private void takeAttendance() {
        // TODO: open attendance dialog for selected section
        JOptionPane.showMessageDialog(this, "Open attendance dialog (TODO).");
    }

    private void uploadGrades() {
        // TODO: start grade upload flow (CSV etc)
        JOptionPane.showMessageDialog(this, "Start grade upload (TODO).");
    }

    private void openProfile() {
        // TODO: navigate to instructor profile panel in your MainFrame
        JOptionPane.showMessageDialog(this, "Open Instructor Profile (TODO).");
    }

    private void openSectionDetail() {
        int row = sectionsTable.getSelectedRow();
        if (row == -1) return;
        int modelRow = sectionsTable.convertRowIndexToModel(row);
        String sectionId = (String) sectionsModel.getValueAt(modelRow, 0);
        // TODO: open a detailed panel for this section (students, attendance, gradebook)
        JOptionPane.showMessageDialog(this, "Open details for section: " + sectionId);
    }

    // ---------- Data handling ----------
    private void loadSampleData() {
        sections.clear();
        sections.add(new SectionRow("SEC101", "CS101 - Intro to CS", "Fall 2025", 42));
        sections.add(new SectionRow("SEC202", "CS201 - Data Structures", "Fall 2025", 38));
        sections.add(new SectionRow("SEC303", "CS305 - Operating Systems", "Spring 2026", 29));
    }

    private void refreshData() {
        SwingUtilities.invokeLater(() -> {
            sectionsModel.setRowCount(0);
            int totalStudents = 0;
            for (SectionRow s : sections) {
                sectionsModel.addRow(new Object[]{s.sectionId, s.course, s.term, s.enrolled});
                totalStudents += s.enrolled;
            }

            lblSectionsCount.setText(String.valueOf(sections.size()));
            lblStudentsCount.setText(String.valueOf(totalStudents));
            // TODO: compute real pending grade count
            lblPendingGradeCount.setText(String.valueOf(3)); // sample
        });
    }

    // ---------- Simple data holder ----------
    private static class SectionRow {
        String sectionId;
        String course;
        String term;
        int enrolled;

        SectionRow(String sectionId, String course, String term, int enrolled) {
            this.sectionId = sectionId;
            this.course = course;
            this.term = term;
            this.enrolled = enrolled;
        }
    }
}
