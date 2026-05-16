package model;

import java.io.Serializable;

public class QuizResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private int resultId;
    private String studentUsername;
    private String quizTitle;
    private int score;
    private int totalQuestions;
    private String submittedAt;

    public QuizResult(int resultId, String studentUsername, String quizTitle,
                      int score, int totalQuestions, String submittedAt) {
        this.resultId = resultId;
        this.studentUsername = studentUsername;
        this.quizTitle = quizTitle;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.submittedAt = submittedAt;
    }

    public int getResultId() {
        return resultId;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public int getScore() {
        return score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    @Override
    public String toString() {
        return "Result ID: " + resultId
                + " | Student: " + studentUsername
                + " | Quiz: " + quizTitle
                + " | Score: " + score + "/" + totalQuestions
                + " | Date: " + submittedAt;
    }
}