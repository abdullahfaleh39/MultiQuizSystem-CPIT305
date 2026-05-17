package server;

import database.DatabaseManager;
import model.Question;
import model.QuizResult;
import model.User;
import util.Protocol;
import util.ReportWriter;
import util.Request;
import util.Response;

import java.util.List;

public class QuizService {
    private DatabaseManager databaseManager;

    public QuizService(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Response handle(Request request) {
        try {
            String action = request.getAction();

            switch (action) {
                case Protocol.LOGIN:
                    return login(request);

                case Protocol.REGISTER:
                    return register(request);

                case Protocol.ADD_QUIZ:
                    return addQuiz(request);

                case Protocol.ADD_QUESTION:
                    return addQuestion(request);

                case Protocol.LIST_QUIZZES:
                    return listQuizzes();

                case Protocol.LIST_QUESTIONS:
                    return listQuestions(request);

                case Protocol.SUBMIT_RESULT:
                    return submitResult(request);

                case Protocol.VIEW_ALL_RESULTS:
                    return viewAllResults();

                case Protocol.VIEW_STUDENT_RESULTS:
                    return viewStudentResults(request);

                case Protocol.EXPORT_RESULTS_REPORT:
                    return exportResultsReport();

                default:
                    return new Response(false, "Unknown request: " + action);
            }
        } catch (Exception ex) {
            return new Response(false, "Server error: " + ex.getMessage());
        }
    }

    private Response login(Request request) throws Exception {
        String username = (String) request.get("username");
        String password = (String) request.get("password");

        User user = databaseManager.login(username, password);

        if (user == null) {
            return new Response(false, "Invalid username or password.");
        }

        return new Response(true, "Login successful.").put("user", user);
    }

    private Response register(Request request) throws Exception {
        String username = (String) request.get("username");
        String password = (String) request.get("password");
        String role = (String) request.get("role");

        databaseManager.registerUser(username, password, role);

        return new Response(true, "Account created successfully.");
    }

    private Response addQuiz(Request request) throws Exception {
        String title = (String) request.get("title");
        int doctorId = (Integer) request.get("doctorId");

        int quizId = databaseManager.addQuiz(title, doctorId);

        return new Response(true, "Quiz added successfully.").put("quizId", quizId);
    }

    private Response addQuestion(Request request) throws Exception {
        Question question = (Question) request.get("question");

        int questionId = databaseManager.addQuestion(question);

        return new Response(true, "Question added successfully.").put("questionId", questionId);
    }

    private Response listQuizzes() throws Exception {
        return new Response(true, "Quizzes loaded.")
                .put("quizzes", databaseManager.getAllQuizzes());
    }

    private Response listQuestions(Request request) throws Exception {
        int quizId = (Integer) request.get("quizId");

        return new Response(true, "Questions loaded.")
                .put("questions", databaseManager.getQuestionsByQuiz(quizId));
    }

    private Response submitResult(Request request) throws Exception {
        int studentId = (Integer) request.get("studentId");
        int quizId = (Integer) request.get("quizId");
        int score = (Integer) request.get("score");
        int totalQuestions = (Integer) request.get("totalQuestions");

        databaseManager.saveResult(studentId, quizId, score, totalQuestions);

        return new Response(true, "Result saved successfully.");
    }

    private Response viewAllResults() throws Exception {
        return new Response(true, "Results loaded.")
                .put("results", databaseManager.getAllResults());
    }

    private Response viewStudentResults(Request request) throws Exception {
        int studentId = (Integer) request.get("studentId");

        return new Response(true, "Student results loaded.")
                .put("results", databaseManager.getResultsByStudent(studentId));
    }

    private Response exportResultsReport() throws Exception {
        List<QuizResult> results = databaseManager.getAllResults();
        String path = ReportWriter.exportResults(results);

        return new Response(true, "Report exported successfully.")
                .put("path", path);
    }
}