package LAB3.service;

import LAB1.Account;
import LAB1.AccountType;

import java.util.List;
public interface AccountService {

    Account createAccount(Long customerId, AccountType accountType, String pin);

    Account getAccount(Long accountId);

    List<Account> getAllAccounts();

    List<Account> getAccountsByCustomer(Long customerId);


    void createPin(Long accountId, String pin);
    void validatePin(Long accountId, String pin);

    void changePin(Long accountId, String oldPin, String newPin);
    void unlockAccount(Long accountId);

    Account deactivateAccount(Long accountId);

    Account activateAccount(Long accountId);
    void deleteInactiveAccount(Long accountId);
}
