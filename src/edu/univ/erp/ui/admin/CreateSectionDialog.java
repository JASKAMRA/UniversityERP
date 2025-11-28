package edu.univ.erp.ui.admin;
import edu.univ.erp.data.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.sql.Timestamp;

public class CreateSectionDialog extends JDialog {

    private boolean succeeded=false;
    private int createdSectionId=-1;
    private JTextField tfCourseId=new JTextField(20);
    private JTextField tfDays=new JTextField(20);
    private JTextField tfStart=new JTextField(20);
    private JTextField tfDeadline=new JTextField(20);
    private JTextField tfEnd=new JTextField(20);
    private JTextField tfYear=new JTextField(20);
    private JTextField tfCapacity=new JTextField(20);
    private JTextField tfInstructorUser=new JTextField(20);
    private JTextField tfSemester=new JTextField(20);

    public CreateSectionDialog(Window owner) {
        super(owner, "Create Section", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        initUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initUI() {

        JPanel form=new JPanel(new GridBagLayout());
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.insets=new Insets(6, 6, 6, 6);
        gbc.fill=GridBagConstraints.HORIZONTAL;
        gbc.anchor=GridBagConstraints.WEST;

        int r=0;

        addRow(form, gbc, r++, "Course ID:", tfCourseId);
        addRow(form, gbc, r++, "Instructor user_id (optional):", tfInstructorUser);
        addRow(form, gbc, r++, "Days (CSV e.g. MONDAY,WEDNESDAY):", tfDays);
        addRow(form, gbc, r++, "Start Time (HH:mm):", tfStart);
        addRow(form, gbc, r++, "End Time (HH:mm):", tfEnd);
        addRow(form, gbc, r++, "Capacity:", tfCapacity);
        addRow(form, gbc, r++, "Semester:", tfSemester);
        addRow(form, gbc, r++, "Year:", tfYear);
        addRow(form, gbc, r++, "Reg Deadline (yyyy-MM-dd HH:mm:ss):", tfDeadline);

        JButton btnOk=new JButton("OK");
        JButton btnCancel=new JButton("Cancel");

        JPanel btnPanel=new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(btnOk);
        btnPanel.add(btnCancel);

        btnCancel.addActionListener(e -> dispose());
        btnOk.addActionListener(e -> onCreate());

        getContentPane().setLayout(new BorderLayout(10, 10));
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(btnPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnOk);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row; 
        gbc.weightx = 0.2;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    private void onCreate() {
        String courseId=tfCourseId.getText().trim();
        if (courseId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Course ID required");
            return;
        }

        String instrUser=tfInstructorUser.getText().trim();
        String days=tfDays.getText().trim();
        String start=tfStart.getText().trim();
        String end=tfEnd.getText().trim();

        Integer cap;
        try { 
            cap = Integer.parseInt(tfCapacity.getText().trim()); 
        }
        catch (Exception exception) { 
            JOptionPane.showMessageDialog(this, "Invalid capacity");
             return;
             }

        String semester=tfSemester.getText().trim();

        Integer year;
        try {
              year=Integer.parseInt(tfYear.getText().trim()); 
            }
        catch (Exception exception) { 
            JOptionPane.showMessageDialog(this, "Invalid year");
             return;
             }

        String deadline=tfDeadline.getText().trim();

        String instructorId=null;
        if (!instrUser.isEmpty()) {
            try (Connection connect=DBConnection.getStudentConnection();
                 PreparedStatement prepStatement=connect.prepareStatement("SELECT instructor_id FROM instructors WHERE user_id = ? LIMIT 1")) {
                prepStatement.setString(1, instrUser);
                ResultSet resultSet=prepStatement.executeQuery();
                if (resultSet.next()) {
                    instructorId=String.valueOf(resultSet.getInt("instructor_id"));
                }    
                else { 
                    JOptionPane.showMessageDialog(this, "Instructor not found"); 
                    return; 
                }
            } 
            catch (Exception exception) { 
                JOptionPane.showMessageDialog(this, "Error checking instructor"); 
                return; 
            }
        }

        String sql = """
            INSERT INTO sections 
            (course_id, instructor_id, day, days, start_time, end_time, capacity, semester, year, registration_deadline)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection connect = DBConnection.getStudentConnection();
             PreparedStatement prepStatement = connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;

            prepStatement.setString(i++, courseId);
            if (instructorId != null) {
                prepStatement.setString(i++, instructorId);
            }
            else {
                  prepStatement.setNull(i++, Types.VARCHAR);
              }

            String primaryDay=days.contains(",") ? days.split(",")[0].trim() : days.trim();
            prepStatement.setString(i++, primaryDay.isEmpty() ? null : primaryDay);

            prepStatement.setString(i++, days.isEmpty() ? null : days);
            prepStatement.setString(i++, start.isEmpty() ? null : start);
            prepStatement.setString(i++, end.isEmpty() ? null : end);
            prepStatement.setInt(i++, cap);
            prepStatement.setString(i++, semester);
            prepStatement.setInt(i++, year);

            if (!deadline.isEmpty()) {
                prepStatement.setTimestamp(i++, Timestamp.valueOf(deadline));
            }
            else {
                prepStatement.setNull(i++, Types.TIMESTAMP);
            }

            prepStatement.executeUpdate();

            try (ResultSet key=prepStatement.getGeneratedKeys()) {
                if (key.next()){
                    createdSectionId=key.getInt(1);
                }
            }

            succeeded=true;
            JOptionPane.showMessageDialog(this, "Section created: " + createdSectionId);
            dispose();

        } catch (Exception exception) {
            JOptionPane.showMessageDialog(this, "Create failed: " + exception.getMessage());
        }
    }

    public boolean isSucceeded(){ 
        return succeeded; 
    }
    public int getCreatedSectionId(){
         return createdSectionId; 
        }
}
