package core.exception;

public class NotFoundConnectionException extends RuntimeException {

    public NotFoundConnectionException(String message) {
        super(message);
    }

    public NotFoundConnectionException(String message, Exception exception) {
        super(message, exception);
    }
}
