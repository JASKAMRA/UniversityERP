package edu.univ.erp.ui.student;

import edu.univ.erp.service.StudentService;

import javax.swing.*;
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

    public TranscriptPanel(StudentService studentService, String userId) {
        this.studentService = studentService;
        this.userId = userId;

        setLayout(new BorderLayout(8,8));
        JLabel lbl = new JLabel("<html><b>Transcript</b><br/>Generate and download your transcript (CSV)</html>");
        lbl.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        add(lbl, BorderLayout.NORTH);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnGenerate = new JButton("Generate & Save CSV");
        btnOpenLast = new JButton("Open Last Generated");
        btnOpenLast.setEnabled(false);
        center.add(btnGenerate);
        center.add(btnOpenLast);
        add(center, BorderLayout.CENTER);

        btnGenerate.addActionListener(e -> onGenerate());
        btnOpenLast.addActionListener(e -> onOpenLast());
    }

    private void onGenerate() {
        btnGenerate.setEnabled(false);
        SwingWorker<File, Void> w = new SwingWorker<>() {
            @Override
            protected File doInBackground() throws Exception {
                // try calling a service method we expect to exist
                // expected signature in service: File generateTranscriptCsv(String userId) throws Exception
                return studentService.generateTranscriptCsv(userId);
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
                    chooser.setDialogTitle("Save transcript as");
                    chooser.setSelectedFile(new File("transcript_" + userId + ".csv"));
                    int rv = chooser.showSaveDialog(TranscriptPanel.this);
                    if (rv != JFileChooser.APPROVE_OPTION) {
                        // user cancelled: keep lastGenerated but don't copy
                        lastGenerated = src;
                        btnOpenLast.setEnabled(true);
                        return;
                    }
                    File dest = chooser.getSelectedFile();
                    // copy file
                    Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    lastGenerated = dest;
                    btnOpenLast.setEnabled(true);

                    JOptionPane.showMessageDialog(TranscriptPanel.this, "Transcript saved to:\n" + dest.getAbsolutePath(), "Saved", JOptionPane.INFORMATION_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Opening files is not supported on this platform.\nFile is at: " + lastGenerated.getAbsolutePath());
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to open file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
