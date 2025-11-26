package edu.univ.erp.ui.admin;

import edu.univ.erp.service.AdminService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.Year;

/**
 * Dialog to create a course (if needed) and section assigned to an instructor (instructor's user_id).
 */
public class CreateCourseSectionDialog extends JDialog {
    private final AdminService adminService;
    private JTextField tfCourseId, tfCourseTitle, tfCredits, tfDepartment;
    private JTextField tfCapacity, tfDay, tfSemester, tfYear, tfInstructorUserId;
    private boolean succeeded = false;
    private int createdSectionId = -1;

    // --- Aesthetic constants ---
    private static final int PADDING = 15;
    private static final int GAP = 8;
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180); // Steel blue
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 18);

    public CreateCourseSectionDialog(Window owner, AdminService adminService) {
        super(owner, "➕ Create Course & Section", ModalityType.APPLICATION_MODAL);
        this.adminService = adminService;
        init();
        // Add padding around the whole dialog content
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        pack();
        setResizable(false);
    }

    private void init() {
        setLayout(new BorderLayout(GAP, GAP));

        // 1. Title (North)
        JLabel title = new JLabel("Define New Course and Schedule Section");
        title.setFont(TITLE_FONT);
        add(title, BorderLayout.NORTH);

        // 2. Main Form Panel (Center) - Divided into two sub-panels
        JPanel mainForm = new JPanel(new GridLayout(1, 2, PADDING, 0)); // Two columns with horizontal padding

        // --- Course Details Panel ---
        JPanel coursePanel = createFormContainer("📚 Course Details");
        coursePanel.setBackground(Color.WHITE);
        
        tfCourseId = new JTextField(10);
        tfCourseTitle = new JTextField(10);
        tfCredits = new JTextField(10);
        tfDepartment = new JTextField(10);
        
        fillFormPanel(coursePanel, new String[]{
            "Course ID:", "Course Title:", "Credits:", "Department ID:"
        }, new JComponent[]{
            tfCourseId, tfCourseTitle, tfCredits, tfDepartment
        });
        
        mainForm.add(coursePanel);

        // --- Section Details Panel ---
        JPanel sectionPanel = createFormContainer("🗓️ Section Schedule & Instructor");
        sectionPanel.setBackground(Color.WHITE);
        
        tfCapacity = new JTextField("30", 10);
        tfDay = new JTextField("MONDAY", 10);
        tfSemester = new JTextField("Fall", 10);
        tfYear = new JTextField(String.valueOf(Year.now().getValue()), 10);
        tfInstructorUserId = new JTextField(10);

        fillFormPanel(sectionPanel, new String[]{
            "Capacity:", "Day (MONDAY...):", "Semester:", "Year:", "Instructor user_id:"
        }, new JComponent[]{
            tfCapacity, tfDay, tfSemester, tfYear, tfInstructorUserId
        });

        mainForm.add(sectionPanel);

        add(mainForm, BorderLayout.CENTER);

        // 3. Buttons (South)
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP, GAP));
        JButton btnCancel = new JButton("Cancel");
        JButton btnCreate = new JButton("Create Section");
        
        btnCreate.setBackground(PRIMARY_COLOR);
        btnCreate.setForeground(Color.WHITE);
        
        bottom.add(btnCancel); 
        bottom.add(btnCreate);
        add(bottom, BorderLayout.SOUTH);

        // 4. Action Listeners
        btnCancel.addActionListener(e -> { succeeded = false; setVisible(false); dispose(); });
        btnCreate.addActionListener(e -> doCreate());
    }
    
    /** Helper to create a titled, bordered container */
    private JPanel createFormContainer(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY), 
            title, 
            TitledBorder.LEFT, 
            TitledBorder.TOP, 
            new Font("Arial", Font.BOLD, 12), 
            PRIMARY_COLOR
        ));
        return panel;
    }
    
    /** Helper to populate a GridBagLayout panel with labels and components */
    private void fillFormPanel(JPanel panel, String[] labels, JComponent[] fields) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < labels.length; i++) {
            // Label
            c.gridx = 0; c.gridy = i; c.weightx = 0;
            panel.add(new JLabel(labels[i]), c);
            
            // Field
            c.gridx = 1; c.gridy = i; c.weightx = 1.0;
            panel.add(fields[i], c);
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
        } catch (NumberFormatException ex) { 
            JOptionPane.showMessageDialog(this,"Credits must be a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE); 
            return; 
        }
        String dept = tfDepartment.getText().trim();

        int capacity;
        try { capacity = Integer.parseInt(tfCapacity.getText().trim()); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this,"Capacity must be a number.", "Input Error", JOptionPane.ERROR_MESSAGE); return; }
        
        String day = tfDay.getText().trim();
        String sem = tfSemester.getText().trim();
        
        int year;
        try { year = Integer.parseInt(tfYear.getText().trim()); } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this,"Year must be a number.", "Input Error", JOptionPane.ERROR_MESSAGE); return; }
        
        String instructorUserId = tfInstructorUserId.getText().trim();

        if (courseId.isEmpty() || instructorUserId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Course ID and Instructor user_id are required fields.", "Missing Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // The core logic call to the AdminService remains unchanged
            int secId = adminService.CreateCandS(courseId, title, credits, dept, capacity, day, sem, year, instructorUserId);
            
            if (secId > 0) {
                succeeded = true;
                createdSectionId = secId;
                JOptionPane.showMessageDialog(this, "Section created successfully. Section ID: **" + secId + "**", "Success", JOptionPane.INFORMATION_MESSAGE);
                setVisible(false);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create section. Possible reasons: Instructor user_id does not exist, or required course fields are missing.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSucceeded() { return succeeded; }
    public int getCreatedSectionId() { return createdSectionId; }
}