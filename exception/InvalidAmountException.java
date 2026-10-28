package exception;

/**
 * The transaction amount is missing, zero, negative, has too many decimals or is too large.
 */
public class InvalidAmountException extends ValidationException {

    private static final long serialVersionUID = 1L;

    public InvalidAmountException(String message) {
        super(message);
    }
}
