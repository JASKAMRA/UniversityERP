package edu.univ.erp.ui.admin;

import edu.univ.erp.data.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Vector;

/**
 * Section management UI: list sections, edit capacity/day/semester/year/registration_deadline, delete.
 * Table: sections(section_id PK, course_id, instructor_id, day, capacity, semester, year, registration_deadline)
 */
public class SectionManagementPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JButton btnRefresh, btnEdit, btnDelete;

    // --- Aesthetic constants ---
    private static final int PADDING = 20;
    private static final int GAP = 12;
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 20);
    private static final Dimension BUTTON_SIZE = new Dimension(160, 35);
    private static final Color PRIMARY_COLOR = new Color(100, 149, 237); // Cornflower Blue

    public SectionManagementPanel() {
        initUI();
        loadSections();
    }

    private void initUI() {
        // 1. Overall Layout and Padding
        setLayout(new BorderLayout(GAP, GAP));
        setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        setBackground(Color.WHITE);

        // 2. Title Section (North)
        JLabel title = new JLabel("📅 Course Section Management");
        title.setFont(TITLE_FONT);
        title.setBorder(new EmptyBorder(0, 0, GAP, 0));
        add(title, BorderLayout.NORTH);

        // 3. Table Setup (Center)
        model = new DefaultTableModel(new Object[]{"Section ID", "Course ID", "Instructor ID", "Day", "Capacity", "Semester", "Year", "Reg Deadline"}, 0) {
            @Override public boolean isCellEditable(int r, int c){ return false; }
        };
        table = new JTable(model);
        // CORRECTED: Used ListSelectionModel instead of the typo
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); 
        table.setRowHeight(25);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        add(scrollPane, BorderLayout.CENTER);

        // 4. Action Buttons Panel (East)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        buttonPanel.setBackground(Color.WHITE);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(GAP / 2, 0, GAP / 2, 0);
        c.gridx = 0; c.weightx = 1.0;

        btnRefresh = new JButton("🔄 Refresh List");
        btnEdit = new JButton("✏️ Edit Selected");
        btnDelete = new JButton("🗑️ Delete Selected");

        // Apply styling and add buttons
        styleButton(btnRefresh, Color.LIGHT_GRAY, Color.BLACK);
        styleButton(btnEdit, new Color(255, 230, 180), Color.BLACK);
        styleButton(btnDelete, new Color(255, 180, 180), Color.RED);
        
        c.gridy = 0; buttonPanel.add(btnRefresh, c);
        c.gridy = 1; buttonPanel.add(Box.createVerticalStrut(GAP * 2), c); // Separator
        c.gridy = 2; buttonPanel.add(btnEdit, c);
        c.gridy = 3; buttonPanel.add(btnDelete, c);
        
        // Push buttons to the top
        c.gridy = 4; c.weighty = 1.0; 
        buttonPanel.add(Box.createVerticalGlue(), c);
        
        add(buttonPanel, BorderLayout.EAST);

        // 5. Action Listeners
        btnRefresh.addActionListener(e -> loadSections());
        btnEdit.addActionListener(e -> openEditDialog());
        btnDelete.addActionListener(e -> deleteSelected());
    }

    /** Helper method to style buttons */
    private void styleButton(JButton button, Color background, Color foreground) {
        button.setPreferredSize(BUTTON_SIZE);
        button.setMinimumSize(BUTTON_SIZE);
        button.setMaximumSize(BUTTON_SIZE);
        button.setFocusPainted(false);
        button.setBackground(background);
        button.setForeground(foreground);
    }

    private void loadSections() {
        model.setRowCount(0);
        String sql = "SELECT section_id, course_id, instructor_id, day, capacity, semester, year, registration_deadline FROM sections ORDER BY section_id";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("section_id"));
                row.add(rs.getString("course_id"));
                row.add(rs.getObject("instructor_id"));
                row.add(rs.getString("day"));
                row.add(rs.getInt("capacity"));
                row.add(rs.getString("semester"));
                row.add(rs.getInt("year"));
                
                Timestamp ts = rs.getTimestamp("registration_deadline");
                row.add(ts == null ? "" : sdf.format(ts));
                
                model.addRow(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load sections.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openEditDialog() {
        int sel = table.getSelectedRow();
        if (sel < 0) { JOptionPane.showMessageDialog(this, "Select a section."); return; }
        
        // --- Extract current data ---
        int sectionId = Integer.parseInt(model.getValueAt(sel,0).toString());
        String courseId = model.getValueAt(sel,1).toString();
        String instr = String.valueOf(model.getValueAt(sel,2));
        String day = String.valueOf(model.getValueAt(sel,3));
        String capacity = model.getValueAt(sel,4).toString();
        String semester = String.valueOf(model.getValueAt(sel,5));
        String year = model.getValueAt(sel,6).toString();
        String regDeadline = String.valueOf(model.getValueAt(sel,7));
        
        // --- Input Fields ---
        JTextField tfDay = new JTextField(day);
        JTextField tfCapacity = new JTextField(capacity);
        JTextField tfSemester = new JTextField(semester);
        JTextField tfYear = new JTextField(year);
        JTextField tfDeadline = new JTextField(regDeadline); // format yyyy-MM-dd HH:mm:ss
        JTextField tfInstructorUserId = new JTextField(instr.equals("null") ? "" : instr);

        // --- Dialog Layout (Improved) ---
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Helper function for adding row components
        int row = 0;
        
        // Fixed Fields
        gbc.gridx = 0; gbc.gridy = row++; p.add(new JLabel("Section ID:"), gbc);
        gbc.gridx = 1; p.add(new JLabel("**" + sectionId + "**"), gbc);
        
        gbc.gridx = 0; gbc.gridy = row++; p.add(new JLabel("Course ID:"), gbc);
        gbc.gridx = 1; p.add(new JLabel(courseId), gbc);

        // Editable Fields
        gbc.gridx = 0; gbc.gridy = row++; p.add(new JLabel("Instructor user_id (optional):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; p.add(tfInstructorUserId, gbc);
        
        gbc.gridx = 0; gbc.gridy = row++; p.add(new JLabel("Day (e.g., MONDAY):"), gbc);
        gbc.gridx = 1; p.add(tfDay, gbc);
        
        gbc.gridx = 0; gbc.gridy = row++; p.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1; p.add(tfCapacity, gbc);
        
        gbc.gridx = 0; gbc.gridy = row++; p.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 1; p.add(tfSemester, gbc);
        
        gbc.gridx = 0; gbc.gridy = row++; p.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1; p.add(tfYear, gbc);
        
        gbc.gridx = 0; gbc.gridy = row++; p.add(new JLabel("<html>Registration Deadline:<br/>(yyyy-MM-dd HH:mm:ss)</html>"), gbc);
        gbc.gridx = 1; p.add(tfDeadline, gbc);


        int r = JOptionPane.showConfirmDialog(this, p, "✏️ Edit Section: " + sectionId, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;

        // --- Validation and Data Extraction ---
        String newDay = tfDay.getText().trim();
        int newCapacity;
        try { newCapacity = Integer.parseInt(tfCapacity.getText().trim()); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this,"Capacity must be a number"); return; }
        
        String newSemester = tfSemester.getText().trim();
        int newYear;
        try { newYear = Integer.parseInt(tfYear.getText().trim()); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this,"Year must be a number"); return; }
        
        String newDeadline = tfDeadline.getText().trim();
        String instructorUserId = tfInstructorUserId.getText().trim();

        // If instructorUserId provided, try to find instructor_id (Logic Unchanged)
        Integer instructorId = null;
        if (!instructorUserId.isEmpty()) {
            try (Connection conn = DBConnection.getStudentConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT instructor_id FROM instructors WHERE user_id = ? LIMIT 1")) {
                ps.setString(1, instructorUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) instructorId = rs.getInt("instructor_id");
                    else {
                        JOptionPane.showMessageDialog(this, "Instructor user_id not found.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, "Error checking instructor.", "Error", JOptionPane.ERROR_MESSAGE); return; }
        }

        // --- Database Update (Logic Unchanged) ---
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE sections SET instructor_id = ?, day = ?, capacity = ?, semester = ?, year = ?, registration_deadline = ? WHERE section_id = ?")) {
            
            if (instructorId == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, instructorId);
            ps.setString(2, newDay);
            ps.setInt(3, newCapacity);
            ps.setString(4, newSemester);
            ps.setInt(5, newYear);
            
            if (newDeadline.isEmpty()) ps.setNull(6, Types.TIMESTAMP);
            else ps.setTimestamp(6, Timestamp.valueOf(newDeadline.replace('T',' ')));
            
            ps.setInt(7, sectionId);
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Section **" + sectionId + "** updated successfully.");
            loadSections();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Update failed. Check date format (yyyy-MM-dd HH:mm:ss) or data types.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int sel = table.getSelectedRow();
        if (sel < 0) { JOptionPane.showMessageDialog(this, "Select a section."); return; }
        
        int sectionId = Integer.parseInt(model.getValueAt(sel,0).toString());
        
        int c = JOptionPane.showConfirmDialog(this, 
            "<html>Are you sure you want to delete section **" + sectionId + "**?<br/>" +
            "**WARNING:** This action will also delete all associated enrollments and grades!</html>", 
            "🗑️ Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (c != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM sections WHERE section_id = ?")) {
            ps.setInt(1, sectionId);
            int rows = ps.executeUpdate();
            
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Section **" + sectionId + "** deleted.");
            } else {
                JOptionPane.showMessageDialog(this, "Section not found.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
            loadSections();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Delete failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}