package edu.univ.erp.ui.instructor;

import edu.univ.erp.data.DBConnection;
import edu.univ.erp.service.InstructorService;

import javax.swing.*;

import javax.swing.border.TitledBorder;
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

    // --- Aesthetic constants ---

    private static final int GAP = 10;
    private static final Font STAT_LABEL_FONT = new Font("Arial", Font.BOLD, 14);

    private static final Color PRIMARY_COLOR = new Color(25, 135, 84); // A pleasant green color

    public ClassStatsPanel(InstructorService instructorService, String instructorUserId, int sectionId, String courseTitle) {
        this.instructorService = instructorService;
        this.instructorUserId = instructorUserId;
        this.sectionId = sectionId;
        this.courseTitle = courseTitle;
        init();
        loadStats();
    }

    private void init() {
        setLayout(new BorderLayout(GAP, GAP));
        setBackground(Color.WHITE);
        
        // 1. Titled Border
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            "📊 Class Statistics — " + courseTitle + " (Section " + sectionId + ")",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 16),
            PRIMARY_COLOR
        ));

        // 2. Statistics Panel (Top - GridBagLayout for better alignment/sizing)
        JPanel statsPanel = new JPanel(new GridBagLayout());
        statsPanel.setBackground(Color.WHITE);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(GAP / 2, GAP, GAP / 2, GAP);
        c.weightx = 1.0;

        lblCount = createStatLabel("Count:", 0);
        lblAvg = createStatLabel("Average:", 1);
        lblMin = createStatLabel("Min Score:", 2);
        lblMax = createStatLabel("Max Score:", 3);

        statsPanel.add(lblCount, getStatConstraints(0, 0));
        statsPanel.add(lblAvg, getStatConstraints(1, 0));
        statsPanel.add(lblMin, getStatConstraints(0, 1));
        statsPanel.add(lblMax, getStatConstraints(1, 1));

        add(statsPanel, BorderLayout.NORTH);

        // 3. Distribution Text Area (Center)
        taDistribution = new JTextArea(10, 40);
        taDistribution.setEditable(false);
        taDistribution.setFont(new Font("Monospaced", Font.PLAIN, 13));
        taDistribution.setBorder(BorderFactory.createTitledBorder("Grade Distribution"));
        
        JScrollPane sp = new JScrollPane(taDistribution);
        add(sp, BorderLayout.CENTER);

        // 4. Refresh Button (South)
        btnRefresh = new JButton("🔄 Refresh Statistics");
        btnRefresh.setFont(STAT_LABEL_FONT);
        btnRefresh.addActionListener(e -> loadStats());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(btnRefresh);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /** Helper to create a structured JLabel for statistics */
    private JLabel createStatLabel(String title, int row) {
        JLabel label = new JLabel(title + " -");
        // Apply different formatting later in loadStats
        return label;
    }
    
    /** Helper to get GridBagConstraints for the 2x2 grid */
    private GridBagConstraints getStatConstraints(int x, int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(5, 10, 5, 10);
        return c;
    }

    public void loadStats() {
        // Ownership check
        if (!instructorService.IsInstructorIn(instructorUserId, sectionId)) {
            JOptionPane.showMessageDialog(this, "Not your section.", "Permission denied", JOptionPane.ERROR_MESSAGE);
            taDistribution.setText("Permission denied: You are not the instructor for this section.");
            return;
        }

        DefaultListModel<BigDecimal> scores = new DefaultListModel<>();
        String getEnrollments = "SELECT e.enrollment_id FROM enrollments e WHERE e.section_id = ?";
        String getFinal = "SELECT score FROM grades WHERE enrollment_id = ? AND component = 'FINAL' LIMIT 1";
        // Calculate average of non-FINAL grades as the default score
        String getAvg = "SELECT AVG(score) AS avg_score FROM grades WHERE enrollment_id = ? AND component <> 'FINAL'";

        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement pEnroll = conn.prepareStatement(getEnrollments);
             PreparedStatement pFinal = conn.prepareStatement(getFinal);
             PreparedStatement pAvg = conn.prepareStatement(getAvg)) {

            pEnroll.setInt(1, sectionId);
            try (ResultSet rs = pEnroll.executeQuery()) {
                while (rs.next()) {
                    int enrollmentId = rs.getInt("enrollment_id");
                    BigDecimal scoreToAdd = null;
                    
                    // 1. Check for FINAL score
                    pFinal.setInt(1, enrollmentId);
                    try (ResultSet rsFinal = pFinal.executeQuery()) {
                        if (rsFinal.next()) {
                            scoreToAdd = rsFinal.getBigDecimal("score");
                        }
                    }
                    
                    // 2. If no FINAL score, check for average of other grades
                    if (scoreToAdd == null) {
                        pAvg.setInt(1, enrollmentId);
                        try (ResultSet rsAvg = pAvg.executeQuery()) {
                            if (rsAvg.next()) {
                                scoreToAdd = rsAvg.getBigDecimal("avg_score");
                            }
                        }
                    }
                    
                    if (scoreToAdd != null) {
                        scores.addElement(scoreToAdd);
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
            lblMin.setText("Min Score: -");
            lblMax.setText("Max Score: -");
            taDistribution.setText("No numeric grades available for this section.");
            return;
        }

        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal min = null, max = null;
        // Grade buckets for distribution
        Map<String, Integer> bucket = new HashMap<>();
        bucket.put("A (90-100)", 0);
        bucket.put("B (80-89)", 0);
        bucket.put("C (70-79)", 0);
        bucket.put("D (60-69)", 0);
        bucket.put("F (0-59)", 0);

        for (int i = 0; i < n; i++) {
            BigDecimal v = scores.get(i);
            if (v == null) continue;
            
            sum = sum.add(v);
            if (min == null || v.compareTo(min) < 0) min = v;
            if (max == null || v.compareTo(max) > 0) max = v;

            double dv = v.doubleValue();
            // Assign score to a bucket
            if (dv >= 90) bucket.put("A (90-100)", bucket.get("A (90-100)") + 1);
            else if (dv >= 80) bucket.put("B (80-89)", bucket.get("B (80-89)") + 1);
            else if (dv >= 70) bucket.put("C (70-79)", bucket.get("C (70-79)") + 1);
            else if (dv >= 60) bucket.put("D (60-69)", bucket.get("D (60-69)") + 1);
            else bucket.put("F (0-59)", bucket.get("F (0-59)") + 1);
        }
        
        // Calculate average using the sum of all collected scores
        BigDecimal avg = sum.divide(BigDecimal.valueOf(n), 2, BigDecimal.ROUND_HALF_UP);

        // --- Update UI Labels ---
        lblCount.setText("<html><b style='font-size:14px;'>Students Graded:</b><br><span style='font-size:16px; color:#333;'>" + n + "</span></html>");
        lblAvg.setText("<html><b style='font-size:14px;'>Class Average:</b><br><span style='font-size:16px; color:" + PRIMARY_COLOR.getRGB() + ";'>" + avg.toPlainString() + "%</span></html>");
        lblMin.setText("<html><b style='font-size:14px;'>Minimum Score:</b><br><span style='font-size:16px;'>" + (min == null ? "-" : min.toPlainString()) + "%</span></html>");
        lblMax.setText("<html><b style='font-size:14px;'>Maximum Score:</b><br><span style='font-size:16px;'>" + (max == null ? "-" : max.toPlainString()) + "%</span></html>");
        
        // --- Update Distribution Area ---
        StringBuilder sb = new StringBuilder();
        sb.append("Grade Distribution (Counts):\n\n");
        // Ensure keys are sorted for display
        bucket.keySet().stream().sorted().forEach(key -> {
            sb.append(String.format("%s : %d%n", key, bucket.get(key)));
        });

        taDistribution.setText(sb.toString());
    }
}