package server;

import core.Logging;
import core.ServerConfig;
import core.ServerConfigProvider;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

// This class listens on a port, accepts new socket connections, and spawns a chat handler for each client.
public final class ChatServer implements Runnable {
    private static final Logging LOG = Logging.serverLogger();
    private final Map<ClientHandler, String> clients = new HashMap<>();
    private final ServerConfig config = ServerConfigProvider.get();
    private ClientHandler clientHandler;
    private ServerSocket serverSocket;

    public ChatServer() {
        LOG.INFO("Starting Server...");
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(config.port(), config.backlog(), config.inetAddress());
            while (true) {
                try {
                    // Wait for a new client to connect
                    Socket clientSocket = serverSocket.accept();
                    clientHandler = new ClientHandler(clientSocket, this);
                    clients.put(clientHandler, "Anonymous");
                } catch (Exception e) {
                    LOG.ERROR("Failed to accept client connection: " + e.getMessage());
                    return;
                } finally {
                    try {
                        new Thread(clientHandler).start();
                    } catch (IllegalThreadStateException e) {
                        LOG.ERROR("Failed to start client handler thread: " + e.getMessage());
                    }
                }
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
    public void broadcastMessage(String msg, ClientHandler exclude) {
        clients.keySet().forEach(clientHandler -> {
            if (clientHandler != exclude) {
                clientHandler.send(msg);
            }
        });
    }

    public void broadcastMessagePrivate(String toName, String msg) {
        clients.keySet().forEach(clientHandler -> {
            if (clientHandler.getClient().getName().equalsIgnoreCase(toName)) {
                clientHandler.send(msg);
            }
        });
    }

    // Getters
    public InetAddress getInetAddress() {
        return config.inetAddress();
    }

    public int getPort() {
        return config.port();
    }
}
