package exception;

/**
 * Base class of all IgirePay errors (unchecked), so callers can catch one type for every business error.
 */
public class IgirePayException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public IgirePayException(String message) {
        super(message);
    }

    public IgirePayException(String message, Throwable cause) {
        super(message, cause);
    }
}
