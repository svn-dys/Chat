// This class listens on a port, accepts new socket connections, and spawns a chat handler for each client.
package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import core.Logging;
import core.ServerConfig;
import core.ServerConfigProvider;

public final class ChatServer implements Runnable {
    private static final Logging logger = Logging.serverLogger();
    private final Map<ClientHandler, String> clients = new HashMap<>();
    private final ServerConfig config = ServerConfigProvider.get();
    private ServerSocket serverSocket;

    public ChatServer() {
        logger.info("Starting Server...");
    }

    @Override
    public void run() {
        startServerInstance();
    }

    private void startServerInstance() {
        try {
            serverSocket = new ServerSocket(config.port(), config.backlog(), config.inetAddress());
            while (true) {
                // Wait for a new client to connect
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.put(clientHandler, "Anonymous");
                new Thread(clientHandler).start();
            }
        } catch (Exception e) {
            logger.error("Failed to start server: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            // TODO: better error handling here
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                    logger.info("Server has been closed.");
                }
            } catch (IOException e) {
                logger.error("Failed to close server socket: " + e.getMessage());
            }
        }
    }

    // Sends a message to all clients on the server except for excluded, `exclude`, ChildHandler
    void broadcast(String msg, ClientHandler exclude) {
        clients.keySet().forEach(clientHandler -> {
            if (clientHandler != exclude) clientHandler.send(msg);
        });
    }

    void sendPrivate(String toName, String msg) {
        clients.keySet().forEach(ch -> {
            if (ch.getClient().getName().equalsIgnoreCase(toName)) {
                ch.send(msg);                // deliver to the one match
            }
        });
    }

    // Getters
    InetAddress getInetAddress() {
        return config.inetAddress();
    }

    int getPort() {
        return config.port();
    }
}
