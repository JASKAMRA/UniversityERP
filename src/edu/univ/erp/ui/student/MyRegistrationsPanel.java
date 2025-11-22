package edu.univ.erp.ui.student;

import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ListSelectionListener;
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

    // --- Aesthetic constants ---
    private static final int PADDING = 15;
    private static final int GAP = 10;
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 20);
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204); // Deep Blue
    private static final Color DROP_COLOR = new Color(220, 50, 50); // Red

    public MyRegistrationsPanel(StudentService service, String userId) {
        this.service = service;
        this.userId = userId;
        initUI();
        loadData();
    }

    private void initUI() {
        // 1. Overall Layout & Padding
        setLayout(new BorderLayout(GAP, GAP));
        setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        setBackground(Color.WHITE);

        // 2. Title (North)
        JLabel title = new JLabel("📝 My Current Registrations");
        title.setFont(TITLE_FONT);
        title.setForeground(PRIMARY_COLOR);
        title.setBorder(new EmptyBorder(0, 0, GAP, 0));
        add(title, BorderLayout.NORTH);

        // 3. Table Setup (Center)
        model = new DefaultTableModel(
                new Object[]{"Enroll ID", "Course", "Section", "Day", "Semester", "Status"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            // Ensure Enroll ID and Section columns are treated as Integer/Number
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

        // 4. Buttons (South)
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP, 0));
        south.setBackground(Color.WHITE);
        
        refreshBtn = new JButton("🔄 Refresh List");
        dropBtn = new JButton("❌ Drop Selected Course");
        dropBtn.setEnabled(false);

        // Style Buttons
        styleButton(refreshBtn, Color.LIGHT_GRAY, Color.BLACK, new Dimension(140, 30));
        styleButton(dropBtn, DROP_COLOR, Color.WHITE, new Dimension(170, 30));
        
        south.add(refreshBtn);
        south.add(dropBtn);
        add(south, BorderLayout.SOUTH);

        // 5. Listeners (Logic Unchanged)
        // Selection listener
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
        // Show loading message
        model.addRow(new Object[]{"Loading...", "", "", "", "", ""});
        
        SwingWorker<List<Object[]>, Void> w = new SwingWorker<>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                // Assuming service.getMyRegistrations returns: [EnrollID, Course, Section, Day, Semester, Status]
                return service.getMyRegistrations(userId);
            }

            @Override
            protected void done() {
                model.setRowCount(0); // Clear loading row
                
                try {
                    List<Object[]> rows = get();
                    if (rows != null) {
                        for (Object[] r : rows) model.addRow(r);
                    } else {
                        JOptionPane.showMessageDialog(MyRegistrationsPanel.this, "No registration data available.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                    String msg = (ex.getCause() != null) ? ex.getCause().getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(MyRegistrationsPanel.this, "Failed to load registrations: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
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
        
        // Ensure index is based on model, not view (if sorting was implemented)
        int modelRow = table.convertRowIndexToModel(r);
        
        Object val = model.getValueAt(modelRow, 0);
        
        // Safety check on data type
        int enrollmentId;
        try {
            enrollmentId = Integer.parseInt(String.valueOf(val));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid enrollment ID in the selected row.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String courseName = String.valueOf(model.getValueAt(modelRow, 1));

        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "<html>Are you sure you want to **DROP** registration for:<br/>**" + courseName + "**?</html>", 
            "❌ Confirm Drop", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
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
                        JOptionPane.showMessageDialog(MyRegistrationsPanel.this, "Successfully Dropped **" + courseName + "**.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(MyRegistrationsPanel.this, "Drop failed. Enrollment may be finalized.", "Failure", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                    String msg = (ex.getCause() != null) ? ex.getCause().getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(MyRegistrationsPanel.this, "Error during drop operation: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    dropBtn.setEnabled(true);
                }
            }
        };
        w.execute();
    }
}