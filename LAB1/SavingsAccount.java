package LAB1;

import exception.TransactionLimitException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;


public class SavingsAccount extends Account {

    public static final BigDecimal MIN_DEPOSIT = new BigDecimal("500.00");
    public static final BigDecimal MAX_SINGLE_WITHDRAWAL = new BigDecimal("500000.00");
    public static final int MAX_WITHDRAWALS_PER_MONTH = 10;
    public static final BigDecimal INTEREST_RATE = new BigDecimal("0.02");
    public static final BigDecimal MIN_INTEREST = new BigDecimal("200.00");
    private int withdrawalsThisMonth;

    public SavingsAccount() {
        super();
    }

    public SavingsAccount(Long customerId) {
        super(customerId);
    }

    public SavingsAccount(Long id, Long customerId, BigDecimal balance, AccountStatus status, LocalDateTime createdAt, int withdrawalsThisMonth) {
        super(id, customerId, balance, status, createdAt);
        this.withdrawalsThisMonth = withdrawalsThisMonth;
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.SAVINGS;
    }

    @Override
    public void deposit(BigDecimal amount) {
        BigDecimal value = Money.requirePositive(amount);
        if (value.compareTo(MIN_DEPOSIT) < 0) {
            throw new TransactionLimitException("The minimum savings deposit is " + Money.format(MIN_DEPOSIT) + ".");
        }
        super.deposit(value);
    }

    @Override
    public void withdraw(BigDecimal amount, TransactionType type) {
        BigDecimal value = Money.requirePositive(amount);
        if (value.compareTo(MAX_SINGLE_WITHDRAWAL) > 0) {
            throw new TransactionLimitException("A savings withdrawal cannot exceed " + Money.format(MAX_SINGLE_WITHDRAWAL) + ".");
        }
        if (withdrawalsThisMonth >= MAX_WITHDRAWALS_PER_MONTH) {
            throw new TransactionLimitException("Savings account #" + getId() + " already made " + MAX_WITHDRAWALS_PER_MONTH + " withdrawals this month.");
        }
        super.withdraw(value, type);   // fee is 0, so balance = balance - amount
        withdrawalsThisMonth++;
    }

    @Override
    protected BigDecimal calculateFee(BigDecimal amount, TransactionType type) {
        return Money.round(BigDecimal.ZERO);
    }

    @Override
    public BigDecimal calculateWithdrawalInterest(BigDecimal amount) {
        BigDecimal interest = Money.requirePositive(amount).multiply(INTEREST_RATE).setScale(Money.SCALE, RoundingMode.HALF_UP);
        return interest.max(MIN_INTEREST);
    }

    public BigDecimal calculatePayout(BigDecimal amount) {
        BigDecimal value = Money.requirePositive(amount);
        return Money.round(value.add(calculateWithdrawalInterest(value)));
    }

    @Override
    public boolean canTransferTo(Account target) {
        return super.canTransferTo(target) && getCustomerId() != null && getCustomerId().equals(target.getCustomerId());
    }

    @Override
    public boolean supportsInstantTransfer() {
        return false;
    }

    @Override
    public List<Transaction> processTransaction(Transaction request) {
        List<Transaction> rows = super.processTransaction(request);
        for (Transaction t : rows) {
            if (t.getTransactionType() == TransactionType.WITHDRAWAL) {
                BigDecimal interest = calculateWithdrawalInterest(t.getAmount());
                BigDecimal payout = t.getAmount().add(interest);
                t.setDescription(t.getDescription()
                        + " withdrawed" + Money.format(t.getAmount())
                        + " 2% " + Money.format(interest)
                        + " interest" + Money.format(payout)
                        + " (on saving " + withdrawalsThisMonth + "/" + MAX_WITHDRAWALS_PER_MONTH + " this month)");
            } else if (t.getTransactionType() == TransactionType.TRANSFER_OUT) {
                t.setDescription(t.getDescription()
                        + " (savings withdrawal " + withdrawalsThisMonth + "/" + MAX_WITHDRAWALS_PER_MONTH + " this month)");
            }
        }
        return rows;
    }

    public int getWithdrawalsThisMonth() {
        return withdrawalsThisMonth;
    }

    public void setWithdrawalsThisMonth(int withdrawalsThisMonth) {
        this.withdrawalsThisMonth = withdrawalsThisMonth;
    }

    public int getRemainingWithdrawalsThisMonth() {
        return Math.max(0, MAX_WITHDRAWALS_PER_MONTH - withdrawalsThisMonth);
    }

    @Override
    public String toString() {
        return "SavingsAccount" + super.toString() + " [withdrawals this month: " + withdrawalsThisMonth + "/" + MAX_WITHDRAWALS_PER_MONTH + "]";
    }
}