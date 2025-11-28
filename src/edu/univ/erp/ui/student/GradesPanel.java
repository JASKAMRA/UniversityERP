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
    private JButton refreshButton;
    private JTable table;
    private DefaultTableModel model;

    public GradesPanel(StudentService studentService, String userId) {
        this.studentService=studentService;
        this.userId=userId;
        loadGrades();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        JLabel title=new JLabel("💯 My Component & Final Grades");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground( new Color(0, 102, 204));
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{"Course", "Section ID", "Component", "Score", "Final Grade"}, 
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex==3) {
                    return BigDecimal.class;
                 }
                return String.class;
            }
        };

        table=new JTable(model);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        
        JScrollPane scrollPane=new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottom=new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setBackground(Color.WHITE);
        
        refreshButton=new JButton("🔄 Refresh Grades");
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
        model.addRow(new Object[]{"Loading...", "", "", "", ""});
        
        SwingWorker<List<Object[]>, Void> w = new SwingWorker<>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {      
                return studentService.getGrade(userId);
            }

            @Override
            protected void done() {
                model.setRowCount(0); 
                refreshButton.setEnabled(true);
                
                try {
                    List<Object[]> rows = get();
                    if (rows.isEmpty()||rows == null){
                        model.addRow(new Object[]{"No grades recorded.", "", "", "", ""});
                        return;
                    }
                    
                    for (Object[] r : rows) {
                        String course = r[0] == null ? "" : String.valueOf(r[0]);
                        Integer sec = r[1] == null ? null : (Integer) r[1];
                        String comp = r[2] == null ? "" : String.valueOf(r[2]);
                        BigDecimal score = r[3] == null ? null : (BigDecimal) r[3];
                        String finalGrade = r[4] == null ? "" : String.valueOf(r[4]);
                        
                        model.addRow(new Object[]{ 
                            course,sec,comp,score,finalGrade 
                        });
                    }
                } 
                catch (InterruptedException intException) {
                    intException.printStackTrace();
                    JOptionPane.showMessageDialog(GradesPanel.this, "Grade load interrupted", "Error", JOptionPane.ERROR_MESSAGE);
                } 
                catch (ExecutionException execException) {
                    execException.printStackTrace();
                    Throwable cause = execException.getCause();
                    JOptionPane.showMessageDialog(GradesPanel.this, "Failed to load grades: " + (cause == null ? execException.getMessage() : cause.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
                } 
                catch (Exception exception) {
                    exception.printStackTrace();
                    JOptionPane.showMessageDialog(GradesPanel.this, "Failed to load grades: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        w.execute();
    }
}