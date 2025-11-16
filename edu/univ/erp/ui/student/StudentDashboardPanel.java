package edu.univ.erp.ui.student;

import javax.swing.*;
import java.awt.*;

/**
 * StudentDashboardPanel
 * - Quick stats (enrolled courses, upcoming classes, GPA)
 * - Shortcuts to Catalog, My Registrations, Timetable, Grades, Profile
 */
public class StudentDashboardPanel extends JPanel {
    private final JLabel lblEnrolled = new JLabel("0");
    private final JLabel lblUpcoming = new JLabel("0");
    private final JLabel lblGPA = new JLabel("0.00");

    public StudentDashboardPanel() {
        setLayout(new BorderLayout(12,12));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Student Dashboard");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        add(title, BorderLayout.NORTH);

        JPanel cards = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 12));
        cards.add(makeStatCard("Enrolled Courses", lblEnrolled));
        cards.add(makeStatCard("Upcoming Classes", lblUpcoming));
        cards.add(makeStatCard("Current GPA", lblGPA));

        add(cards, BorderLayout.CENTER);

        JPanel shortcuts = new JPanel(new GridLayout(1, 5, 12, 12));
        shortcuts.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        shortcuts.add(makeShortcut("Course Catalog"));
        shortcuts.add(makeShortcut("My Registrations"));
        shortcuts.add(makeShortcut("Timetable"));
        shortcuts.add(makeShortcut("Grades"));
        shortcuts.add(makeShortcut("Profile"));
        add(shortcuts, BorderLayout.SOUTH);

        // load sample numbers
        loadSample();
    }

    private JPanel makeStatCard(String label, JLabel value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setPreferredSize(new Dimension(220, 80));
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220,220,220)), BorderFactory.createEmptyBorder(8,8,8,8)));
        value.setFont(new Font("Segoe UI", Font.BOLD, 22));
        value.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(value, BorderLayout.CENTER);
        JLabel l = new JLabel(label, SwingConstants.CENTER);
        p.add(l, BorderLayout.SOUTH);
        return p;
    }

    private JButton makeShortcut(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBackground(new Color(52,152,219));
        b.setForeground(Color.WHITE);
        return b;
    }

    private void loadSample() {
        // TODO: replace with real student context
        lblEnrolled.setText("4");
        lblUpcoming.setText("2");
        lblGPA.setText("8.32");
    }
}
