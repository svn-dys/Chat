package server;

import java.net.InetAddress;

// ServerContext is a lightweight object that contains minimal information about the server in which
// a client is connected to. The main purpose behind creating a context is to prevent passing
// sensitive information about the host server to the client. Otherwise, we would have to pass the whole server object
// or create verbose getters.
public class ServerContext {
    private final InetAddress inetAddress;
    private final int port;

    public ServerContext(InetAddress inetAddress, int serverPort) {
        this.inetAddress = inetAddress;
        this.port = serverPort;
    }

    // Getters
    public InetAddress getInetAddress() { return inetAddress; }
    public int getPort() { return port; }
}
