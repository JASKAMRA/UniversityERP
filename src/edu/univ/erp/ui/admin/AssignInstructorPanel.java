package edu.univ.erp.ui.admin;

import edu.univ.erp.data.DBConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.*;

public class AssignInstructorPanel extends JPanel{
    private JComboBox<String> Section;
    private JComboBox<String> instructor;
    private JButton Assign_bttn, Refresh_bttn;

    public AssignInstructorPanel() {
        INITUI();
        Load_Section();
        Load_Instructors();
    }

    private void INITUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Assign Section Instructor");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBorder(new EmptyBorder(0, 0, 10, 0)); 
        add(title,BorderLayout.NORTH);

        JPanel FormPanel = new JPanel(new GridBagLayout());
        FormPanel.setBackground(Color.WHITE);
        FormPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Assignment Details"));

        GridBagConstraints Constraint = new GridBagConstraints();
        Constraint.insets = new Insets(5, 10, 5, 10);
        Constraint.gridx = 0; Constraint.gridy = 0; Constraint.weightx = 0.3;
        Constraint.fill = GridBagConstraints.HORIZONTAL;

        JLabel Section_Label=new JLabel("Section (ID - Course ID):");
        Section_Label.setFont(new Font("Arial", Font.PLAIN, 14));
        FormPanel.add(Section_Label,Constraint);

        Constraint.gridx=1;Constraint.gridy=0;Constraint.weightx=0.7;
        Section=new JComboBox<>();
        FormPanel.add(Section,Constraint);


        Constraint.gridx=0;Constraint.gridy=1;Constraint.weightx=0.3;
        JLabel instructorLabel=new JLabel("Instructor:");
        instructorLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        FormPanel.add(instructorLabel, Constraint);

        Constraint.gridx=1;Constraint.gridy=1;Constraint.weightx=0.7;
        instructor=new JComboBox<>();
        FormPanel.add(instructor,Constraint);

        Constraint.gridx=0;Constraint.gridy=2;Constraint.weighty=1.0; 
        FormPanel.add(Box.createVerticalGlue(),Constraint);

        JPanel CENTER_wrap=new JPanel(new FlowLayout(FlowLayout.CENTER));
        CENTER_wrap.setBackground(Color.WHITE);
        CENTER_wrap.add(FormPanel);
        add(CENTER_wrap, BorderLayout.CENTER);
        JPanel Bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); 
        Bottom.setBackground(Color.WHITE);
        Refresh_bttn = new JButton("🔄 Refresh Data");
        Assign_bttn = new JButton("✅ Assign Instructor");
        Style_Button(Refresh_bttn);
        Style_Button(Assign_bttn);
        Assign_bttn.setBackground(new Color(60, 140, 240)); 
        Assign_bttn.setForeground(Color.WHITE);
        Bottom.add(Refresh_bttn);
        Bottom.add(Assign_bttn);
        add(Bottom,BorderLayout.SOUTH);
        Refresh_bttn.addActionListener(e->{Load_Section();Load_Instructors(); });
        Assign_bttn.addActionListener(e->doAssign());
    }
    private void Style_Button(JButton button) {
        button.setPreferredSize(new Dimension(120, 30));
        button.setFocusPainted(false);
    }


    private void Load_Section() {
        Section.removeAllItems();
        String sql = "SELECT s.section_id, s.course_id FROM sections s ORDER BY s.section_id";
        try (Connection conn=DBConnection.getStudentConnection();
             PreparedStatement prepared_statement=conn.prepareStatement(sql);
             ResultSet rs=prepared_statement.executeQuery()) {
            while (rs.next()) {
                String item=rs.getInt("Section_id")+" - " + rs.getString("Course_id");
                Section.addItem(item);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load sections.", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void Load_Instructors() {
        instructor.removeAllItems();
        String sql = "SELECT i.instructor_id, i.user_id, i.name FROM instructors i ORDER BY i.name";
        try (Connection conn = DBConnection.getStudentConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String item = rs.getString("user_id") + " - " + rs.getString("name");
                instructor.addItem(item);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load instructors.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doAssign() {
        String secSel = (String) Section.getSelectedItem();
        String instSel = (String) instructor.getSelectedItem();
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