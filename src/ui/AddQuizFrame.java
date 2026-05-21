package ui;

import client.ServerConnection;
import model.User;
import util.Protocol;
import util.Request;
import util.Response;

import javax.swing.*;
import java.awt.*;

public class AddQuizFrame extends JFrame {
    private ServerConnection connection;
    private User doctor;
    private DoctorDashboardFrame parent;

    private JTextField titleField;

    public AddQuizFrame(ServerConnection connection, User doctor, DoctorDashboardFrame parent) {
        this.connection = connection;
        this.doctor = doctor;
        this.parent = parent;

        setTitle("Add New Quiz");
        setSize(420, 180);
        setLocationRelativeTo(null);
        setResizable(false);

        buildUI();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Quiz Title:");
        titleField = new JTextField();

        JPanel formPanel = new JPanel(new BorderLayout(10, 10));
        formPanel.add(titleLabel, BorderLayout.WEST);
        formPanel.add(titleField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> saveQuiz());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void saveQuiz() {
        try {
            String title = titleField.getText().trim();

            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter quiz title.");
                return;
            }

            Request request = new Request(Protocol.ADD_QUIZ)
                    .put("title", title)
                    .put("doctorId", doctor.getId());

            Response response = connection.sendRequest(request);

            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage());
                parent.loadQuizzes();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage());
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not save quiz: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}