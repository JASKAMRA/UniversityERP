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
    private JButton Assign_btn, Refresh_btn;

    public AssignInstructorPanel() {
        INITUI();
        Load_Section();
        Load_Instructors();
    }

    private void INITUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        JLabel title=new JLabel("Assign Section Instructor");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setBorder(new EmptyBorder(0, 0, 10, 0)); 
        add(title,BorderLayout.NORTH);

        JPanel FPanel=new JPanel(new GridBagLayout());
        FPanel.setBackground(Color.WHITE);
        FPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Assignment Details"));

        GridBagConstraints Constraint=new GridBagConstraints();
        Constraint.insets=new Insets(5, 10, 5, 10);
        Constraint.gridx=0; Constraint.gridy = 0; Constraint.weightx = 0.3;
        Constraint.fill=GridBagConstraints.HORIZONTAL;

        JLabel Section_Label=new JLabel("Section (ID - Course ID):");
        Section_Label.setFont(new Font("Arial", Font.PLAIN, 14));
        FPanel.add(Section_Label,Constraint);

        Constraint.gridx=1;
        Constraint.gridy=0;
        Constraint.weightx=0.7;
        Section=new JComboBox<>();
        FPanel.add(Section,Constraint);

        Constraint.gridx=0;
        Constraint.gridy=1;
        Constraint.weightx=0.3;
        JLabel instructorLabel=new JLabel("Instructor:");
        instructorLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        FPanel.add(instructorLabel, Constraint);

        Constraint.gridx=1;
        Constraint.gridy=1;
        Constraint.weightx=0.7;
        instructor=new JComboBox<>();
        FPanel.add(instructor,Constraint);

        Constraint.gridx=0;
        Constraint.gridy=2;
        Constraint.weighty=1.0; 
        FPanel.add(Box.createVerticalGlue(),Constraint);

        JPanel CENTER_wrap=new JPanel(new FlowLayout(FlowLayout.CENTER));
        CENTER_wrap.setBackground(Color.WHITE);
        CENTER_wrap.add(FPanel);
        add(CENTER_wrap, BorderLayout.CENTER);
        JPanel Bottom=new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); 
        Bottom.setBackground(Color.WHITE);
        Refresh_btn=new JButton("🔄 Refresh Data");
        Assign_btn= new JButton("✅ Assign Instructor");
        Style_Button(Refresh_btn);
        Style_Button(Assign_btn);
        Assign_btn.setBackground(new Color(60, 140, 240)); 
        Assign_btn.setForeground(Color.WHITE);
        Bottom.add(Refresh_btn);
        Bottom.add(Assign_btn);
        add(Bottom,BorderLayout.SOUTH);
        Refresh_btn.addActionListener(e->{Load_Section();Load_Instructors(); });
        Assign_btn.addActionListener(e->doAssign());
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
             ResultSet resultSet=prepared_statement.executeQuery()) {
            while (resultSet.next()) {
                String item=resultSet.getInt("Section_id")+" - " + resultSet.getString("Course_id");
                Section.addItem(item);
            }
        } 
        catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(
                this,
                 "Failed to load sections.",
                  "ERROR", JOptionPane.ERROR_MESSAGE
                );
        }
    }

    private void Load_Instructors() {
        instructor.removeAllItems();
        String sql = "SELECT i.instructor_id, i.user_id, i.name FROM instructors i ORDER BY i.name";
        try (Connection connect = DBConnection.getStudentConnection();
             PreparedStatement prepStatement = connect.prepareStatement(sql);
             ResultSet resultSet = prepStatement.executeQuery()) {
            while (resultSet.next()) {
                String item = resultSet.getString("user_id") + " - " + resultSet.getString("name");
                instructor.addItem(item);
            }
        } 
        catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load instructors.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doAssign() {
        String secSel=(String) Section.getSelectedItem();
        String instSel=(String) instructor.getSelectedItem();
        if (instSel == null ||secSel == null ) { 
            JOptionPane.showMessageDialog(this, "Select both section and instructor.", "Warning", JOptionPane.WARNING_MESSAGE);
            return; 
        }

        int sectionId=Integer.parseInt(secSel.split(" - ")[0].trim());
        String instructorUserId=instSel.split(" - ")[0].trim();

        try (Connection connect=DBConnection.getStudentConnection()) {
            Integer instructorId=null;
            try (PreparedStatement prepStatement=connect.prepareStatement("SELECT instructor_id FROM instructors WHERE user_id = ? LIMIT 1")) {
                prepStatement.setString(1, instructorUserId);
                try (ResultSet resultSet=prepStatement.executeQuery()) {
                    if (resultSet.next()) {
                        instructorId = resultSet.getInt("instructor_id");
                    }
                }
            }
            if (instructorId==null){
                JOptionPane.showMessageDialog(this, "Instructor not found. Check user data.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try (PreparedStatement prepStatement = connect.prepareStatement("UPDATE sections SET instructor_id = ? WHERE section_id = ?")) {
                prepStatement.setInt(1, instructorId);
                prepStatement.setInt(2, sectionId);
                prepStatement.executeUpdate();
                JOptionPane.showMessageDialog(this, "Successfully assigned instructor to section " + sectionId + ".");
            }
        } 
        catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this, "Assign failed due to database error.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}