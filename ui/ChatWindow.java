package ui;

import core.Logging;

import javax.swing.*;
import java.awt.*;

// This class represents the UI. Its responsibility is to display data received from its chatNetWorkThread.
public class ChatWindow extends JFrame {
    private final static Logging LOG = Logging.uiLogger();
    private final UINetworkThread UINetworkThread;
    private static final int WINDOW_WIDTH = 400;
    private static final int WINDOW_HEIGHT = 600;
    private final JTextArea area = new JTextArea();
    private final JTextField input = new JTextField();
    private JFrame window;

    public ChatWindow() {
        super("Chat Window | Made by Sven Dysthe @ NSCC");
        LOG.INFO("Starting a ChatWindow...");
        this.UINetworkThread = new UINetworkThread();
        Thread uiNetworkThreadThread = new Thread(UINetworkThread, "ui-network-thread");
        uiNetworkThreadThread.start();

        setUserNameInChat();
        initChatWindow();
    }

    private void setUserNameInChat() {
        String userName = JOptionPane.showInputDialog(this, "Enter a user name (16 characters max):");
        if (userName.length() > 16) {
            JOptionPane.showMessageDialog(this, "User name must be less than 16 characters.");
            setUserNameInChat();
            return;
        }

        UINetworkThread.setThisUserName(userName.isEmpty() ? "Anonymous" : userName);
    }

    private void initChatWindow() {
        window = new JFrame("Chat");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        window.setResizable(false);
        window.setVisible(true);
        setSize(600,500);
        setLayout(new BorderLayout());

        // Input.
        window.add(input, BorderLayout.SOUTH);
        input.grabFocus();

        // ChatBox.
        createChatBox();

        input.addActionListener(e -> {
            String text = input.getText().trim();
            input.setText("");

            // Check if the user is sending a PM or sending a global message.
            if (text.startsWith("/w ") || text.startsWith("/pm ") || text.startsWith("/whisper ")) {
                String[] p = text.split("\\s+", 3); // [/pm, Bob, rest]
                if (p.length == 3) {
                    UINetworkThread.sendPrivateMessage(p[1], p[2]);
                } else {
                    area.append("(System) Usage: /w OR /pm OR /whisper <user> <message>\n");
                }
            } else {
                UINetworkThread.sendMessageToServer(text);
            }
        });
    }

    // A chat box that listens for messages. It then appends the messages to the ChatBox.
    private void createChatBox() {
        UINetworkThread.registerChatBoxListener(this::chatBoxMessageReceived);
        area.setEditable(false);
        window.add(new JScrollPane(area), BorderLayout.CENTER);
    }

    private void chatBoxMessageReceived(String message) {
        area.append(message + "\n");
    }
}
