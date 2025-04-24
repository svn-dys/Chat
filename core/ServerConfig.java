package core;

import java.net.InetAddress;

// Do not directly create a ServerConfig instance. Use ServerConfigProvider.initialize(...) instead.
public record ServerConfig(InetAddress inetAddress, int port, int backlog, boolean loggingEnabled) {
}