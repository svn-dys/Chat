package testing;

import core.ServerConfig;
import core.ServerConfigProvider;
import core.Logging;
import server.ChatServer;
import ui.ChatWindow;

import javax.swing.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/*
    [Instructions & usage guide]:
        1: Compile Java files from inside /src/ folder:
            javac -d out core\*.java server\*.java ui\*.java testing\*.java
        2: This command starts the chat server and 2 chat windows:
            java -ea -cp out testing.ChatTest --windowamount 2
    [Runtime args]:
        --noserver: Don't start a server, create X number of chat windows
        --windowamount: Specify the number of chat windows to create
*/
public final class ChatTest {
    private static final int PORT = 1337;
    private static final int BACKLOG = 50;
    private static final Logging LOG = Logging.serverLogger();

    public static void main(String[] args) {
        boolean startServer = true;
        int windows = 1;
        String host = "127.0.0.1";

        for (int i = 0; i < args.length; i++) {
            String param = args[i];

            if ("--noserver".equalsIgnoreCase(param)) {
                startServer = false;
            } else if ("--windowamount".equalsIgnoreCase(param)) {
                if (i + 1 < args.length) {
                    windows = Integer.parseInt(args[++i]);
                } else {
                    LOG.ERROR("[TEST] Missing a value for --windowamount");
                }
            } else {
                host = param;
            }
        }

        if (startServer) bootServerOnce();
        launchUI(windows, host);
    }

    private static void bootServerOnce() {
        try {
            ServerConfigProvider.initialize(new ServerConfig(
                    InetAddress.getByName("0.0.0.0"),
                    PORT,
                    BACKLOG));
            ChatServer server = new ChatServer();
            Thread thread = new Thread(server, "chat-server-thread");
            thread.setDaemon(true);
            thread.start();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    private static void launchUI(int numOfWindows, String host) {
        InetSocketAddress address = new InetSocketAddress(host, PORT);
        SwingUtilities.invokeLater(() -> {
            for (int i = 0; i < numOfWindows; i++) {
                new ChatWindow(address);
            }
        });
    }
}
