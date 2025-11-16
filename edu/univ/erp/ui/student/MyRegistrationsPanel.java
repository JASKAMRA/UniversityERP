package edu.univ.erp.ui.student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * MyRegistrationsPanel
 * - Lists student's registrations
 * - Drop / View details
 */
public class MyRegistrationsPanel extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;
    private final List<RegRow> regs = new ArrayList<>();
    private final JLabel lblCount = new JLabel("0");

    public MyRegistrationsPanel() {
        setLayout(new BorderLayout(8,8));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("My Registrations");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"Reg ID","Course","Section","Term","Status"}, 0) {
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        table = new JTable(model);
        table.setRowHeight(26);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton dropBtn = new JButton("Drop");
        JButton viewBtn = new JButton("View");
        bottom.add(lblCount);
        bottom.add(viewBtn);
        bottom.add(dropBtn);
        add(bottom, BorderLayout.SOUTH);

        dropBtn.addActionListener(e -> dropSelected());
        viewBtn.addActionListener(e -> viewSelected());

        loadSample();
        refresh();
    }

    private void loadSample() {
        regs.clear();
        regs.add(new RegRow("R-9001","CS101","SEC101","Fall 2025","Enrolled"));
        regs.add(new RegRow("R-9002","CS201","SEC202","Fall 2025","Waitlisted"));
    }

    private void refresh() {
        SwingUtilities.invokeLater(() -> {
            model.setRowCount(0);
            for (RegRow r : regs) model.addRow(new Object[]{r.regId, r.course, r.section, r.term, r.status});
            lblCount.setText(model.getRowCount() + " registrations");
        });
    }

    private void dropSelected() {
        int r = table.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Select a registration to drop."); return; }
        int m = table.convertRowIndexToModel(r);
        RegRow rr = regs.get(m);
        int conf = JOptionPane.showConfirmDialog(this, "Drop registration " + rr.regId + " for " + rr.course + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;
        // TODO: call drop API
        regs.remove(m);
        refresh();
        JOptionPane.showMessageDialog(this, "Dropped.");
    }

    private void viewSelected() {
        int r = table.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Select a registration."); return; }
        int m = table.convertRowIndexToModel(r);
        RegRow rr = regs.get(m);
        JOptionPane.showMessageDialog(this, String.format("Registration: %s\nCourse: %s\nSection: %s\nTerm: %s\nStatus: %s", rr.regId, rr.course, rr.section, rr.term, rr.status), "Registration Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private static class RegRow {
        String regId, course, section, term, status;
        RegRow(String regId, String course, String section, String term, String status){ this.regId=regId; this.course=course; this.section=section; this.term=term; this.status=status; }
    }
}
