package exception;

/**
 * The account is locked after too many wrong PIN attempts. Only an administrator can unlock it.
 */
public class AccountLockedException extends ValidationException {

    private static final long serialVersionUID = 1L;

    public AccountLockedException(Long accountId) {
        super("Account #" + accountId + " is locked after too many wrong PIN attempts. Ask an administrator to unlock it.");
    }
}
