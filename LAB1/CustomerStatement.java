package LAB1;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


public class CustomerStatement {

    private Customer customer;
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<Account> accounts;
    private List<Transaction> transactions;

    public CustomerStatement() {
    }

    public CustomerStatement(Customer customer, LocalDate fromDate, LocalDate toDate, List<Account> accounts, List<Transaction> transactions) {
        this.customer = customer;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.accounts = accounts;
        this.transactions = transactions;
    }

    public BigDecimal getTotalCredits() {
        return transactions.stream().filter(t -> t.getTransactionType().isCredit()).map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalDebits() {
        return transactions.stream().filter(t -> !t.getTransactionType().isCredit()).map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getNetMovement() {
        return getTotalCredits().subtract(getTotalDebits());
    }

    public BigDecimal getTotalBalance() {
        return accounts.stream().map(Account::getBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
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

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public String toString() {
        return "CustomerStatement{customer=" + (customer == null ? null : customer.getFullName()) + ", from=" + fromDate + ", to=" + toDate + ", accounts=" + (accounts == null ? 0 : accounts.size()) + ", transactions=" + (transactions == null ? 0 : transactions.size()) + "}";
    }
}
