package edu.univ.erp.ui.student;

import javax.swing.*;
import java.awt.*;

public class TimetablePanel extends JPanel {
    public TimetablePanel() {
        setLayout(new GridLayout(6,5));
        for (int i=0;i<30;i++) add(new JLabel((i+1)+". Slot"));
    }
}
