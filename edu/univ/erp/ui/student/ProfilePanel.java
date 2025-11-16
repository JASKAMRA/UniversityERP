package edu.univ.erp.ui.student;

import javax.swing.*;
import java.awt.*;

/**
 * ProfilePanel (Student)
 * - View/edit basic profile details and change password
 */
public class ProfilePanel extends JPanel {
    private final JTextField nameField, emailField, phoneField;
    private final JButton editBtn, saveBtn, cancelBtn, changePwdBtn;
    private boolean editMode = false;

    public ProfilePanel() {
        setLayout(new BorderLayout(8,8));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("My Profile");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        g.gridx=0; g.gridy=0; center.add(new JLabel("Name:"), g);
        nameField = new JTextField("Student Name",30); nameField.setEditable(false); g.gridx=1; center.add(nameField,g);

        g.gridx=0; g.gridy++; center.add(new JLabel("Email:"), g);
        emailField = new JTextField("student@uni.edu",30); emailField.setEditable(false); g.gridx=1; center.add(emailField,g);

        g.gridx=0; g.gridy++; center.add(new JLabel("Phone:"), g);
        phoneField = new JTextField("+91-XXXXXXXXXX",20); phoneField.setEditable(false); g.gridx=1; center.add(phoneField,g);

        add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        editBtn = new JButton("Edit"); saveBtn = new JButton("Save"); cancelBtn = new JButton("Cancel"); changePwdBtn = new JButton("Change Password");
        saveBtn.setVisible(false); cancelBtn.setVisible(false);
        actions.add(editBtn); actions.add(saveBtn); actions.add(cancelBtn); actions.add(changePwdBtn);
        add(actions, BorderLayout.SOUTH);

        editBtn.addActionListener(e -> setEdit(true));
        cancelBtn.addActionListener(e -> setEdit(false));
        saveBtn.addActionListener(e -> saveProfile());
        changePwdBtn.addActionListener(e -> changePassword());
    }

    private void setEdit(boolean on) {
        editMode = on;
        nameField.setEditable(on);
        emailField.setEditable(on);
        phoneField.setEditable(on);
        editBtn.setVisible(!on);
        changePwdBtn.setVisible(!on);
        saveBtn.setVisible(on);
        cancelBtn.setVisible(on);
    }

    private void saveProfile() {
        // TODO: persist updates to DB
        setEdit(false);
        JOptionPane.showMessageDialog(this, "Profile saved (demo).");
    }

    private void changePassword() {
        JPasswordField cur = new JPasswordField(); JPasswordField nw = new JPasswordField(); JPasswordField conf = new JPasswordField();
        JPanel p = new JPanel(new GridLayout(3,2,6,6));
        p.add(new JLabel("Current password:")); p.add(cur);
        p.add(new JLabel("New password:")); p.add(nw);
        p.add(new JLabel("Confirm new:")); p.add(conf);
        int res = JOptionPane.showConfirmDialog(this, p, "Change Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        String n = new String(nw.getPassword()), c = new String(conf.getPassword());
        if (n.length() < 6) { JOptionPane.showMessageDialog(this, "Password too short."); return; }
        if (!n.equals(c)) { JOptionPane.showMessageDialog(this, "Passwords do not match."); return; }
        // TODO: call auth service
        JOptionPane.showMessageDialog(this, "Password changed (demo).");
    }
}
