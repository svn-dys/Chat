// This class listens on a port, accepts new socket connections, and spawns a chat handler for each client.
package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import core.Logging;
import core.ServerConfig;

public final class ChatServer {
    private final Logging logger = Logging.serverLogger();
    private ServerConfig config = new ServerConfig(InetAddress.getLoopbackAddress(), 1337, 50, true);
    private ArrayList<ChatClient> clients = new ArrayList<>(); // ArrayList of ChatClient clientHandlers
    private ServerSocket serverSocket;

    public ChatServer() {
        startServer();
    }

    private void startServer() {
        try {
            logger.info("Starting Server...");
            logger.info("Server is running on port: " + config.port());

            serverSocket = new ServerSocket(config.port(), config.backlog(), config.inetAddress());
            while (true) {
                // Wait for a new client to connect
                Socket clientSocket = serverSocket.accept();
                ChatClient clientHandler = new ChatClient(clientSocket, serverSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (Exception e) {
            logger.error("Failed to start server: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            try {
                if (!serverSocket.isClosed()) {
                    serverSocket.close();
                    logger.info("Server has been closed.");
                }
            } catch (IOException e) {
                logger.error("Failed to close server socket: " + e.getMessage());
            }
        }
    }
}
