package LAB1;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
public class Transaction {

    private Long id;
    private Long accountId;
    private String referenceId;
    private BigDecimal amount;
    private TransactionType transactionType;
    private LocalDateTime timestamp;
    private String description;

    public Transaction() {
    }


    public Transaction(String referenceId, TransactionType transactionType, BigDecimal amount) {
        this.referenceId = referenceId;
        this.transactionType = transactionType;
        this.amount = amount;
    }

    public Transaction(Long id, Long accountId, String referenceId, BigDecimal amount, TransactionType transactionType, LocalDateTime timestamp, String description) {
        this.id = id;
        this.accountId = accountId;
        this.referenceId = referenceId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.timestamp = timestamp;
        this.description = description;
    }

    /** Amount with sign: positive for credits, negative for debits (used by statements). */
    public BigDecimal getSignedAmount() {
        if (amount == null || transactionType == null) {
            return BigDecimal.ZERO;
        }
        return transactionType.isCredit() ? amount : amount.negate();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Transaction other)) {
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
        return "Transaction{id=" + id + ", accountId=" + accountId + ", referenceId='" + referenceId + "', type=" + transactionType + ", amount=" + Money.format(amount) + ", timestamp=" + timestamp + ", description='" + description + "'}";
    }
}
