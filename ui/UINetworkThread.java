// Threaded class that orchestrates the connection and messages
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
    private final Logging logger = Logging.uiLogger();
    private final static ServerConfig config = ServerConfigProvider.get();
    private final Consumer<String> messageToWindowCallback;
    private PrintWriter writer;

    public UINetworkThread(Consumer<String> messageToWindowCallback) {
        this.messageToWindowCallback = messageToWindowCallback;
    }

    public void sendMessageToServer(String msg) {
        // Write to this socket's output stream. The server will handle this in
        // the ClientHandler thread "ClientHandler.run()" method.
        writer.println(msg);
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(config.inetAddress(), config.port());
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.writer = writer;

            String messageFromServer;
            while ((messageFromServer = reader.readLine()) != null) {
                String msg = messageFromServer;
                logger.info("Received message from server: " + msg);

                // Pass the msg from the server to the callback and
                // dispatch the execution of the callback on the JFrame EDT thread.
                SwingUtilities.invokeLater(() -> messageToWindowCallback.accept(msg));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
