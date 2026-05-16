package server;

import database.DatabaseManager;

import java.net.ServerSocket;
import java.net.Socket;

public class QuizServer {
    private static final int PORT = 5000;

    public static void main(String[] args) {
        try {
            DatabaseManager databaseManager = new DatabaseManager();
            databaseManager.initializeDatabase();

            QuizService quizService = new QuizService(databaseManager);

            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Multi Quiz Server is running on port " + PORT);
                System.out.println("Default Doctor account: doctor / 1234");
                System.out.println("Default Student account: student / 1234");

                while (true) {
                    Socket clientSocket = serverSocket.accept();

                    ClientHandler handler = new ClientHandler(clientSocket, quizService);
                    Thread thread = new Thread(handler);

                    thread.start();
                }
            }

        } catch (Exception ex) {
            System.out.println("Server failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}