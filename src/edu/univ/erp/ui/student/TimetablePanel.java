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

public class TimetablePanel extends JPanel {
    private final StudentService studentService;
    private final String userId;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
    private JTable table;
    private DefaultTableModel model;
    private JButton btnRefresh;

    private final List<String> DAYS = Arrays.asList(
            "MONDAY",
            "TUESDAY",
            "WEDNESDAY",
            "THURSDAY",
            "FRIDAY",
            "SATURDAY",
            "SUNDAY"
    );

    public TimetablePanel(StudentService studentService, String userId) {
        this.studentService=studentService;
        this.userId=userId;
        loadTimetable();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(6,6));
        JPanel top=new JPanel(new BorderLayout());
        JLabel title=new JLabel("Weekly Timetable", SwingConstants.LEFT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        top.add(title, BorderLayout.WEST);

        btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadTimetable());
        top.add(btnRefresh, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        model=new DefaultTableModel();
        table=new JTable(model);
        table.setRowHeight(120); 
        table.setEnabled(false);

        table.setDefaultRenderer(Object.class, new MultiLineCellRenderer());

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void loadTimetable() {
        try {
            List<Section> sec=studentService.getTimeTable(userId);
            if (sec==null){
                sec=Collections.emptyList();
            }

            Map<String, List<String>> map=new LinkedHashMap<>();
            for (String d : DAYS){
                map.put(d, new ArrayList<>());
            }

            for (Section s : sec) {
                List<String> sectionDays = parseDays(s);
                String start=safeTime(s.GetStartTime());
                String end=safeTime(s.GetEndTime());
                String time_Display=(start.isEmpty() && end.isEmpty()) ? "TBA" : (start + (end.isEmpty() ? "" : " - " + end));
                String course=safeStr(s.GetCourseID());
                int sec_Id=safeSectionId(s);
                String line=String.format("%s (S%d) — %s", course, sec_Id, time_Display);

                for (String day : sectionDays) {
                    day = day.toUpperCase();
                    if (!map.containsKey(day)) {
                        continue;
                    }
                    map.get(day).add(line);
                }
            }

            List<String> header=new ArrayList<>(DAYS);
            model=new DefaultTableModel(header.toArray(), 0);

            Object[] row=new Object[header.size()];
            for (int i = 0; i < header.size(); i++) {
                List<String> lines = map.get(header.get(i));
                if (lines.isEmpty()||lines == null) {
                    row[i] = "";
                } 
                else {
                    StringBuilder string_b = new StringBuilder();
                    for (int k = 0; k<lines.size(); k++) {
                        string_b.append(lines.get(k));
                        if (k<lines.size() - 1) {
                            string_b.append("\n");
                        }
                    }
                    row[i]=string_b.toString();
                }
            }
            model.addRow(row);
            table.setModel(model);

            table.setDefaultRenderer(Object.class, new MultiLineCellRenderer());
            adjustRowHeights();

            int colCount = table.getColumnCount();
            if (colCount > 0) {
                int w = Math.max(100, getWidth() / Math.max(1, colCount));
                for (int i = 0; i < colCount; i++) {
                    table.getColumnModel().getColumn(i).setPreferredWidth(w);
                }
            }

        } 
        catch (Exception exception) {
            exception.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading timetable: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void adjustRowHeights() {
        int rows = table.getRowCount();
        for (int row = 0; row < row; row++) {
            int maxHeight = table.getRowHeight(); // start with current
            for (int column = 0; column < table.getColumnCount(); column++) {
                TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(cellRenderer, row, column);
                comp.setSize(table.getColumnModel().getColumn(column).getWidth(), Integer.MAX_VALUE);
                int prefHeight = comp.getPreferredSize().height + table.getIntercellSpacing().height;
                maxHeight = Math.max(maxHeight, prefHeight);
            }
            if (table.getRowHeight(row)!=maxHeight) {
                table.setRowHeight(row, maxHeight);
            }
        }
    }

    private static class MultiLineCellRenderer extends JTextArea implements TableCellRenderer {
        public MultiLineCellRenderer() {
            setLineWrap(true);
            setFont(new Font("Monospaced", Font.PLAIN, 12)); 
            setWrapStyleWord(true);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            if (value != null) {
                setText(String.valueOf(value));
            } 
            else {
                setText("");
            }

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } 
            else {
                if (row % 2 != 0){
                    setBackground(new Color(250, 250, 250));
                }
                else{
                    setBackground(Color.WHITE);
                }
                setForeground(Color.BLACK);
            }
            setSize(table.getColumnModel().getColumn(column).getWidth(), Short.MAX_VALUE);

            return this;
        }
    }

    private List<String> parseDays(Section sec) {
        String daysCsv=sec.GetDays(); 
        if (!daysCsv.trim().isEmpty() && daysCsv != null ) {
            String[] parts = daysCsv.split(",");
            List<String> out = new ArrayList<>();
            for (String p : parts){ 
                if (!p.trim().isEmpty()) {
                    out.add(p.trim().toUpperCase());
                }
            }
            return out;
        }
        try {
            Object dayObj = sec.GetDay();
            if (dayObj != null) {
                return Collections.singletonList(dayObj.toString().toUpperCase());
            }
        } 
        catch (Exception ignored) {}
        return Collections.emptyList();
    }

    private String safeTime(String t) {
        if (t == null) {
            return "";
        }
        t = t.trim();
        if (t.isEmpty()) {
            return "";
        }   
        try {
            LocalTime local_t = LocalTime.parse(t);
            return local_t.format(fmt());
        } 
        catch (Exception exception) {
            try {
                LocalTime local_t = LocalTime.parse(t, DateTimeFormatter.ofPattern("H:mm"));
                return local_t.format(fmt());
            } 
            catch (Exception e) {
                return t;
            }
        }
    }

    private DateTimeFormatter fmt() { 
        return DateTimeFormatter.ofPattern("HH:mm"); 
    }

    private int safeSectionId(Section sec) {
        try { 
            return sec.GetSectionID(); 
        } 
        catch (Exception exception) { 
            return -1; 
        }
    }

    private String safeStr(Object obj) { 
        return obj == null ? "" : String.valueOf(obj); 
    }
}
