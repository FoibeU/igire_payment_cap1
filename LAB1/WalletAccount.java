package LAB1;

import exception.TransactionLimitException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public class WalletAccount extends Account {

    public static final BigDecimal MAX_BALANCE = new BigDecimal("5000000.00");
    public static final BigDecimal MIN_WITHDRAWAL = new BigDecimal("100.00");
    public static final BigDecimal WITHDRAWAL_FEE_RATE = new BigDecimal("0.10");
    public static final BigDecimal MAX_WITHDRAWAL_FEE = new BigDecimal("100.00");

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

    @Override
    public void deposit(BigDecimal amount) {
        BigDecimal value = Money.requirePositive(amount);
        if (getBalance().add(value).compareTo(MAX_BALANCE) > 0) {
            throw new TransactionLimitException("A wallet cannot hold more than " + Money.format(MAX_BALANCE) + ". Account #" + getId() + " can receive at most " + Money.format(MAX_BALANCE.subtract(getBalance())) + ".");
        }
        super.deposit(value);
    }

    @Override
    public void withdraw(BigDecimal amount, TransactionType type) {
        BigDecimal value = Money.requirePositive(amount);
        if (type == TransactionType.WITHDRAWAL && value.compareTo(MIN_WITHDRAWAL) < 0) {
            throw new TransactionLimitException("The minimum wallet withdrawal is " + Money.format(MIN_WITHDRAWAL) + ".");
        }
        super.withdraw(value, type);
    }

    @Override
    public BigDecimal calculateWithdrawalInterest(BigDecimal amount) {
        BigDecimal fee = amount.multiply(WITHDRAWAL_FEE_RATE).setScale(Money.SCALE, RoundingMode.HALF_UP);
        return fee.min(MAX_WITHDRAWAL_FEE);
    }

    @Override
    protected BigDecimal calculateFee(BigDecimal amount, TransactionType type) {
        if (type == TransactionType.TRANSFER_OUT) {
            return Money.round(BigDecimal.ZERO);
        }
        return calculateWithdrawalInterest(amount);
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