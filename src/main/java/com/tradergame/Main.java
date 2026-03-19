package com.tradergame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            LoginUI loginWindow = new LoginUI();
            loginWindow.setLocationRelativeTo(null);
            loginWindow.setVisible(true);
        });
    }
}