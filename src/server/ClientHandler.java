package server;

import util.Protocol;
import util.Request;
import util.Response;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;
    private QuizService quizService;

    public ClientHandler(Socket socket, QuizService quizService) {
        this.socket = socket;
        this.quizService = quizService;
    }

    @Override
    public void run() {
        String clientName = socket.getInetAddress().getHostAddress();

        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.flush();
            System.out.println("Client connected: " + clientName);

            while (true) {
                Object object = in.readObject();

                if (!(object instanceof Request)) {
                    out.writeObject(new Response(false, "Invalid request object."));
                    out.flush();
                    continue;
                }

                Request request = (Request) object;

                if (Protocol.EXIT.equals(request.getAction())) {
                    out.writeObject(new Response(true, "Goodbye."));
                    out.flush();
                    break;
                }

                Response response = quizService.handle(request);
                out.writeObject(response);
                out.flush();
            }

        } catch (EOFException ex) {
            System.out.println("Client disconnected: " + clientName);
        } catch (Exception ex) {
            System.out.println("Client error: " + ex.getMessage());
        } finally {
            try {
                socket.close();
            } catch (Exception ignored) {}
        }
    }
}