package ui;

import client.ServerConnection;
import model.Question;
import model.User;
import util.Protocol;
import util.Request;
import util.Response;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TakeQuizFrame extends JFrame {
    private ServerConnection connection;
    private User student;
    private int quizId;
    private String quizTitle;

    private List<Question> questions;
    private int currentIndex = 0;
    private int score = 0;

    private JLabel progressLabel;
    private JTextArea questionArea;
    private JRadioButton optionAButton;
    private JRadioButton optionBButton;
    private JRadioButton optionCButton;
    private JRadioButton optionDButton;
    private ButtonGroup optionsGroup;
    private JButton nextButton;

    public TakeQuizFrame(ServerConnection connection, User student, int quizId, String quizTitle) {
        this.connection = connection;
        this.student = student;
        this.quizId = quizId;
        this.quizTitle = quizTitle;

        setTitle("Take Quiz - " + quizTitle);
        setSize(700, 450);
        setLocationRelativeTo(null);

        buildUI();
        loadQuestions();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        progressLabel = new JLabel("Question 0 of 0", SwingConstants.CENTER);
        progressLabel.setFont(new Font("Arial", Font.BOLD, 18));

        questionArea = new JTextArea();
        questionArea.setFont(new Font("Arial", Font.PLAIN, 16));
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setEditable(false);

        optionAButton = new JRadioButton();
        optionBButton = new JRadioButton();
        optionCButton = new JRadioButton();
        optionDButton = new JRadioButton();

        optionsGroup = new ButtonGroup();
        optionsGroup.add(optionAButton);
        optionsGroup.add(optionBButton);
        optionsGroup.add(optionCButton);
        optionsGroup.add(optionDButton);

        JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 8, 8));
        optionsPanel.add(optionAButton);
        optionsPanel.add(optionBButton);
        optionsPanel.add(optionCButton);
        optionsPanel.add(optionDButton);

        nextButton = new JButton("Next");
        nextButton.addActionListener(e -> submitCurrentAnswer());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(nextButton);

        mainPanel.add(progressLabel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(questionArea), BorderLayout.CENTER);
        mainPanel.add(optionsPanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadQuestions() {
        try {
            Request request = new Request(Protocol.LIST_QUESTIONS)
                    .put("quizId", quizId);

            Response response = connection.sendRequest(request);

            if (!response.isSuccess()) {
                JOptionPane.showMessageDialog(this, response.getMessage());
                dispose();
                return;
            }

            @SuppressWarnings("unchecked")
            List<Question> loadedQuestions = (List<Question>) response.get("questions");

            questions = loadedQuestions;

            if (questions == null || questions.isEmpty()) {
                JOptionPane.showMessageDialog(this, "This quiz has no questions yet.");
                dispose();
                return;
            }

            showQuestion();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load questions: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            dispose();
        }
    }

    private void showQuestion() {
        Question question = questions.get(currentIndex);

        progressLabel.setText("Question " + (currentIndex + 1) + " of " + questions.size());

        questionArea.setText(question.getQuestionText());

        optionAButton.setText("A) " + question.getOptionA());
        optionBButton.setText("B) " + question.getOptionB());
        optionCButton.setText("C) " + question.getOptionC());
        optionDButton.setText("D) " + question.getOptionD());

        optionsGroup.clearSelection();

        if (currentIndex == questions.size() - 1) {
            nextButton.setText("Finish Quiz");
        } else {
            nextButton.setText("Next");
        }
    }

    private void submitCurrentAnswer() {
        String selectedAnswer = getSelectedAnswer();

        if (selectedAnswer == null) {
            JOptionPane.showMessageDialog(this, "Please select an answer.");
            return;
        }

        Question question = questions.get(currentIndex);

        if (selectedAnswer.equalsIgnoreCase(question.getCorrectAnswer())) {
            score++;
        }

        currentIndex++;

        if (currentIndex < questions.size()) {
            showQuestion();
        } else {
            finishQuiz();
        }
    }

    private String getSelectedAnswer() {
        if (optionAButton.isSelected()) {
            return "A";
        }

        if (optionBButton.isSelected()) {
            return "B";
        }

        if (optionCButton.isSelected()) {
            return "C";
        }

        if (optionDButton.isSelected()) {
            return "D";
        }

        return null;
    }

    private void finishQuiz() {
        try {
            Request request = new Request(Protocol.SUBMIT_RESULT)
                    .put("studentId", student.getId())
                    .put("quizId", quizId)
                    .put("score", score)
                    .put("totalQuestions", questions.size());

            Response response = connection.sendRequest(request);

            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Quiz finished!\nYour score: " + score + "/" + questions.size()
                );
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, response.getMessage());
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not save result: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}