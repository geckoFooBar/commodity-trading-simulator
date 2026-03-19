package com.tradergame;

import com.tradergame.dao.PlayerDAO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginUI extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private PlayerDAO playerDAO = new PlayerDAO();

    public LoginUI() {
        setTitle("Global Commodity Trader Simulator - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel backgroundPanel = new JPanel(new GridBagLayout()); // GridBagLayout automatically centers its contents
        backgroundPanel.setBackground(new Color(15, 23, 42)); // Slate 900 (Dark modern blue)
        add(backgroundPanel);

        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(Color.WHITE);

        cardPanel.setBorder(new CompoundBorder(
                new LineBorder(new Color(226, 232, 240), 1, true),
                new EmptyBorder(50, 60, 50, 60)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0); // 10px vertical spacing between rows
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel titleLabel = new JLabel("Global Commodity Trader Sim", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(30, 41, 59)); // Dark slate text
        cardPanel.add(titleLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 30, 0); // Extra gap below subtitle
        JLabel subtitleLabel = new JLabel("Please login or create an account to begin trading.", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(100, 116, 139));
        cardPanel.add(subtitleLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(5, 0, 5, 0);
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        cardPanel.add(userLabel, gbc);

        gbc.gridy++;
        usernameField = new JTextField(20);
        styleInputField(usernameField);
        cardPanel.add(usernameField, gbc);

        // 4. Password Label & Field
        gbc.gridy++;
        gbc.insets = new Insets(15, 0, 5, 0);
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        cardPanel.add(passLabel, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(5, 0, 25, 0);
        passwordField = new JPasswordField(20);
        styleInputField(passwordField);
        cardPanel.add(passwordField, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(5, 0, 10, 0);
        JButton loginButton = createModernButton("Login", new Color(37, 99, 235), new Color(29, 78, 216)); // Primary Blue
        loginButton.addActionListener(e -> attemptLogin());
        cardPanel.add(loginButton, gbc);

        gbc.gridy++;
        JButton signupButton = createModernButton("Create New Account", new Color(51, 65, 85), new Color(30, 41, 59)); // Secondary Slate
        signupButton.addActionListener(e -> attemptSignup());
        cardPanel.add(signupButton, gbc);

        backgroundPanel.add(cardPanel);
    }

    private void styleInputField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 16));
        field.setBackground(new Color(248, 250, 252));
        field.setBorder(new CompoundBorder(
                new LineBorder(new Color(203, 213, 225), 1, true),
                new EmptyBorder(12, 15, 12, 15)
        ));
    }

    private JButton createModernButton(String text, Color defaultColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setBackground(defaultColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setBorder(new EmptyBorder(12, 0, 12, 0));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(defaultColor);
            }
        });

        return button;
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int loggedInId = playerDAO.authenticateUser(username, password);

        if (loggedInId != -1) {
            TraderUI dashboard = new TraderUI(loggedInId, username);
            dashboard.setExtendedState(JFrame.MAXIMIZED_BOTH);
            dashboard.setVisible(true);

            System.out.println("Successfully logged in!!!");
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials. Please try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void attemptSignup() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean success = playerDAO.registerUser(username, password);

        if (success) {
            JOptionPane.showMessageDialog(this, "Account created successfully! You have been granted $100,000 starting cash. Please log in.");
            passwordField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Username already exists! Please choose another.", "Signup Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}