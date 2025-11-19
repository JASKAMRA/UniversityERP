package edu.univ.erp.ui.instructor;

import edu.univ.erp.data.DBConnection;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Shows basic statistics for a section. Verifies instructor ownership before computing.
 * Constructor: ClassStatsPanel(InstructorService instructorService, String instructorUserId, int sectionId, String courseTitle)
 */
public class ClassStatsPanel extends JPanel {
    private final int sectionId;
    private final String courseTitle;
    private final InstructorService instructorService;
    private final String instructorUserId;

    private JLabel lblCount;
    private JLabel lblAvg;
    private JLabel lblMin;
    private JLabel lblMax;
    private JTextArea taDistribution;
    private JButton btnRefresh;

    public ClassStatsPanel(InstructorService instructorService, String instructorUserId, int sectionId, String courseTitle) {
        this.instructorService = instructorService;
        this.instructorUserId = instructorUserId;
        this.sectionId = sectionId;
        this.courseTitle = courseTitle;
        init();
        loadStats();
    }

    private void init() {
        setLayout(new BorderLayout(8,8));
        JPanel top = new JPanel(new GridLayout(2,2,6,6));
        lblCount = new JLabel("Count: -");
        lblAvg = new JLabel("Average: -");
        lblMin = new JLabel("Min: -");
        lblMax = new JLabel("Max: -");

        top.add(lblCount);
        top.add(lblAvg);
        top.add(lblMin);
        top.add(lblMax);

        taDistribution = new JTextArea(10, 40);
        taDistribution.setEditable(false);
        JScrollPane sp = new JScrollPane(taDistribution);

        btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadStats());

        add(top, BorderLayout.NORTH);
        add(sp, BorderLayout.CENTER);
        add(btnRefresh, BorderLayout.SOUTH);
        setBorder(BorderFactory.createTitledBorder("Class statistics - " + courseTitle + " (Section " + sectionId + ")"));
    }

    public void loadStats() {
        // Ownership check
        if (!instructorService.isInstructorOfSection(instructorUserId, sectionId)) {
            JOptionPane.showMessageDialog(this, "Not your section.", "Permission denied", JOptionPane.ERROR_MESSAGE);
            taDistribution.setText("Permission denied.");
            return;
        }

        DefaultListModel<BigDecimal> scores = new DefaultListModel<>();
        String getEnrollments = "SELECT e.enrollment_id FROM enrollments e WHERE e.section_id = ?";
        String getFinal = "SELECT score FROM grades WHERE enrollment_id = ? AND component = 'FINAL' LIMIT 1";
        String getAvg = "SELECT AVG(score) AS avg_score FROM grades WHERE enrollment_id = ? AND component <> 'FINAL'";

        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement pEnroll = conn.prepareStatement(getEnrollments);
             PreparedStatement pFinal = conn.prepareStatement(getFinal);
             PreparedStatement pAvg = conn.prepareStatement(getAvg)) {

            pEnroll.setInt(1, sectionId);
            try (ResultSet rs = pEnroll.executeQuery()) {
                while (rs.next()) {
                    int enrollmentId = rs.getInt("enrollment_id");
                    pFinal.setInt(1, enrollmentId);
                    try (ResultSet rsFinal = pFinal.executeQuery()) {
                        if (rsFinal.next()) {
                            BigDecimal sc = rsFinal.getBigDecimal("score");
                            if (sc != null) scores.addElement(sc);
                            continue;
                        }
                    }
                    pAvg.setInt(1, enrollmentId);
                    try (ResultSet rsAvg = pAvg.executeQuery()) {
                        if (rsAvg.next()) {
                            BigDecimal sc = rsAvg.getBigDecimal("avg_score");
                            if (sc != null) scores.addElement(sc);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load stats: " + ex.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int n = scores.getSize();
        if (n == 0) {
            lblCount.setText("Count: 0");
            lblAvg.setText("Average: -");
            lblMin.setText("Min: -");
            lblMax.setText("Max: -");
            taDistribution.setText("No numeric grades available for this section.");
            return;
        }

        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal min = null, max = null;
        Map<String, Integer> bucket = new HashMap<>();
        bucket.put("90-100 (A)", 0);
        bucket.put("80-89  (B)", 0);
        bucket.put("70-79  (C)", 0);
        bucket.put("60-69  (D)", 0);
        bucket.put("0-59   (F)", 0);

        for (int i = 0; i < n; i++) {
            BigDecimal v = scores.get(i);
            if (v == null) continue;
            sum = sum.add(v);
            if (min == null || v.compareTo(min) < 0) min = v;
            if (max == null || v.compareTo(max) > 0) max = v;

            double dv = v.doubleValue();
            if (dv >= 90) bucket.put("90-100 (A)", bucket.get("90-100 (A)") + 1);
            else if (dv >= 80) bucket.put("80-89  (B)", bucket.get("80-89  (B)") + 1);
            else if (dv >= 70) bucket.put("70-79  (C)", bucket.get("70-79  (C)") + 1);
            else if (dv >= 60) bucket.put("60-69  (D)", bucket.get("60-69  (D)") + 1);
            else bucket.put("0-59   (F)", bucket.get("0-59   (F)") + 1);
        }

        BigDecimal avg = sum.divide(BigDecimal.valueOf(n), 2, BigDecimal.ROUND_HALF_UP);

        lblCount.setText("Count: " + n);
        lblAvg.setText("Average: " + avg.toPlainString());
        lblMin.setText("Min: " + (min == null ? "-" : min.toPlainString()));
        lblMax.setText("Max: " + (max == null ? "-" : max.toPlainString()));

        StringBuilder sb = new StringBuilder();
        sb.append("Distribution (counts):\n");
        for (Map.Entry<String,Integer> e : bucket.entrySet()) {
            sb.append(String.format("%s : %d%n", e.getKey(), e.getValue()));
        }
        taDistribution.setText(sb.toString());
    }
}
