package edu.univ.erp.ui.student; // Yeh ui.student package mein hai

import javax.swing.*;
import javax.swing.table.DefaultTableModel; // Table ke data model ke liye
import java.awt.*;

/**
 * Student Dashboard ka UI Panel.
 * Yahaan student courses dekh (browse), register/drop kar sakta hai,
 * timetable aur grades dekh sakta hai.
 * [Project Brief Ref: 25-33, 58]
 */
public class StudentDashboardPanel extends JPanel {

    // Document ke hisaab se Student features
    private JButton browseCatalogButton;
    private JButton registerButton;
    private JButton dropButton;
    private JButton viewTimetableButton;
    private JButton viewGradesButton;
    private JButton downloadTranscriptButton;
    
    private JTable contentTable; // Catalog/Timetable/Grades dikhane ke liye
    private JScrollPane tableScrollPane;
    private DefaultTableModel tableModel; // Table ke data ko manage karne ke liye

    public StudentDashboardPanel() {
        // Main panel ka layout: BorderLayout
        setLayout(new BorderLayout(10, 10)); // (horizontal gap, vertical gap)
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding

        // --- 1. Top Panel (Jismein saare action buttons honge) ---
        JPanel actionPanel = new JPanel();
        // FlowLayout buttons ko ek line mein rakhta hai
        actionPanel.setLayout(new FlowLayout(FlowLayout.LEFT)); 
        actionPanel.setBorder(BorderFactory.createTitledBorder("Student Actions"));

        // Buttons banana (document ke features ke hisaab se)
        browseCatalogButton = new JButton("Browse Catalog"); // [cite: 26]
        registerButton = new JButton("Register for Section"); // [cite: 27]
        dropButton = new JButton("Drop Section"); // [cite: 28]
        viewTimetableButton = new JButton("View Timetable"); // [cite: 29]
        viewGradesButton = new JButton("View Grades"); // [cite: 30]
        downloadTranscriptButton = new JButton("Download Transcript"); // [cite: 33]

        // Buttons ko action panel mein add karna
        actionPanel.add(browseCatalogButton);
        actionPanel.add(registerButton);
        actionPanel.add(dropButton);
        actionPanel.add(viewTimetableButton);
        actionPanel.add(viewGradesButton);
        actionPanel.add(downloadTranscriptButton);

        // --- 2. Center Panel (Jismein Table dikhegi) ---
        // Document kehta hai "list courses/sections in sortable tables" [cite: 24]
        
        // Ek empty table model banayenge. Data baad mein logic se aayega.
        tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(new String[]{"Code", "Title", "Credits", "Instructor", "Capacity"}); // Default columns
        
        contentTable = new JTable(tableModel);
        contentTable.setFillsViewportHeight(true); // Table ko poori height lene do
        
        // Table ko scrollable banana (taaki data zyada ho toh scroll kar sakein)
        tableScrollPane = new JScrollPane(contentTable);

        // --- 3. Panels ko main dashboard mein add karna ---
        add(actionPanel, BorderLayout.NORTH); // Buttons waala panel upar
        add(tableScrollPane, BorderLayout.CENTER); // Table waala panel beech mein
    }
    
    // Yahaan hum public methods bana sakte hain taaki in buttons par
    // action listeners (logic) add kar sakein (yeh kaam baad mein karenge)
    public JButton getRegisterButton() {
        return registerButton;
    }
    
    public JButton getDropButton() {
        return dropButton;
    }
    
    // etc.
}
