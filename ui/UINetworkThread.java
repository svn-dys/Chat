package ui;

import core.Logging;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.function.Consumer;

/*
 * This class orchestrates the connection and messages
 * between the UI and the server. This thread is owned by ChatWindow.
*/
public class UINetworkThread implements Runnable {
    private static final Logging LOG = Logging.uiLogger();
    private static final int CONNECT_TIMEOUT_MS = 50;
    private final InetSocketAddress serverAddress;
    private Consumer<String> chatBoxCallback;
    private PrintWriter writer;

    public UINetworkThread(InetSocketAddress serverAddr) {
        this.serverAddress = serverAddr;
    }

    public void registerChatBoxListener(Consumer<String> chatBoxCallback) {
        this.chatBoxCallback = chatBoxCallback;
    }

    public void setThisUserName(String username) {
        if (writer != null) {
            writer.println("SET_USERNAME:" + username);
        } else {
            LOG.ERROR("`writer` was null.");
            echoMessageToWindow("[System] Could not connect to server at "
                    + serverAddress + ". Is the server running?");
        }
    }

    public void sendMessageToServer(String message) {
        if (message == null || message.isBlank()) return;

        if (writer != null) {
            echoMessageToWindow("[You]: " + message);
            writer.println("MSG:" + message);
        } else {
            LOG.ERROR("`writer` was null.");
            echoMessageToWindow("[You]: " + message);
        }
    }

    public void sendPrivateMessage(String recipient, String message) {
        if (message == null || message.isBlank()) return;

        // If *you* sent the message, then display (You) in the chat instead of your name. E.g. (You):
        echoMessageToWindow("[private message → " + recipient + "] " + message);

        // Write to this socket's output stream. The server will handle this in "ClientHandler.run()" method.
        writer.println("PM:" + recipient + ":" + message);
    }

    @Override
    public void run() {
        try (Socket socket = new Socket()) {
            socket.connect(serverAddress, CONNECT_TIMEOUT_MS);
            writer = new PrintWriter(socket.getOutputStream(), true);
            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    final String message = line;
                    SwingUtilities.invokeLater(() -> deliverToWindow(message));
                }
            }
        } catch (IOException ex) {
            SwingUtilities.invokeLater(() -> deliverToWindow("[System] Could not connect to server at "
                + serverAddress + ". Is the server running?"));
        }
    }

    private void deliverToWindow(String message) {
        if (chatBoxCallback != null)
            chatBoxCallback.accept(message);
    }

    private void echoMessageToWindow(String message) {
        SwingUtilities.invokeLater(() -> deliverToWindow(message));
    }
}
