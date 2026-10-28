//package LAB3.service;
//
//import exception.DuplicateTransactionException;
//import exception.IgirePayException;
//import exception.InvalidAccountException;
//import exception.ValidationException;
//import LAB1.Account;
//import LAB1.FailedTransaction;
//import LAB1.SavingsAccount;
//import LAB1.Transaction;
//import LAB1.TransactionType;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//
//public class InMemoryPaymentProcessor {
//
//    private final TransactionLedger ledger;
//
//    public InMemoryPaymentProcessor(TransactionLedger ledger) {
//        this.ledger = ledger;
//    }
//
//    public List<Transaction> deposit(String referenceId, Long accountId, BigDecimal amount) {
//        return process(referenceId, accountId, new Transaction(referenceId, TransactionType.DEPOSIT, amount));
//    }
//
//    public List<Transaction> withdraw(String referenceId, Long accountId, BigDecimal amount) {
//        return process(referenceId, accountId, new Transaction(referenceId, TransactionType.WITHDRAWAL, amount));
//    }
//
//    public List<Transaction> transfer(String referenceId, Long fromAccountId, Long toAccountId, BigDecimal amount) {
//        String reference = requireReference(referenceId);
//        if (!ledger.markProcessed(reference)) {
//            return rejectDuplicate(reference, fromAccountId, TransactionType.TRANSFER_OUT, amount);
//        }
//        Account from = null;
//        Account to = null;
//        BigDecimal fromBalance = null;
//        BigDecimal toBalance = null;
//        int fromWithdrawals = 0;
//        try {
//            from = findAccount(fromAccountId);
//            to = findAccount(toAccountId);
//            if (from.getId().equals(to.getId())) {
//                throw new ValidationException("Source and destination accounts must be different.");
//            }
//            if (!from.canTransferTo(to)) {
//                throw new InvalidAccountException(from.getAccountType() + " account #" + from.getId() + " cannot transfer to account #" + to.getId() + (from instanceof SavingsAccount ? " (savings can only transfer to the same customer's accounts)." : "."));
//            }
//            // snapshot, so a failure on the destination restores the source (all-or-nothing)
//            fromBalance = from.getBalance();
//            toBalance = to.getBalance();
//            fromWithdrawals = from instanceof SavingsAccount savings ? savings.getWithdrawalsThisMonth() : 0;
//
//            Transaction out = new Transaction(reference, TransactionType.TRANSFER_OUT, amount);
//            out.setDescription("Transfer to account #" + to.getId());
//            Transaction in = new Transaction(reference, TransactionType.TRANSFER_IN, amount);
//            in.setDescription("Transfer from account #" + from.getId());
//
//            List<Transaction> rows = new ArrayList<>(from.processTransaction(out));
//            rows.addAll(to.processTransaction(in));
//            ledger.recordTransactions(rows);
//            return rows;
//        } catch (IgirePayException e) {
//            if (fromBalance != null) {
//                from.setBalance(fromBalance);
//                to.setBalance(toBalance);
//                if (from instanceof SavingsAccount savings) {
//                    savings.setWithdrawalsThisMonth(fromWithdrawals);
//                }
//            }
//            ledger.releaseReference(reference);
//            ledger.recordFailure(new FailedTransaction(reference, fromAccountId, TransactionType.TRANSFER_OUT, amount, e.getMessage()));
//            throw e;
//        }
//    }
//
//    private List<Transaction> process(String referenceId, Long accountId, Transaction request) {
//        String reference = requireReference(referenceId);
//        if (!ledger.markProcessed(reference)) {
//            return rejectDuplicate(reference, accountId, request.getTransactionType(), request.getAmount());
//        }
//        try {
//            Account account = findAccount(accountId);
//            List<Transaction> rows = account.processTransaction(request);
//            ledger.recordTransactions(rows);
//            return rows;
//        } catch (IgirePayException e) {
//            ledger.releaseReference(reference);
//            ledger.recordFailure(new FailedTransaction(reference, accountId, request.getTransactionType(), request.getAmount(), e.getMessage()));
//            throw e;
//        }
//    }
//
//    private List<Transaction> rejectDuplicate(String reference, Long accountId, TransactionType type, BigDecimal amount) {
//        DuplicateTransactionException duplicate = new DuplicateTransactionException(reference);
//        ledger.recordFailure(new FailedTransaction(reference, accountId, type, amount, duplicate.getMessage()));
//        throw duplicate;
//    }
//
//    private Account findAccount(Long accountId) {
//        if (accountId == null) {
//            throw new InvalidAccountException("Account ID is required.");
//        }
//        return ledger.findAccount(accountId).orElseThrow(() -> InvalidAccountException.notFound(accountId));
//    }
//
//    private String requireReference(String referenceId) {
//        if (referenceId == null || referenceId.isBlank()) {
//            throw new ValidationException("Reference ID is required.");
//        }
//        return referenceId.trim();
//    }
//}
