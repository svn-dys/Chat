package server;

import core.Logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/*
 * This class, the client handler, acts as the Server's listener for messages
 * from the UI ChatWindow AND also sends messages to the UI ChatWindow.
 * A client represents a single open ChatWindow (see ui/ChatWindow for the ChatWindow implementation).
*/
public class ClientHandler implements Runnable {
    private static final Logging LOG = Logging.serverLogger();
    private final Socket socket;
    private final ChatServer server;
    private final PrintWriter writer;
    private final Client client;

    ClientHandler(Socket socket,
                  ChatServer server) throws IOException {
        LOG.INFO("Client " + socket.getRemoteSocketAddress() +
                " connected to " + socket.getLocalAddress() +
                ":" + socket.getLocalPort());
        this.socket = socket;
        this.server = server;
        this.writer = new PrintWriter(socket.getOutputStream(), true);
        this.client  = new Client("Anonymous", this);
    }

    // Write to the output stream for `this` client.
    public void send(String message) {
        writer.println(message);
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()))) {
            String messageFromClient;
            while ((messageFromClient = reader.readLine()) != null) {
                // Command "SET_USERNAME" Sets the userName of this client:
                if (messageFromClient.startsWith("SET_USERNAME:")) {
                    String newName = messageFromClient.substring("SET_USERNAME:".length());
                    client.setName(newName);
                    LOG.INFO("User with IP" + socket.getInetAddress() + ":" +
                            socket.getPort() + " named themselves " + newName + ".");
                    continue;
                }

                // Command "PM" Private Message:
                if (messageFromClient.startsWith("PM:")) {
                    String[] p = messageFromClient.split(":", 3); // [PM, Bob, text]
                    if (p.length == 3) {
                        String to   = p[1];
                        String text = p[2];
                        server.broadcastMessagePrivate(to, client.getName() + " [private]: " + text);
                    }
                    continue;
                }

                // Command "MSG" Normal Global Message:
                if (messageFromClient.startsWith("MSG:")) {
                    String chat = messageFromClient.substring("MSG:".length());
                    server.broadcastMessage(client.getName() + ": " + chat, this); // exclude this client
                    continue;
                }

                /*
                 * This can happen if someone connects to the server's IP via telnet,
                 * similar protocols, or the web-browser.
                */
                LOG.ERROR("Unhandled chat command was read from client: " + messageFromClient);
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Getters
    public Client getClient() {
        return client;
    }
}
