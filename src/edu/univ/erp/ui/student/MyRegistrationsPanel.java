package edu.univ.erp.ui.student;

import edu.univ.erp.service.StudentService;
import edu.univ.erp.data.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MyRegistrationsPanel extends JPanel {

    private final StudentService service;
    private final String userId;

    private DefaultTableModel model;
    private JTable table;
    private JButton refreshBtn;
    private JButton dropBtn;

    private static final int PADDING = 15;
    private static final int GAP = 10;
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 20);
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color DROP_COLOR = new Color(220, 50, 50);

    public MyRegistrationsPanel(StudentService service, String userId) {
        this.service = service;
        this.userId = userId;
        initUI();
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout(GAP, GAP));
        setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("📝 My Current Registrations");
        title.setFont(TITLE_FONT);
        title.setForeground(PRIMARY_COLOR);
        title.setBorder(new EmptyBorder(0, 0, GAP, 0));
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{"Enroll ID", "Course", "Section", "Days", "Semester", "Status"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }

            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 2) return Integer.class;
                return String.class;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        add(scrollPane, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP, 0));
        south.setBackground(Color.WHITE);

        refreshBtn = new JButton("🔄 Refresh List");
        dropBtn = new JButton("❌ Drop Selected Course");
        dropBtn.setEnabled(false);

        styleButton(refreshBtn, Color.LIGHT_GRAY, Color.BLACK, new Dimension(140, 30));
        styleButton(dropBtn, DROP_COLOR, Color.WHITE, new Dimension(170, 30));

        south.add(refreshBtn);
        south.add(dropBtn);
        add(south, BorderLayout.SOUTH);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                dropBtn.setEnabled(table.getSelectedRow() >= 0);
            }
        });

        refreshBtn.addActionListener(e -> loadData());
        dropBtn.addActionListener(e -> dropSelected());
    }

    private void styleButton(JButton button, Color bg, Color fg, Dimension size) {
        button.setFocusPainted(false);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setPreferredSize(size);
    }

    private void loadData() {
        model.setRowCount(0);
        refreshBtn.setEnabled(false);
        model.addRow(new Object[]{"Loading...", "", "", "", "", ""});

        SwingWorker<List<Object[]>, Void> w = new SwingWorker<>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                return service.GetReg(userId);
            }

            @Override
            protected void done() {
                model.setRowCount(0);

                try {
                    List<Object[]> rows = get();
                    if (rows != null) {
                        for (Object[] r : rows) model.addRow(r);  // r[3] = days (CSV string)
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(MyRegistrationsPanel.this,
                            "Failed to load registrations: " +
                                    (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()),
                            "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    refreshBtn.setEnabled(true);
                }
            }
        };
        w.execute();
    }

    private void dropSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        int modelRow = table.convertRowIndexToModel(row);
        int enrollmentId;

        try {
            enrollmentId = Integer.parseInt(String.valueOf(model.getValueAt(modelRow, 0)));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid enrollment ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String courseName = String.valueOf(model.getValueAt(modelRow, 1));

        // check the registration_deadline for this enrollment's section
        Timestamp regDeadline = null;
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT s.registration_deadline FROM enrollments e JOIN sections s ON e.section_id = s.section_id WHERE e.enrollment_id = ? LIMIT 1")) {
            ps.setInt(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    regDeadline = rs.getTimestamp("registration_deadline");
                } else {
                    JOptionPane.showMessageDialog(this, "Enrollment not found in DB.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            int proceed = JOptionPane.showConfirmDialog(this, "Could not verify registration deadline due to error. Proceed to drop?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (proceed != JOptionPane.YES_OPTION) return;
        }

        if (regDeadline != null) {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            if (now.after(regDeadline)) {
                JOptionPane.showMessageDialog(this, "Cannot drop: registration deadline has passed (" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(regDeadline.getTime())) + ").", "Drop blocked", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Plain-text confirmation (no HTML)
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Drop " + courseName + "?",
                "Confirm Drop",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        dropBtn.setEnabled(false);

        SwingWorker<Boolean, Void> w = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return service.looseEnroll(enrollmentId);
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        JOptionPane.showMessageDialog(MyRegistrationsPanel.this,
                                "Dropped: " + courseName);
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(MyRegistrationsPanel.this,
                                "Drop failed. It may have been disallowed by the server (deadline/maintenance).", "Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MyRegistrationsPanel.this,
                            "Drop error: " +
                                    (ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()));
                } finally {
                    dropBtn.setEnabled(true);
                }
            }
        };

        w.execute();
    }
}
