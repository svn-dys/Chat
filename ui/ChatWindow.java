package ui;

import core.Logging;
import core.ServerConfig;
import core.ServerConfigProvider;

import javax.swing.*;
import java.awt.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;

// This class represents the UI. Its responsibility is to display data received from its networkThread.
public class ChatWindow extends JFrame {
    private final static Logging LOG = Logging.uiLogger();
    private final static ServerConfig config = ServerConfigProvider.get();
    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 600;
    private static final int DEFAULT_PORT = 1337;
    private static final int WRAP_WIDTH = 60;
    private final UINetworkThread networkThread;
    private final InetSocketAddress serverAddress;
    private final JTextArea area = new JTextArea();
    private final JTextField input = new JTextField();

    public ChatWindow() {
        this(new InetSocketAddress(InetAddress.getLoopbackAddress(), DEFAULT_PORT));
    }

    public ChatWindow(InetSocketAddress serverAddress) {
        super("Chat Window");
        this.serverAddress = serverAddress;
        this.networkThread = new UINetworkThread(serverAddress);

        startNetworkThread();
        // `askForUserName` blocks the rest of UI creation until the user chooses a username.
        askForUserName();
        buildUI();

        LOG.INFO("ChatWindow initialised on server address: " + serverAddress);
    }

    private void startNetworkThread() {
        Thread thread = new Thread(networkThread, "ui-network-thread");
        thread.setDaemon(true);
        thread.start();
    }

    private void askForUserName() {
        while (true) {
            String name = JOptionPane.showInputDialog(
                    this, "Enter a chat user name (16 characters max):");

            // If a user presses cancel, dispose of the JOptionPane.
            if (name == null) {
                dispose();
                return;
            }
            if (name.length() > 16) {
                JOptionPane.showMessageDialog(
                        this, "User name must be less than or " +
                                "equal to 16 characters.");
                continue;
            }

            networkThread.setThisUserName(name.isBlank() ? "Anonymous" : name);
            break;
        }
    }

    private void buildUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLayout(new BorderLayout());
        setResizable(false);

        // Chat history box (area).
        area.setEditable(false);
        add(new JScrollPane(area), BorderLayout.CENTER);

        // Add input field.
        add(input, BorderLayout.SOUTH);

        // Send messages to the server via UINetworkThread.
        input.addActionListener(e ->  onUserTyped());

        // Listen for messages and append to the chat box.
        networkThread.registerChatBoxListener(message -> {
            message = wrapText(message);
            area.append(message + "\n");
        });

        setVisible(true);
        input.grabFocus();
    }

    // Wrap messages to a max width of 60 chars.
    private String wrapText(String message) {
        // TODO: Algorithm to prevent midword splitting
        if (message.length() <= WRAP_WIDTH) {
            return message;
        }
        StringBuilder builder = new StringBuilder(message);
        int i = WRAP_WIDTH;
        while (i < builder.length()) {
            builder.insert(i, '\n');
            i += WRAP_WIDTH;
        }
        message = builder.toString();
        return message;
    }

    private void onUserTyped() {
        String text = input.getText().trim();
        input.setText("");

        // Check if the user is sending a PM or sending a global message.
        if (text.startsWith("/w ") || text.startsWith("/pm ") || text.startsWith("/whisper ")) {
            String[] p = text.split("\\s+", 3); // splits like so: [/pm Bob yo!]
            if (p.length == 3) {
                networkThread.sendPrivateMessage(p[1], p[2]);
            } else {
                area.append(wrapText("(System) To send a PM use: /w OR /pm OR /whisper <user> <message>\n"));
            }
        } else {
            networkThread.sendMessageToServer(text);
        }
    }
}
