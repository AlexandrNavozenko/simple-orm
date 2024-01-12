package core.exception;

public class ConnectionClearException extends RuntimeException {

    public ConnectionClearException(String message) {
        super(message);
    }

    public ConnectionClearException(String message, Exception exception) {
        super(message, exception);
    }
}
