package database;

import model.Question;
import model.Quiz;
import model.QuizResult;
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:derby:MultiQuizDB;create=true";

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        } catch (ClassNotFoundException ignored) {
        }

        return DriverManager.getConnection(DB_URL);
    }

    public void initializeDatabase() throws SQLException {
        createUsersTable();
        createQuizzesTable();
        createQuestionsTable();
        createResultsTable();
        insertDefaultUsers();
    }

    private void createUsersTable() throws SQLException {
        String sql = "CREATE TABLE APP_USERS ("
                + "id INT NOT NULL GENERATED ALWAYS AS IDENTITY, "
                + "username VARCHAR(100) NOT NULL UNIQUE, "
                + "password_value VARCHAR(100) NOT NULL, "
                + "role_value VARCHAR(20) NOT NULL, "
                + "PRIMARY KEY (id)"
                + ")";
        executeCreate(sql);
    }

    private void createQuizzesTable() throws SQLException {
        String sql = "CREATE TABLE QUIZZES ("
                + "id INT NOT NULL GENERATED ALWAYS AS IDENTITY, "
                + "title VARCHAR(200) NOT NULL, "
                + "doctor_id INT NOT NULL, "
                + "PRIMARY KEY (id), "
                + "FOREIGN KEY (doctor_id) REFERENCES APP_USERS(id)"
                + ")";
        executeCreate(sql);
    }

    private void createQuestionsTable() throws SQLException {
        String sql = "CREATE TABLE QUESTIONS ("
                + "id INT NOT NULL GENERATED ALWAYS AS IDENTITY, "
                + "quiz_id INT NOT NULL, "
                + "question_text VARCHAR(500) NOT NULL, "
                + "option_a VARCHAR(250) NOT NULL, "
                + "option_b VARCHAR(250) NOT NULL, "
                + "option_c VARCHAR(250) NOT NULL, "
                + "option_d VARCHAR(250) NOT NULL, "
                + "correct_answer CHAR(1) NOT NULL, "
                + "PRIMARY KEY (id), "
                + "FOREIGN KEY (quiz_id) REFERENCES QUIZZES(id)"
                + ")";
        executeCreate(sql);
    }

    private void createResultsTable() throws SQLException {
        String sql = "CREATE TABLE QUIZ_RESULTS ("
                + "id INT NOT NULL GENERATED ALWAYS AS IDENTITY, "
                + "student_id INT NOT NULL, "
                + "quiz_id INT NOT NULL, "
                + "score INT NOT NULL, "
                + "total_questions INT NOT NULL, "
                + "submitted_at TIMESTAMP NOT NULL, "
                + "PRIMARY KEY (id), "
                + "FOREIGN KEY (student_id) REFERENCES APP_USERS(id), "
                + "FOREIGN KEY (quiz_id) REFERENCES QUIZZES(id)"
                + ")";
        executeCreate(sql);
    }

    private void executeCreate(String sql) throws SQLException {
        try (Connection conn = getConnection();
             Statement statement = conn.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException ex) {
            if (!"X0Y32".equals(ex.getSQLState())) {
                throw ex;
            }
        }
    }

    private void insertDefaultUsers() throws SQLException {
        String countSql = "SELECT COUNT(*) FROM APP_USERS";

        try (Connection conn = getConnection();
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery(countSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                insertUser(conn, "doctor", "1234", "DOCTOR");
                insertUser(conn, "student", "1234", "STUDENT");
            }
        }
    }

    private void insertUser(Connection conn, String username, String password, String role) throws SQLException {
        String sql = "INSERT INTO APP_USERS(username, password_value, role_value) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role.toUpperCase());
            ps.executeUpdate();
        }
    }

    public boolean registerUser(String username, String password, String role) throws SQLException {
        try (Connection conn = getConnection()) {
            insertUser(conn, username, password, role);
            return true;
        }
    }

    public User login(String username, String password) throws SQLException {
        String sql = "SELECT id, username, role_value FROM APP_USERS "
                + "WHERE username = ? AND password_value = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("role_value")
                    );
                }
            }
        }

        return null;
    }

    public int addQuiz(String title, int doctorId) throws SQLException {
        String sql = "INSERT INTO QUIZZES(title, doctor_id) VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, title);
            ps.setInt(2, doctorId);
            ps.executeUpdate();

            return getGeneratedId(ps);
        }
    }

    public int addQuestion(Question question) throws SQLException {
        String sql = "INSERT INTO QUESTIONS("
                + "quiz_id, question_text, option_a, option_b, option_c, option_d, correct_answer"
                + ") VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, question.getQuizId());
            ps.setString(2, question.getQuestionText());
            ps.setString(3, question.getOptionA());
            ps.setString(4, question.getOptionB());
            ps.setString(5, question.getOptionC());
            ps.setString(6, question.getOptionD());
            ps.setString(7, question.getCorrectAnswer());

            ps.executeUpdate();

            return getGeneratedId(ps);
        }
    }

    private int getGeneratedId(PreparedStatement ps) throws SQLException {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (keys.next()) {
                return keys.getInt(1);
            }
        }

        throw new SQLException("Could not get generated ID.");
    }

    public List<Quiz> getAllQuizzes() throws SQLException {
        List<Quiz> quizzes = new ArrayList<>();

        String sql = "SELECT q.id, q.title, u.username AS doctor_username "
                + "FROM QUIZZES q "
                + "INNER JOIN APP_USERS u ON q.doctor_id = u.id "
                + "ORDER BY q.id";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                quizzes.add(new Quiz(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("doctor_username")
                ));
            }
        }

        return quizzes;
    }

    public List<Question> getQuestionsByQuiz(int quizId) throws SQLException {
        List<Question> questions = new ArrayList<>();

        String sql = "SELECT * FROM QUESTIONS WHERE quiz_id = ? ORDER BY id";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quizId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    questions.add(new Question(
                            rs.getInt("id"),
                            rs.getInt("quiz_id"),
                            rs.getString("question_text"),
                            rs.getString("option_a"),
                            rs.getString("option_b"),
                            rs.getString("option_c"),
                            rs.getString("option_d"),
                            rs.getString("correct_answer")
                    ));
                }
            }
        }

        return questions;
    }

    public void saveResult(int studentId, int quizId, int score, int totalQuestions) throws SQLException {
        String sql = "INSERT INTO QUIZ_RESULTS("
                + "student_id, quiz_id, score, total_questions, submitted_at"
                + ") VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studentId);
            ps.setInt(2, quizId);
            ps.setInt(3, score);
            ps.setInt(4, totalQuestions);
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));

            ps.executeUpdate();
        }
    }

    public List<QuizResult> getAllResults() throws SQLException {
        String sql = "SELECT r.id, u.username, q.title, r.score, r.total_questions, r.submitted_at "
                + "FROM QUIZ_RESULTS r "
                + "INNER JOIN APP_USERS u ON r.student_id = u.id "
                + "INNER JOIN QUIZZES q ON r.quiz_id = q.id "
                + "ORDER BY r.id DESC";

        return getResults(sql, -1);
    }

    public List<QuizResult> getResultsByStudent(int studentId) throws SQLException {
        String sql = "SELECT r.id, u.username, q.title, r.score, r.total_questions, r.submitted_at "
                + "FROM QUIZ_RESULTS r "
                + "INNER JOIN APP_USERS u ON r.student_id = u.id "
                + "INNER JOIN QUIZZES q ON r.quiz_id = q.id "
                + "WHERE r.student_id = ? "
                + "ORDER BY r.id DESC";

        return getResults(sql, studentId);
    }

    private List<QuizResult> getResults(String sql, int studentId) throws SQLException {
        List<QuizResult> results = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (studentId != -1) {
                ps.setInt(1, studentId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp timestamp = rs.getTimestamp("submitted_at");

                    results.add(new QuizResult(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("title"),
                            rs.getInt("score"),
                            rs.getInt("total_questions"),
                            timestamp == null ? "" : timestamp.toString()
                    ));
                }
            }
        }

        return results;
    }
}