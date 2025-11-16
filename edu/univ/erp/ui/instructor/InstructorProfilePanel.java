package edu.univ.erp.ui.instructor;

import javax.swing.*;
import java.awt.*;

/**
 * InstructorProfilePanel
 * - View and edit basic instructor profile
 * - Change password (UI demo)
 */
public class InstructorProfilePanel extends JPanel {

    private final JTextField nameField;
    private final JTextField emailField;
    private final JTextField phoneField;
    private final JTextField officeField;
    private final JTextArea bioArea;
    private final JButton editBtn, saveBtn, cancelBtn, changePwdBtn;

    private Instructor instructor = new Instructor(4001, "Dr. Rahul Mehta", "rahul.mehta@uni.edu", "+91-98xxxx", "Room 12, Building A", "PhD, OS; Research: Distributed Systems");

    public InstructorProfilePanel() {
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Instructor Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx=0; g.gridy=0; center.add(new JLabel("Name:"), g);
        nameField = new JTextField(instructor.name, 30); nameField.setEditable(false); g.gridx=1; center.add(nameField, g);

        g.gridx=0; g.gridy++; center.add(new JLabel("Email:"), g);
        emailField = new JTextField(instructor.email, 30); emailField.setEditable(false); g.gridx=1; center.add(emailField, g);

        g.gridx=0; g.gridy++; center.add(new JLabel("Phone:"), g);
        phoneField = new JTextField(instructor.phone, 20); phoneField.setEditable(false); g.gridx=1; center.add(phoneField, g);

        g.gridx=0; g.gridy++; center.add(new JLabel("Office:"), g);
        officeField = new JTextField(instructor.office, 20); officeField.setEditable(false); g.gridx=1; center.add(officeField, g);

        g.gridx=0; g.gridy++; center.add(new JLabel("Bio / Research:"), g);
        bioArea = new JTextArea(instructor.bio, 5, 40); bioArea.setEditable(false); g.gridx=1; center.add(new JScrollPane(bioArea), g);

        add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        editBtn = new JButton("Edit");
        saveBtn = new JButton("Save");
        cancelBtn = new JButton("Cancel");
        changePwdBtn = new JButton("Change Password");
        stylePrimary(editBtn); stylePrimary(saveBtn); stylePrimary(cancelBtn); stylePrimary(changePwdBtn);

        saveBtn.setVisible(false); cancelBtn.setVisible(false);
        actions.add(editBtn); actions.add(saveBtn); actions.add(cancelBtn); actions.add(changePwdBtn);
        add(actions, BorderLayout.SOUTH);

        editBtn.addActionListener(e -> enterEdit(true));
        cancelBtn.addActionListener(e -> enterEdit(false));
        saveBtn.addActionListener(e -> saveProfile());
        changePwdBtn.addActionListener(e -> openChangePasswordDialog());
    }

    private void enterEdit(boolean en) {
        nameField.setEditable(en);
        emailField.setEditable(en);
        phoneField.setEditable(en);
        officeField.setEditable(en);
        bioArea.setEditable(en);

        editBtn.setVisible(!en);
        changePwdBtn.setVisible(!en);
        saveBtn.setVisible(en);
        cancelBtn.setVisible(en);
    }

    private void saveProfile() {
        // validate simple
        if (nameField.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this,"Name required."); return; }
        // TODO: persist to DB
        instructor.name = nameField.getText().trim();
        instructor.email = emailField.getText().trim();
        instructor.phone = phoneField.getText().trim();
        instructor.office = officeField.getText().trim();
        instructor.bio = bioArea.getText().trim();
        enterEdit(false);
        JOptionPane.showMessageDialog(this, "Profile saved (demo). Replace TODO with DB code.");
    }

    private void openChangePasswordDialog() {
        JPasswordField cur = new JPasswordField(); JPasswordField nw = new JPasswordField(); JPasswordField conf = new JPasswordField();
        JPanel p = new JPanel(new GridLayout(3,2,6,6));
        p.add(new JLabel("Current password:")); p.add(cur);
        p.add(new JLabel("New password:")); p.add(nw);
        p.add(new JLabel("Confirm new:")); p.add(conf);
        int res = JOptionPane.showConfirmDialog(this, p, "Change Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        String n = new String(nw.getPassword()), c = new String(conf.getPassword());
        if (n.length() < 6) { JOptionPane.showMessageDialog(this,"Password too short."); return; }
        if (!n.equals(c)) { JOptionPane.showMessageDialog(this,"Passwords don't match."); return; }
        // TODO: actually call Auth service to change password
        JOptionPane.showMessageDialog(this, "Password changed (demo).");
    }

    private void stylePrimary(JButton b) {
        b.setBackground(new Color(52,152,219));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
    }

    private static class Instructor {
        int id; String name,email,phone,office,bio;
        Instructor(int id,String name,String email,String phone,String office,String bio){ this.id=id;this.name=name;this.email=email;this.phone=phone;this.office=office;this.bio=bio; }
    }
}
