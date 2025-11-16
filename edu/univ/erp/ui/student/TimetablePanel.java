package edu.univ.erp.ui.student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * TimetablePanel
 * - Weekly timetable view simple grid (Mon-Fri, timeslots)
 * - Click a cell to view class details (dialog)
 */
public class TimetablePanel extends JPanel {
    private final String[] days = {"Time","Monday","Tuesday","Wednesday","Thursday","Friday"};
    private final DefaultTableModel model;
    private final JTable table;
    private final List<ClassSlot> slots = new ArrayList<>();
    private final SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm");

    public TimetablePanel() {
        setLayout(new BorderLayout(8,8));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Timetable");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(days, 0) {
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        table = new JTable(model);
        table.setRowHeight(60);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadSample();
        buildGrid();

        table.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseClicked(java.awt.event.MouseEvent e){
                int r = table.rowAtPoint(e.getPoint()), c = table.columnAtPoint(e.getPoint());
                if (r>=0 && c>=1) {
                    String cell = (String) model.getValueAt(r,c);
                    if (cell != null && !cell.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(TimetablePanel.this, cell, "Class details", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });
    }

    private void loadSample() {
        slots.clear();
        slots.add(new ClassSlot("Mon","09:00","10:00","CS101 - Lecture (Room A)"));
        slots.add(new ClassSlot("Wed","09:00","10:00","CS101 - Lecture (Room A)"));
        slots.add(new ClassSlot("Tue","11:00","12:00","MA101 - Tutorial (Room B)"));
    }

    private void buildGrid() {
        // timeslots: simple set of rows covering day hours
        String[] times = {"08:00-09:00","09:00-10:00","10:00-11:00","11:00-12:00","13:00-14:00","14:00-15:00"};
        model.setRowCount(0);
        for (String t : times) {
            Object[] row = new Object[6];
            row[0] = t;
            for (int i=1;i<6;i++) row[i] = "";
            model.addRow(row);
        }
        for (ClassSlot s : slots) {
            int col = Arrays.asList("Monday","Tuesday","Wednesday","Thursday","Friday").indexOf(dayFull(s.day)) + 1;
            int row = findRowForTime(s.start);
            if (col>=1 && row>=0) {
                String prev = (String) model.getValueAt(row, col);
                model.setValueAt((prev==null?"":prev+"\n") + s.desc, row, col);
            }
        }
    }

    private int findRowForTime(String start) {
        String[] times = {"08:00-09:00","09:00-10:00","10:00-11:00","11:00-12:00","13:00-14:00","14:00-15:00"};
        for (int i=0;i<times.length;i++) if (times[i].startsWith(start)) return i;
        return -1;
    }

    private String dayFull(String shortDay) {
        switch (shortDay.toLowerCase()) {
            case "mon": return "Monday";
            case "tue": return "Tuesday";
            case "wed": return "Wednesday";
            case "thu": return "Thursday";
            case "fri": return "Friday";
        }
        return shortDay;
    }

    private static class ClassSlot {
        String day, start, end, desc;
        ClassSlot(String day, String start, String end, String desc){ this.day=day; this.start=start; this.end=end; this.desc=desc; }
    }
}
