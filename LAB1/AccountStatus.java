package LAB1;

/**
 * ACTIVE accounts can move money. INACTIVE accounts are closed (zero balance) and can be deleted.
 */
public enum AccountStatus {

    ACTIVE("Active"),
    INACTIVE("Inactive");

    private final String description;

    AccountStatus(String description) {
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
