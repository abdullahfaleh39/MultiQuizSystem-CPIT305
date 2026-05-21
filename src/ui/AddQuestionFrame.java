package ui;

import client.ServerConnection;
import model.Question;
import model.Quiz;
import util.Protocol;
import util.Request;
import util.Response;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AddQuestionFrame extends JFrame {
    private ServerConnection connection;
    private DoctorDashboardFrame parent;

    private JComboBox<Quiz> quizComboBox;
    private JTextArea questionArea;
    private JTextField optionAField;
    private JTextField optionBField;
    private JTextField optionCField;
    private JTextField optionDField;
    private JComboBox<String> correctAnswerComboBox;

    public AddQuestionFrame(ServerConnection connection, DoctorDashboardFrame parent) {
        this.connection = connection;
        this.parent = parent;

        setTitle("Add Question");
        setSize(650, 520);
        setLocationRelativeTo(null);

        buildUI();
        loadQuizzes();
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        quizComboBox = new JComboBox<>();
        questionArea = new JTextArea(4, 30);
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);

        optionAField = new JTextField();
        optionBField = new JTextField();
        optionCField = new JTextField();
        optionDField = new JTextField();

        correctAnswerComboBox = new JComboBox<>(new String[]{"A", "B", "C", "D"});

        addRow(formPanel, gbc, 0, "Quiz:", quizComboBox);
        addRow(formPanel, gbc, 1, "Question:", new JScrollPane(questionArea));
        addRow(formPanel, gbc, 2, "Option A:", optionAField);
        addRow(formPanel, gbc, 3, "Option B:", optionBField);
        addRow(formPanel, gbc, 4, "Option C:", optionCField);
        addRow(formPanel, gbc, 5, "Option D:", optionDField);
        addRow(formPanel, gbc, 6, "Correct Answer:", correctAnswerComboBox);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton saveButton = new JButton("Save Question");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> saveQuestion());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, Component component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.2;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 0.8;
        panel.add(component, gbc);
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

            quizComboBox.removeAllItems();

            for (Quiz quiz : quizzes) {
                quizComboBox.addItem(quiz);
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

    private void saveQuestion() {
        try {
            Quiz selectedQuiz = (Quiz) quizComboBox.getSelectedItem();

            if (selectedQuiz == null) {
                JOptionPane.showMessageDialog(this, "Please select a quiz.");
                return;
            }

            String questionText = questionArea.getText().trim();
            String optionA = optionAField.getText().trim();
            String optionB = optionBField.getText().trim();
            String optionC = optionCField.getText().trim();
            String optionD = optionDField.getText().trim();
            String correctAnswer = (String) correctAnswerComboBox.getSelectedItem();

            if (questionText.isEmpty()
                    || optionA.isEmpty()
                    || optionB.isEmpty()
                    || optionC.isEmpty()
                    || optionD.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }

            Question question = new Question(
                    0,
                    selectedQuiz.getId(),
                    questionText,
                    optionA,
                    optionB,
                    optionC,
                    optionD,
                    correctAnswer
            );

            Request request = new Request(Protocol.ADD_QUESTION)
                    .put("question", question);

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
                    "Could not save question: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}