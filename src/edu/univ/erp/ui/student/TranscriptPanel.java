package edu.univ.erp.ui.student;
import edu.univ.erp.service.StudentService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutionException;

public class TranscriptPanel extends JPanel {
    private final StudentService studentService;
    private final String userId;
    private final JButton btnGenerate;
    private final JButton btnOpenLast;
    private File lastGenerated;


    public TranscriptPanel(StudentService studentService, String userId) {
        this.studentService=studentService;
        this.userId=userId;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        JPanel header=new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel title=new JLabel("📜  Official Transcript Download");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(new Color(0, 102, 204));

        JLabel sub=new JLabel("Generate and download your academic record as a CSV file.");
        sub.setFont(new Font("Arial", Font.PLAIN, 12));
        sub.setForeground(new Color(120, 120, 120));

        JPanel title_Block=new JPanel();
        title_Block.setLayout(new BoxLayout(title_Block, BoxLayout.Y_AXIS));
        title_Block.setBackground(Color.WHITE);
        title_Block.add(title);
        title_Block.add(Box.createVerticalStrut(6));
        title_Block.add(sub);

        header.add(title_Block, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel center=new JPanel(new GridBagLayout());
        center.setBackground(Color.WHITE);

        GridBagConstraints constraint=new GridBagConstraints();
        constraint.insets=new Insets(15, 15, 15, 15);
        constraint.gridx=0;

        btnGenerate=new JButton("💾 Generate & Save Transcript");
        btnOpenLast=new JButton("📁 Open Last Saved File");

        styleButton(btnGenerate, new Color(0, 102, 204), Color.WHITE);
        styleButton(btnOpenLast, Color.LIGHT_GRAY, Color.BLACK);

        btnOpenLast.setEnabled(false); 

        constraint.gridy = 0;
        center.add(btnGenerate, constraint);
        constraint.gridy = 1;
        center.add(btnOpenLast, constraint);

        add(center, BorderLayout.CENTER);

        btnGenerate.addActionListener(e -> onGenerate());
        btnOpenLast.addActionListener(e -> onOpenLast());
    }

    private void styleButton(JButton btn, Color b, Color f) {
        btn.setPreferredSize(new Dimension(220, 40));
        btn.setFocusPainted(false);
        btn.setBackground(b);
        btn.setForeground(f);
    }

    private void onGenerate() {
        btnGenerate.setEnabled(false);
        SwingWorker<File, Void> w=new SwingWorker<>() {
            @Override
            protected File doInBackground() throws Exception {
                return studentService.CsvGeneration(userId);
            }

            @Override
            protected void done() {
                btnGenerate.setEnabled(true);
                try {
                    File src = get();
                    if (src == null|| !src.exists()) {
                        JOptionPane.showMessageDialog(TranscriptPanel.this, "Transcript generation failed (service returned no file).", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    JFileChooser choose=new JFileChooser();
                    choose.setDialogTitle("Save Transcript as");
                    choose.setSelectedFile(new File("transcript_" + userId + ".csv"));

                    int rv=choose.showSaveDialog(TranscriptPanel.this);
                    if (rv!=JFileChooser.APPROVE_OPTION) {
                        lastGenerated=src;
                        btnOpenLast.setEnabled(true);
                        return;
                    }

                    File dest = choose.getSelectedFile();
                    Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    lastGenerated = dest;
                    btnOpenLast.setEnabled(true);

                    JOptionPane.showMessageDialog(TranscriptPanel.this, "Transcript saved successfully to:\n" + dest.getAbsolutePath(), "Saved", JOptionPane.INFORMATION_MESSAGE);

                } 
                catch (ExecutionException execException) {
                    Throwable cause = execException.getCause();
                    JOptionPane.showMessageDialog(TranscriptPanel.this, "Failed to generate transcript: " + (cause == null ? execException.getMessage() : cause.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
                    if (execException.getCause() != null) execException.getCause().printStackTrace();
                } catch (InterruptedException intException) {
                    JOptionPane.showMessageDialog(TranscriptPanel.this, "Operation interrupted", "Error", JOptionPane.ERROR_MESSAGE);
                    Thread.currentThread().interrupt();
                } catch (Exception exception) {
                    JOptionPane.showMessageDialog(TranscriptPanel.this, "Error saving transcript: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    exception.printStackTrace();
                }
            }
        };
        w.execute();
    }

    private void onOpenLast() {
        if (!lastGenerated.exists()||lastGenerated == null) {
            JOptionPane.showMessageDialog(this, "No generated transcript available.", "Info", JOptionPane.INFORMATION_MESSAGE);
            btnOpenLast.setEnabled(false);
            return;
        }
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(lastGenerated);
            } 
            else {
                JOptionPane.showMessageDialog(this, "Opening files is not supported on this platform.\nFile is at: " + lastGenerated.getAbsolutePath(), "Unsupported", JOptionPane.WARNING_MESSAGE);
            }
        } 
        catch (Exception exception) {
            JOptionPane.showMessageDialog(this, "Failed to open file: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            exception.printStackTrace();
        }
    }
}
