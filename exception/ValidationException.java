package exception;

/**
 * Invalid user input or a broken business rule. The message is safe to show to the user.
 */
public class ValidationException extends IgirePayException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String message) {
        super(message);
    }
}
