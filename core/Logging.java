package core;

public class Logging {
    private final Context context;

    // BLUE = INFO,
    // RED = ERROR
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String BLUE = "\u001B[34m";

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

    private void log(String level, String message, String color) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stackTrace[3];
        String location = caller.getFileName() + ":" + caller.getLineNumber();

        System.out.println(color + "[" + location + "][" + context + "]" + level + message + RESET);
    }

    public void error(String message) {
        log("[ERROR]: ", message, RED);
    }

    public void info(String message) {
        log("[INFO]: ", message, BLUE);
    }
}
