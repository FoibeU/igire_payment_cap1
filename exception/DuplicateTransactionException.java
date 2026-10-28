package exception;

/**
 * A request with this reference ID was already processed (idempotency protection).
 */
public class DuplicateTransactionException extends ValidationException {

    private static final long serialVersionUID = 1L;

    private final String referenceId;

    public DuplicateTransactionException(String referenceId) {
        super("Duplicate request: reference '" + referenceId + "' was already processed. It was NOT applied again.");
        this.referenceId = referenceId;
    }

    public String getReferenceId() {
        return referenceId;
    }
}
