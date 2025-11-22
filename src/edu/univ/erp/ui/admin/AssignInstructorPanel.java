package edu.univ.erp.ui.admin;

import edu.univ.erp.data.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
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

    // --- Aesthetic constants ---
    private static final int PADDING = 20;
    private static final int GAP = 10;
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 20);
    private static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 14);
    private static final Dimension BUTTON_SIZE = new Dimension(120, 30);

    public AssignInstructorPanel() {
        initUI();
        loadSections();
        loadInstructors();
    }

    private void initUI() {
        // 1. Overall Layout and Padding
        setLayout(new BorderLayout(GAP, GAP));
        setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        setBackground(Color.WHITE);

        // 2. Title Section (North)
        JLabel title = new JLabel("🧑‍🏫 Assign Section Instructor");
        title.setFont(TITLE_FONT);
        title.setBorder(new EmptyBorder(0, 0, GAP, 0)); // Padding below the title
        add(title, BorderLayout.NORTH);

        // 3. Center Panel for Form (Using GridBagLayout for precise control)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Assignment Details"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(GAP / 2, GAP, GAP / 2, GAP);
        c.fill = GridBagConstraints.HORIZONTAL;

        // Row 1: Section Selection
        c.gridx = 0; c.gridy = 0; c.weightx = 0.3;
        JLabel sectionLabel = new JLabel("Section (ID - Course ID):");
        sectionLabel.setFont(LABEL_FONT);
        formPanel.add(sectionLabel, c);

        c.gridx = 1; c.gridy = 0; c.weightx = 0.7;
        cbSection = new JComboBox<>();
        formPanel.add(cbSection, c);

        // Row 2: Instructor Selection
        c.gridx = 0; c.gridy = 1; c.weightx = 0.3;
        JLabel instructorLabel = new JLabel("Instructor (User ID - Name):");
        instructorLabel.setFont(LABEL_FONT);
        formPanel.add(instructorLabel, c);

        c.gridx = 1; c.gridy = 1; c.weightx = 0.7;
        cbInstructor = new JComboBox<>();
        formPanel.add(cbInstructor, c);

        // Spacer to push components to the top/center
        c.gridx = 0; c.gridy = 2; c.weighty = 1.0; // Give vertical weight to this empty cell
        formPanel.add(Box.createVerticalGlue(), c);


        // Wrap the formPanel in a flow layout to prevent it from stretching horizontally
        JPanel wrapCenter = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapCenter.setBackground(Color.WHITE);
        wrapCenter.add(formPanel);

        add(wrapCenter, BorderLayout.CENTER);

        // 4. Bottom Panel for Actions (South)
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP, 0)); // Right aligned
        bottom.setBackground(Color.WHITE);

        btnRefresh = new JButton("🔄 Refresh Data");
        btnAssign = new JButton("✅ Assign Instructor");

        // Style Buttons
        styleButton(btnRefresh);
        styleButton(btnAssign);
        btnAssign.setBackground(new Color(60, 140, 240)); // Highlight Assign button
        btnAssign.setForeground(Color.WHITE);


        bottom.add(btnRefresh);
        bottom.add(btnAssign);
        add(bottom, BorderLayout.SOUTH);

        // 5. Action Listeners (Functionality unchanged)
        btnRefresh.addActionListener(e -> { loadSections(); loadInstructors(); });
        btnAssign.addActionListener(e -> doAssign());
    }

    /**
     * Helper method to apply consistent styling to buttons.
     */
    private void styleButton(JButton button) {
        button.setPreferredSize(BUTTON_SIZE);
        button.setFocusPainted(false);
    }

    // --- Existing Functionality Methods (Unchanged logic) ---

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
        if (secSel == null || instSel == null) { JOptionPane.showMessageDialog(this, "Select both section and instructor.", "Warning", JOptionPane.WARNING_MESSAGE); return; }

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
                JOptionPane.showMessageDialog(this, "Instructor not found. Check user data.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement ps = conn.prepareStatement("UPDATE sections SET instructor_id = ? WHERE section_id = ?")) {
                ps.setInt(1, instructorId);
                ps.setInt(2, sectionId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Successfully assigned instructor to section " + sectionId + ".");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Assign failed due to database error.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}