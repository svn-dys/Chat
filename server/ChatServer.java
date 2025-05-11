package server;

import core.Logging;
import core.ServerConfig;
import core.ServerConfigProvider;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

// ChatServer is the main, single-instance TCP server for the chat application.
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
            logReachableAddresses();
            while (true) {
                try {
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

    // Sends a message to all clients on the server except for excluded, `exclude`, ChildHandler.
    public void broadcastMessage(String message, ClientHandler exclude) {
        clients.keySet().forEach(clientHandler -> {
            if (clientHandler != exclude) {
                clientHandler.send(message);
            }
        });
    }

    // Parses clients and sends a private message to a specified client.
    public void broadcastMessagePrivate(String toName, String message) {
        clients.keySet().forEach(clientHandler -> {
            if (clientHandler.getClient().getName().equalsIgnoreCase(toName)) {
                clientHandler.send(message);
            }
        });
    }

    // Prints the current IP from which this server is running on.
    private void logReachableAddresses() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface network = interfaces.nextElement();
                if (network.isUp() && !network.isLoopback() && !network.isVirtual()) {
                    Enumeration<InetAddress> addresses = network.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        InetAddress address = addresses.nextElement();

                        if (!address.isLoopbackAddress() && !address.isLinkLocalAddress()) {
                            String ip = address.getHostAddress();
                            LOG.INFO("Server reachable at: " + ip + ":" + config.port());
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            LOG.ERROR("Failed to get network interfaces: " + ex.toString());
        }
    }
}
