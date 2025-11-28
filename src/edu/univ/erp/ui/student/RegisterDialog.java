package edu.univ.erp.ui.student;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.StudentService;
import edu.univ.erp.data.DBConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class RegisterDialog extends JDialog {
    private boolean registered = false;
    private final String userId;
    private final StudentService service;
    private final String courseCode;

    private static final int PADDING = 15;
    private static final int GAP = 10;
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color SUCCESS_COLOR = new Color(50, 160, 50);
    private static final Font COURSE_TITLE_FONT = new Font("Arial", Font.BOLD, 18);

    private static class SectionItem {
        final Section section;
        final String label;
        final Timestamp deadline;
        final boolean closed;
        SectionItem(Section section, String label, Timestamp deadline, boolean closed) {
            this.section = section; this.label = label; this.deadline = deadline; this.closed = closed;
        }
        @Override public String toString() { 
            return label; 
        }
    }

    public RegisterDialog(Window owner, StudentService service, String userId, String courseCode) {
        super(owner, "Enrollment: " + courseCode, ModalityType.APPLICATION_MODAL);
        this.service=service;
        this.userId=userId;
        this.courseCode=courseCode;

        JPanel contentPane=new JPanel(new BorderLayout(GAP, GAP));
        contentPane.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        contentPane.setBackground(Color.WHITE);
        
        setLayout(new BorderLayout());
        add(contentPane, BorderLayout.CENTER);

        JPanel info=new JPanel(new GridLayout(0, 1));
        info.setBackground(Color.WHITE);
        
        JLabel titleLabel=new JLabel("➕ Register for Course: " + courseCode);
        titleLabel.setFont(COURSE_TITLE_FONT);
        titleLabel.setForeground(PRIMARY_COLOR);
        
        JLabel instructionLabel=new JLabel("Select an available section (deadline shown). Closed sections are disabled.");
        instructionLabel.setBorder(new EmptyBorder(GAP, 0, 0, 0));

        info.add(titleLabel);
        info.add(instructionLabel);
        contentPane.add(info, BorderLayout.NORTH);

        JComboBox<SectionItem> cbSections=new JComboBox<>();
        cbSections.setFont(new Font("Monospaced", Font.PLAIN, 12));
        cbSections.setPrototypeDisplayValue(new SectionItem(new Section(), "000 — SEM — cap:000 — Instructor: Dr. Long Name — Deadline: yyyy-MM-dd HH:mm:ss", null, true)); 
        contentPane.add(cbSections, BorderLayout.CENTER);

        JPanel south=new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP, 0));
        south.setBackground(Color.WHITE);
        
        JButton btnRegister=new JButton("Register");
        JButton btnCancel=new JButton("Cancel");

        btnRegister.setBackground(SUCCESS_COLOR);
        btnRegister.setForeground(Color.WHITE);
        btnCancel.setBackground(Color.LIGHT_GRAY);

        south.add(btnCancel);
        south.add(btnRegister);
        contentPane.add(south, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());

        btnRegister.setEnabled(false);

        final SimpleDateFormat simpledf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        SwingWorker<List<Section>, Void> loader = new SwingWorker<>() {
            @Override
            protected List<Section> doInBackground() throws Exception {
                return service.GetSec(courseCode);
            }
            @Override
            protected void done() {
                try {
                    List<Section> secs=get();
                    cbSections.removeAllItems(); 

                    if (secs.isEmpty() ||secs == null  ) {
                        cbSections.addItem(new SectionItem(new Section(), "No sections available", null, true));
                    } 
                    else {
                        for (Section s : secs) {
                        
                            Timestamp regDeadline=null;
                            try (Connection connect=DBConnection.getStudentConnection();
                                 PreparedStatement prepStatement=connect.prepareStatement("SELECT registration_deadline FROM sections WHERE section_id = ?")) {
                                prepStatement.setInt(1, s.GetSectionID());
                                try (ResultSet resultSet=prepStatement.executeQuery()) {
                                    if (resultSet.next()) {
                                        regDeadline = resultSet.getTimestamp("registration_deadline");
                                    }
                                }
                            } 
                            catch (Exception exception) {  
                            }
                            boolean closed=false;
                            if (regDeadline != null) {
                                Timestamp now = new Timestamp(System.currentTimeMillis());
                                closed = now.after(regDeadline);
                            }

                            String instruction = "";
                            try { 
                                instruction = service.GetInstName_sec(s);
                             } 
                            catch (Exception exception) { 
                                instruction = "TBD"; 
                            }

                            String deadlineStr=regDeadline == null ? "No deadline" : simpledf.format(new Date(regDeadline.getTime()));
                            String closedTag=closed ? " (CLOSED)" : "";
                            String label=String.format("Sec %d — %s Yr:%d Cap:%s — Instr:%s — Deadline:%s%s",
                                    s.GetSectionID(),
                                    (s.GetSemester() == null ? "" : s.GetSemester()),
                                    s.GetYear() == null ? 0 : s.GetYear(),
                                    s.GetCapacity() == null ? "N/A" : s.GetCapacity().toString(),
                                    ( instruction.isEmpty()|| instruction == null ? "TBD" : instruction),
                                    deadlineStr,
                                    closedTag);

                            cbSections.addItem(new SectionItem(s, label, regDeadline, closed));
                        }
                        boolean hasOpen = false;
                        for (int i = 0; i < cbSections.getItemCount(); i++) {
                            SectionItem it = cbSections.getItemAt(i);
                            if (!it.closed) { 
                                hasOpen = true; 
                                break; 
                            }
                        }
                        btnRegister.setEnabled(hasOpen);
                    }
                } 
                catch (InterruptedException exception) {
                    cbSections.addItem(new SectionItem(new Section(), "Error loading sections (Interrupted)", null, true));
                    exception.printStackTrace();
                    JOptionPane.showMessageDialog(RegisterDialog.this, "Load interrupted.");
                } 
                catch (ExecutionException exception) {
                    String msg = exception.getCause() != null ? exception.getCause().toString() : exception.toString();
                    cbSections.addItem(new SectionItem(new Section(), "Error loading sections: " + msg, null, true));
                    exception.printStackTrace();
                    JOptionPane.showMessageDialog(RegisterDialog.this, "Failed to load sections: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        loader.execute();

        cbSections.addActionListener(e -> {
            SectionItem select = (SectionItem) cbSections.getSelectedItem();
            if (select==null) {
                btnRegister.setEnabled(false);
                return;
            }
            if (select.closed) {
                btnRegister.setEnabled(false);
                cbSections.setToolTipText("This section's registration deadline has passed: " + (select.deadline == null ? "none" : simpledf.format(new Date(select.deadline.getTime()))));
            } 
            else {
                btnRegister.setEnabled(true);
                cbSections.setToolTipText(null);
            }
        });

        btnRegister.addActionListener(e -> {
            SectionItem selItem=(SectionItem) cbSections.getSelectedItem();
            if (selItem.section == null ||selItem == null) {
                JOptionPane.showMessageDialog(this, "Select a valid section to register.");
                return;
            }
            int section_Id=selItem.section.GetSectionID();

            Timestamp regDeadline=null;
            try (Connection connect=DBConnection.getStudentConnection();
                 PreparedStatement prepStatement = connect.prepareStatement("SELECT registration_deadline FROM sections WHERE section_id = ?")) {
                prepStatement.setInt(1, section_Id);
                try (ResultSet resultSet = prepStatement.executeQuery()) {
                    if (resultSet.next()) {
                        regDeadline=resultSet.getTimestamp("registration_deadline");
                    }
                }
            } catch (Exception exception) {
            }

            if (regDeadline!=null) {
                Timestamp now = new Timestamp(System.currentTimeMillis());
                if (now.after(regDeadline)) {
                    JOptionPane.showMessageDialog(this, "Registration deadline has passed for this section (" + simpledf.format(new Date(regDeadline.getTime())) + ").", "Registration closed", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            btnRegister.setEnabled(false);
            SwingWorker<Boolean, Void> reg=new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return service.SecReg(userId, section_Id);
                }
                @Override
                protected void done() {
                    btnRegister.setEnabled(true);
                    try {
                        boolean ok=get();
                        if (!ok) {
                            registered = true;
                            JOptionPane.showMessageDialog(RegisterDialog.this, "Registration failed (duplicate, full, or deadline passed).", "Registration Failed", JOptionPane.ERROR_MESSAGE);
                        } 
                        else {
                            JOptionPane.showMessageDialog(RegisterDialog.this, "Registration successful!");
                            dispose();
                        }
                    } 
                    catch (InterruptedException exception) {
                        exception.printStackTrace();
                        JOptionPane.showMessageDialog(RegisterDialog.this, "Registration interrupted.");
                    } 
                    catch (ExecutionException exception) {
                        exception.printStackTrace();
                        String msg = exception.getCause() != null ? exception.getCause().toString() : exception.toString();
                        JOptionPane.showMessageDialog(RegisterDialog.this, "Registration error: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            reg.execute();
        });

        pack();
        setSize(650, 260); 
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    public boolean isRegisteredSuccessfully() {
        return registered;
    }
}
