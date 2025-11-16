package edu.univ.erp.ui.instructor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * ClassStatsPanel
 * - Aggregated stats per section (average grade, pass rate, avg attendance)
 * - Simple table + summary cards
 */
public class ClassStatsPanel extends JPanel {

    private final JLabel avgClassGradeLabel = new JLabel();
    private final JLabel passRateLabel = new JLabel();
    private final JLabel avgAttendanceLabel = new JLabel();
    private final DefaultTableModel model;

    // demo data
    private final List<StatRow> rows = new ArrayList<>();

    public ClassStatsPanel() {
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Class Statistics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        JPanel cards = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        cards.setOpaque(false);
        avgClassGradeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        passRateLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        avgAttendanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        cards.add(makeCard("Avg Grade", avgClassGradeLabel));
        cards.add(makeCard("Pass Rate", passRateLabel));
        cards.add(makeCard("Avg Attendance", avgAttendanceLabel));
        add(cards, BorderLayout.CENTER);

        model = new DefaultTableModel(new String[]{"Section","Avg Grade","Median","Pass %","Avg Attendance"},0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(26);
        add(new JScrollPane(table), BorderLayout.SOUTH);

        loadSample();
        refresh();
    }

    private JPanel makeCard(String label, JLabel value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(220, 70));
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220,220,220)), BorderFactory.createEmptyBorder(8,8,8,8)));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(value, BorderLayout.CENTER);
        p.add(l, BorderLayout.SOUTH);
        return p;
    }

    private void loadSample() {
        rows.clear();
        rows.add(new StatRow("SEC101", 78.4, 79, 88, 85));
        rows.add(new StatRow("SEC202", 74.1, 75, 82, 80));
        rows.add(new StatRow("SEC303", 81.6, 82, 92, 88));
    }

    private void refresh() {
        SwingUtilities.invokeLater(() -> {
            model.setRowCount(0);
            double sum = 0; int count=0; double passSum=0; double attSum=0;
            for (StatRow r : rows) {
                model.addRow(new Object[]{r.section, String.format("%.1f", r.avgGrade), r.median, r.passPercent+"%", r.avgAttendance+"%"});
                sum += r.avgGrade; passSum += r.passPercent; attSum += r.avgAttendance; count++;
            }
            avgClassGradeLabel.setText(count>0 ? String.format("%.1f", sum/count) : "-");
            passRateLabel.setText(count>0 ? String.format("%.1f%%", passSum/count) : "-");
            avgAttendanceLabel.setText(count>0 ? String.format("%.1f%%", attSum/count) : "-");
        });
    }

    private static class StatRow {
        String section;
        double avgGrade;
        int median;
        int passPercent;
        int avgAttendance;
        StatRow(String section,double avgGrade,int median,int passPercent,int avgAttendance){
            this.section=section;this.avgGrade=avgGrade;this.median=median;this.passPercent=passPercent;this.avgAttendance=avgAttendance;
        }
    }
}
