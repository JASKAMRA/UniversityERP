package edu.univ.erp.ui.admin;

import edu.univ.erp.data.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;


/**
 * Assign or reassign instructor to a section.
 * Simple UI: choose section, then choose instructor user (by user_id).
 */
public class AssignInstructorPanel extends JPanel {
    private JComboBox<String> cbSection;
    private JComboBox<String> cbInstructor;
    private JButton btnAssign, btnRefresh;

    public AssignInstructorPanel() {
        initUI();
        loadSections();
        loadInstructors();
    }

    private void initUI() {
        setLayout(new BorderLayout(8,8));
        JLabel title = new JLabel("Assign Instructor to Section");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(0,2,6,6));
        cbSection = new JComboBox<>();
        cbInstructor = new JComboBox<>();
        center.add(new JLabel("Section (id - course_id):")); center.add(cbSection);
        center.add(new JLabel("Instructor (user_id - name):")); center.add(cbInstructor);

        add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRefresh = new JButton("Refresh");
        btnAssign = new JButton("Assign");
        bottom.add(btnRefresh); bottom.add(btnAssign);
        add(bottom, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> { loadSections(); loadInstructors(); });
        btnAssign.addActionListener(e -> doAssign());
    }

    private void loadSections() {
        cbSection.removeAllItems();
        String sql = "SELECT s.section_id, s.course_id FROM sections s ORDER BY s.section_id";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String item = rs.getInt("section_id") + " - " + rs.getString("course_id");
                cbSection.addItem(item);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load sections.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadInstructors() {
        cbInstructor.removeAllItems();
        String sql = "SELECT i.instructor_id, i.user_id, i.name FROM instructors i ORDER BY i.name";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String item = rs.getString("user_id") + " - " + rs.getString("name");
                cbInstructor.addItem(item);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load instructors.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doAssign() {
        String secSel = (String) cbSection.getSelectedItem();
        String instSel = (String) cbInstructor.getSelectedItem();
        if (secSel == null || instSel == null) { JOptionPane.showMessageDialog(this, "Select both section and instructor."); return; }

        int sectionId = Integer.parseInt(secSel.split(" - ")[0].trim());
        String instructorUserId = instSel.split(" - ")[0].trim();

        try (Connection conn = DBConnection.getStudentConnection()) {
            // find instructor_id
            Integer instructorId = null;
            try (PreparedStatement pFind = conn.prepareStatement("SELECT instructor_id FROM instructors WHERE user_id = ? LIMIT 1")) {
                pFind.setString(1, instructorUserId);
                try (ResultSet rs = pFind.executeQuery()) {
                    if (rs.next()) instructorId = rs.getInt("instructor_id");
                }
            }
            if (instructorId == null) {
                JOptionPane.showMessageDialog(this, "Instructor not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement ps = conn.prepareStatement("UPDATE sections SET instructor_id = ? WHERE section_id = ?")) {
                ps.setInt(1, instructorId);
                ps.setInt(2, sectionId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Assigned instructor.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Assign failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
