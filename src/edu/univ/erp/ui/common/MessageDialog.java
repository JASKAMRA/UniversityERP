package edu.univ.erp.ui.common;

import javax.swing.*;
import java.awt.*;

public class MessageDialog {
    public static void showError(Component callee, String message) {
        JOptionPane.showMessageDialog(callee, message, "ERROR", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component callee, String message) {
        JOptionPane.showMessageDialog(callee, message, "INFO", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static boolean confirm(Component callee, String message) {
        int r=JOptionPane.showConfirmDialog(callee, message, "CONFIRM", JOptionPane.YES_NO_OPTION);
        if(r==JOptionPane.YES_OPTION){
            return true;
        }
        else{
            return (false);
        }
    }
}

