package ui;

import client.ServerConnection;
import model.QuizResult;
import model.User;
import util.Protocol;
import util.Request;
import util.Response;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ResultsFrame extends JFrame {
    private ServerConnection connection;
    private User student;

    private JTable resultsTable;

    public ResultsFrame(ServerConnection connection, User student) {
        this.connection = connection;
        this.student = student;

        setTitle("My Results - " + student.getUsername());
        setSize(750, 400);
        setLocationRelativeTo(null);

        buildUI();
        loadResults();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        resultsTable = new JTable();
        resultsTable.setModel(new DefaultTableModel(
                new Object[]{"Result ID", "Student", "Quiz", "Score", "Total", "Submitted At"},
                0
        ));

        JScrollPane scrollPane = new JScrollPane(resultsTable);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton refreshButton = new JButton("Refresh");
        JButton closeButton = new JButton("Close");

        refreshButton.addActionListener(e -> loadResults());
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadResults() {
        try {
            Request request = new Request(Protocol.VIEW_STUDENT_RESULTS)
                    .put("studentId", student.getId());

            Response response = connection.sendRequest(request);

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
}