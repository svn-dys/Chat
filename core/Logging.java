package core;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public final class Logging {
    private final Context context;
    /* BLUE = INFO,
     * RED = ERROR */
    private static final String RESET = "\u001B[0m";
    private static final String BLUE = "\u001B[34m";
    private static final String RED = "\u001B[31m";
    private static final String LOG_FILE = "log.txt";

    private enum Context {
        SERVER, UI
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

    public static Logging uiLogger() {
        return new Logging(Context.UI);
    }

    private void LOG(String level, String message, String color) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stackTrace[3];
        String location = caller.getFileName() + ":" + caller.getLineNumber();
        String logMessage = "[" + location + "][" + context + "]" + level + message;
        System.out.println(color + logMessage + RESET);
        logToFile(message);
    }

    private void logToFile(String logMessage) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            writer.println(logMessage);
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    public void ERROR(String message) {
        LOG("[ERROR]: ", message, RED);
    }

    public void INFO(String message) {
        LOG("[INFO]: ", message, BLUE);
    }
}
