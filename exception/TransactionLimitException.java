package exception;

/**
 * An account-type rule was broken, e.g. a savings withdrawal limit or a wallet balance cap.
 */
public class TransactionLimitException extends ValidationException {

    private static final long serialVersionUID = 1L;

    public TransactionLimitException(String message) {
        super(message);
    }
}
