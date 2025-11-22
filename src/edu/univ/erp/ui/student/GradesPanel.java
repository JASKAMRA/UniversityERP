package edu.univ.erp.ui.student;

import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;
import java.util.List;

public class GradesPanel extends JPanel {

    private final StudentService studentService;
    private final String userId;
    private JTable table;
    private DefaultTableModel model;
    private JButton refreshButton;

    // --- Aesthetic constants ---
    private static final int PADDING = 15;
    private static final int GAP = 10;
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 20);
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204); // Deep Blue

    public GradesPanel(StudentService studentService, String userId) {
        this.studentService = studentService;
        this.userId = userId;

        initUI();
        loadGrades();
    }

    private void initUI() {
        // 1. Overall Layout & Padding
        setLayout(new BorderLayout(GAP, GAP));
        setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        setBackground(Color.WHITE);

        // 2. Title (North)
        JLabel title = new JLabel("💯 My Component & Final Grades");
        title.setFont(TITLE_FONT);
        title.setForeground(PRIMARY_COLOR);
        title.setBorder(new EmptyBorder(0, 0, GAP, 0));
        add(title, BorderLayout.NORTH);


        // 3. Table Setup (Center)
        model = new DefaultTableModel(
                new Object[]{"Course", "Section ID", "Component", "Score", "Final Grade"}, // Added Section ID for clarity
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
            // Define column classes for proper alignment/sorting
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) return BigDecimal.class; // Score
                return String.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        
        // Align Score column to the right (assuming default renderer handles BigDecimal right alignment)
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        add(scrollPane, BorderLayout.CENTER);

        // 4. Buttons (South)
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP, 0));
        bottom.setBackground(Color.WHITE);
        
        refreshButton = new JButton("🔄 Refresh Grades");
        refreshButton.setFocusPainted(false);
        refreshButton.setBackground(new Color(230, 230, 230));
        refreshButton.setPreferredSize(new Dimension(150, 30));
        
        refreshButton.addActionListener(e -> loadGrades());
        bottom.add(refreshButton);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadGrades() {
        refreshButton.setEnabled(false);
        model.setRowCount(0);
        
        // Show loading message
        model.addRow(new Object[]{"Loading...", "", "", "", ""});
        
        SwingWorker<List<Object[]>, Void> w = new SwingWorker<>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                // Assuming the service returns: [CourseTitle, SectionID, Component, Score, FinalGrade]
                return studentService.getGrades(userId);
            }

            @Override
            protected void done() {
                model.setRowCount(0); // Clear loading row
                refreshButton.setEnabled(true);
                
                try {
                    List<Object[]> rows = get();
                    if (rows == null || rows.isEmpty()) {
                        model.addRow(new Object[]{"No grades recorded.", "", "", "", ""});
                        return;
                    }
                    
                    for (Object[] r : rows) {
                        String course = r[0] == null ? "" : String.valueOf(r[0]);
                        Integer sec = r[1] == null ? null : (Integer) r[1];
                        String comp = r[2] == null ? "" : String.valueOf(r[2]);
                        BigDecimal score = r[3] == null ? null : (BigDecimal) r[3];
                        String finalGrade = r[4] == null ? "" : String.valueOf(r[4]);
                        
                        // Use BigDecimal object directly for correct table rendering/sorting
                        model.addRow(new Object[]{ 
                            course, 
                            sec, 
                            comp, 
                            score, 
                            finalGrade 
                        });
                    }
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                    JOptionPane.showMessageDialog(GradesPanel.this, "Grade load interrupted", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (ExecutionException ee) {
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