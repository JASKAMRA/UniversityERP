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
 * Section management UI: list sections, edit capacity/days/start_time/end_time/semester/year/registration_deadline, delete, add
 * HTML tags removed from dialogs/labels.
 */
public class SectionManagementPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private JButton btnRefresh, btnEdit, btnDelete, btnAdd;

    private static final int PADDING = 20;
    private static final int GAP = 12;
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 20);
    private static final Dimension BUTTON_SIZE = new Dimension(160, 35);

    public SectionManagementPanel() {
        initUI();
        loadSections();
    }

    private void initUI() {
        setLayout(new BorderLayout(GAP, GAP));
        setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Course Section Management");
        title.setFont(TITLE_FONT);
        title.setBorder(new EmptyBorder(0, 0, GAP, 0));
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{
                "Section ID", "Course ID", "Instructor ID", "Days", "Start", "End", "Capacity", "Semester", "Year", "Reg Deadline"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c){ return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        buttonPanel.setBackground(Color.WHITE);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(GAP / 2, 0, GAP / 2, 0);
        c.gridx = 0; c.weightx = 1.0;

        btnRefresh = new JButton("Refresh List");
        btnEdit = new JButton("Edit Selected");
        btnDelete = new JButton("Delete Selected");
        btnAdd = new JButton("Add Section");

        styleButton(btnRefresh, Color.LIGHT_GRAY, Color.BLACK);
        styleButton(btnEdit, new Color(255, 230, 180), Color.BLACK);
        styleButton(btnDelete, new Color(255, 180, 180), Color.RED);
        styleButton(btnAdd, new Color(200, 240, 200), Color.BLACK);

        c.gridy = 0; buttonPanel.add(btnRefresh, c);
        c.gridy = 1; buttonPanel.add(Box.createVerticalStrut(GAP * 2), c);
        c.gridy = 2; buttonPanel.add(btnEdit, c);
        c.gridy = 3; buttonPanel.add(btnDelete, c);
        c.gridy = 4; buttonPanel.add(Box.createVerticalStrut(GAP), c);
        c.gridy = 5; buttonPanel.add(btnAdd, c);
        c.gridy = 6; c.weighty = 1.0; buttonPanel.add(Box.createVerticalGlue(), c);

        add(buttonPanel, BorderLayout.EAST);

        btnRefresh.addActionListener(e -> loadSections());
        btnEdit.addActionListener(e -> openEditDialog());
        btnDelete.addActionListener(e -> deleteSelected());
        btnAdd.addActionListener(e -> openAddDialog());
    }

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
        String sql = "SELECT section_id, course_id, instructor_id, days, start_time, end_time, capacity, semester, year, registration_deadline FROM sections ORDER BY section_id";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("section_id"));
                row.add(rs.getString("course_id"));
                Object instrObj = rs.getObject("instructor_id");
                row.add(instrObj == null ? "" : String.valueOf(instrObj));
                row.add(rs.getString("days"));
                row.add(rs.getString("start_time"));
                row.add(rs.getString("end_time"));

                int cap = rs.getInt("capacity");
                row.add(rs.wasNull() ? null : cap);

                row.add(rs.getString("semester"));

                int yr = rs.getInt("year");
                row.add(rs.wasNull() ? null : yr);

                Timestamp ts = rs.getTimestamp("registration_deadline");
                row.add(ts == null ? "" : sdf.format(ts));

                model.addRow(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load sections.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openAddDialog() {
        CreateSectionDialog dlg = new CreateSectionDialog(SwingUtilities.getWindowAncestor(this));
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        if (dlg.isSucceeded()) {
            loadSections();
            JOptionPane.showMessageDialog(this, "Created section id: " + dlg.getCreatedSectionId());
        }
    }

    private void openEditDialog() {
        int sel = table.getSelectedRow();
        if (sel < 0) { JOptionPane.showMessageDialog(this, "Select a section."); return; }

        // convert view index -> model index (safe for sorting)
        int modelRow = table.convertRowIndexToModel(sel);

        int sectionId = Integer.parseInt(String.valueOf(model.getValueAt(modelRow, 0)));
        String courseId = model.getValueAt(modelRow, 1) == null ? "" : model.getValueAt(modelRow, 1).toString();
        String instr = model.getValueAt(modelRow, 2) == null ? "" : model.getValueAt(modelRow, 2).toString();
        String days = model.getValueAt(modelRow, 3) == null ? "" : model.getValueAt(modelRow, 3).toString();
        String start = model.getValueAt(modelRow, 4) == null ? "" : model.getValueAt(modelRow, 4).toString();
        String end = model.getValueAt(modelRow, 5) == null ? "" : model.getValueAt(modelRow, 5).toString();
        String capacity = model.getValueAt(modelRow, 6) == null ? "" : model.getValueAt(modelRow, 6).toString();
        String semester = model.getValueAt(modelRow, 7) == null ? "" : model.getValueAt(modelRow, 7).toString();
        String year = model.getValueAt(modelRow, 8) == null ? "" : model.getValueAt(modelRow, 8).toString();
        String regDeadline = model.getValueAt(modelRow, 9) == null ? "" : model.getValueAt(modelRow, 9).toString();

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        int row = 0;

        gbc.gridx = 0; gbc.gridy = row; p.add(new JLabel("Course ID:"), gbc);
        gbc.gridx = 1; p.add(new JLabel(courseId), gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; p.add(new JLabel("Instructor ID (numeric, optional):"), gbc);
        JTextField tfInstr = new JTextField(instr); gbc.gridx = 1; p.add(tfInstr, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; p.add(new JLabel("Days (comma sep e.g. MONDAY,WEDNESDAY):"), gbc);
        JTextField tfDays = new JTextField(days); gbc.gridx = 1; p.add(tfDays, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; p.add(new JLabel("Start Time (HH:mm):"), gbc);
        JTextField tfStart = new JTextField(start); gbc.gridx = 1; p.add(tfStart, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; p.add(new JLabel("End Time (HH:mm):"), gbc);
        JTextField tfEnd = new JTextField(end); gbc.gridx = 1; p.add(tfEnd, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; p.add(new JLabel("Capacity:"), gbc);
        JTextField tfCapacity = new JTextField(capacity); gbc.gridx = 1; p.add(tfCapacity, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; p.add(new JLabel("Semester:"), gbc);
        JTextField tfSem = new JTextField(semester); gbc.gridx = 1; p.add(tfSem, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; p.add(new JLabel("Year:"), gbc);
        JTextField tfYear = new JTextField(year); gbc.gridx = 1; p.add(tfYear, gbc); row++;

        gbc.gridx = 0; gbc.gridy = row; p.add(new JLabel("Registration Deadline (yyyy-MM-dd HH:mm:ss):"), gbc);
        JTextField tfDead = new JTextField(regDeadline); gbc.gridx = 1; p.add(tfDead, gbc); row++;

        int r = JOptionPane.showConfirmDialog(this, p, "Edit Section: " + sectionId, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;

        String newInstr = tfInstr.getText().trim();
        String newDays = tfDays.getText().trim();
        String newStart = tfStart.getText().trim();
        String newEnd = tfEnd.getText().trim();
        Integer newCapacity = null;
        try { if (!tfCapacity.getText().trim().isEmpty()) newCapacity = Integer.parseInt(tfCapacity.getText().trim()); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this,"Capacity must be a number"); return; }
        String newSem = tfSem.getText().trim();
        Integer newYear = null;
        try { if (!tfYear.getText().trim().isEmpty()) newYear = Integer.parseInt(tfYear.getText().trim()); }
        catch (Exception ex) { JOptionPane.showMessageDialog(this,"Year must be a number"); return; }
        String newDead = tfDead.getText().trim();

        Integer instrId = null;
        if (!newInstr.isEmpty()) {
            try {
                instrId = Integer.parseInt(newInstr);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Instructor ID must be numeric (the numeric PK from instructors table).", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE sections SET instructor_id = ?, days = ?, start_time = ?, end_time = ?, capacity = ?, semester = ?, year = ?, registration_deadline = ? WHERE section_id = ?")) {

            if (instrId == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, instrId);
            ps.setString(2, newDays.isEmpty()?null:newDays);
            ps.setString(3, newStart.isEmpty()?null:newStart);
            ps.setString(4, newEnd.isEmpty()?null:newEnd);
            if (newCapacity == null) ps.setNull(5, Types.INTEGER); else ps.setInt(5, newCapacity);
            ps.setString(6, newSem);
            if (newYear == null) ps.setNull(7, Types.INTEGER); else ps.setInt(7, newYear);
            if (newDead.isEmpty()) ps.setNull(8, Types.TIMESTAMP); else ps.setTimestamp(8, Timestamp.valueOf(newDead.replace('T',' ')));
            ps.setInt(9, sectionId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Section updated.");
            loadSections();
        } catch (Exception ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage()); }
    }

    private void deleteSelected() {
        int selView = table.getSelectedRow();
        if (selView < 0) { JOptionPane.showMessageDialog(this, "Select a section."); return; }

        // convert view index -> model index
        int modelRow = table.convertRowIndexToModel(selView);
        int sectionId = Integer.parseInt(String.valueOf(model.getValueAt(modelRow,0)));

        int c = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete section " + sectionId + "?\nThis will also delete associated enrollments and grades.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM sections WHERE section_id = ?")) {
            ps.setInt(1, sectionId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Section deleted.");
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
