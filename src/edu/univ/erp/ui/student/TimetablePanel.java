package edu.univ.erp.ui.student;

import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Course;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
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

    public TimetablePanel(StudentService service, String userId) {
        this.studentService = service;
        this.userId = userId;
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new Object[] {"Course","Section","Day","Semester","Year"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshBtn = new JButton("Refresh");
        south.add(refreshBtn);
        add(south, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadTimetable());

        // initial load
        loadTimetable();
    }

    private void loadTimetable() {
        model.setRowCount(0);
        refreshBtn.setEnabled(false);
        SwingWorker<List<Section>, Void> w = new SwingWorker<>() {
            @Override
            protected List<Section> doInBackground() throws Exception {
                return studentService.getTimetable(userId);
            }

            @Override
            protected void done() {
                try {
                    List<Section> list = get();
                    if (list != null) {
                        for (Section s : list) {
                            // courseId exists in section
                            String courseId = s.GetCourseID();
                            Integer secId = s.GetSectionID();
                            String day = s.GetDay() == null ? "" : s.GetDay().name();
                            String sem = s.GetSemester() == null ? "" : s.GetSemester();
                            Integer year = s.GetYear();
                            model.addRow(new Object[] { courseId, secId, day, sem, year });
                        }
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(TimetablePanel.this, "Failed to load timetable: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    refreshBtn.setEnabled(true);
                }
            }
        };
        w.execute();
    }
}
