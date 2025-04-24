import core.ServerConfig;
import core.ServerConfigProvider;
import server.ChatServer;
import ui.ChatWindow;

import javax.swing.*;
import java.net.InetAddress;

public class ChatMain {
    public static void main(String[] args) {
        ServerConfigProvider.initialize(new ServerConfig(
                InetAddress.getLoopbackAddress(), 1337, 50, true));

        ChatServer server = new ChatServer();
        Thread chatServerThread = new Thread(server, "chat-server-thread");
        chatServerThread.setDaemon(true);
        chatServerThread.start();

        SwingUtilities.invokeLater(() -> {
            new ChatWindow();
            new ChatWindow();
        });
    }
}
