package core;

import java.net.InetAddress;

/*
  * Do not directly create a ServerConfig instance. Instead, use
  * ServerConfigProvider.initialize(...) in core/ServerConfigProvider.java instead.
*/
public record ServerConfig(InetAddress inetAddress, int port, int backlog) {
}