// This class that orchestrates the connection and messages
// between the UI and the server. This thread is owned by ChatWindow.
package ui;

import core.ServerConfig;
import core.Logging;
import core.ServerConfigProvider;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class UINetworkThread implements Runnable {
    private final static Logging logger = Logging.uiLogger();
    private final static ServerConfig config = ServerConfigProvider.get();
    private Consumer<String> chatBoxCallback; // maybe turn into an array of callbacks to make this function more generic?
    private PrintWriter writer;

    protected void registerChatBoxListener(Consumer<String> chatBoxCallback) {
        this.chatBoxCallback = chatBoxCallback;
    }

    protected void setThisUserName(String username) {
        writer.println("SET_USERNAME:" + username);
    }

    protected void sendMessageToServer(String msg) {
        if (msg == null || msg.isBlank() && chatBoxCallback != null) return;

        // If *you* sent the message, then display (You) in the chat instead of your name. e.g. (You):
        SwingUtilities.invokeLater(() -> chatBoxCallback.accept("(You): " + msg));

        // Write to this socket's output stream. The server will handle this in
        // the ClientHandler thread "ClientHandler.run()" method. Finally, the this.run() will
        // execute the consumer callback, `messageToWindowCallback`, and null out the callback.
        writer.println("MSG:" + msg);
    }

    protected void sendPrivateMessage(String recipient, String msg) {
        if (msg == null || msg.isBlank() && chatBoxCallback != null) return;

        // If *you* sent the message, then display (You) in the chat instead of your name. e.g. (You):
        SwingUtilities.invokeLater(() -> chatBoxCallback.accept("(Private Message to: " + recipient + ") " + msg));

        writer.println("PM:" + recipient + ":" + msg);   // over the wire
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
                logger.info(this + "Received message from server: " + msg);

                // If there is a chatbox listening for messages from the server,
                // invoke its callback.
                SwingUtilities.invokeLater(() -> {
                    if (chatBoxCallback != null) {
                        chatBoxCallback.accept(msg); // coupled
                    }
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
