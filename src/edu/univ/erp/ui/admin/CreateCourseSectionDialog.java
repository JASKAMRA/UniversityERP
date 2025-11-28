package edu.univ.erp.ui.admin;

import edu.univ.erp.service.AdminService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.Year;


public class CreateCourseSectionDialog extends JDialog {
    private final AdminService adminService;
    private JTextField tfCourseId, tfCourseTitle, tfCredits, tfDepartment;
    private JTextField tfCapacity, tfDay, tfSemester, tfYear, tfInstructorUserId;
    private boolean succeeded = false;
    private int createdSectionId = -1;

    public CreateCourseSectionDialog(Window owner, AdminService adminService) {
        super(owner, "➕ Create Course & Section", ModalityType.APPLICATION_MODAL);
        this.adminService=adminService;
        init();
        JPanel content=(JPanel)getContentPane();
        content.setBorder(new EmptyBorder(15, 15, 15, 15));
        pack();
        setResizable(false);
    }

    private void init() {
        setLayout(new BorderLayout(8, 8));

     
        JLabel title=new JLabel("Define New Course and Schedule Section");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        JPanel mainForm=new JPanel(new GridLayout(1, 2, 15, 0)); 

    
        JPanel coursePanel=createFormContainer("📚 Course Details");
        coursePanel.setBackground(Color.WHITE);
        
        tfCourseId=new JTextField(10);
        tfCourseTitle=new JTextField(10);
        tfCredits=new JTextField(10);
        tfDepartment=new JTextField(10);
        
        fillFormPanel(coursePanel, new String[]{
            "Course ID:", "Course Title:", "Credits:", "Department ID:"
        }, 
        new JComponent[]{
            tfCourseId, tfCourseTitle, tfCredits, tfDepartment
        });
        
        mainForm.add(coursePanel);

        
        JPanel sectionPanel=createFormContainer("🗓️ Section Schedule & Instructor");
        sectionPanel.setBackground(Color.WHITE);
        
        tfCapacity=new JTextField("30", 10);
        tfDay=new JTextField("MONDAY", 10);
        tfSemester=new JTextField("Fall", 10);
        tfYear=new JTextField(String.valueOf(Year.now().getValue()), 10);
        tfInstructorUserId=new JTextField(10);

        fillFormPanel(sectionPanel, new String[]{
            "Capacity:", "Day (MONDAY...):", "Semester:", "Year:", "Instructor user_id:"
        }, new JComponent[]{
            tfCapacity, tfDay, tfSemester, tfYear, tfInstructorUserId
        });

        mainForm.add(sectionPanel);

        add(mainForm, BorderLayout.CENTER);

        JPanel bottom=new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton btnCancel=new JButton("Cancel");
        JButton btnCreate=new JButton("Create Section");
        
        btnCreate.setBackground(new Color(70, 130, 180));
        btnCreate.setForeground(Color.WHITE);
        
        bottom.add(btnCancel); 
        bottom.add(btnCreate);
        add(bottom, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> { succeeded = false; setVisible(false); dispose(); });
        btnCreate.addActionListener(e -> doCreate());
    }

    private JPanel createFormContainer(String title) {
        JPanel panel=new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY), title, TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("Arial", Font.BOLD, 12), 
            new Color(70, 130, 180)
        ));
        return panel;
    }
    
    private void fillFormPanel(JPanel panel, String[] labels, JComponent[] fields) {
        GridBagConstraints constraint = new GridBagConstraints();
        constraint.insets = new Insets(4, 4, 4, 4);
        constraint.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < labels.length; i++) {
           
            constraint.gridx = 0; constraint.gridy = i; constraint.weightx = 0;
            panel.add(new JLabel(labels[i]), constraint);
           
            constraint.gridx = 1; constraint.gridy = i; constraint.weightx = 1.0;
            panel.add(fields[i], constraint);
        }
    }


    private void doCreate() {
        String courseId = tfCourseId.getText().trim();
        String title = tfCourseTitle.getText().trim();
        
        Integer credits = null;
        try { 
            if (!tfCredits.getText().trim().isEmpty()) {
                credits = Integer.parseInt(tfCredits.getText().trim());
            } 
        } 
        catch (NumberFormatException exception) { 
            JOptionPane.showMessageDialog(this,"Credits must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE); 
            return; 
        }
        String depart=tfDepartment.getText().trim();

        int capacity;
        try { 
            capacity=Integer.parseInt(tfCapacity.getText().trim()); 
        } 
        catch (NumberFormatException exception) { 
            JOptionPane.showMessageDialog(this,"Capacity must be a number.", "Input Error", JOptionPane.ERROR_MESSAGE); 
            return; 
        }
        
        String day=tfDay.getText().trim();
        String sem = tfSemester.getText().trim();
        
        int year;
        try { 
            year = Integer.parseInt(tfYear.getText().trim()); 
        } 
        catch (NumberFormatException ex) { 
            JOptionPane.showMessageDialog(this,"Year must be a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
             return; 
            }
        
        String instructorUserId = tfInstructorUserId.getText().trim();

        if ( instructorUserId.isEmpty() || courseId.isEmpty() ) {
            JOptionPane.showMessageDialog(this, "Course ID and Instructor user_id are required fields.", "Missing Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int secId = adminService.CreateCandS(courseId, title, credits, depart, capacity, day, sem, year, instructorUserId);
            
            if (secId > 0) {
                succeeded = true;
                createdSectionId = secId;
                JOptionPane.showMessageDialog(this, "Section created successfully. Section ID: **" + secId + "**", "Success", JOptionPane.INFORMATION_MESSAGE);
                setVisible(false);
                dispose();
            } 
            else {
                JOptionPane.showMessageDialog(this, "Failed to create section. Possible reasons: Instructor user_id does not exist, or required course fields are missing.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } 
        catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSucceeded() { 
        return succeeded; 
    }
    public int getCreatedSectionId() { 
        return createdSectionId; 
    }
}