package LAB1;


import exception.*;
import util.PasswordHasher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public abstract class Account {

    public static final int MAX_PIN_ATTEMPTS = 3;
    private static final Pattern PIN_PATTERN = Pattern.compile("^[0-9]{4,6}$");

    private Long id;
    private Long customerId;
    private String customerName;
    private BigDecimal balance = Money.round(BigDecimal.ZERO);
    private AccountStatus status = AccountStatus.ACTIVE;
    private LocalDateTime createdAt;
    private String pinHash;
    private String pinSalt;
    private int failedPinAttempts;
    private boolean locked;

    protected Account() {
    }

    protected Account(Long customerId) {
        this.customerId = customerId;
    }

    protected Account(Long id, Long customerId, BigDecimal balance, AccountStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.customerId = customerId;
        this.balance = Money.round(balance);
        this.status = status == null ? AccountStatus.ACTIVE : status;
        this.createdAt = createdAt;
    }

    public static Account of(AccountType type) {
        if (type == null) {
            throw new ValidationException("Account type is required.");
        }
        return switch (type) {
            case WALLET -> new WalletAccount();
            case SAVINGS -> new SavingsAccount();
        };
    }

    public abstract AccountType getAccountType();

    public void deposit(BigDecimal amount) {
        ensureActive();
        BigDecimal value = Money.requirePositive(amount);
        balance = balance.add(value);
    }

    public void withdraw(BigDecimal amount, TransactionType type) {
        ensureCanDebit();
        BigDecimal value = Money.requirePositive(amount);
        BigDecimal fee = calculateFee(value, type);
        BigDecimal total = value.add(fee);
        if (balance.compareTo(total) < 0) {
            throw new InsufficientBalanceException(id, balance, total);
        }
        balance = Money.round(balance.subtract(total));
    }

    public BigDecimal calculateWithdrawalInterest(BigDecimal amount) {
        return Money.round(BigDecimal.ZERO);
    }

    protected BigDecimal calculateFee(BigDecimal amount, TransactionType type) {
        return Money.round(calculateWithdrawalInterest(amount));
    }

    public boolean canTransferTo(Account target) {
        return target != null && target.getStatus() == AccountStatus.ACTIVE;
    }

    public abstract boolean supportsInstantTransfer();

    public List<Transaction> processTransaction(Transaction request) {
        if (request == null || request.getTransactionType() == null) {
            throw new ValidationException("Transaction type is required.");
        }
        BigDecimal amount = Money.requirePositive(request.getAmount());
        TransactionType type = request.getTransactionType();
        List<Transaction> rows = new ArrayList<>();
        switch (type) {
            case DEPOSIT, TRANSFER_IN -> {
                deposit(amount);
                rows.add(newEntry(request, type, amount, describe(request, type)));
            }
            case WITHDRAWAL, TRANSFER_OUT -> {
                BigDecimal fee = calculateFee(amount, type);   // same fee that withdraw() deducts
                withdraw(amount, type);
                rows.add(newEntry(request, type, amount, describe(request, type)));
                if (fee.signum() > 0) {
                    rows.add(newEntry(request, TransactionType.FEE, fee, getAccountType() + " fee on " + type.getDescription().toLowerCase()));
                }
            }
            case FEE ->
                    throw new ValidationException("FEE transactions are created automatically and cannot be requested.");
        }
        return rows;
    }

    protected String describe(Transaction request, TransactionType type) {
        return request.getDescription() == null || request.getDescription().isBlank() ? type.getDescription() : request.getDescription();
    }

    protected Transaction newEntry(Transaction request, TransactionType type, BigDecimal amount, String description) {
        return new Transaction(null, id, request.getReferenceId(), Money.round(amount), type, LocalDateTime.now(), description);
    }

    protected void ensureActive() {
        if (status != AccountStatus.ACTIVE) {
            throw new InvalidAccountException("Account #" + id + " is inactive and cannot be used.");
        }
    }

    protected void ensureCanDebit() {
        ensureActive();
        if (locked) {
            throw new AccountLockedException(id);
        }
    }

    public void deactivate() {
        if (balance.signum() != 0) {
            throw new ValidationException("Account #" + id + " still has a balance of " + Money.format(balance) + ". Withdraw or transfer it before closing the account.");
        }
        status = AccountStatus.INACTIVE;
    }

    public void activate() {
        status = AccountStatus.ACTIVE;
    }

    public boolean hasPin() {
        return pinHash != null && pinSalt != null;
    }

    public void setPin(String pin) {
        if (pin == null || !PIN_PATTERN.matcher(pin).matches()) {
            throw new InvalidPinException("PIN must be 4 to 6 digits.");
        }
        if (pin.chars().distinct().count() == 1) {
            throw new InvalidPinException("PIN must not use the same digit only (e.g. 1111).");
        }
        this.pinSalt = PasswordHasher.newSalt();
        this.pinHash = PasswordHasher.hash(pin, pinSalt);
        this.failedPinAttempts = 0;
    }

    public boolean verifyPin(String pin) {
        if (locked) {
            throw new AccountLockedException(id);
        }
        if (!hasPin()) {
            throw new InvalidPinException("Account #" + id + " has no PIN yet. Create a PIN first.");
        }
        if (PasswordHasher.matches(pin, pinSalt, pinHash)) {
            failedPinAttempts = 0;
            return true;
        }
        failedPinAttempts++;
        if (failedPinAttempts >= MAX_PIN_ATTEMPTS) {
            locked = true;
        }
        return false;
    }

    public int getRemainingPinAttempts() {
        return Math.max(0, MAX_PIN_ATTEMPTS - failedPinAttempts);
    }

    public void unlock() {
        locked = false;
        failedPinAttempts = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        if (balance == null || balance.signum() < 0) {
            throw new ValidationException("Balance cannot be empty or negative.");
        }
        this.balance = Money.round(balance);
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPinHash() {
        return pinHash;
    }

    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
    }

    public String getPinSalt() {
        return pinSalt;
    }

    public void setPinSalt(String pinSalt) {
        this.pinSalt = pinSalt;
    }

    public int getFailedPinAttempts() {
        return failedPinAttempts;
    }

    public void setFailedPinAttempts(int failedPinAttempts) {
        this.failedPinAttempts = failedPinAttempts;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Account other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        String owner = customerName == null ? "customer " + customerId : customerName;
        return "#" + id + " " + getAccountType() + " - " + owner
                + " (balance " + Money.format(balance)
                + (status == AccountStatus.INACTIVE ? ", INACTIVE" : "")
                + (locked ? ", LOCKED" : "") + ")";
    }
}