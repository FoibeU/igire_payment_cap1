package LAB3.service;

import LAB2.AccountDao;
import LAB2.CustomerDao;
import exception.AccountLockedException;
import exception.InvalidAccountException;
import exception.InvalidPinException;
import exception.ValidationException;
import LAB1.Account;
import LAB1.AccountStatus;
import LAB1.AccountType;
import util.TransactionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
public class AccountServiceImpl implements AccountService {

    private final AccountDao accountDao;
    private final CustomerDao customerDao;
    private final TransactionManager transactionManager;
    private final TransactionLedger ledger;

    public AccountServiceImpl(AccountDao accountDao, CustomerDao customerDao, TransactionManager transactionManager, TransactionLedger ledger) {
        this.accountDao = accountDao;
        this.customerDao = customerDao;
        this.transactionManager = transactionManager;
        this.ledger = ledger;
    }

    @Override
    public Account createAccount(Long customerId, AccountType accountType, String pin) {
        if (customerId == null) {
            throw new ValidationException("Please select a customer.");
        }
        Account account = Account.of(accountType);
        account.setCustomerId(customerId);
        account.setPin(pin);
        Account created = transactionManager.inTransaction(connection -> {
            if (customerDao.findById(connection, customerId).isEmpty()) {
                throw new ValidationException("Customer #" + customerId + " does not exist.");
            }
            Account saved = accountDao.create(connection, account);
            return accountDao.findById(connection, saved.getId()).orElse(saved);
        });
        ledger.registerAccount(created);
        return created;
    }

    @Override
    public Account getAccount(Long accountId) {
        requireId(accountId);
        Account account = transactionManager.inTransaction(connection -> accountDao.findById(connection, accountId)).orElseThrow(() -> InvalidAccountException.notFound(accountId));
        ledger.registerAccount(account);
        return account;
    }

    @Override
    public List<Account> getAllAccounts() {
        List<Account> accounts = transactionManager.inTransaction(accountDao::findAll);
        accounts.forEach(ledger::registerAccount);
        return accounts;
    }

    @Override
    public List<Account> getAccountsByCustomer(Long customerId) {
        if (customerId == null) {
            throw new ValidationException("Please select a customer.");
        }
        List<Account> accounts = transactionManager.inTransaction(connection -> {
            if (customerDao.findById(connection, customerId).isEmpty()) {
                throw new ValidationException("Customer #" + customerId + " does not exist.");
            }
            return accountDao.findByCustomerId(connection, customerId);
        });
        accounts.forEach(ledger::registerAccount);
        return accounts;
    }

    @Override
    public void createPin(Long accountId, String pin) {
        requireId(accountId);
        transactionManager.inTransaction(connection -> {
            Account account = lock(connection, accountId);
            if (account.hasPin()) {
                throw new InvalidPinException("Account #" + accountId + " already has a PIN. Use 'Change PIN' instead.");
            }
            account.setPin(pin);
            return accountDao.update(connection, account);
        });
    }

    @Override
    public void validatePin(Long accountId, String pin) {
        requireId(accountId);
        if (pin == null || pin.isBlank()) {
            throw new InvalidPinException("PIN is required.");
        }
        Account account = transactionManager.inTransaction(connection -> {
            Account locked = lock(connection, accountId);
            if (locked.isLocked()) {
                return locked;
            }
            locked.verifyPin(pin);
            accountDao.update(connection, locked);
            return locked;
        });
        if (account.isLocked()) {
            throw new AccountLockedException(accountId);
        }
        if (account.getFailedPinAttempts() > 0) {
            throw new InvalidPinException("Wrong PIN. " + account.getRemainingPinAttempts() + " attempt(s) left before account #" + accountId + " is locked.");
        }
    }

    @Override
    public void changePin(Long accountId, String oldPin, String newPin) {
        validatePin(accountId, oldPin);
        if (oldPin.equals(newPin)) {
            throw new InvalidPinException("The new PIN must be different from the current PIN.");
        }
        transactionManager.inTransaction(connection -> {
            Account account = lock(connection, accountId);
            account.setPin(newPin);
            return accountDao.update(connection, account);
        });
    }

    @Override
    public void unlockAccount(Long accountId) {
//        SessionContext.requireAdmin();
        requireId(accountId);
        transactionManager.inTransaction(connection -> {
            Account account = lock(connection, accountId);
            account.unlock();
            return accountDao.update(connection, account);
        });
    }

    @Override
    public Account deactivateAccount(Long accountId) {
        requireId(accountId);
        return transactionManager.inTransaction(connection -> {
            Account account = lock(connection, accountId);
            if (account.getStatus() == AccountStatus.INACTIVE) {
                throw new ValidationException("Account #" + accountId + " is already inactive.");
            }
            account.deactivate();
            accountDao.update(connection, account);
            return account;
        });
    }

    @Override
    public Account activateAccount(Long accountId) {
        requireId(accountId);
        return transactionManager.inTransaction(connection -> {
            Account account = lock(connection, accountId);
            account.activate();
            accountDao.update(connection, account);
            return account;
        });
    }

    @Override
    public void deleteInactiveAccount(Long accountId) {
//        SessionContext.requireAdmin();
        requireId(accountId);
        transactionManager.inTransaction(connection -> {
            Account account = lock(connection, accountId);
            if (account.getStatus() != AccountStatus.INACTIVE) {
                throw new ValidationException("Only inactive accounts can be deleted. Close (deactivate) account #" + accountId + " first.");
            }
            if (account.getBalance().signum() != 0) {
                throw new ValidationException("Account #" + accountId + " still has a balance and cannot be deleted.");
            }
            return accountDao.deleteInactive(connection, accountId);
        });
        ledger.removeAccount(accountId);
    }

    private Account lock(Connection connection, Long accountId) throws SQLException {
        return accountDao.findByIdForUpdate(connection, accountId).orElseThrow(() -> InvalidAccountException.notFound(accountId));
    }

    private void requireId(Long accountId) {
        if (accountId == null) {
            throw new InvalidAccountException("Please select an account.");
        }
    }
}
