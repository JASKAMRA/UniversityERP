package edu.univ.erp.ui.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * BackupRestorePanel
 *
 * Features:
 * - Backup: copy current DB file to chosen folder with timestamped filename
 * - Restore: choose a backup file and overwrite current DB file (confirmation)
 * - Schedule UI (simple save to file; replace with real scheduler if needed)
 * - Progress bar + log area
 *
 * IMPORTANT:
 * - Set DATABASE_PATH to your actual DB file location (e.g., UNIVERSITYERP/data/university.db)
 * - This UI uses file-copy semantics. For production DBs (open DB connections),
 *   flush/close DB connections before copying/restore or use DB dump utilities.
 */
public class BackupRestorePanel extends JPanel {

    // TODO: set this to the actual path to your DB file
    private static final Path DATABASE_PATH = Paths.get("UNIVERSITYERP", "data", "university.db");

    private final JTextField dbPathField;
    private final JButton backupBtn;
    private final JButton restoreBtn;
    private final JButton chooseBackupFolderBtn;
    private final JProgressBar progressBar;
    private final JTextArea logArea;
    private final JTextField scheduleTimeField;
    private final JComboBox<String> scheduleFrequencyBox;
    private final JButton saveScheduleBtn;

    private Path lastBackupFolder = Paths.get(System.getProperty("user.home"));

    public BackupRestorePanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setBackground(Color.WHITE);

        // Header
        JLabel title = new JLabel("Backup & Restore");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        // Center panel
        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        add(center, BorderLayout.CENTER);

        // Control panel on top
        JPanel ctrl = new JPanel(new GridBagLayout());
        ctrl.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        // Database path display
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        ctrl.add(new JLabel("Database file:"), g);
        dbPathField = new JTextField(DATABASE_PATH.toString(), 40);
        dbPathField.setEditable(false);
        g.gridx = 1; g.weightx = 1.0;
        ctrl.add(dbPathField, g);

        // Choose backup folder
        chooseBackupFolderBtn = new JButton("Backup Folder...");
        g.gridx = 2; g.weightx = 0;
        ctrl.add(chooseBackupFolderBtn, g);

        // Backup / Restore buttons
        backupBtn = new JButton("Backup Now");
        restoreBtn = new JButton("Restore From Backup");

        stylePrimary(backupBtn);
        restoreBtn.setBackground(new Color(220, 80, 80));
        restoreBtn.setForeground(Color.WHITE);

        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        ctrl.add(backupBtn, g);
        g.gridx = 1; g.gridwidth = 2; g.anchor = GridBagConstraints.EAST;
        ctrl.add(restoreBtn, g);
        g.gridwidth = 1; g.anchor = GridBagConstraints.WEST;

        center.add(ctrl, BorderLayout.NORTH);

        // Progress + log
        JPanel mid = new JPanel(new BorderLayout(8, 8));
        mid.setOpaque(false);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        mid.add(progressBar, BorderLayout.NORTH);

        logArea = new JTextArea(10, 80);
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        mid.add(logScroll, BorderLayout.CENTER);

        center.add(mid, BorderLayout.CENTER);

        // Schedule panel (bottom)
        JPanel schedulePanel = new JPanel(new GridBagLayout());
        schedulePanel.setOpaque(false);
        schedulePanel.setBorder(BorderFactory.createTitledBorder("Schedule Backup (UI-only)"));

        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        schedulePanel.add(new JLabel("Frequency:"), g);
        scheduleFrequencyBox = new JComboBox<>(new String[]{"Daily", "Weekly", "Monthly", "Manual only"});
        g.gridx = 1; g.weightx = 1.0;
        schedulePanel.add(scheduleFrequencyBox, g);

        g.gridx = 0; g.gridy = 1; g.weightx = 0;
        schedulePanel.add(new JLabel("Time (HH:mm):"), g);
        scheduleTimeField = new JTextField("02:00", 8);
        g.gridx = 1;
        schedulePanel.add(scheduleTimeField, g);

        saveScheduleBtn = new JButton("Save Schedule");
        stylePrimary(saveScheduleBtn);
        g.gridx = 2;
        schedulePanel.add(saveScheduleBtn, g);

        add(schedulePanel, BorderLayout.SOUTH);

        // Wire actions
        chooseBackupFolderBtn.addActionListener(this::onChooseBackupFolder);
        backupBtn.addActionListener(this::onBackupNow);
        restoreBtn.addActionListener(this::onRestore);
        saveScheduleBtn.addActionListener(this::onSaveSchedule);

        // initial log
        log("Initialized Backup & Restore panel.");
        if (Files.notExists(DATABASE_PATH)) {
            log("Warning: database file not found at: " + DATABASE_PATH.toString());
        }
    }

    // ------------------ Actions ------------------

    private void onChooseBackupFolder(ActionEvent ev) {
        JFileChooser chooser = new JFileChooser(lastBackupFolder.toFile());
        chooser.setDialogTitle("Select folder to store backups");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            lastBackupFolder = chooser.getSelectedFile().toPath();
            log("Backup folder set to: " + lastBackupFolder.toString());
        }
    }

    private void onBackupNow(ActionEvent ev) {
        if (Files.notExists(DATABASE_PATH)) {
            JOptionPane.showMessageDialog(this, "Database file not found: " + DATABASE_PATH, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Build timestamped filename
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupName = "university_backup_" + timestamp + ".db";
        Path target = lastBackupFolder.resolve(backupName);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Create backup:\n" + target.toString(),
                "Confirm backup", JOptionPane.OK_CANCEL_OPTION);
        if (confirm != JOptionPane.OK_OPTION) return;

        // disable controls during backup
        setControlsEnabled(false);
        BackupWorker worker = new BackupWorker(DATABASE_PATH, target, false);
        worker.execute();
    }

    private void onRestore(ActionEvent ev) {
        JFileChooser chooser = new JFileChooser(lastBackupFolder.toFile());
        chooser.setDialogTitle("Select a backup file to restore");
        chooser.setFileFilter(new FileNameExtensionFilter("Database files", "db", "sqlite", "sqlite3"));
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        Path source = chooser.getSelectedFile().toPath();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Restore will overwrite the current database file:\n" + DATABASE_PATH.toString() +
                        "\n\nAre you sure you want to continue?",
                "Confirm restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        // disable controls during restore
        setControlsEnabled(false);
        BackupWorker worker = new BackupWorker(source, DATABASE_PATH, true);
        worker.execute();
    }

    private void onSaveSchedule(ActionEvent ev) {
        String freq = (String) scheduleFrequencyBox.getSelectedItem();
        String time = scheduleTimeField.getText().trim();
        // naive validation for HH:mm
        if (!time.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            JOptionPane.showMessageDialog(this, "Enter time in HH:mm format (24-hour).", "Invalid time", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // For demo we save schedule to a small file. Replace with cron/task scheduler in production.
        try {
            Path cfg = Paths.get("UNIVERSITYERP", "backup_schedule.conf");
            Files.createDirectories(cfg.getParent());
            String content = String.format("frequency=%s%ntime=%s%nfolder=%s%n", freq, time, lastBackupFolder.toString());
            Files.write(cfg, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log("Saved schedule: " + content.replaceAll("\n", "; "));
            JOptionPane.showMessageDialog(this, "Schedule saved (UI-only). Use a system scheduler to run backups automatically.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            log("Failed to save schedule: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Failed to save schedule: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ------------------ Helper + UI ------------------

    private void setControlsEnabled(boolean enabled) {
        backupBtn.setEnabled(enabled);
        restoreBtn.setEnabled(enabled);
        chooseBackupFolderBtn.setEnabled(enabled);
        saveScheduleBtn.setEnabled(enabled);
    }

    private void log(String line) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + line + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void stylePrimary(JButton b) {
        b.setBackground(new Color(52, 152, 219));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
    }

    // ------------------ Worker for copy operations ------------------

    /**
     * Copies file from source -> target while updating progress bar and log.
     * If replaceExisting==true and target exists, it's overwritten.
     * For safety, the copy writes to a temp file and renames on completion.
     */
    private class BackupWorker extends SwingWorker<Void, Integer> {
        private final Path src;
        private final Path dest;
        private final boolean isRestore;

        BackupWorker(Path src, Path dest, boolean isRestore) {
            this.src = src;
            this.dest = dest;
            this.isRestore = isRestore;
        }

        @Override
        protected Void doInBackground() {
            log((isRestore ? "Restoring" : "Backing up") + " from: " + src + " -> " + dest);
            try {
                // ensure parent exists
                Files.createDirectories(dest.getParent());

                long totalBytes = Files.size(src);
                // temp target to avoid partial file replacement
                Path tmp = dest.resolveSibling(dest.getFileName().toString() + ".tmp");
                try (FileChannel in = FileChannel.open(src, StandardOpenOption.READ);
                     FileChannel out = FileChannel.open(tmp, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {

                    long position = 0L;
                    final long chunk = 8 * 1024 * 1024; // 8MB chunks
                    while (position < totalBytes) {
                        long transferred = in.transferTo(position, chunk, out);
                        if (transferred <= 0) break;
                        position += transferred;
                        int pct = (int) Math.round((position / (double) totalBytes) * 100.0);
                        setProgress(Math.min(pct, 100));
                    }
                }

                // atomic move to final destination (replace if exists)
                Files.move(tmp, dest, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                setProgress(100);
                log((isRestore ? "Restore" : "Backup") + " completed: " + dest.toString());
            } catch (AtomicMoveNotSupportedException amnse) {
                // fallback: non-atomic move
                try {
                    Files.move(dest.resolveSibling(dest.getFileName().toString() + ".tmp"), dest, StandardCopyOption.REPLACE_EXISTING);
                    setProgress(100);
                    log((isRestore ? "Restore" : "Backup") + " completed (non-atomic move): " + dest.toString());
                } catch (Exception ex) {
                    log("Move fallback failed: " + ex.getMessage());
                    cancel(true);
                }
            } catch (Exception ex) {
                log("Error during " + (isRestore ? "restore" : "backup") + ": " + ex.getMessage());
                cancel(true);
            }
            return null;
        }

        @Override
        protected void process(java.util.List<Integer> chunks) {
            if (!chunks.isEmpty()) {
                int last = chunks.get(chunks.size() - 1);
                progressBar.setValue(last);
            }
        }

        @Override
        protected void done() {
            try {
                get(); // rethrow exceptions if any
                progressBar.setValue(100);
            } catch (Exception e) {
                log("Operation failed: " + e.getMessage());
                JOptionPane.showMessageDialog(BackupRestorePanel.this, "Operation failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                setControlsEnabled(true);
                // small delay then reset progress to 0
                new Timer(1500, ae -> progressBar.setValue(0)).start();
            }
        }
    }
}
