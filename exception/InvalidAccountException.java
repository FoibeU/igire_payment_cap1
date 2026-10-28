package exception;

/**
 * The account ID does not exist, or the account cannot be used for this operation.
 */
public class InvalidAccountException extends ValidationException {

    private static final long serialVersionUID = 1L;

    public InvalidAccountException(String message) {
        super(message);
    }

    public static InvalidAccountException notFound(Long accountId) {
        return new InvalidAccountException("Account #" + accountId + " does not exist.");
    }
}
