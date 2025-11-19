package edu.univ.erp.ui.student;

import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MyRegistrationsPanel extends JPanel {

    private final StudentService service;
    private final String userId;

    private DefaultTableModel model;
    private JTable table;
    private JButton refreshBtn;
    private JButton dropBtn;

    public MyRegistrationsPanel(StudentService service, String userId) {
        this.service = service;
        this.userId = userId;
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        model = new DefaultTableModel(
                new Object[]{"EnrollID","Course","Section","Day","Semester","Status"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        refreshBtn = new JButton("Refresh");
        dropBtn = new JButton("Drop Selected");
        dropBtn.setEnabled(false);

        south.add(refreshBtn);
        south.add(dropBtn);
        add(south, BorderLayout.SOUTH);

        // selection listener
        table.getSelectionModel().addListSelectionListener(e ->
                dropBtn.setEnabled(table.getSelectedRow() >= 0)
        );

        refreshBtn.addActionListener(e -> loadData());
        dropBtn.addActionListener(e -> dropSelected());
    }

    private void loadData() {
        model.setRowCount(0);
        refreshBtn.setEnabled(false);
        SwingWorker<List<Object[]>, Void> w = new SwingWorker<>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                return service.getMyRegistrations(userId);
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> rows = get();
                    if (rows != null) {
                        for (Object[] r : rows) model.addRow(r);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(MyRegistrationsPanel.this, "Failed to load: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    refreshBtn.setEnabled(true);
                }
            }
        };
        w.execute();
    }

    private void dropSelected() {
        int r = table.getSelectedRow();
        if (r < 0) return;
        Object val = model.getValueAt(r, 0);
        int enrollmentId = Integer.parseInt(String.valueOf(val));

        int confirm = JOptionPane.showConfirmDialog(this, "Drop this registration?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        dropBtn.setEnabled(false);
        SwingWorker<Boolean, Void> w = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return service.dropEnrollment(enrollmentId);
            }
            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        JOptionPane.showMessageDialog(MyRegistrationsPanel.this, "Dropped.");
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(MyRegistrationsPanel.this, "Drop failed.");
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(MyRegistrationsPanel.this, "Error: " + ex.getMessage());
                } finally {
                    dropBtn.setEnabled(true);
                }
            }
        };
        w.execute();
    }
}
