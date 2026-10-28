package LAB3.service;

import LAB2.AccountDao;
import LAB2.FailedTransactionDao;
import LAB2.ProcessedRequestDao;
import LAB2.TransactionDao;
import exception.DuplicateTransactionException;
import exception.IgirePayException;
import exception.InvalidAccountException;
import exception.ValidationException;
import LAB1.Account;
import LAB1.FailedTransaction;
import LAB1.Money;
import LAB1.SavingsAccount;
import LAB1.Transaction;
import LAB1.TransactionFilter;
import LAB1.TransactionType;
import util.TransactionManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class PaymentServiceImpl implements PaymentService {

    private static final Pattern REFERENCE_PATTERN = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._:-]{0,99}$");
    private static final DateTimeFormatter REFERENCE_DATE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final AccountDao accountDao;
    private final TransactionDao transactionDao;
    private final ProcessedRequestDao processedRequestDao;
    private final FailedTransactionDao failedTransactionDao;
    private final AccountService accountService;
    private final TransactionManager transactionManager;
    private final TransactionLedger ledger;

    public PaymentServiceImpl(AccountDao accountDao, TransactionDao transactionDao, ProcessedRequestDao processedRequestDao, FailedTransactionDao failedTransactionDao, AccountService accountService, TransactionManager transactionManager, TransactionLedger ledger) {
        this.accountDao = accountDao;
        this.transactionDao = transactionDao;
        this.processedRequestDao = processedRequestDao;
        this.failedTransactionDao = failedTransactionDao;
        this.accountService = accountService;
        this.transactionManager = transactionManager;
        this.ledger = ledger;
    }

    @Override
    public List<Transaction> deposit(String referenceId, Long accountId, BigDecimal amount) {
        Transaction request = new Transaction(referenceId, TransactionType.DEPOSIT, amount);
        return execute(request, accountId, false, null, () -> transactionManager.inTransaction(connection -> {
            claimReference(connection, request.getReferenceId());
            Account account = lock(connection, accountId);
            List<Transaction> rows = account.processTransaction(request);
            save(connection, account, rows);
            return rows;
        }));
    }

    @Override
    public List<Transaction> withdraw(String referenceId, Long accountId, BigDecimal amount, String pin) {
        Transaction request = new Transaction(referenceId, TransactionType.WITHDRAWAL, amount);
        return execute(request, accountId, true, pin, () -> transactionManager.inTransaction(connection -> {
            claimReference(connection, request.getReferenceId());
            Account account = lock(connection, accountId);
            List<Transaction> rows = account.processTransaction(request);
            save(connection, account, rows);
            return rows;
        }));
    }

    @Override
    public List<Transaction> transfer(String referenceId, Long fromAccountId, Long toAccountId, BigDecimal amount, String pin) {
        Transaction request = new Transaction(referenceId, TransactionType.TRANSFER_OUT, amount);
        if (toAccountId == null) {
            return execute(request, fromAccountId, false, null, () -> {
                throw new InvalidAccountException("Please select the destination account.");
            });
        }
        if (toAccountId.equals(fromAccountId)) {
            return execute(request, fromAccountId, false, null, () -> {
                throw new ValidationException("Source and destination accounts must be different.");
            });
        }
        return execute(request, fromAccountId, true, pin, () -> transactionManager.inTransaction(connection -> {
            claimReference(connection, request.getReferenceId());
            // lock both rows in id order, so two opposite transfers cannot deadlock
            Account first = lock(connection, Math.min(fromAccountId, toAccountId));
            Account second = lock(connection, Math.max(fromAccountId, toAccountId));
            Account from = first.getId().equals(fromAccountId) ? first : second;
            Account to = first.getId().equals(toAccountId) ? first : second;
            if (!from.canTransferTo(to)) {
                String reason = from instanceof SavingsAccount ? " Savings accounts can only transfer to the same customer's accounts." : " The destination account is not active.";
                throw new InvalidAccountException(from.getAccountType() + " account #" + from.getId() + " cannot transfer to account #" + to.getId() + "." + reason);
            }
            Transaction out = new Transaction(request.getReferenceId(), TransactionType.TRANSFER_OUT, request.getAmount());
            out.setDescription("Transfer to account #" + to.getId() + " (" + to.getCustomerName() + ")");
            Transaction in = new Transaction(request.getReferenceId(), TransactionType.TRANSFER_IN, request.getAmount());
            in.setDescription("Transfer from account #" + from.getId() + " (" + from.getCustomerName() + ")");

            List<Transaction> rows = new ArrayList<>(from.processTransaction(out));
            List<Transaction> incoming = to.processTransaction(in);
            rows.addAll(incoming);
            save(connection, from, List.of());
            save(connection, to, List.of());
            for (Transaction row : rows) {
                transactionDao.create(connection, row);
            }
            return rows;
        }));
    }

    @Override
    public List<Transaction> getTransactionHistory(Long accountId) {
        if (accountId == null) {
            throw new InvalidAccountException("Please select an account.");
        }
        return transactionManager.inTransaction(connection -> {
            if (accountDao.findById(connection, accountId).isEmpty()) {
                throw InvalidAccountException.notFound(accountId);
            }
            return transactionDao.findByAccountId(connection, accountId);
        });
    }

    @Override
    public List<Transaction> searchTransactions(TransactionFilter filter) {
        TransactionFilter criteria = filter == null ? new TransactionFilter() : filter;
        if (criteria.getFromDate() != null && criteria.getToDate() != null && criteria.getFromDate().isAfter(criteria.getToDate())) {
            throw new ValidationException("'From' date must be before or equal to 'To' date.");
        }
        return transactionManager.inTransaction(connection -> transactionDao.search(connection, criteria));
    }

    @Override
    public boolean isProcessed(String referenceId) {
        String reference = validateReference(referenceId);
        return ledger.isProcessed(reference) || transactionManager.inTransaction(connection -> processedRequestDao.exists(connection, reference));
    }

    @Override
    public List<FailedTransaction> getFailedTransactions(int limit) {
//        SessionContext.requireAdmin();
        return transactionManager.inTransaction(connection -> failedTransactionDao.findRecent(connection, limit));
    }

    @Override
    public String newReferenceId() {
        return "IGP-" + LocalDateTime.now().format(REFERENCE_DATE) + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    public void loadProcessedReferences() {
        ledger.loadProcessedReferences(transactionManager.inTransaction(processedRequestDao::findAllReferenceIds));
    }

    //

    private List<Transaction> execute(Transaction request, Long accountId, boolean pinRequired, String pin, Supplier<List<Transaction>> work) {
        String rawReference = request.getReferenceId();
        try {
            String reference = validateReference(rawReference);
            request.setReferenceId(reference);
            request.setAmount(Money.requirePositive(request.getAmount()));
            if (accountId == null) {
                throw new InvalidAccountException("Please select an account.");
            }
            if (ledger.isProcessed(reference)) {
                throw new DuplicateTransactionException(reference);
            }
            if (pinRequired) {
                accountService.validatePin(accountId, pin);
            }
            List<Transaction> rows = work.get();
            ledger.markProcessed(reference);
            ledger.recordTransactions(rows);
            return rows;
        } catch (IgirePayException e) {
            if (e instanceof DuplicateTransactionException duplicate) {
                ledger.markProcessed(duplicate.getReferenceId());
            }
            logFailure(new FailedTransaction(rawReference, accountId, request.getTransactionType(), request.getAmount(), e.getMessage()));
            throw e;
        }
    }

    private void claimReference(Connection connection, String reference) throws SQLException {
        if (!processedRequestDao.create(connection, reference)) {
            throw new DuplicateTransactionException(reference);
        }
    }

    private Account lock(Connection connection, Long accountId) throws SQLException {
        return accountDao.findByIdForUpdate(connection, accountId).orElseThrow(() -> InvalidAccountException.notFound(accountId));
    }

    private void save(Connection connection, Account account, List<Transaction> rows) throws SQLException {
        accountDao.updateBalance(connection, account.getId(), account.getBalance());
        for (Transaction row : rows) {
            transactionDao.create(connection, row);
        }
    }

    /** Writes the failure to the database log and the in-memory log. Never hides the original error. */
    private void logFailure(FailedTransaction failure) {
        ledger.recordFailure(failure);
        try {
            transactionManager.inTransaction(connection -> failedTransactionDao.create(connection, failure));
        } catch (RuntimeException ignored) {

        }
    }

    private String validateReference(String referenceId) {
        String reference = referenceId == null ? "" : referenceId.trim();
        if (reference.isEmpty()) {
            throw new ValidationException("Reference ID is required. Every transaction request must have a unique reference ID.");
        }
        if (!REFERENCE_PATTERN.matcher(reference).matches()) {
            throw new ValidationException("Reference ID may contain only letters, digits and . _ : - (max 100 characters).");
        }
        return reference;
    }
}
