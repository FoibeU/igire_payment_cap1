package LAB1;

import java.time.LocalDate;

public class TransactionFilter {

    private Long accountId;
    private Long customerId;
    private TransactionType transactionType;
    private String referenceContains;
    private LocalDate fromDate;
    private LocalDate toDate;
    private int limit = 1000;

    public TransactionFilter() {
    }

    public static TransactionFilter forAccount(Long accountId) {
        TransactionFilter filter = new TransactionFilter();
        filter.setAccountId(accountId);
        return filter;
    }

    public static TransactionFilter forCustomer(Long customerId, LocalDate fromDate, LocalDate toDate) {
        TransactionFilter filter = new TransactionFilter();
        filter.setCustomerId(customerId);
        filter.setFromDate(fromDate);
        filter.setToDate(toDate);
        return filter;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getReferenceContains() {
        return referenceContains;
    }

    public void setReferenceContains(String referenceContains) {
        this.referenceContains = referenceContains;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return "TransactionFilter{accountId="
                + accountId + ", customerId="
                + customerId + ", type="
                + transactionType + ", reference~'"
                + referenceContains + "', from="
                + fromDate + ", to="
                + toDate + ", limit="
                + limit + "}";
    }
}
