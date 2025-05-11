package server;

/*
 * This class represents a client inside the server.
 * It holds information about the client as well as the
 * methods to manipulate the client.
*/
public final class Client {
    private String name;

    Client(String name, ClientHandler handler) {
        this.name = name;
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
}
