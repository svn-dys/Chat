package core;

import java.time.LocalDateTime;

// Logging class for logging messages to the console.
public class Logging {
    private final Context context;

    // The context of the object from which the message was logged.
    private enum Context {
        SERVER, CLIENT, UI
    }

    private Logging(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not be null. Define the context from where you're calling from.");
        }
        this.context = context;
    }

    public static Logging serverLogger() {
        return new Logging(Context.SERVER);
    }

    public static Logging clientLogger() {
        return new Logging(Context.CLIENT);
    }

    public static Logging guiLogger() {
        return new Logging(Context.UI);
    }

    private void log(String message) {
        System.out.println("[" + LocalDateTime.now() + "][" + context + "]" + message);
    }

    public void error(String message) {
        log("[ERROR]: " + message);
    }

    public void info(String message) {
        log("[INFO]: " + message);
    }
}