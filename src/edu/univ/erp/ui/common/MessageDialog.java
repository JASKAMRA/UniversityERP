package edu.univ.erp.ui.common;
import javax.swing.*;
import java.awt.*;

public class MessageDialog {
    public static void showError(Component callee, String mssg) {
        JOptionPane.showMessageDialog(callee, mssg, "ERROR", JOptionPane.ERROR_MESSAGE);
    }

    public static void showInfo(Component callee, String mssg) {
        JOptionPane.showMessageDialog(callee, mssg, "INFO", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static boolean confirm(Component callee, String mssg) {
        int r=JOptionPane.showConfirmDialog(callee, mssg, "CONFIRM", JOptionPane.YES_NO_OPTION);
        if(r!=JOptionPane.YES_OPTION){
            return false;
        }
        else{
            return true;
        }
    }
}

