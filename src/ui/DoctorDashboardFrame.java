package ui;

import client.ServerConnection;
import model.Quiz;
import model.QuizResult;
import model.User;
import util.Protocol;
import util.Request;
import util.Response;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DoctorDashboardFrame extends JFrame {
    private ServerConnection connection;
    private User doctor;

    private JTable quizzesTable;
    private JTable resultsTable;

    public DoctorDashboardFrame(ServerConnection connection, User doctor) {
        this.connection = connection;
        this.doctor = doctor;

        setTitle("Doctor Dashboard - " + doctor.getUsername());
        setSize(850, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        buildUI();
        loadQuizzes();
        loadResults();
    }

    private void buildUI() {
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Quizzes", buildQuizzesPanel());
        tabbedPane.addTab("Results", buildResultsPanel());

        add(tabbedPane, BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildQuizzesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        quizzesTable = new JTable();
        quizzesTable.setModel(new DefaultTableModel(
                new Object[]{"Quiz ID", "Title", "Doctor"},
                0
        ));

        JScrollPane scrollPane = new JScrollPane(quizzesTable);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addQuizButton = new JButton("Add Quiz");
        JButton addQuestionButton = new JButton("Add Question");
        JButton refreshButton = new JButton("Refresh");

        addQuizButton.addActionListener(e -> new AddQuizFrame(connection, doctor, this).setVisible(true));
        addQuestionButton.addActionListener(e -> new AddQuestionFrame(connection, this).setVisible(true));
        refreshButton.addActionListener(e -> loadQuizzes());

        buttonsPanel.add(addQuizButton);
        buttonsPanel.add(addQuestionButton);
        buttonsPanel.add(refreshButton);

        panel.add(buttonsPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        resultsTable = new JTable();
        resultsTable.setModel(new DefaultTableModel(
                new Object[]{"Result ID", "Student", "Quiz", "Score", "Total", "Submitted At"},
                0
        ));

        JScrollPane scrollPane = new JScrollPane(resultsTable);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton refreshButton = new JButton("Refresh Results");
        JButton exportButton = new JButton("Export Report");

        refreshButton.addActionListener(e -> loadResults());
        exportButton.addActionListener(e -> exportReport());

        buttonsPanel.add(refreshButton);
        buttonsPanel.add(exportButton);

        panel.add(buttonsPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton logoutButton = new JButton("Logout");

        logoutButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        panel.add(logoutButton);

        return panel;
    }

    public void loadQuizzes() {
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

    private void loadResults() {
        try {
            Response response = connection.sendRequest(new Request(Protocol.VIEW_ALL_RESULTS));

            if (!response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage());
                return;
            }

            @SuppressWarnings("unchecked")
            List<QuizResult> results = (List<QuizResult>) response.get("results");

            DefaultTableModel model = (DefaultTableModel) resultsTable.getModel();
            model.setRowCount(0);

            for (QuizResult result : results) {
                model.addRow(new Object[]{
                        result.getResultId(),
                        result.getStudentUsername(),
                        result.getQuizTitle(),
                        result.getScore(),
                        result.getTotalQuestions(),
                        result.getSubmittedAt()
                });
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load results: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void exportReport() {
        try {
            Response response = connection.sendRequest(new Request(Protocol.EXPORT_RESULTS_REPORT));

            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(
                        this,
                        response.getMessage() + "\nPath:\n" + response.get("path")
                );
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage());
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not export report: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}