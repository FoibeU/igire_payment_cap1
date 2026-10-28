package LAB1;

import exception.TransactionLimitException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Mobile-money style wallet.
 * <ul>
 *     <li>Instant transfers to any active account, with no fee.</li>
 *     <li>The balance cannot exceed {@link #MAX_BALANCE}.</li>
 *     <li>Minimum withdrawal is {@link #MIN_WITHDRAWAL}.</li>
 * </ul>
 */
public class WalletAccount extends Account {

    public static final BigDecimal MAX_BALANCE = new BigDecimal("5000000.00");
    public static final BigDecimal MIN_WITHDRAWAL = new BigDecimal("100.00");

    public WalletAccount() {
        super();
    }

    public WalletAccount(Long customerId) {
        super(customerId);
    }

    public WalletAccount(Long id, Long customerId, BigDecimal balance, AccountStatus status, LocalDateTime createdAt) {
        super(id, customerId, balance, status, createdAt);
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.WALLET;
    }

    /** Wallets have a balance cap. */
    @Override
    public void deposit(BigDecimal amount) {
        BigDecimal value = Money.requirePositive(amount);
        if (getBalance().add(value).compareTo(MAX_BALANCE) > 0) {
            throw new TransactionLimitException("A wallet cannot hold more than " + Money.format(MAX_BALANCE) + ". Account #" + getId() + " can receive at most " + Money.format(MAX_BALANCE.subtract(getBalance())) + ".");
        }
        super.deposit(value);
    }

    /** Wallets have a minimum cash-out amount and no fee. */
    @Override
    public void withdraw(BigDecimal amount) {
        BigDecimal value = Money.requirePositive(amount);
        if (value.compareTo(MIN_WITHDRAWAL) < 0) {
            throw new TransactionLimitException("The minimum wallet withdrawal is " + Money.format(MIN_WITHDRAWAL) + ".");
        }
        super.withdraw(value);
    }

    @Override
    public boolean supportsInstantTransfer() {
        return true;
    }

    @Override
    public List<Transaction> processTransaction(Transaction request) {
        List<Transaction> rows = super.processTransaction(request);
        rows.stream().filter(t -> t.getTransactionType() == TransactionType.TRANSFER_OUT || t.getTransactionType() == TransactionType.TRANSFER_IN).forEach(t -> t.setDescription("Instant wallet transfer - " + t.getDescription()));
        return rows;
    }

    @Override
    public String toString() {
        return "WalletAccount" + super.toString();
    }
}
