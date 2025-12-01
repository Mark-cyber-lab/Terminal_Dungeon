package utilities;

public interface Loggable {
    // Default logging method uses class name as header
    default void log(String message) {
        String header = this.getClass().getSimpleName(); // class name as header
        DebugLogger.log(header, message);
    }

    // Static logging method, requires a class reference for header
    static void log(Class<?> clazz, String message) {
        String header = clazz.getSimpleName();
        DebugLogger.log(header, message);
    }

    // Static logging method with custom header
    static void log(String header, String message) {
        DebugLogger.log(header, message);
    }
}