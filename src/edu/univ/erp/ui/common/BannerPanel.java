package edu.univ.erp.ui.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class BannerPanel extends JPanel {
    private JLabel label;

    public BannerPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1, 30)); 
        label = new JLabel(" "); //any text label
        label.setFont(new Font("Arial", Font.BOLD, 14));  //changing the font
        label.setOpaque(true); //adding opacity
        label.setHorizontalAlignment(SwingConstants.CENTER);// adding allignment
        label.setBorder(new EmptyBorder(5, 10, 5, 10)); //addnig border
        add(label, BorderLayout.CENTER);
        SetMantanence(false); // Set initial state
    }

    public void SetMantanence(boolean on) {
        //if mantanence is ON then we have show the messages otherwise we dont have to show
        if (!on) {
            label.setText(" ");
            label.setBackground(new Color(220, 220, 220));
            label.setForeground(Color.BLACK);
            ;
        } else {
            label.setText("⚠️Caution--- Sytem is under Maintenance Mode, You cannot do any Write operations!");
            label.setBackground(new Color(180, 0, 0));
            label.setForeground(Color.YELLOW); 
        }
        revalidate(); 
        repaint();
    }
}