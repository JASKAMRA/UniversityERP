package edu.univ.erp.ui.student;

import edu.univ.erp.service.StudentService;
import javax.swing.table.DefaultTableModel;


import javax.swing.*;
import java.awt.*;

public class GradesPanel extends JPanel {

    private final StudentService studentService;
    private final String userId;
    private JTable table;
    private DefaultTableModel model;

    public GradesPanel(StudentService studentService, String userId) {
        this.studentService = studentService;
        this.userId = userId;

        initUI();
        loadGrades();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        model = new DefaultTableModel(
                new Object[]{"Course", "Component", "Score", "Final Grade"},
                0
        ) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> loadGrades());
        add(refresh, BorderLayout.SOUTH);
    }

    private void loadGrades() {
    model.setRowCount(0);
    SwingWorker<java.util.List<Object[]>, Void> w = new SwingWorker<>() {
        @Override
        protected java.util.List<Object[]> doInBackground() throws Exception {
            return studentService.getGrades(userId);
        }

        @Override
        protected void done() {
            try {
                java.util.List<Object[]> rows = get();
                if (rows == null || rows.isEmpty()) {
                    // nothing to show
                    return;
                }
                for (Object[] r : rows) {
                    String course = r[0] == null ? "" : String.valueOf(r[0]);
                    Integer sec = r[1] == null ? null : (Integer) r[1];
                    String comp = r[2] == null ? "" : String.valueOf(r[2]);
                    java.math.BigDecimal score = r[3] == null ? null : (java.math.BigDecimal) r[3];
                    String finalGrade = r[4] == null ? "" : String.valueOf(r[4]);
                    model.addRow(new Object[]{ course, comp, score == null ? "" : score.toPlainString(), finalGrade });
                }
            } catch (InterruptedException ie) {
                ie.printStackTrace();
                JOptionPane.showMessageDialog(GradesPanel.this, "Grade load interrupted", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (java.util.concurrent.ExecutionException ee) {
                ee.printStackTrace();
                Throwable cause = ee.getCause();
                JOptionPane.showMessageDialog(GradesPanel.this, "Failed to load grades: " + (cause == null ? ee.getMessage() : cause.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(GradesPanel.this, "Failed to load grades: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    };
    w.execute();
}

}
