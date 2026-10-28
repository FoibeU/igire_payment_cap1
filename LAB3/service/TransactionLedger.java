package LAB3.service;

import LAB1.Account;
import LAB1.FailedTransaction;
import LAB1.Transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
public class TransactionLedger {

    private final Map<Long, List<Account>> customerAccounts = new HashMap<>();
    private final List<Transaction> transactionHistory = new ArrayList<>();
    private final Set<String> processedReferenceIds = new HashSet<>();
    private final List<FailedTransaction> failedTransactionLog = new ArrayList<>();

    public synchronized void registerAccount(Account account) {
        List<Account> accounts = customerAccounts.computeIfAbsent(account.getCustomerId(), id -> new ArrayList<>());
        accounts.removeIf(existing -> existing.getId() != null && existing.getId().equals(account.getId()));
        accounts.add(account);
    }

    public synchronized void removeAccount(Long accountId) {
        customerAccounts.values().forEach(accounts -> accounts.removeIf(a -> accountId.equals(a.getId())));
    }

    public synchronized List<Account> getAccountsOfCustomer(Long customerId) {
        return List.copyOf(customerAccounts.getOrDefault(customerId, Collections.emptyList()));
    }

    public synchronized Optional<Account> findAccount(Long accountId) {
        return customerAccounts.values().stream().flatMap(List::stream).filter(a -> accountId.equals(a.getId())).findFirst();
    }

    public synchronized Map<Long, List<Account>> getCustomerAccounts() {
        Map<Long, List<Account>> copy = new HashMap<>();
        customerAccounts.forEach((customerId, accounts) -> copy.put(customerId, List.copyOf(accounts)));
        return Collections.unmodifiableMap(copy);
    }

    public synchronized boolean markProcessed(String referenceId) {
        return processedReferenceIds.add(referenceId);
    }

    public synchronized boolean isProcessed(String referenceId) {
        return processedReferenceIds.contains(referenceId);
    }


    public synchronized void releaseReference(String referenceId) {
        processedReferenceIds.remove(referenceId);
    }

    public synchronized void loadProcessedReferences(Collection<String> referenceIds) {
        processedReferenceIds.addAll(referenceIds);
    }

    public synchronized Set<String> getProcessedReferenceIds() {
        return Set.copyOf(processedReferenceIds);
    }



    public synchronized void recordTransactions(List<Transaction> transactions) {
        transactionHistory.addAll(transactions);
    }

    public synchronized List<Transaction> getTransactionHistory() {
        return List.copyOf(transactionHistory);
    }

    public synchronized List<Transaction> getTransactionHistory(Long accountId) {
        return transactionHistory.stream().filter(t -> accountId.equals(t.getAccountId())).toList();
    }



    public synchronized void recordFailure(FailedTransaction failure) {
        failedTransactionLog.add(failure);
    }

    public synchronized List<FailedTransaction> getFailedTransactionLog() {
        return List.copyOf(failedTransactionLog);
    }
}
