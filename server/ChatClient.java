// Handles a single client (user) to the ChatServer
package server;

import core.Logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatClient implements Runnable {
    private final Logging logger = Logging.clientLogger();
    private Socket socket;

    ChatClient(Socket socket,
               ServerSocket serverSocket) {
        logger.info("Client connected to server IP: " +
                serverSocket.getInetAddress() + ":" + serverSocket.getLocalPort() + " from client IP: " +
                socket.getInetAddress() + ":" + socket.getPort() + ".");
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            String messageFromClient;
            while ((messageFromClient = reader.readLine()) != null) {
                logger.info("Received message from client: " + messageFromClient);
            }
        } catch(IOException e) {

        }

    }
}
