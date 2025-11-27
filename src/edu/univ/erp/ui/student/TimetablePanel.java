package edu.univ.erp.ui.student;

import edu.univ.erp.domain.Section;
import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * TimetablePanel without HTML: uses a JTextArea-based cell renderer to show multi-line cells.
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
        table.setRowHeight(120); // initial guess; will be adjusted after filling data
        table.setEnabled(false);

        // Use our multiline renderer for Object.class (applies to all columns)
        table.setDefaultRenderer(Object.class, new MultiLineCellRenderer());

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    /**
     * Loads student's registered sections and places them into the day columns.
     * This version places plain text with '\n' for new lines and uses a JTextArea renderer.
     */
    public void loadTimetable() {
        try {
            List<Section> secs = studentService.getTimeTable(userId);
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
                        // ignore unknown days
                        continue;
                    }
                    map.get(d).add(line);
                }
            }

            // build table: columns = days
            List<String> header = new ArrayList<>(DAYS);
            model = new DefaultTableModel(header.toArray(), 0);

            // single row: each column cell contains plain text with newline separators
            Object[] row = new Object[header.size()];
            for (int i = 0; i < header.size(); i++) {
                List<String> lines = map.get(header.get(i));
                if (lines == null || lines.isEmpty()) {
                    row[i] = "";
                } else {
                    // join with newline — renderer will wrap correctly
                    StringBuilder sb = new StringBuilder();
                    for (int k = 0; k < lines.size(); k++) {
                        sb.append(lines.get(k));
                        if (k < lines.size() - 1) sb.append("\n");
                    }
                    row[i] = sb.toString();
                }
            }
            model.addRow(row);
            table.setModel(model);

            // ensure the same multiline renderer remains set (model reset can change renderers)
            table.setDefaultRenderer(Object.class, new MultiLineCellRenderer());

            // adjust row height to fit tallest cell
            adjustRowHeights();

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

    /**
     * Adjust the table row height so that the multiline text fits.
     * Works for single-row timetable (but also safe if multiple rows later).
     */
    private void adjustRowHeights() {
        int rows = table.getRowCount();
        for (int row = 0; row < rows; row++) {
            int maxHeight = table.getRowHeight(); // start with current
            for (int column = 0; column < table.getColumnCount(); column++) {
                TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(cellRenderer, row, column);
                comp.setSize(table.getColumnModel().getColumn(column).getWidth(), Integer.MAX_VALUE);
                int prefHeight = comp.getPreferredSize().height + table.getIntercellSpacing().height;
                maxHeight = Math.max(maxHeight, prefHeight);
            }
            if (table.getRowHeight(row) != maxHeight) {
                table.setRowHeight(row, maxHeight);
            }
        }
    }

    /**
     * Renderer that uses a JTextArea to display multiline content (line wrap).
     */
    private static class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {
        public MultiLineCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
            setFont(new Font("Monospaced", Font.PLAIN, 12)); // you can change font
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            if (value == null) {
                setText("");
            } else {
                setText(String.valueOf(value));
            }

            // background/foreground styling
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                // striped rows look: alternate background if you want
                if (row % 2 == 0) setBackground(Color.WHITE);
                else setBackground(new Color(250, 250, 250));
                setForeground(Color.BLACK);
            }

            // set width to column width so preferredSize wraps properly
            setSize(table.getColumnModel().getColumn(column).getWidth(), Short.MAX_VALUE);

            return this;
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
}
