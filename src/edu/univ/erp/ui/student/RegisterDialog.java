package edu.univ.erp.ui.student;

import edu.univ.erp.domain.Section;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * RegisterDialog: lists sections for a course and allows the student to register.
 * Updated to show Instructor name for each section.
 */
public class RegisterDialog extends JDialog {
    private boolean registered = false;

    // --- Aesthetic constants ---
    private static final int PADDING = 15;
    private static final int GAP = 10;
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color SUCCESS_COLOR = new Color(50, 160, 50);
    private static final Font COURSE_TITLE_FONT = new Font("Arial", Font.BOLD, 18);

    // small holder to keep Section + label (so JComboBox shows nice text but we keep the Section object)
    private static class SectionItem {
        final Section section;
        final String label;
        SectionItem(Section section, String label) { this.section = section; this.label = label; }
        @Override public String toString() { return label; }
    }

    public RegisterDialog(Window owner, StudentService service, String userId, String courseCode) {
        super(owner, "Enrollment: " + courseCode, ModalityType.APPLICATION_MODAL);
        
        // Use JPanel container for internal spacing
        JPanel contentPane = new JPanel(new BorderLayout(GAP, GAP));
        contentPane.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        contentPane.setBackground(Color.WHITE);
        
        setLayout(new BorderLayout());
        add(contentPane, BorderLayout.CENTER);


        // --- 1. Info Panel (NORTH) ---
        JPanel info = new JPanel(new GridLayout(0, 1));
        info.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("➕ Register for Course: " + courseCode);
        titleLabel.setFont(COURSE_TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        
        JLabel instructionLabel = new JLabel("Select an available section:");
        instructionLabel.setBorder(new EmptyBorder(GAP, 0, 0, 0));

        info.add(titleLabel);
        info.add(instructionLabel);
        contentPane.add(info, BorderLayout.NORTH);

        // --- 2. Section ComboBox (CENTER) ---
        // JComboBox now holds SectionItem objects
        JComboBox<SectionItem> cbSections = new JComboBox<>();
        cbSections.setFont(new Font("Monospaced", Font.PLAIN, 12));
        // width hint
        cbSections.setPrototypeDisplayValue(new SectionItem(new Section(), "000 — SEM — cap:000 — Instructor: Dr. Theodore Long Name")); 
        contentPane.add(cbSections, BorderLayout.CENTER);

        // --- 3. Buttons (SOUTH) ---
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP, 0));
        south.setBackground(Color.WHITE);
        
        JButton btnRegister = new JButton("Register");
        JButton btnCancel = new JButton("Cancel");
        
        // Style Buttons
        btnRegister.setBackground(SUCCESS_COLOR);
        btnRegister.setForeground(Color.WHITE);
        btnCancel.setBackground(Color.LIGHT_GRAY);

        south.add(btnCancel);
        south.add(btnRegister);
        contentPane.add(south, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());

        // --- 4. Section Loading Logic ---
        btnRegister.setEnabled(false);
        SwingWorker<List<Section>, Void> loader = new SwingWorker<>() {
            @Override
            protected List<Section> doInBackground() throws Exception {
                return service.getSectionsForCourse(courseCode);
            }
            @Override
            protected void done() {
                try {
                    List<Section> secs = get();
                    cbSections.removeAllItems(); // Clear any default items

                    if (secs == null || secs.isEmpty()) {
                        cbSections.addItem(new SectionItem(new Section(), "No sections available"));
                    } else {
                        for (Section s : secs) {
                            String instr = "";
                            try {
                                // ask service for instructor name
                                instr = service.getInstructorNameForSection(s);
                            } catch (Exception ex) {
                                // non-fatal: leave instructor blank
                                instr = "Unknown";
                            }
                            // Formatted label for display
                            String label = String.format("Sec %d — %s, %d / Cap: %d — Instr: %s",
                                    s.GetSectionID(),
                                    (s.GetSemester() == null ? "" : s.GetSemester()),
                                    s.GetYear(),
                                    s.GetCapacity(),
                                    (instr == null || instr.isEmpty() ? "TBD" : instr));
                            
                            cbSections.addItem(new SectionItem(s, label));
                        }
                        btnRegister.setEnabled(true);
                    }
                } catch (InterruptedException ex) {
                    cbSections.addItem(new SectionItem(new Section(), "Error loading sections (Interrupted)"));
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(RegisterDialog.this, "Load interrupted.");
                } catch (ExecutionException ex) {
                    String msg = ex.getCause() != null ? ex.getCause().toString() : ex.toString();
                    cbSections.addItem(new SectionItem(new Section(), "Error loading sections: " + msg));
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(RegisterDialog.this, "Failed to load sections: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        loader.execute();

        // --- 5. Registration Action ---
        btnRegister.addActionListener(e -> {
            SectionItem selItem = (SectionItem) cbSections.getSelectedItem();
            if (selItem == null || selItem.section == null || selItem.toString().startsWith("No sections") || selItem.toString().startsWith("Error")) {
                JOptionPane.showMessageDialog(this, "Select a valid section to register.");
                return;
            }

            int sectionId = selItem.section.GetSectionID();

            btnRegister.setEnabled(false);
            // register in background
            SwingWorker<Boolean, Void> reg = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return service.registerForSection(userId, sectionId);
                }
                @Override
                protected void done() {
                    btnRegister.setEnabled(true);
                    try {
                        boolean ok = get();
                        if (ok) {
                            registered = true;
                            JOptionPane.showMessageDialog(RegisterDialog.this, "Registration successful!");
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(RegisterDialog.this, "Registration failed (possible reasons: already registered, no seats, or registration deadline passed).", "Registration Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(RegisterDialog.this, "Registration interrupted.");
                    } catch (ExecutionException ex) {
                        ex.printStackTrace();
                        String msg = ex.getCause() != null ? ex.getCause().toString() : ex.toString();
                        JOptionPane.showMessageDialog(RegisterDialog.this, "Registration error: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            reg.execute();
        });

        pack();
        setSize(550, 200); // Set fixed size for consistency
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    public boolean isRegisteredSuccessfully() {
        return registered;
    }
}