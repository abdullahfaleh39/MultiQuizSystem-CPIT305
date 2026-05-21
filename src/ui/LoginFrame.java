package ui;

import client.ServerConnection;
import model.User;
import util.Protocol;
import util.Request;
import util.Response;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private ServerConnection connection;

    public LoginFrame() {
        connection = new ServerConnection();

        try {
            connection.connect();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Cannot connect to server.\nPlease run server.QuizServer first.",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        setTitle("Multi Quiz System - Login");
        setSize(430, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel titleLabel = new JLabel("Multi Quiz System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register Student");

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> registerStudent());

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        JLabel hintLabel = new JLabel(
                "<html><center>Default accounts:<br>Doctor: doctor / 1234<br>Student: student / 1234</center></html>",
                SwingConstants.CENTER
        );

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout(10, 10));
        southPanel.add(buttonPanel, BorderLayout.NORTH);
        southPanel.add(hintLabel, BorderLayout.SOUTH);

        mainPanel.add(southPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void login() {
        try {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter username and password.");
                return;
            }

            Request request = new Request(Protocol.LOGIN)
                    .put("username", username)
                    .put("password", password);

            Response response = connection.sendRequest(request);

            if (!response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage());
                return;
            }

            User user = (User) response.get("user");

            JOptionPane.showMessageDialog(this, "Welcome, " + user.getUsername());

            if (user.isDoctor()) {
                new DoctorDashboardFrame(connection, user).setVisible(true);
            } else {
                new StudentDashboardFrame(connection, user).setVisible(true);
            }

            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Login error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void registerStudent() {
        try {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter username and password first.");
                return;
            }

            Request request = new Request(Protocol.REGISTER)
                    .put("username", username)
                    .put("password", password)
                    .put("role", "STUDENT");

            Response response = connection.sendRequest(request);

            JOptionPane.showMessageDialog(this, response.getMessage());

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Register error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}