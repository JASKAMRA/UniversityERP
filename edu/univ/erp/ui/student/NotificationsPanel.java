package edu.univ.erp.ui.student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * NotificationsPanel
 * - List unread/read notifications; mark read; archive
 */
public class NotificationsPanel extends JPanel {
    private final DefaultTableModel model;
    private final JTable table;
    private final java.util.List<Notif> notifs = new ArrayList<>();

    public NotificationsPanel() {
        setLayout(new BorderLayout(8,8));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Notifications");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"Time","Message","Status"},0) {
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        table = new JTable(model);
        table.setRowHeight(26);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton markRead = new JButton("Mark Read");
        JButton archive = new JButton("Archive");
        bottom.add(markRead); bottom.add(archive);
        add(bottom, BorderLayout.SOUTH);

        markRead.addActionListener(e -> markSelected(true));
        archive.addActionListener(e -> archiveSelected());

        loadSample();
        refresh();
    }

    private void loadSample() {
        notifs.clear();
        notifs.add(new Notif("2025-11-01 10:00","Registration confirmed for CS101","Unread"));
        notifs.add(new Notif("2025-11-05 08:30","Grades released for CS101","Unread"));
        notifs.add(new Notif("2025-10-28 09:00","Fee payment reminder","Read"));
    }

    private void refresh() {
        SwingUtilities.invokeLater(() -> {
            model.setRowCount(0);
            for (Notif n : notifs) model.addRow(new Object[]{n.time, n.message, n.status});
        });
    }

    private void markSelected(boolean read) {
        int r = table.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Select a notification."); return; }
        int m = table.convertRowIndexToModel(r);
        Notif n = notifs.get(m);
        n.status = read ? "Read" : "Unread";
        refresh();
    }

    private void archiveSelected() {
        int r = table.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Select a notification."); return; }
        int m = table.convertRowIndexToModel(r);
        notifs.remove(m);
        refresh();
    }

    private static class Notif { String time, message, status; Notif(String t,String m,String s){time=t;message=m;status=s;} }
}
