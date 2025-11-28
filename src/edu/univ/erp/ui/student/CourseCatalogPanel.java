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

    public CourseCatalogPanel(StudentService service, String userId) {
        this.studentService = service;
        this.currentUserId = userId;
        System.out.println("[DEBUG] CourseCatalogPanel created for user=" + userId);
        Thread.dumpStack();

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("📚 Full Course Catalog");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(0, 102, 204));
        add(title, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{"Code", "Title", "Credits", "Department"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { 
                return false; 
            }
            @Override public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex==2) {
                    return Integer.class;
                } 
                return String.class;
            }
        };

        table=new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        add(scrollPane, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        south.setBackground(Color.WHITE);
        
        refresh=new JButton("🔄 Refresh List");
        register=new JButton("➕ Register for Course");
        
        styleButton(refresh, Color.LIGHT_GRAY, Color.BLACK);
        styleButton(register, new Color(0, 102, 204), Color.WHITE);
        
        south.add(refresh);
        south.add(register);
        add(south, BorderLayout.SOUTH);

        register.setEnabled(false);

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent exception) {
                if (!exception.getValueIsAdjusting()) {
                    boolean select=table.getSelectedRow() >= 0;
                    register.setEnabled(select);
                }
            }
        });

        refresh.addActionListener(e -> loadCatalog());
        register.addActionListener(e -> openRegisterDialog());

        loadCatalog();
    }
    
    private void styleButton(JButton button, Color b, Color f) {
        button.setFocusPainted(false);
        button.setBackground(b);
        button.setForeground(f);
        button.setPreferredSize(new Dimension(160, 35));
    }


    private void loadCatalog() {
        refresh.setEnabled(false);
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{"Loading...", "Please Wait...", null, null});
        
        SwingWorker<List<Course>, Void> wk = new SwingWorker<>() {
            @Override
            protected List<Course> doInBackground() throws Exception {
                return studentService.GetCat();
            }
            @Override
            protected void done() {
                tableModel.setRowCount(0); 
                
                try {
                    List<Course> list = get();
                    if (list!= null) {
                        for (Course c : list) {
                            String deptId = c.GetDepartmentID() != null ? c.GetDepartmentID() : ""; 
                            tableModel.addRow(new Object[]{c.GetCourseID(), c.GetTitle(), c.GetCredits(), deptId});
                        }
                    } 
                    else {
                        JOptionPane.showMessageDialog(CourseCatalogPanel.this, "Catalog is empty.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                } 
                catch (InterruptedException exception) {
                    exception.printStackTrace();
                    JOptionPane.showMessageDialog(CourseCatalogPanel.this, "Load interrupted.");
                } 
                catch (ExecutionException exception) {
                    exception.printStackTrace(); 
                    String msg = exception.getCause() != null ? exception.getCause().toString() : exception.toString();
                    JOptionPane.showMessageDialog(CourseCatalogPanel.this, "Failed to load catalog: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    refresh.setEnabled(true);
                }
            }
        };
        wk.execute();
    }

    private void openRegisterDialog() {
        int s=table.getSelectedRow();
        if (s<0) {
            JOptionPane.showMessageDialog(this, "Select a course first.");
            return;
        }
        int modelRow=table.convertRowIndexToModel(s);
        String code=(String) tableModel.getValueAt(modelRow, 0);
        
        RegisterDialog dialog=new RegisterDialog(
            SwingUtilities.getWindowAncestor(this), 
            studentService, 
            currentUserId, 
            code
        );
        
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        
        if (dialog.isRegisteredSuccessfully()) {
            JOptionPane.showMessageDialog(this, "Registration successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadCatalog(); 
        }
    }
}
