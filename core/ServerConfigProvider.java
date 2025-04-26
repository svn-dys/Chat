package core;

/*
    Provides a single ServerConfig instance. Cannot be reinstantiated after initialization.
    The main idea behind a ServerConfigProvider is to have a single source of truth for the server configuration.
    This way, the server configuration is highly decoupled from the rest of the application. Along with this,
    the clients/UI-Network-Thread know of the server configuration and can use it to connect to the server
    through its associated UI network sockets.
*/
public final class ServerConfigProvider {
    private static final Logging LOG = Logging.serverLogger();
    private static ServerConfig instance;

    public static void initialize(ServerConfig config) {
        if (instance != null) {
            throw new IllegalStateException("Cannot initialize another ServerConfig that is already initialized.");
        }
        instance = config;
        logCurrentConfig();
    }

    static void logCurrentConfig() {
        LOG.INFO("Current Server Config: " + instance);
    }

    public static ServerConfig get() {
        return instance;
    }
}
