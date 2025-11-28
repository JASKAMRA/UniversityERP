package edu.univ.erp.ui.instructor;
import edu.univ.erp.access.AccessControl;
import edu.univ.erp.data.DBConnection;
import edu.univ.erp.domain.Role;
import edu.univ.erp.ui.util.CurrentSession;
import edu.univ.erp.service.InstructorService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class CSVImportExportDialog extends JDialog {

    public enum Mode { EXPORT, IMPORT }

    private final InstructorService instructorService;
    private final int sectionId;
    private final String courseTitle;
    private final Mode mode;
    private final String instructorUserId;

    private JButton bChoose;
    private JButton bRun;
    private JTextArea tLog;
    private JFileChooser fc;
    private JLabel lblTarget;

    public CSVImportExportDialog(
            Window owner,
            InstructorService instructorService,
            String instructorUserId,
            int sectionId,
            String courseTitle,
            Mode mode
    ) {
        super(owner, mode == Mode.EXPORT ? "📤 Export Grades" : "📥 Import Grades", ModalityType.APPLICATION_MODAL);
        this.instructorService = instructorService;
        this.instructorUserId = instructorUserId;
        this.sectionId = sectionId;
        this.courseTitle = courseTitle;
        this.mode = mode;

        init();
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void init() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        JPanel headerPanel= new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        
        String action= mode == Mode.EXPORT ? "EXPORT" : "IMPORT";
        JLabel title=new JLabel(String.format("File Operation: %s Grades", action));
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(new Color(70, 130, 180));
        
        lblTarget=new JLabel("Target: " + courseTitle + " (Section ID: " + sectionId + ")");
        lblTarget.setFont(new Font("Arial", Font.ITALIC, 14));

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(lblTarget, BorderLayout.SOUTH);
        
        add(headerPanel, BorderLayout.NORTH);


        JPanel controlPanel=new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controlPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        controlPanel.setBackground(Color.WHITE);
        
        bChoose= new JButton(mode == Mode.EXPORT ? "Select Output File" : "Select Input CSV");
        bRun= new JButton(mode == Mode.EXPORT ? "Run Export" : "Run Import");

        styleButton(bChoose, Color.LIGHT_GRAY, Color.BLACK);
        styleButton(bRun, mode == Mode.EXPORT ? new Color(180, 220, 255) : new Color(255, 180, 180), mode == Mode.EXPORT ? new Color(70, 130, 180) : Color.RED);

        controlPanel.add(bChoose);
        controlPanel.add(bRun);
        add(controlPanel, BorderLayout.CENTER);

        tLog= new JTextArea(10, 60);
        tLog.setEditable(false);
        tLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tLog.setMargin(new Insets(6,6,6,6));
        tLog.setBorder(BorderFactory.createTitledBorder("Operation Log"));
        
        add(new JScrollPane(tLog), BorderLayout.SOUTH);


        fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        if (mode==Mode.EXPORT) {
            fc.setSelectedFile(new File(courseTitle.replace(' ', '_') + "_Section" + sectionId + "_Grades.csv"));
        }


        bChoose.addActionListener(e -> handleChooseFile());
        bRun.addActionListener(e -> runAction());
        
        tLog.append("Dialog initialized. Please select file and run the action.\n");
    }
    
    private void styleButton(JButton button, Color background, Color foreground) {
        button.setPreferredSize(new Dimension(180, 35));
        button.setFocusPainted(false);
        button.setBackground(background);
        button.setForeground(foreground);
    }
    
    private void handleChooseFile() {
        if (mode==Mode.EXPORT) {
            fc.setDialogType(JFileChooser.SAVE_DIALOG);
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file=fc.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".csv"))
                    file=new File(file.getAbsolutePath() + ".csv");
                bChoose.putClientProperty("selectedFile", file);
                bChoose.setText("Save To: " + file.getName());
                tLog.append("Selected output file: " + file.getName() + "\n");
            }
        } else {
            fc.setDialogType(JFileChooser.OPEN_DIALOG);
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                bChoose.putClientProperty("selectedFile", f);
                bChoose.setText("File: " + f.getName());
                tLog.append("Selected input file: " + f.getName() + "\n");
            }
        }
    }

    private void runAction() {
        File file = (File) bChoose.getClientProperty("selectedFile");
        if (file == null) {
            JOptionPane.showMessageDialog(this, "Please choose a file.", "No file", JOptionPane.WARNING_MESSAGE);
            return;
        }
        tLog.setText("Starting operation...\n");

        Role role = CurrentSession.get().getUsr().GetRole();
        if (!AccessControl.isActionAllowed(role, true)) {
            tLog.append("Operation denied: System in maintenance mode.\n");
            JOptionPane.showMessageDialog(this,
                    "System is in maintenance mode.\nWrite operations (Import/Export) are disabled.",
                    "Maintenance ON",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!instructorService.isInstructorIn(instructorUserId, sectionId)) {
            tLog.append("Operation denied: Instructor does not own this section.\n");
            JOptionPane.showMessageDialog(this, "Not your section!", "Permission denied", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (mode != Mode.EXPORT){
            doImport(file);
        }   
        else{ 
            doExport(file);
        }
    }

    private void doExport(File file) {
        int rowsExported = 0;
        try (Connection connect = DBConnection.getStudentConnection()) {
  
            String sql =
                    "SELECT e.enrollment_id, st.roll_no, st.name, g.component, g.score " +
                    "FROM enrollments e " +
                    "JOIN students st ON e.student_id = st.student_id " +
                    "LEFT JOIN grades g ON e.enrollment_id = g.enrollment_id " +
                    "WHERE e.section_id = ? " +
                    "ORDER BY st.roll_no, g.component";

            PreparedStatement prepStatement = connect.prepareStatement(sql);
            prepStatement.setInt(1, sectionId);

            ResultSet resultSet = prepStatement.executeQuery();

            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println("enrollment_id,roll_no,name,component,score");
                while (resultSet.next()) {
                    pw.printf("%s,%s,%s,%s,%s%n",
                            resultSet.getInt("enrollment_id"),
                            escape(resultSet.getString("roll_no")),
                            escape(resultSet.getString("name")),
                            escape(resultSet.getString("component")),
                            resultSet.getObject("score")
                    );
                    rowsExported++;
                }
            }
            tLog.append(String.format("Export complete. %d rows written to %s.\n", rowsExported, file.getName()));
            JOptionPane.showMessageDialog(this, String.format("Export successful! %d records exported.", rowsExported));

        } catch (Exception exception) {
            tLog.append("Error during export: " + exception.getMessage() + "\n");
            JOptionPane.showMessageDialog(this, "Export failed: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doImport(File file) {
        int rowsRead=0;
        int successfulSaves=0;
        
        try (BufferedReader bufferRead = new BufferedReader(new FileReader(file));
             Connection connect = DBConnection.getStudentConnection()) {

            String header=bufferRead.readLine(); 
            if (header==null) {
                tLog.append("Error: Input file is empty.\n");
                JOptionPane.showMessageDialog(this, "Input file is empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (header.contains("component") &&!(header.contains("enrollment_id")  && header.contains("score"))) {
                 tLog.append("Error: CSV header is missing required fields (enrollment_id, component, score).\n");
                 JOptionPane.showMessageDialog(this, "CSV header is invalid. Must contain enrollment_id, component, and score.", "Error", JOptionPane.ERROR_MESSAGE);
                 return;
            }

            String line;
            while ((line = bufferRead.readLine()) != null) {
                rowsRead++;
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    String[] a=split(line);
                    int enrollmentId=Integer.parseInt(a[0].trim());
                    String comp=a[3].trim();
                    String scoreStr=a[4].trim();
                    
                    BigDecimal score=scoreStr.isEmpty() ? null : new BigDecimal(scoreStr);
                    
                    boolean ok=instructorService.Save_Grade(enrollmentId, comp, score);
                    
                    if (!ok) {
                        tLog.append(String.format("Row %d: Failed to save (EnrollID %d, Comp %s). Check enrollment ID.\n", rowsRead, enrollmentId, comp));
                    }
                    else {
                        successfulSaves++;
                    }
                } 
                catch (NumberFormatException | ArrayIndexOutOfBoundsException exception) {
                    tLog.append(String.format("Row %d: Parsing error or missing data. Skipped line: %s\n", rowsRead, line.substring(0, Math.min(line.length(), 40)) + "..."));
                }
            }

            tLog.append(String.format("Import complete. Read %d rows. Successfully saved/updated %d records.\n", rowsRead, successfulSaves));
            JOptionPane.showMessageDialog(this, String.format("Import finished. Successfully updated %d records.", successfulSaves));

        } 
        catch (Exception exception) {
            tLog.append("Fatal error during import: " + exception.getMessage() + "\n");
            JOptionPane.showMessageDialog(this, "Import failed: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String escape(String str) {
        if (str==null){ 
            return "";
        }
        if (str.contains("\"") || str.contains(",") || str.contains("\n")){
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }

    private String[] split(String l) {
        List<String> o=new ArrayList<>();
        StringBuilder stringbuild=new StringBuilder();
        boolean inQ=false;

        for (char c : l.toCharArray()) {
            if (c == '"') inQ = !inQ;
            else if (c == ',' && !inQ) {
                o.add(stringbuild.toString().trim());
                stringbuild.setLength(0);
            } else stringbuild.append(c);
        }
        o.add(stringbuild.toString().trim());
        return o.toArray(new String[0]);
    }
}