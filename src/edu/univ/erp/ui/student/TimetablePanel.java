package edu.univ.erp.ui.student;

import edu.univ.erp.domain.Section;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TimetablePanel extends JPanel {

    private final StudentService studentService;
    private final String userId;

    private final DefaultTableModel model;
    private final JTable table;
    private final JButton refreshBtn;

    // --- Aesthetic constants ---
    private static final int PADDING = 15;
    private static final int GAP = 10;
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 20);
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204); // Deep Blue

    public TimetablePanel(StudentService service, String userId) {
        this.studentService = service;
        this.userId = userId;
        
        // 1. Overall Layout & Padding
        setLayout(new BorderLayout(GAP, GAP));
        setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        setBackground(Color.WHITE);

        // 2. Title (North)
        JLabel title = new JLabel("⏰ My Current Timetable");
        title.setFont(TITLE_FONT);
        title.setForeground(PRIMARY_COLOR);
        title.setBorder(new EmptyBorder(0, 0, GAP, 0));
        add(title, BorderLayout.NORTH);

        // 3. Table Setup (Center)
        model = new DefaultTableModel(new Object[] {"Course","Section","Day","Semester","Year"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            // Ensure Section and Year columns are treated as Integer/Number
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1 || columnIndex == 4) return Integer.class;
                return String.class;
            }
        };

        table = new JTable(model);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        add(scrollPane, BorderLayout.CENTER);

        // 4. Buttons (South)
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP, 0));
        south.setBackground(Color.WHITE);
        
        refreshBtn = new JButton("🔄 Refresh Timetable");
        
        // Style Button
        refreshBtn.setFocusPainted(false);
        refreshBtn.setBackground(Color.LIGHT_GRAY);
        refreshBtn.setPreferredSize(new Dimension(160, 30));
        
        south.add(refreshBtn);
        add(south, BorderLayout.SOUTH);

        // 5. Actions
        refreshBtn.addActionListener(e -> loadTimetable());

        // initial load
        loadTimetable();
    }

    private void loadTimetable() {
        model.setRowCount(0);
        refreshBtn.setEnabled(false);
        // Show loading message
        model.addRow(new Object[]{"Loading...", "", "", "", ""});
        
        SwingWorker<List<Section>, Void> w = new SwingWorker<>() {
            @Override
            protected List<Section> doInBackground() throws Exception {
                return studentService.getTimetable(userId);
            }

            @Override
            protected void done() {
                model.setRowCount(0); // Clear loading row
                
                try {
                    List<Section> list = get();
                    if (list != null) {
                        for (Section s : list) {
                            // Safely retrieve data from Section object
                            String courseId = s.GetCourseID();
                            Integer secId = s.GetSectionID();
                            String day = s.GetDay() == null ? "" : s.GetDay().name();
                            String sem = s.GetSemester() == null ? "" : s.GetSemester();
                            Integer year = s.GetYear();
                            
                            model.addRow(new Object[] { courseId, secId, day, sem, year });
                        }
                    } else {
                        JOptionPane.showMessageDialog(TimetablePanel.this, "No sections found in your timetable.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                    String msg = (ex.getCause() != null) ? ex.getCause().getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(TimetablePanel.this, "Failed to load timetable: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    refreshBtn.setEnabled(true);
                }
            }
        };
        w.execute();
    }
}