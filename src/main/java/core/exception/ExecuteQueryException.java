package core.exception;

public class ExecuteQueryException extends RuntimeException {

    public ExecuteQueryException(String message, Exception exception) {
        super(message, exception);
    }
}
