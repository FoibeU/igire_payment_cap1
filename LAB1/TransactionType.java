package LAB1;

public enum TransactionType {

    DEPOSIT("Deposit", true),
    WITHDRAWAL("Withdrawal", false),
    TRANSFER_IN("Transfer In", true),
    TRANSFER_OUT("Transfer Out", false),
    FEE("Fee", false);

    private final String description;
    private final boolean credit;

    TransactionType(String description, boolean credit) {
        this.description = description;
        this.credit = credit;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCredit() {
        return credit;
    }

    @Override
    public String toString() {
        return description;
    }
}
