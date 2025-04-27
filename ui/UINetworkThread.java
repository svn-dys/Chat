package ui;

import core.ServerConfig;
import core.ServerConfigProvider;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

/*
    This class that orchestrates the connection and messages
    between the UI and the server. This thread is owned by ChatWindow.
*/
public class UINetworkThread implements Runnable {
    private final static ServerConfig config = ServerConfigProvider.get();
    private Consumer<String> chatBoxCallback;
    private PrintWriter writer;

    public void registerChatBoxListener(Consumer<String> chatBoxCallback) {
        this.chatBoxCallback = chatBoxCallback;
    }

    public void setThisUserName(String username) {
        // Write to this socket's output stream. The server will handle this in "ClientHandler.run()" method.
        writer.println("SET_USERNAME:" + username);
    }

    public void sendMessageToServer(String msg) {
        if (msg == null || msg.isBlank() && chatBoxCallback != null) return;

        // If *you* sent the message, then display (You) in the chat instead of your name. E.g. (You):
        SwingUtilities.invokeLater(() -> chatBoxCallback.accept("(You): " + msg));

        // Write to this socket's output stream. The server will handle this in "ClientHandler.run()" method.
        writer.println("MSG:" + msg);
    }

    public void sendPrivateMessage(String recipient, String msg) {
        if (msg == null || msg.isBlank() && chatBoxCallback != null) return;

        // If *you* sent the message, then display (You) in the chat instead of your name. E.g. (You):
        SwingUtilities.invokeLater(() -> chatBoxCallback.accept("(private message to: " + recipient + ") " + msg));

        // Write to this socket's output stream. The server will handle this in "ClientHandler.run()" method.
        writer.println("PM:" + recipient + ":" + msg);
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(config.inetAddress(), config.port())) {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = writer;

            String messageFromServer;
            while ((messageFromServer = reader.readLine()) != null) {
                String msg = messageFromServer;
                // If there is a chatbox listening for messages from the server,
                // invoke its callback.
                SwingUtilities.invokeLater(() -> {
                    if (chatBoxCallback != null) {
                        chatBoxCallback.accept(msg);
                    }
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
