package edu.univ.erp.ui.instructor;

import edu.univ.erp.service.InstructorService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * MySectionsPanel
 *
 * - Shows either ALL sections (default) or only sections assigned to current instructor
 * - Toggle button "My sections only" switches query (no "Owned" column)
 * - Uses InstructorService.getAllSections() and getAssignedSections(userId)
 * - Falls back to getAssignedSections() if getAllSections() not available
 */
public class MySectionsPanel extends JPanel {
    private final InstructorService instructorService;
    private final String currentUserId; // instructor's user_id from auth DB

    private JTable table;
    private DefaultTableModel model;
    private JButton btnRefresh;
    private JToggleButton toggleMyOnly;

    // --- Aesthetic constants ---
    private static final int PADDING = 15;
    private static final int GAP = 10;
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 18);
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180); // Steel Blue

    public MySectionsPanel(InstructorService service, String currentUserId) {
        this.instructorService = service;
        this.currentUserId = currentUserId;
        initComponents();
        loadSections(); // load default view (All sections, or assigned if All is unsupported)
    }

    private void initComponents() {
        // 1. Overall Layout & Padding
        setLayout(new BorderLayout(GAP, GAP));
        setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        setBackground(Color.WHITE);

        // 2. Title (North)
        JLabel title = new JLabel("🗓️ Section Browser");
        title.setFont(TITLE_FONT);
        title.setForeground(PRIMARY_COLOR);
        add(title, BorderLayout.NORTH);

        // 3. Top control bar (Filter/Refresh)
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, 0));
        topBar.setBackground(Color.WHITE);
        
        // Toggle Button
        toggleMyOnly = new JToggleButton("Filter: My Sections Only");
        toggleMyOnly.setBackground(new Color(255, 255, 200)); // Light yellow for toggle
        toggleMyOnly.setFocusPainted(false);
        
        // Refresh Button
        btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.setBackground(Color.LIGHT_GRAY);
        btnRefresh.setFocusPainted(false);

        topBar.add(toggleMyOnly);
        topBar.add(btnRefresh);
        
        // Add a titled panel wrapper for controls
        JPanel controlsWrapper = new JPanel(new BorderLayout());
        controlsWrapper.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "View Options"));
        controlsWrapper.add(topBar, BorderLayout.CENTER);
        add(controlsWrapper, BorderLayout.NORTH);


        // 4. Table model and setup
        model = new DefaultTableModel(new Object[]{
                "Section ID", "Course ID", "Course Title", "Day", "Semester", "Year", "Capacity"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
            // Define column classes for proper alignment/sorting
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 5 || columnIndex == 6) return Integer.class;
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

        // 5. Wire actions
        btnRefresh.addActionListener(e -> loadSections());
        toggleMyOnly.addActionListener(e -> loadSections());
    }

    /**
     * Loads sections according to toggle state:
     * - if toggleMyOnly is selected -> instructorService.getAssignedSections(currentUserId)
     * - else -> instructorService.getAllSections() (fallbacks to assigned if not implemented)
     */
    public void loadSections() {
        SwingUtilities.invokeLater(() -> {
            model.setRowCount(0);
            List<Map<String,Object>> rows = null;
            boolean myOnly = toggleMyOnly.isSelected();
            
            // Show loading row
            model.addRow(new Object[]{"Loading...", "Please Wait...", "", "", "", "", ""});
            
            try {
                if (myOnly) {
                    rows = instructorService.getAssignedSections(currentUserId);
                } else {
                    // Prefer getAllSections(); fallback to assignedSections if not implemented
                    try {
                        rows = instructorService.getAllSections();
                    } catch (AbstractMethodError | UnsupportedOperationException ame) {
                        // Fallback logic
                        rows = instructorService.getAssignedSections(currentUserId);
                        // Optional: Show warning that full list is unavailable
                        JOptionPane.showMessageDialog(this, "Full section list unavailable; defaulting to your assigned sections.", "Info", JOptionPane.INFORMATION_MESSAGE);
                        toggleMyOnly.setSelected(true); // Ensure toggle reflects actual data loaded
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                // If anything goes wrong, try one last fallback to assigned sections
                try {
                    rows = instructorService.getAssignedSections(currentUserId);
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Failed to load sections. Check DB connection.", "Error", JOptionPane.ERROR_MESSAGE);
                    rows = null;
                }
            }
            
            model.setRowCount(0); // Clear loading row

            if (rows == null || rows.isEmpty()) {
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
        if (o == null) return null;
        if (o instanceof Integer) return (Integer) o;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (Exception ex) {
            return null;
        }
    }

    private String safeToString(Object o) {
        return o == null ? "" : o.toString();
    }

    /**
     * Returns the selected section id (or null).
     */
    public Integer getSelectedSectionId() {
        int sel = table.getSelectedRow();
        if (sel < 0) return null;
        Object v = model.getValueAt(sel, 0);
        return safeGetIntOrNull(v);
    }

    /**
     * Expose table/model for callers (e.g., InstructorDashboardPanel)
     */
    public JTable getTable() { return table; }
    public DefaultTableModel getModel() { return model; }

}