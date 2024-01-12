package core.exception;

public class InitPoolConnectionException extends RuntimeException {

    public InitPoolConnectionException(String message) {
        super(message);
    }

    public InitPoolConnectionException(String message, Exception exception) {
        super(message, exception);
    }
}
