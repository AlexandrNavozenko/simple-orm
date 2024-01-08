package core.exception;

public class ParseResultSetException extends RuntimeException {


    public ParseResultSetException(String message, Exception exception) {
        super(message, exception);
    }
}
