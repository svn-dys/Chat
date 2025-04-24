// This class represents the UI. Its responsibility is to display data received from its chatNetWorkThread.

package ui;

import core.Logging;
import javax.swing.*;
import core.ServerConfig;
import core.ServerConfigProvider;
import java.awt.*;

public class ChatWindow extends JFrame {
    private final Logging logger = Logging.uiLogger();
    private final ServerConfig config = ServerConfigProvider.get();
    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 600;
    private final JTextArea area = new JTextArea();
    private final JTextField input = new JTextField();
    private final UINetworkThread UINetworkThread;

    private Thread chatNetworkThread;

    public ChatWindow() {
        // super("Chat");
        logger.info("Initializing ChatWindow...");
        //InitChatWindow();

        JFrame window = new JFrame("Chat");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setSize(600,400);
        setLayout(new BorderLayout());
        window.setVisible(true);

        // Area code for test
        area.setEditable(false);
        window.add(new JScrollPane(area), BorderLayout.CENTER);
        area.setVisible(true);
        window.add(input, BorderLayout.SOUTH);

        // TODO: Figure out why moving this to the top of the constructor causes the socket to fail to connect.
        // Create and start the network thread for this window.
        UINetworkThread = new UINetworkThread(this::messageToWindowCallback);
        new Thread(UINetworkThread).start();

        // Must come after the network thread is started.
        input.addActionListener(e -> {
            sendInputToServer();
        });
    }

    // Send the input to the server. E.g., the user presses the enter button in the input field.
    private void sendInputToServer() {
        String inputText = input.getText();
        if (inputText.isBlank()) return;

        UINetworkThread.sendMessageToServer(inputText);
        input.setText("");
    }

    public void messageToWindowCallback(String message) {
        logger.info("Frame: " + message);
        area.append(message + "\n");
    }

    private void InitChatWindow() {

    }
}
