package core.exception;

public class CreateNewInstanceException extends RuntimeException {

    public CreateNewInstanceException(String message, Exception exception) {
        super(message, exception);
    }
}
