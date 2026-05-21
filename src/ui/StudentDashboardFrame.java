package ui;

import client.ServerConnection;
import model.Quiz;
import model.User;
import util.Protocol;
import util.Request;
import util.Response;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class StudentDashboardFrame extends JFrame {
    private ServerConnection connection;
    private User student;

    private JTable quizzesTable;

    public StudentDashboardFrame(ServerConnection connection, User student) {
        this.connection = connection;
        this.student = student;

        setTitle("Student Dashboard - " + student.getUsername());
        setSize(750, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        buildUI();
        loadQuizzes();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Available Quizzes", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));

        quizzesTable = new JTable();
        quizzesTable.setModel(new DefaultTableModel(
                new Object[]{"Quiz ID", "Title", "Doctor"},
                0
        ));

        JScrollPane scrollPane = new JScrollPane(quizzesTable);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton refreshButton = new JButton("Refresh");
        JButton takeQuizButton = new JButton("Take Selected Quiz");
        JButton myResultsButton = new JButton("My Results");
        JButton logoutButton = new JButton("Logout");

        refreshButton.addActionListener(e -> loadQuizzes());
        takeQuizButton.addActionListener(e -> openSelectedQuiz());
        myResultsButton.addActionListener(e -> new ResultsFrame(connection, student).setVisible(true));

        logoutButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        buttonPanel.add(refreshButton);
        buttonPanel.add(takeQuizButton);
        buttonPanel.add(myResultsButton);
        buttonPanel.add(logoutButton);

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadQuizzes() {
        try {
            Response response = connection.sendRequest(new Request(Protocol.LIST_QUIZZES));

            if (!response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage());
                return;
            }

            @SuppressWarnings("unchecked")
            List<Quiz> quizzes = (List<Quiz>) response.get("quizzes");

            DefaultTableModel model = (DefaultTableModel) quizzesTable.getModel();
            model.setRowCount(0);

            for (Quiz quiz : quizzes) {
                model.addRow(new Object[]{
                        quiz.getId(),
                        quiz.getTitle(),
                        quiz.getDoctorUsername()
                });
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load quizzes: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void openSelectedQuiz() {
        int selectedRow = quizzesTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a quiz first.");
            return;
        }

        int quizId = (Integer) quizzesTable.getValueAt(selectedRow, 0);
        String quizTitle = (String) quizzesTable.getValueAt(selectedRow, 1);

        new TakeQuizFrame(connection, student, quizId, quizTitle).setVisible(true);
    }
}