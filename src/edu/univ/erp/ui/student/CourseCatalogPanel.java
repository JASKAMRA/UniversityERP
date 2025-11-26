package edu.univ.erp.ui.student;

import edu.univ.erp.domain.Course;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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

    // --- Aesthetic constants ---
    private static final int PADDING = 15;
    private static final int GAP = 10;
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 20);
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204); // Deep Blue

    public CourseCatalogPanel(StudentService service, String userId) {
        this.studentService = service;
        this.currentUserId = userId;

        // debug - will print stack trace so you can see who created it (maintained)
        System.out.println("[DEBUG] CourseCatalogPanel created for user=" + userId);
        Thread.dumpStack();

        // 1. Overall Layout & Padding
        setLayout(new BorderLayout(GAP, GAP));
        setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        setBackground(Color.WHITE);

        // 2. Title (North)
        JLabel title = new JLabel("📚 Full Course Catalog");
        title.setFont(TITLE_FONT);
        title.setForeground(PRIMARY_COLOR);
        add(title, BorderLayout.NORTH);

        // 3. Table Setup (Center)
        tableModel = new DefaultTableModel(new Object[]{"Code", "Title", "Credits", "Department"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) return Integer.class; // Credits column
                return String.class;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        add(scrollPane, BorderLayout.CENTER);

        // 4. Action Buttons (South)
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP, 0));
        south.setBackground(Color.WHITE);
        
        refresh = new JButton("🔄 Refresh List");
        register = new JButton("➕ Register for Course");
        
        // Style Buttons
        styleButton(refresh, Color.LIGHT_GRAY, Color.BLACK);
        styleButton(register, PRIMARY_COLOR, Color.WHITE);
        
        south.add(refresh);
        south.add(register);
        add(south, BorderLayout.SOUTH);

        // 5. Initial State & Listeners
        register.setEnabled(false);

        // Selection listener: enable register only when row selected
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    boolean sel = table.getSelectedRow() >= 0;
                    register.setEnabled(sel);
                }
            }
        });

        // Actions
        refresh.addActionListener(e -> loadCatalog());
        register.addActionListener(e -> openRegisterDialog());

        // Initial load
        loadCatalog();
    }
    
    private void styleButton(JButton button, Color bg, Color fg) {
        button.setFocusPainted(false);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setPreferredSize(new Dimension(160, 35));
    }


    private void loadCatalog() {
        refresh.setEnabled(false);
        // Temporarily clear table and show loading message
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{"Loading...", "Please Wait...", null, null});
        
        SwingWorker<List<Course>, Void> w = new SwingWorker<>() {
            @Override
            protected List<Course> doInBackground() throws Exception {
                return studentService.GetCatalog();
            }
            @Override
            protected void done() {
                // Clear the temporary loading row
                tableModel.setRowCount(0); 
                
                try {
                    List<Course> list = get();
                    if (list != null) {
                        for (Course c : list) {
                            // Assuming Course object has GetDepartmentID or similar
                            String deptId = c.GetDepartmentID() != null ? c.GetDepartmentID() : ""; 
                            tableModel.addRow(new Object[]{c.GetCourseID(), c.GetTitle(), c.GetCredits(), deptId});
                        }
                    } else {
                        JOptionPane.showMessageDialog(CourseCatalogPanel.this, "Catalog is empty.", "Info", JOptionPane.INFORMATION_MESSAGE);
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
        
        // Ensure index is based on model, not view (if sorting was implemented)
        int modelRow = table.convertRowIndexToModel(sel);
        
        String code = (String) tableModel.getValueAt(modelRow, 0);
        
        RegisterDialog dlg = new RegisterDialog(
            SwingUtilities.getWindowAncestor(this), 
            studentService, 
            currentUserId, 
            code
        );
        
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        
        if (dlg.isRegisteredSuccessfully()) {
            JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadCatalog(); // Refresh catalog after registration
        }
    }
}
