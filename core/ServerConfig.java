package core;

import java.net.InetAddress;

public record ServerConfig(InetAddress inetAddress, int port, int backlog, boolean loggingEnabled) {
}