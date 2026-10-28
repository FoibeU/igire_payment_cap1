package LAB1;

/**
 * Allowed values of accounts.account_type. Each type maps to its own Account subclass.
 */
public enum AccountType {

    WALLET("Wallet"),
    SAVINGS("Savings");

    private final String description;

    AccountType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
