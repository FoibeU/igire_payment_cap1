package LAB3.service;

import LAB1.FailedTransaction;
import LAB1.Transaction;
import LAB1.TransactionFilter;

import java.math.BigDecimal;
import java.util.List;


public interface PaymentService {

    List<Transaction> deposit(String referenceId, Long accountId, BigDecimal amount);


    List<Transaction> withdraw(String referenceId, Long accountId, BigDecimal amount, String pin);

    List<Transaction> transfer(String referenceId, Long fromAccountId, Long toAccountId, BigDecimal amount, String pin);

    List<Transaction> getTransactionHistory(Long accountId);

    List<Transaction> searchTransactions(TransactionFilter filter);

    boolean isProcessed(String referenceId);

//    Not Finished the rolebased part
    List<FailedTransaction> getFailedTransactions(int limit);
//    Generate References key
    String newReferenceId();

    void loadProcessedReferences();
}
