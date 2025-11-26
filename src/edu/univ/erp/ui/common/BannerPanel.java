package edu.univ.erp.ui.common;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class BannerPanel extends JPanel {
    private JLabel labels;

    public BannerPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1, 30)); 
        labels = new JLabel(" "); 
        labels.setFont(new Font("Arial", Font.BOLD, 14));  
        labels.setOpaque(true); 
        labels.setHorizontalAlignment(SwingConstants.CENTER);
        labels.setBorder(new EmptyBorder(5, 10, 5, 10)); 
        add(labels, BorderLayout.CENTER);
        SetMantanence(false); 
    }

    private void setLabelText(JLabel label, String text) {
    label.setText(text);
}
    public void SetMantanence(boolean on) {
        if (on) {
            setLabelText(labels,"⚠️Caution--- Sytem is under Maintenance Mode, You cannot preform any Write operations!");
            labels.setBackground(new Color(180, 0, 0));
            labels.setForeground(Color.YELLOW); 
        }
        else {
            setLabelText(labels," ");
            labels.setBackground(new Color(220, 220, 220));
            labels.setForeground(Color.BLACK);
            
        } 
        revalidate(); 
        repaint();
    }
}