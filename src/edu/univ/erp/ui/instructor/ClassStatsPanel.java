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
        this.instructorService=instructorService;
        this.courseTitle=courseTitle;
        this.instructorUserId=instructorUserId;
        this.sectionId=sectionId;
        init();
        loadStats();
    }

    private void init() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            "📊 Class Statistics — " + courseTitle + " (Section " + sectionId + ")",TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 16), new Color(25, 135, 84)
        ));

        JPanel statPanel=new JPanel(new GridBagLayout());
        statPanel.setBackground(Color.WHITE);
        GridBagConstraints constraint=new GridBagConstraints();
        constraint.fill=GridBagConstraints.HORIZONTAL;
        constraint.insets=new Insets(10 / 2, 10, 10 / 2, 10);
        constraint.weightx=1.0;

        lblCount=createStatLabel("Count:", 0);
        lblAvg=createStatLabel("Average:", 1);
        lblMin=createStatLabel("Min Score:", 2);
        lblMax=createStatLabel("Max Score:", 3);

        statPanel.add(lblCount, getStatConstraints(0, 0));
        statPanel.add(lblAvg, getStatConstraints(1, 0));
        statPanel.add(lblMin, getStatConstraints(0, 1));
        statPanel.add(lblMax, getStatConstraints(1, 1));

        add(statPanel, BorderLayout.NORTH);

        taDistribution=new JTextArea(10, 40);
        taDistribution.setEditable(false);
        taDistribution.setFont(new Font("Monospaced", Font.PLAIN, 13));
        taDistribution.setBorder(BorderFactory.createTitledBorder("Grade Distribution"));
        
        JScrollPane scrollP=new JScrollPane(taDistribution);
        add(scrollP, BorderLayout.CENTER);

        btnRefresh=new JButton("🔄 Refresh Statistics");
        btnRefresh.setFont(new Font("Arial", Font.BOLD, 14));
        btnRefresh.addActionListener(e -> loadStats());

        JPanel bottomPanel=new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(btnRefresh);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private GridBagConstraints getStatConstraints(int x, int y) {
        GridBagConstraints constraint=new GridBagConstraints();
        constraint.gridx=x;
        constraint.gridy=y;
        constraint.weightx=1.0;
        constraint.fill=GridBagConstraints.HORIZONTAL;
        constraint.insets=new Insets(5, 10, 5, 10);
        return constraint;
    }

    private JLabel createStatLabel(String title, int row) {
        JLabel label=new JLabel(title + " -");
        return label;
    }
    

    public void loadStats() {
        if (!instructorService.isInstructorIn(instructorUserId, sectionId)) {
            JOptionPane.showMessageDialog(this, "Not your section.", "Permission denied", JOptionPane.ERROR_MESSAGE);
            taDistribution.setText("Permission denied: You are not the instructor for this section.");
            return;
        }

        DefaultListModel<BigDecimal> scores = new DefaultListModel<>();
        String getEnrollments = "SELECT e.enrollment_id FROM enrollments e WHERE e.section_id = ?";
        String getFinal = "SELECT score FROM grades WHERE enrollment_id = ? AND component = 'FINAL' LIMIT 1";
        String getAvg = "SELECT AVG(score) AS avg_score FROM grades WHERE enrollment_id = ? AND component <> 'FINAL'";

        try (Connection connect=DBConnection.getStudentConnection();
        PreparedStatement pFinal=connect.prepareStatement(getFinal);
        PreparedStatement pEnroll=connect.prepareStatement(getEnrollments);
        PreparedStatement pAvg=connect.prepareStatement(getAvg)) {
            pEnroll.setInt(1, sectionId);
            try (ResultSet resultSet = pEnroll.executeQuery()) {
                while (resultSet.next()) {
                    int enrollmentId=resultSet.getInt("enrollment_id");
                    BigDecimal scoreToAdd=null;
                    pFinal.setInt(1, enrollmentId);
                    try (ResultSet rsFinal=pFinal.executeQuery()) {
                        if (rsFinal.next()) {
                            scoreToAdd=rsFinal.getBigDecimal("score");
                        }
                    } 
                    if (scoreToAdd==null) {
                        pAvg.setInt(1, enrollmentId);
                        try (ResultSet rsAvg=pAvg.executeQuery()) {
                            if (rsAvg.next()) {
                                scoreToAdd=rsAvg.getBigDecimal("avg_score");
                            }
                        }
                    }                 
                    if (scoreToAdd!=null) {
                        scores.addElement(scoreToAdd);
                    }
                }
            }
        } 
        catch (SQLException exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load stats: " + exception.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int n=scores.getSize();
        if (n==0){
            lblCount.setText("Count: 0");
            lblAvg.setText("Average: -");
            lblMin.setText("Min Score: -");
            lblMax.setText("Max Score: -");
            taDistribution.setText("No numeric grades available for this section.");
            return;
        }

    BigDecimal sum = BigDecimal.ZERO;
    BigDecimal min = null;
    BigDecimal max = null;
    Map<String, Integer> bucket=new HashMap<>();    

    bucket.put("A (90-100)", 0);
    bucket.put("B (80-89)", 0);
    bucket.put("C (70-79)", 0);
    bucket.put("D (60-69)", 0);
    bucket.put("F (0-59)", 0);

for (int i = 0; i < n; i++) {
    BigDecimal v=scores.get(i);
    if (v == null) {
        continue;
    }
    sum = sum.add(v);
    if ( v.compareTo(min) < 0||min == null ) {
        min=v;
    }
    if ( v.compareTo(max) > 0 || max == null ) {
        max=v;
    }

    double dv=v.doubleValue();
    if (dv >= 90) {
        bucket.put("A (90-100)", bucket.get("A (90-100)") + 1);
    }
    else if (dv >= 80) {
        bucket.put("B (80-89)", bucket.get("B (80-89)") + 1);
    }
    else if (dv >= 70) {
        bucket.put("C (70-79)", bucket.get("C (70-79)") + 1);
    }
    else if (dv >= 60) {
        bucket.put("D (60-69)", bucket.get("D (60-69)") + 1);
    }
    else {
        bucket.put("F (0-59)", bucket.get("F (0-59)") + 1);
    }
}

BigDecimal avg=BigDecimal.ZERO;
if (n>0){
    avg=sum.divide(BigDecimal.valueOf(n), 2, BigDecimal.ROUND_HALF_UP);
}

lblCount.setText("Count: " + n);
lblAvg.setText("Average: " + avg.toPlainString() + "%");
lblMin.setText("Min Score: " + (min == null ? "-" : min.toPlainString()) + "%");
lblMax.setText("Max Score: " + (max == null ? "-" : max.toPlainString()) + "%");

StringBuilder strbuild = new StringBuilder();
strbuild.append("Grade Distribution (Counts):\n\n");
bucket.keySet().stream().sorted().forEach(key -> strbuild.append(String.format("%s : %d%n", key, bucket.get(key))));
taDistribution.setText(strbuild.toString());

    }
}