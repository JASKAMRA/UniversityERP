package edu.univ.erp.ui.student;

import edu.univ.erp.domain.Section;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * RegisterDialog: lists sections for a course and allows the student to register.
 * Updated to show Instructor name for each section.
 */
public class RegisterDialog extends JDialog {
    private boolean registered = false;

    // small holder to keep Section + label (so JComboBox shows nice text but we keep the Section object)
    private static class SectionItem {
        final Section section;
        final String label;
        SectionItem(Section section, String label) { this.section = section; this.label = label; }
        @Override public String toString() { return label; }
    }

    public RegisterDialog(Window owner, StudentService service, String userId, String courseCode) {
        super(owner, "Register for " + courseCode, ModalityType.APPLICATION_MODAL);
        setLayout(new BorderLayout());

        JPanel info = new JPanel(new GridLayout(0,1));
        info.add(new JLabel("Course: " + courseCode));
        info.add(new JLabel("Select section:"));
        add(info, BorderLayout.NORTH);

        // JComboBox now holds SectionItem objects
        JComboBox<SectionItem> cbSections = new JComboBox<>();
        cbSections.setPrototypeDisplayValue(new SectionItem(new Section(), "000 — SEM — cap:000 — Instructor: Some Long Name")); // width hint
        add(cbSections, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRegister = new JButton("Register");
        JButton btnCancel = new JButton("Cancel");
        south.add(btnCancel);
        south.add(btnRegister);
        add(south, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());

        // load sections in background and populate combo with labels that include instructor name
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
                    if (secs == null || secs.isEmpty()) {
                        cbSections.addItem(new SectionItem(new Section(), "No sections available"));
                    } else {
                        for (Section s : secs) {
                            String instr = "";
                            try {
                                // ask service for instructor name (implementation should handle schema differences)
                                instr = service.getInstructorNameForSection(s);
                            } catch (Exception ex) {
                                // non-fatal: leave instructor blank
                                instr = "";
                            }
                            String label = s.GetSectionID()
                                    + " — " + (s.GetSemester() == null ? "" : s.GetSemester())
                                    + " / cap:" + s.GetCapacity()
                                    + (instr == null || instr.isEmpty() ? "" : " — Instructor: " + instr);
                            cbSections.addItem(new SectionItem(s, label));
                        }
                        btnRegister.setEnabled(true);
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(RegisterDialog.this, "Load interrupted.");
                } catch (ExecutionException ex) {
                    ex.printStackTrace();
                    String msg = ex.getCause() != null ? ex.getCause().toString() : ex.toString();
                    JOptionPane.showMessageDialog(RegisterDialog.this, "Failed to load sections: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        loader.execute();

        btnRegister.addActionListener(e -> {
            SectionItem selItem = (SectionItem) cbSections.getSelectedItem();
            if (selItem == null || selItem.section == null || selItem.toString().startsWith("No sections") || selItem.toString().startsWith("Error")) {
                JOptionPane.showMessageDialog(this, "Select a valid section.");
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
                    try {
                        boolean ok = get();
                        if (ok) {
                            registered = true;
                            JOptionPane.showMessageDialog(RegisterDialog.this, "Registered successfully.");
                            dispose();
                        } else {
                            JOptionPane.showMessageDialog(RegisterDialog.this, "Registration failed (maybe already registered or no seats).");
                            btnRegister.setEnabled(true);
                        }
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(RegisterDialog.this, "Registration interrupted.");
                        btnRegister.setEnabled(true);
                    } catch (ExecutionException ex) {
                        ex.printStackTrace();
                        String msg = ex.getCause() != null ? ex.getCause().toString() : ex.toString();
                        JOptionPane.showMessageDialog(RegisterDialog.this, "Registration error: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                        btnRegister.setEnabled(true);
                    }
                }
            };
            reg.execute();
        });

        pack();
        setSize(520, 260);
        setResizable(false);
    }

    public boolean isRegisteredSuccessfully() {
        return registered;
    }
}
