package server;

/*
    This class represents a client inside the server.
    It holds information about the client as well as methods to manipulate the client.
*/
public class Client {
    private String name;
    private final ClientHandler handler;

    Client(String name, ClientHandler handler) {
        this.name = name;
        this.handler = handler;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    // Getters
    public String getName() {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Client name cannot be null or blank.");
        }
        return name;
    }

    public ClientHandler getHandler() {
        return handler;
    }
}
