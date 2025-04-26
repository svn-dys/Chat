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
    private static final Logging LOG = Logging.serverLogger();
    private final Map<ClientHandler, String> clients = new HashMap<>();
    private final ServerConfig config = ServerConfigProvider.get();
    private ServerSocket serverSocket;

    public ChatServer() {
        LOG.INFO("Starting Server...");
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
            LOG.ERROR("Failed to start server: " + e.getMessage());
            throw new RuntimeException(e);
        } finally {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                    LOG.INFO("Server has been closed.");
                }
            } catch (IOException e) {
                LOG.ERROR("Failed to close server socket: " + e.getMessage());
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
        clients.keySet().forEach(clientHandler -> {
            if (clientHandler.getClient().getName().equalsIgnoreCase(toName)) {
                clientHandler.send(msg);
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
