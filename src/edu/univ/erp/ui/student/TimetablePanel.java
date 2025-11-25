package edu.univ.erp.ui.student;

import edu.univ.erp.domain.Section;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Very simple timetable: columns = days, each cell lists registered courses for that day
 * with their start-end times and section id.
 */
public class TimetablePanel extends JPanel {
    private final StudentService studentService;
    private final String userId;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

    private JTable table;
    private DefaultTableModel model;
    private JButton btnRefresh;

    private final List<String> DAYS = Arrays.asList(
            "MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY","SUNDAY"
    );

    public TimetablePanel(StudentService studentService, String userId) {
        this.studentService = studentService;
        this.userId = userId;
        initUI();
        loadTimetable();
    }

    private void initUI() {
        setLayout(new BorderLayout(6,6));
        JPanel top = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Weekly Timetable", SwingConstants.LEFT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        top.add(title, BorderLayout.WEST);

        btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadTimetable());
        top.add(btnRefresh, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel();
        table = new JTable(model);
        table.setRowHeight(120); // enough for multiple lines
        table.setEnabled(false);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    /**
     * Loads student's registered sections and places them into the day columns.
     */
    public void loadTimetable() {
        try {
            List<Section> secs = studentService.getTimetable(userId);
            if (secs == null) secs = Collections.emptyList();

            // prepare map day -> list of display lines
            Map<String, List<String>> map = new LinkedHashMap<>();
            for (String d : DAYS) map.put(d, new ArrayList<>());

            for (Section s : secs) {
                List<String> sectionDays = parseDays(s);
                String start = safeTime(s.GetStartTime());
                String end = safeTime(s.GetEndTime());
                String timeDisplay = (start.isEmpty() && end.isEmpty()) ? "TBA" : (start + (end.isEmpty() ? "" : " - " + end));
                String course = safeStr(s.GetCourseID());
                int secId = safeSectionId(s);
                String line = String.format("%s (S%d) — %s", course, secId, timeDisplay);

                for (String d : sectionDays) {
                    d = d.toUpperCase();
                    if (!map.containsKey(d)) {
                        // ignore unknown days, or you could add them
                        continue;
                    }
                    map.get(d).add(line);
                }
            }

            // build table: columns = days
            List<String> header = new ArrayList<>(DAYS);
            model = new DefaultTableModel(header.toArray(), 0);

            // single row: each column cell contains HTML list of lines
            Object[] row = new Object[header.size()];
            for (int i = 0; i < header.size(); i++) {
                List<String> lines = map.get(header.get(i));
                if (lines == null || lines.isEmpty()) row[i] = "";
                else {
                    StringBuilder sb = new StringBuilder("<html>");
                    for (String L : lines) {
                        sb.append(escapeHtml(L)).append("<br>");
                    }
                    sb.append("</html>");
                    row[i] = sb.toString();
                }
            }
            model.addRow(row);
            table.setModel(model);

            // nice widths
            int colCount = table.getColumnCount();
            if (colCount > 0) {
                int w = Math.max(100, getWidth() / Math.max(1, colCount));
                for (int i = 0; i < colCount; i++) table.getColumnModel().getColumn(i).setPreferredWidth(w);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading timetable: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<String> parseDays(Section s) {
        String daysCsv = s.GetDays(); // preferred
        if (daysCsv != null && !daysCsv.trim().isEmpty()) {
            String[] parts = daysCsv.split(",");
            List<String> out = new ArrayList<>();
            for (String p : parts) if (!p.trim().isEmpty()) out.add(p.trim().toUpperCase());
            return out;
        }
        // fallback: single-day enum GetDay()
        try {
            Object dayObj = s.GetDay();
            if (dayObj != null) return Collections.singletonList(dayObj.toString().toUpperCase());
        } catch (Exception ignored) {}
        return Collections.emptyList();
    }

    private String safeTime(String t) {
        if (t == null) return "";
        t = t.trim();
        if (t.isEmpty()) return "";
        // if it's already HH:mm return as is; else try parse/format
        try {
            LocalTime lt = LocalTime.parse(t);
            return lt.format(fmt());
        } catch (Exception e) {
            try {
                LocalTime lt = LocalTime.parse(t, DateTimeFormatter.ofPattern("H:mm"));
                return lt.format(fmt());
            } catch (Exception ex) {
                return t; // fallback raw
            }
        }
    }

    private DateTimeFormatter fmt() { return DateTimeFormatter.ofPattern("HH:mm"); }

    private int safeSectionId(Section s) {
        try { return s.GetSectionID(); } catch (Exception ex) { return -1; }
    }

    private String safeStr(Object o) { return o == null ? "" : String.valueOf(o); }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}
