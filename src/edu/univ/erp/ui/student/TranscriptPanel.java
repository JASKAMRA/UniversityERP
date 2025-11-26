package edu.univ.erp.ui.student;

import edu.univ.erp.service.StudentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutionException;

/**
 * TranscriptPanel - simple UI to request transcript generation (CSV) from StudentService,
 * let the user choose a save location and (optionally) open the file after saving.
 *
 * Constructor: TranscriptPanel(StudentService service, String userId)
 */
public class TranscriptPanel extends JPanel {

    private final StudentService studentService;
    private final String userId;

    private final JButton btnGenerate;
    private final JButton btnOpenLast;
    private File lastGenerated;

    // --- Aesthetic constants ---
    private static final int PADDING = 20;
    private static final int GAP = 15;
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 20);
    private static final Dimension BUTTON_SIZE = new Dimension(220, 40);
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204); // Deep Blue

    public TranscriptPanel(StudentService studentService, String userId) {
        this.studentService = studentService;
        this.userId = userId;

        // 1. Overall Layout & Padding
        setLayout(new BorderLayout(GAP, GAP));
        setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));
        setBackground(Color.WHITE);
        
        // 2. Header (North)
        JLabel lbl = new JLabel("<html>📜 **Official Transcript Download**<br/><span style='font-size:12px; color:gray;'>Generate and download your academic record as a CSV file.</span></html>");
        lbl.setFont(TITLE_FONT);
        lbl.setForeground(PRIMARY_COLOR);
        lbl.setBorder(new EmptyBorder(0, 0, GAP, 0)); 
        add(lbl, BorderLayout.NORTH);

        // 3. Center Action Panel (using GridBagLayout to center content)
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(Color.WHITE);
        
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(GAP, GAP, GAP, GAP);
        c.gridx = 0;
        
        btnGenerate = new JButton("💾 Generate & Save Transcript");
        btnOpenLast = new JButton("📁 Open Last Saved File");
        
        // Style Buttons
        styleButton(btnGenerate, PRIMARY_COLOR, Color.WHITE);
        styleButton(btnOpenLast, Color.LIGHT_GRAY, Color.BLACK);
        
        btnOpenLast.setEnabled(false); // Initial state

        c.gridy = 0; center.add(btnGenerate, c);
        c.gridy = 1; center.add(btnOpenLast, c);
        
        add(center, BorderLayout.CENTER);

        // 4. Actions
        btnGenerate.addActionListener(e -> onGenerate());
        btnOpenLast.addActionListener(e -> onOpenLast());
    }
    
    private void styleButton(JButton button, Color bg, Color fg) {
        button.setPreferredSize(BUTTON_SIZE);
        button.setFocusPainted(false);
        button.setBackground(bg);
        button.setForeground(fg);
    }

    private void onGenerate() {
        btnGenerate.setEnabled(false);
        SwingWorker<File, Void> w = new SwingWorker<>() {
            @Override
            protected File doInBackground() throws Exception {
                // expected signature in service: File generateTranscriptCsv(String userId) throws Exception
                return studentService.GenerateCSV(userId);
            }

            @Override
            protected void done() {
                btnGenerate.setEnabled(true);
                try {
                    File src = get();
                    if (src == null || !src.exists()) {
                        JOptionPane.showMessageDialog(TranscriptPanel.this, "Transcript generation failed (service returned no file).", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // ask user where to save
                    JFileChooser chooser = new JFileChooser();
                    chooser.setDialogTitle("Save Transcript as");
                    chooser.setSelectedFile(new File("transcript_" + userId + ".csv"));
                    
                    int rv = chooser.showSaveDialog(TranscriptPanel.this);
                    if (rv != JFileChooser.APPROVE_OPTION) {
                        // user cancelled: keep lastGenerated (temp file) but don't copy
                        lastGenerated = src;
                        btnOpenLast.setEnabled(true);
                        return;
                    }
                    
                    File dest = chooser.getSelectedFile();
                    // copy file
                    Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    lastGenerated = dest;
                    btnOpenLast.setEnabled(true);

                    JOptionPane.showMessageDialog(TranscriptPanel.this, "Transcript saved successfully to:\n" + dest.getAbsolutePath(), "Saved", JOptionPane.INFORMATION_MESSAGE);
                
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    JOptionPane.showMessageDialog(TranscriptPanel.this, "Failed to generate transcript: " + (cause == null ? ex.getMessage() : cause.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
                    cause.printStackTrace();
                } catch (InterruptedException ie) {
                    JOptionPane.showMessageDialog(TranscriptPanel.this, "Operation interrupted", "Error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(TranscriptPanel.this, "Error saving transcript: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        };
        w.execute();
    }

    private void onOpenLast() {
        if (lastGenerated == null || !lastGenerated.exists()) {
            JOptionPane.showMessageDialog(this, "No generated transcript available.", "Info", JOptionPane.INFORMATION_MESSAGE);
            btnOpenLast.setEnabled(false);
            return;
        }
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(lastGenerated);
            } else {
                JOptionPane.showMessageDialog(this, "Opening files is not supported on this platform.\nFile is at: " + lastGenerated.getAbsolutePath(), "Unsupported", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to open file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}