package exception;

/**
 * Wrong username/password, or the logged-in user is not allowed to perform the operation.
 */
public class AuthenticationException extends ValidationException {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }
}
