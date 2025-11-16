package edu.univ.erp.ui.admin;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

/**
 * ManageUsersPanel
 * - List users (id, username, email, role, status)
 * - Search/filter
 * - Add / Edit / Delete user dialogs (UI-only — TODO: persist)
 * - Import/Export CSV (simple)
 *
 * Replace TODO blocks with real DB calls.
 */
public class ManageUsersPanel extends JPanel {

    private static final String[] COLS = {"ID", "Username", "Full Name", "Email", "Role", "Status"};
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField searchField;
    private final JLabel recordCount;

    // in-memory store (replace with DB-backed)
    private final List<UserRow> users = new ArrayList<>();

    public ManageUsersPanel() {
        setLayout(new BorderLayout(12,12));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        setBackground(Color.WHITE);

        // Header + actions
        JPanel top = new JPanel(new BorderLayout(8,8));
        top.setOpaque(false);
        JLabel title = new JLabel("Manage Users");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        top.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        searchField = new JTextField(28);
        searchField.setToolTipText("Search by username, name, email or role...");
        actions.add(searchField);

        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton delBtn = new JButton("Delete");
        JButton importBtn = new JButton("Import CSV");
        JButton exportBtn = new JButton("Export CSV");

        stylePrimary(addBtn);
        editBtn.setBackground(new Color(95,158,160)); editBtn.setForeground(Color.WHITE);
        delBtn.setBackground(new Color(220,80,80)); delBtn.setForeground(Color.WHITE);

        actions.add(addBtn);
        actions.add(editBtn);
        actions.add(delBtn);
        actions.add(importBtn);
        actions.add(exportBtn);

        top.add(actions, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Footer
        recordCount = new JLabel("0 records");
        add(recordCount, BorderLayout.SOUTH);

        // Wire actions
        addBtn.addActionListener(e -> openAddDialog());
        editBtn.addActionListener(e -> openEditDialog());
        delBtn.addActionListener(e -> deleteSelected());
        importBtn.addActionListener(e -> importCSV());
        exportBtn.addActionListener(e -> exportCSV());

        // Search filter
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
            private void applyFilter() {
                String t = searchField.getText().trim();
                if (t.isEmpty()) sorter.setRowFilter(null);
                else {
                    String expr = "(?i).*" + Pattern.quote(t) + ".*";
                    sorter.setRowFilter(RowFilter.regexFilter(expr, 1,2,3,4));
                }
                updateCount();
            }
        });

        // double-click to edit
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount()==2 && table.getSelectedRow() != -1) openEditDialog();
            }
        });

        // sample data
        loadSampleData();
        refreshTable();
    }

    // ---------- CRUD UI actions ----------
    private void openAddDialog() {
        UserForm form = new UserForm(null);
        int res = JOptionPane.showConfirmDialog(this, form.getPanel(), "Add User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        UserRow newU = form.toUserRow();
        if (newU == null) return;
        newU.id = generateNextId();
        // TODO: persist to DB and get real ID
        users.add(newU);
        refreshTable();
        JOptionPane.showMessageDialog(this, "User added.");
    }

    private void openEditDialog() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a user to edit.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        UserRow u = findById(id);
        if (u == null) return;

        UserForm form = new UserForm(u);
        int res = JOptionPane.showConfirmDialog(this, form.getPanel(), "Edit User", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        UserRow updated = form.toUserRow();
        if (updated == null) return;
        // TODO: update DB
        u.username = updated.username;
        u.fullName = updated.fullName;
        u.email = updated.email;
        u.role = updated.role;
        u.status = updated.status;
        refreshTable();
        JOptionPane.showMessageDialog(this, "User updated.");
    }

    private void deleteSelected() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a user to delete.", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int id = (int) tableModel.getValueAt(modelRow, 0);
        UserRow u = findById(id);
        if (u == null) return;
        int conf = JOptionPane.showConfirmDialog(this,
                "Delete user '" + u.username + "'?",
                "Confirm delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf == JOptionPane.YES_OPTION) {
            // TODO: delete from DB
            users.removeIf(x -> x.id == id);
            refreshTable();
            JOptionPane.showMessageDialog(this, "User deleted.");
        }
    }

    // ---------- CSV import/export ----------
    private void exportCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export users CSV");
        chooser.setSelectedFile(new File("users_export.csv"));
        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File target = chooser.getSelectedFile();
        try (PrintWriter pw = new PrintWriter(target)) {
            pw.println("id,username,full_name,email,role,status");
            for (UserRow u : users) {
                pw.printf("%d,%s,%s,%s,%s,%s%n",
                        u.id,
                        escapeCsv(u.username),
                        escapeCsv(u.fullName),
                        escapeCsv(u.email),
                        escapeCsv(u.role),
                        escapeCsv(u.status));
            }
            JOptionPane.showMessageDialog(this, "Exported " + users.size() + " users to " + target.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import users CSV");
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File src = chooser.getSelectedFile();
        int added = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(src))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                String[] parts = parseCsvLine(line);
                if (parts.length < 6) continue;
                UserRow u = new UserRow(
                        0,
                        parts[1].trim(),
                        parts[2].trim(),
                        parts[3].trim(),
                        parts[4].trim(),
                        parts[5].trim()
                );
                u.id = generateNextId();
                // TODO: persist
                users.add(u);
                added++;
            }
            refreshTable();
            JOptionPane.showMessageDialog(this, "Imported " + added + " users from CSV.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Import failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ---------- Helpers ----------
    private void refreshTable() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (UserRow u : users) tableModel.addRow(new Object[]{u.id, u.username, u.fullName, u.email, u.role, u.status});
            updateCount();
        });
    }

    private void updateCount() { recordCount.setText(tableModel.getRowCount() + " records"); }

    private UserRow findById(int id) {
        return users.stream().filter(u -> u.id == id).findFirst().orElse(null);
    }

    private int generateNextId() {
        return users.stream().mapToInt(u -> u.id).max().orElse(1000) + 1;
    }

    private static String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private static String[] parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i+1 < line.length() && line.charAt(i+1) == '"') {
                    cur.append('"'); i++;
                } else inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else cur.append(ch);
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    // ---------- Sample data ----------
    private void loadSampleData() {
        users.clear();
        users.add(new UserRow(1001, "admin", "Administrator", "admin@uni.edu", "Admin", "Active"));
        users.add(new UserRow(1002, "rahul", "Dr. Rahul Mehta", "rahul.mehta@uni.edu", "Instructor", "Active"));
        users.add(new UserRow(1003, "sana", "Sana Khan", "sana.khan@student.uni.edu", "Student", "Active"));
    }

    private void stylePrimary(JButton b) {
        b.setBackground(new Color(52, 152, 219));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(6,10,6,10));
    }

    // ---------- Inner classes ----------
    private static class UserRow {
        int id;
        String username;
        String fullName;
        String email;
        String role;
        String status;

        UserRow(int id, String username, String fullName, String email, String role, String status) {
            this.id = id; this.username = username; this.fullName = fullName; this.email = email; this.role = role; this.status = status;
        }
    }

    // Add/Edit form
    private static class UserForm {
        private final JPanel panel;
        private final JTextField usernameField = new JTextField(20);
        private final JTextField fullNameField = new JTextField(30);
        private final JTextField emailField = new JTextField(30);
        private final JComboBox<String> roleBox = new JComboBox<>(new String[]{"Student","Instructor","Admin"});
        private final JComboBox<String> statusBox = new JComboBox<>(new String[]{"Active","Inactive"});

        UserForm(UserRow existing) {
            panel = new JPanel(new GridBagLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
            GridBagConstraints g = new GridBagConstraints();
            g.insets = new Insets(6,6,6,6);
            g.anchor = GridBagConstraints.WEST;

            g.gridx=0; g.gridy=0;
            panel.add(new JLabel("Username:"), g);
            g.gridx=1;
            panel.add(usernameField, g);

            g.gridx=0; g.gridy++;
            panel.add(new JLabel("Full name:"), g);
            g.gridx=1;
            panel.add(fullNameField, g);

            g.gridx=0; g.gridy++;
            panel.add(new JLabel("Email:"), g);
            g.gridx=1;
            panel.add(emailField, g);

            g.gridx=0; g.gridy++;
            panel.add(new JLabel("Role:"), g);
            g.gridx=1;
            panel.add(roleBox, g);

            g.gridx=0; g.gridy++;
            panel.add(new JLabel("Status:"), g);
            g.gridx=1;
            panel.add(statusBox, g);

            if (existing != null) {
                usernameField.setText(existing.username);
                fullNameField.setText(existing.fullName);
                emailField.setText(existing.email);
                roleBox.setSelectedItem(existing.role);
                statusBox.setSelectedItem(existing.status);
            }
        }

        JPanel getPanel() { return panel; }

        UserRow toUserRow() {
            String username = usernameField.getText().trim();
            String fullname = fullNameField.getText().trim();
            String email = emailField.getText().trim();
            String role = (String) roleBox.getSelectedItem();
            String status = (String) statusBox.getSelectedItem();

            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Username is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            if (fullname.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Full name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            if (email.isEmpty() || !email.contains("@")) {
                JOptionPane.showMessageDialog(panel, "Valid email is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return null;
            }
            return new UserRow(0, username, fullname, email, role, status);
        }
    }
}
