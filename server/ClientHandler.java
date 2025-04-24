// Handles a single client (user) to the ChatServer.
package server;

import core.Logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Logging logger = Logging.serverLogger();
    private final Socket socket;
    private final ChatServer server;
    private final PrintWriter writer;

    ClientHandler(Socket socket,
                  ChatServer server) throws IOException {
        logger.info("A client connected to server IP: " +
                server.getInetAddress() + ":" + server.getPort() + " from client IP: " +
                socket.getInetAddress() + ":" + socket.getPort() + ".");
        this.socket = socket;
        this.server = server;
        this.writer = new PrintWriter(socket.getOutputStream(), true);
    }

    public void send(String msg) {
        // Write to the output stream for `this` client
        logger.info("Sending message to UI network listeners: " + msg);
        writer.println(msg);
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))) {
            // In reality, this message should be from the UI network thread
            String messageFromClient;
            while ((messageFromClient = reader.readLine()) != null) {
                server.notifyClientListenersOfMessage(messageFromClient);
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}
