package edu.univ.erp.ui.student;

import edu.univ.erp.domain.Course;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CourseCatalogPanel extends JPanel {
    private final StudentService studentService;
    private final String currentUserId;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JButton refresh;
    private final JButton register;

    public CourseCatalogPanel(StudentService service, String userId) {
        this.studentService = service;
        this.currentUserId = userId;

        // debug - will print stack trace so you can see who created it
        System.out.println("[DEBUG] CourseCatalogPanel created for user=" + userId);
        Thread.dumpStack();

        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new Object[]{"Code","Title","Credits"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refresh = new JButton("Refresh");
        register = new JButton("Register");
        south.add(refresh);
        south.add(register);
        add(south, BorderLayout.SOUTH);

        // initial state
        register.setEnabled(false);

        // selection listener: enable register only when row selected
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                boolean sel = table.getSelectedRow() >= 0;
                register.setEnabled(sel);
            }
        });

        // actions
        refresh.addActionListener(e -> loadCatalog());
        register.addActionListener(e -> openRegisterDialog());

        // initial load
        loadCatalog();
    }

    private void loadCatalog() {
        refresh.setEnabled(false);
        SwingWorker<List<Course>, Void> w = new SwingWorker<>() {
            @Override
            protected List<Course> doInBackground() throws Exception {
                return studentService.getCourseCatalog();
            }
            @Override
            protected void done() {
                try {
                    List<Course> list = get();
                    tableModel.setRowCount(0);
                    if (list != null) {
                        for (Course c : list) {
                            tableModel.addRow(new Object[]{c.GetCourseID(), c.GetTitle(), c.GetCredits()});
                        }
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(CourseCatalogPanel.this, "Load interrupted.");
                } catch (ExecutionException ex) {
                    ex.printStackTrace(); // VERY important for debugging
                    String msg = ex.getCause() != null ? ex.getCause().toString() : ex.toString();
                    JOptionPane.showMessageDialog(CourseCatalogPanel.this, "Failed to load catalog: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    refresh.setEnabled(true);
                }
            }
        };
        w.execute();
    }

    private void openRegisterDialog() {
        int sel = table.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Select a course first.");
            return;
        }
        String code = (String) tableModel.getValueAt(sel, 0);
        RegisterDialog dlg = new RegisterDialog(SwingUtilities.getWindowAncestor(this), studentService, currentUserId, code);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        if (dlg.isRegisteredSuccessfully()) {
            loadCatalog(); // refresh
        }
    }
}
