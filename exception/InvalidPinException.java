package exception;

/**
 * The PIN is wrong, missing or does not respect the PIN format.
 */
public class InvalidPinException extends ValidationException {

    private static final long serialVersionUID = 1L;

    public InvalidPinException(String message) {
        super(message);
    }
}
