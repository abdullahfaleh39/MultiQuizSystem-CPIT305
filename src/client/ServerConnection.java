package client;

import util.Protocol;
import util.Request;
import util.Response;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerConnection {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 5000;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void connect() throws Exception {
        socket = new Socket(HOST, PORT);

        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();

        in = new ObjectInputStream(socket.getInputStream());
    }

    public Response sendRequest(Request request) throws Exception {
        out.writeObject(request);
        out.flush();

        Object object = in.readObject();

        if (object instanceof Response) {
            return (Response) object;
        }

        return new Response(false, "Invalid response from server.");
    }

    public void close() {
        try {
            if (out != null) {
                sendRequest(new Request(Protocol.EXIT));
                out.close();
            }

            if (in != null) {
                in.close();
            }

            if (socket != null) {
                socket.close();
            }
        } catch (Exception ignored) {
        }
    }
}