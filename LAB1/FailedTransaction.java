package LAB1;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FailedTransaction {

    private Long id;
    private String referenceId;
    private Long accountId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private String reason;
    private LocalDateTime failedAt;

    public FailedTransaction(String referenceId, Long accountId, TransactionType transactionType, BigDecimal amount, String reason) {
        this(null, referenceId, accountId, transactionType, amount, reason, LocalDateTime.now());
    }

    public FailedTransaction(Long id, String referenceId, Long accountId, TransactionType transactionType, BigDecimal amount, String reason, LocalDateTime failedAt) {
        this.id = id;
        this.referenceId = referenceId;
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.reason = reason;
        this.failedAt = failedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(LocalDateTime failedAt) {
        this.failedAt = failedAt;
    }

    @Override
    public String toString() {
        return "FailedTransaction{referenceId='"
                + referenceId + "', accountId="
                + accountId + ", type="
                + transactionType + ", amount="
                + (amount == null ? null : Money.format(amount)) + ", reason='"
                + reason + "', failedAt=" + failedAt + "}";
    }
}
