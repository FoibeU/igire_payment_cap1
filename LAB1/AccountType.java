package LAB1;

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
