package edu.univ.erp.ui.instructor;
import edu.univ.erp.service.InstructorService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class MySectionsPanel extends JPanel {
    private final InstructorService instructorService;
    private final String currentUserId; 
    private JTable table;
    private DefaultTableModel model;
    private JButton btnRefresh;
    private JToggleButton toggleMyOnly;

    public MySectionsPanel(InstructorService service, String currentUserId) {
        this.instructorService=service;
        this.currentUserId=currentUserId;
        initComponents();
        loadSections(); 
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15,15, 15));
        setBackground(Color.WHITE);
        JLabel title = new JLabel("🗓️ Section Browser");
        title.setFont( new Font("Arial", Font.BOLD, 18));
        title.setForeground( new Color(70, 130, 180));
        add(title, BorderLayout.NORTH);

        JPanel topBar=new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topBar.setBackground(Color.WHITE);

        toggleMyOnly=new JToggleButton("Filter: My Sections Only");
        toggleMyOnly.setBackground(new Color(255, 255, 200)); 
        toggleMyOnly.setFocusPainted(false);

        btnRefresh=new JButton("🔄 Refresh");
        btnRefresh.setBackground(Color.LIGHT_GRAY);
        btnRefresh.setFocusPainted(false);

        topBar.add(toggleMyOnly);
        topBar.add(btnRefresh);
        JButton btnOpenGrades=new JButton("📘 Open Grades");
        btnOpenGrades.setBackground(new Color(200,220,255));
        btnOpenGrades.setFocusPainted(false);
        topBar.add(btnOpenGrades);
        btnOpenGrades.addActionListener(e -> openGradebookForSelectedSection());

        JPanel controlsWrapper=new JPanel(new BorderLayout());
        controlsWrapper.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "View Options"));
        controlsWrapper.add(topBar, BorderLayout.CENTER);
        add(controlsWrapper, BorderLayout.NORTH);

        model=new DefaultTableModel(new Object[]{
                "Section ID", "Course ID", "Course Title", "Day", "Semester", "Year", "Capacity"
        }, 0) {

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 5 || columnIndex == 6) return Integer.class;
                return String.class;
            }
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
            
        };

        table=new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        
        JScrollPane scPane=new JScrollPane(table);
        scPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        add(scPane, BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> loadSections());
        toggleMyOnly.addActionListener(e -> loadSections());
    }

    private void openGradebookForSelectedSection() {
    Integer sec_id=getSelectedSectionId();
    if (sec_id==null) {
        JOptionPane.showMessageDialog(this, "Select a section first.", "No selection", JOptionPane.WARNING_MESSAGE);
        return;
    }

    int select = table.getSelectedRow();
    String courseTitle="";
        try {
            Object ct=model.getValueAt(select, 2);
            courseTitle=ct == null ? "" : ct.toString();
        } catch (Exception ignored) {}

        GradebookPanel gbook=new GradebookPanel(instructorService, sec_id, courseTitle, currentUserId);
        JDialog dialog=new JDialog(SwingUtilities.getWindowAncestor(this), "Gradebook - Section " + sec_id, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(gbook, BorderLayout.CENTER);
        dialog.pack();
        dialog.setSize(1000, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public void loadSections() {
        SwingUtilities.invokeLater(() -> {
            model.setRowCount(0);
            List<Map<String,Object>> rows=null;
            boolean myOnly=toggleMyOnly.isSelected();

            model.addRow(new Object[]{"Loading...", "Please Wait...", "", "", "", "", ""});
            
            try {
                if (myOnly) {
                    rows = instructorService.GetAssgnSec(currentUserId);
                } else {
                    try {
                        rows = instructorService.GetAllSec();
                    } catch (AbstractMethodError | UnsupportedOperationException ame) {
                        rows = instructorService.GetAssgnSec(currentUserId);
                        JOptionPane.showMessageDialog(this, "Full section list unavailable; defaulting to your assigned sections.", "Info", JOptionPane.INFORMATION_MESSAGE);
                        toggleMyOnly.setSelected(true); 
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                try {
                    rows = instructorService.GetAssgnSec(currentUserId);
                } catch (Exception exception2) {
                    exception2.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Failed to load sections. Check DB connection.", "Error", JOptionPane.ERROR_MESSAGE);
                    rows = null;
                }
            }
            model.setRowCount(0); 

            if (rows.isEmpty() || rows == null) {
                 model.addRow(new Object[]{"No sections found.", "", "", "", "", "", ""});
                 return;
            }

            for (Map<String,Object> r : rows) {
                model.addRow(new Object[]{
                        safeGetIntOrNull(r.get("section_id")),
                        safeToString(r.get("course_id")),
                        safeToString(r.get("course_title")),
                        safeToString(r.get("day")),
                        safeToString(r.get("semester")),
                        safeGetIntOrNull(r.get("year")),
                        safeGetIntOrNull(r.get("capacity"))
                });
            }
        });
    }

    private Integer safeGetIntOrNull(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Integer) {
            return (Integer) o;
        }
        if (o instanceof Number) {
            return ((Number) o).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception ex) {
            return null;
        }
    }

    private String safeToString(Object o) {
        return o==null ? "" : o.toString();
    }
    public Integer getSelectedSectionId() {
        int select = table.getSelectedRow();
        if (select < 0) {
            return null;
        }
        Object val = model.getValueAt(select, 0);
        return safeGetIntOrNull(val);
    }

    public JTable getTable() {
         return table; 
    }
    public DefaultTableModel getModel() {
         return model; 
    }

}