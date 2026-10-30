package LAB3;


import exception.AuthenticationException;
import exception.DataAccessException;
import exception.DatabaseConnectionException;
import exception.DuplicateTransactionException;
import exception.IgirePayException;
import exception.ValidationException;
import LAB1.Account;
import LAB1.AccountType;
//import LAB1.AppUser;
import LAB1.Customer;
import LAB1.CustomerStatement;
import LAB1.DailySummary;
import LAB1.FailedTransaction;
import LAB1.Money;
//import LAB1.Role;
import LAB1.SavingsAccount;
import LAB1.Transaction;
import LAB1.TransactionFilter;
import LAB1.TransactionType;
import LAB3.service.AccountService;
import LAB3.service.AppContext;
//import LAB3.service.AuthService;
import LAB3.service.CustomerService;
import LAB3.service.PaymentService;
import LAB3.service.ReportService;
//import LAB3.service.SessionContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
public class ConsoleApp {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FunctionalInterface
    private interface Action {
        void run();
    }

    private final ConsoleIO io;
    private final AppContext context;
    private final CustomerService customerService;
    private final AccountService accountService;
    private final PaymentService paymentService;
    private final ReportService reportService;
//    private final AuthService authService;

    public ConsoleApp(AppContext context, ConsoleIO io) {
        this.context = context;
        this.io = io;
        this.customerService = context.getCustomerService();
        this.accountService = context.getAccountService();
        this.paymentService = context.getPaymentService();
        this.reportService = context.getReportService();
//        this.authService = context.getAuthService();
    }

    public static void main(String[] args) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        ConsoleIO io = new ConsoleIO(in, out, System.console());
        int exitCode = new ConsoleApp(new AppContext(), io).run();
        System.exit(exitCode);
    }

    /** @return 0 on normal exit, 1 when the database is not reachable */
    public int run() {
        io.println("=================================================================");
        io.println("  IgirePay - Wallet & Payment Management System (console)");
        io.println("=================================================================");
        try {
            context.start();
        } catch (DatabaseConnectionException e) {
            io.error("Cannot start: " + e.getMessage());
            return 1;
        }
        try {
//            if (!login()) {
//                return 0;
//            }
            mainMenu();
        } catch (ConsoleIO.EndOfInputException e) {
            io.println();
        }
//        authService.logout();
        io.println("Goodbye.");
        return 0;
    }

    // ===================================================== LOGIN (bonus: roles)

//    private boolean login() {
//        if (!authService.hasUsers()) {
//            io.header("First start: create the administrator account");
//            while (true) {
//                try {
//                    String username = io.readRequired("Admin username: ");
//                    String password = io.readSecret("Admin password (min 8, letters + digits): ");
//                    String repeat = io.readSecret("Repeat password: ");
//                    if (!password.equals(repeat)) {
//                        throw new ValidationException("The passwords do not match.");
//                    }
//                    AppUser admin = authService.createFirstAdmin(username, password);
//                    io.success("Administrator '" + admin.getUsername() + "' created. You are logged in.");
//                    return true;
//                } catch (IgirePayException e) {
//                    io.error(e.getMessage());
//                }
//            }
//        }
//        for (int attempt = 1; attempt <= 3; attempt++) {
//            try {
//                io.header("Login");
//                String username = io.readRequired("Username: ");
//                String password = io.readSecret("Password: ");
//                AppUser user = authService.login(username, password);
//                io.success("Welcome " + user.getUsername() + " (" + user.getRole() + ").");
//                return true;
//            } catch (IgirePayException e) {
//                io.error(e.getMessage() + (attempt < 3 ? " (" + (3 - attempt) + " attempt(s) left)" : ""));
//            }
//        }
//        io.error("Too many failed logins.");
//        return false;
//    }

    // Services Menu

    private void mainMenu() {
        while (true) {
//            AppUser user = SessionContext.getCurrentUser();
            io.header("Main menu - ");
            io.println("1. Customer management");
            io.println("2. Account management");
            io.println("3. Transaction management");
            io.println("4. Reports");
//            io.println("5. Users & password");
            io.println("0. Exit");
            switch (io.readLine("Choose: ")) {
                case "1" -> customerMenu();
                case "2" -> accountMenu();
                case "3" -> transactionMenu();
                case "4" -> reportMenu();
//                case "5" -> userMenu();
                case "0" -> {
                    return;
                }
                default -> io.error("Unknown option.");
            }
        }
    }

    private void customerMenu() {
        while (true) {
            io.header("Customer management");
            io.println("1. Register customer");
            io.println("2. Update customer information");
            io.println("3. View customer accounts");
            io.println("4. List / search customers");
            io.println("5. Delete customer (no accounts)");
            io.println("0. Back");
            String choice = io.readLine("Choose: ");
            switch (choice) {
                case "1" -> safely(this::registerCustomer);
                case "2" -> safely(this::updateCustomer);
                case "3" -> safely(this::viewCustomerAccounts);
                case "4" -> safely(this::listCustomers);
                case "5" -> safely(this::deleteCustomer);
                case "0" -> {
                    return;
                }
                default -> io.error("Unknown option.");
            }
        }
    }

    private void accountMenu() {
        while (true) {
            io.header("Account management");
            io.println("1. Create wallet account");
            io.println("2. Create savings account");
            io.println("3. View account balance");
            io.println("4. List all accounts");
            io.println("5. Create PIN (accounts without a PIN)");
            io.println("6. Change PIN");
            io.println("7. Close account (make inactive)");
            io.println("8. Reactivate account");
            io.println("9. Delete inactive account (admin)");
            io.println("10. Unlock account (admin)");
            io.println("0. Back");
            switch (io.readLine("Choose: ")) {
                case "1" -> safely(() -> createAccount(AccountType.WALLET));
                case "2" -> safely(() -> createAccount(AccountType.SAVINGS));
                case "3" -> safely(this::viewBalance);
                case "4" -> safely(() -> printAccounts(accountService.getAllAccounts()));
                case "5" -> safely(this::createPin);
                case "6" -> safely(this::changePin);
                case "7" -> safely(this::closeAccount);
                case "8" -> safely(this::reactivateAccount);
//                case "9" -> safely(this::deleteInactiveAccount);
                case "10" -> safely(this::unlockAccount);
                case "0" -> {
                    return;
                }
                default -> io.error("Unknown option.");
            }
        }
    }

    private void transactionMenu() {
        while (true) {
            io.header("Transaction management");
            io.println("Every request needs a unique reference ID. Re-using a reference is rejected (duplicate protection).");
            io.println("1. Deposit money");
            io.println("2. Withdraw money");
            io.println("3. Transfer money");
            io.println("4. View transaction history of an account");
            io.println("5. Search / filter transactions");
            io.println("6. Check whether a reference ID was already processed");
            io.println("0. Back");
            switch (io.readLine("Choose: ")) {
                case "1" -> safely(this::deposit);
                case "2" -> safely(this::withdraw);
                case "3" -> safely(this::transfer);
                case "4" -> safely(this::history);
                case "5" -> safely(this::searchTransactions);
                case "6" -> safely(this::checkReference);
                case "0" -> {
                    return;
                }
                default -> io.error("Unknown option.");
            }
        }
    }

    private void reportMenu() {
        while (true) {
            io.header("Reports");
            io.println("1. Export transaction history to CSV(Not yet done) ");
            io.println("2. Daily transaction summary");
            io.println("3. Customer transaction statement");
            io.println("4. Failed transaction log (admin)");
//            io.println("5. In-memory records (Set of processed references, failed log of this session)");
            io.println("0. Back");
            switch (io.readLine("Choose: ")) {
                case "1" -> safely(this::exportCsv);
                case "2" -> safely(this::dailySummary);
                case "3" -> safely(this::customerStatement);
                case "4" -> safely(this::failedLog);
//                case "5" -> safely(this::inMemoryRecords);
                case "0" -> {
                    return;
                }
                default -> io.error("Unknown option.");
            }
        }
    }

//    private void userMenu() {
//        while (true) {
//            io.header("Users & password");
//            io.println("1. Change my password");
//            io.println("2. List users (admin)");
//            io.println("3. Create user (admin)");
//            io.println("4. Activate / deactivate user (admin)");
//            io.println("0. Back");
//            switch (io.readLine("Choose: ")) {
//                case "1" -> safely(this::changePassword);
//                case "2" -> safely(this::listUsers);
//                case "3" -> safely(this::createUser);
//                case "4" -> safely(this::toggleUser);
//                case "0" -> {
//                    return;
//                }
//                default -> io.error("Unknown option.");
//            }
//        }
//    }

    /** Lab 3.2: one place that turns every exception type into a clear message. */
    private void safely(Action action) {
        try {
            action.run();
        } catch (DuplicateTransactionException e) {
            io.error("DUPLICATE TRANSACTION - " + e.getMessage());
        } catch (AuthenticationException e) {
            io.error("Not allowed - " + e.getMessage());
        } catch (ValidationException e) {
            io.error(e.getMessage());
        } catch (DatabaseConnectionException e) {
            io.error("Database connection failure - " + e.getMessage());
        } catch (DataAccessException e) {
            io.error("SQL error - " + e.getMessage() + (e.getSqlState() == null ? "" : " (SQLSTATE " + e.getSqlState() + ")"));
        } catch (IgirePayException e) {
            io.error(e.getMessage());
        } catch (ConsoleIO.EndOfInputException e) {
            throw e;
        } catch (RuntimeException e) {
            io.error("Unexpected error: " + e);
        }
    }

    // ===================================================== CUSTOMERS

    private void registerCustomer() {
        String name = io.readLine("Full name: ");
        String email = io.readLine("Email: ");
        String phone = io.readLine("Phone number: ");
        Customer customer = customerService.registerCustomer(new Customer(name, email, phone));
        io.success("Customer registered with ID " + customer.getId() + ".");
    }

    private void updateCustomer() {
        Customer customer = customerService.getCustomer(io.readId("Customer ID: "));
        io.println("Press Enter to keep the current value.");
        String name = io.readLine("Full name [" + customer.getFullName() + "]: ");
        String email = io.readLine("Email [" + customer.getEmail() + "]: ");
        String phone = io.readLine("Phone number [" + customer.getPhoneNumber() + "]: ");
        if (!name.isEmpty()) {
            customer.setFullName(name);
        }
        if (!email.isEmpty()) {
            customer.setEmail(email);
        }
        if (!phone.isEmpty()) {
            customer.setPhoneNumber(phone);
        }
        customerService.updateCustomer(customer);
        io.success("Customer #" + customer.getId() + " updated.");
    }

    private void viewCustomerAccounts() {
        Long customerId = io.readId("Customer ID: ");
        Customer customer = customerService.getCustomer(customerId);
        io.println("Accounts of " + customer.getFullName() + " (" + customer.getEmail() + "):");
        printAccounts(accountService.getAccountsByCustomer(customerId));
    }

    private void listCustomers() {
        String text = io.readLine("Search text (Enter = all): ");
        List<List<String>> rows = new ArrayList<>();
        for (Customer c : customerService.searchCustomers(text)) {
            rows.add(List.of(String.valueOf(c.getId()), c.getFullName(), c.getEmail(), c.getPhoneNumber()));
        }
        io.table(List.of("ID", "Full name", "Email", "Phone"), rows);
    }

    private void deleteCustomer() {
        Long customerId = io.readId("Customer ID: ");
        Customer customer = customerService.getCustomer(customerId);
        if (io.confirm("Delete customer '" + customer.getFullName() + "'?")) {
            customerService.deleteCustomer(customerId);
            io.success("Customer deleted.");
        }
    }

    // ===================================================== ACCOUNTS

    private void createAccount(AccountType type) {
        Long customerId = io.readId("Customer ID: ");
        Customer customer = customerService.getCustomer(customerId);
        io.println("Creating a " + type + " account for " + customer.getFullName() + ".");
        String pin = readNewPin();
        Account account = accountService.createAccount(customerId, type, pin);
        io.success(type + " account #" + account.getId() + " created (balance 0.00).");
        if (account instanceof SavingsAccount) {
            io.println("Savings rules: min deposit " + Money.format(SavingsAccount.MIN_DEPOSIT)
                    + ", max " + SavingsAccount.MAX_WITHDRAWALS_PER_MONTH
                    + " withdrawals/month, max " + Money.format(SavingsAccount.MAX_SINGLE_WITHDRAWAL)
                    + " per withdrawal, interst will be 2% (min " + Money.format(SavingsAccount.MIN_INTEREST)
                    + "), transfers only to own accounts.");
        } else {
            io.println("Wallet rules: instant transfers to any account, no fees, balance up to 5,000,000.00.");
        }
    }

    private void viewBalance() {
        Account account = accountService.getAccount(io.readId("Account ID: "));
        io.println("Account   : #" + account.getId() + " (" + account.getAccountType() + ")");
        io.println("Owner     : " + account.getCustomerName());
        io.println("Balance   : " + Money.format(account.getBalance()) + " FRW");
        io.println("Status    : " + account.getStatus() + (account.isLocked() ? " - LOCKED (wrong PIN)" : ""));
        io.println("PIN       : " + (account.hasPin() ? "set" : "NOT SET - create one in Account management"));
        if (account instanceof SavingsAccount savings) {
            io.println("Withdrawals left this month: " + savings.getRemainingWithdrawalsThisMonth());
        }
    }

    private void createPin() {
        Long accountId = io.readId("Account ID: ");
        accountService.createPin(accountId, readNewPin());
        io.success("PIN created for account #" + accountId + ".");
    }

    private void changePin() {
        Long accountId = io.readId("Account ID: ");
        String oldPin = io.readSecret("Current PIN: ");
        String newPin = readNewPin();
        accountService.changePin(accountId, oldPin, newPin);
        io.success("PIN changed for account #" + accountId + ".");
    }

    private void closeAccount() {
        Long accountId = io.readId("Account ID: ");
        if (io.confirm("Close account #" + accountId + "? (balance must be 0)")) {
            accountService.deactivateAccount(accountId);
            io.success("Account #" + accountId + " is now inactive.");
        }
    }

    private void reactivateAccount() {
        Long accountId = io.readId("Account ID: ");
        accountService.activateAccount(accountId);
        io.success("Account #" + accountId + " is active again.");
    }

//    private void deleteInactiveAccount() {
//        SessionContext.requireAdmin();
//        Long accountId = io.readId("Inactive account ID: ");
//        if (io.confirm("Permanently delete account #" + accountId + "? Its transaction history is kept.")) {
//            accountService.deleteInactiveAccount(accountId);
//            io.success("Account #" + accountId + " deleted.");
//        }
//    }

    private void unlockAccount() {
//        SessionContext.requireAdmin();
        Long accountId = io.readId("Account ID: ");
        accountService.unlockAccount(accountId);
        io.success("Account #" + accountId + " unlocked. Failed PIN attempts reset.");
    }

    private String readNewPin() {
        String pin = io.readSecret("New PIN (4-6 digits): ");
        String repeat = io.readSecret("Repeat PIN: ");
        if (!pin.equals(repeat)) {
            throw new ValidationException("The PINs do not match.");
        }
        return pin;
    }

    private void printAccounts(List<Account> accounts) {
        List<List<String>> rows = new ArrayList<>();
        for (Account a : accounts) {
            rows.add(List.of(String.valueOf(a.getId()), a.getAccountType().toString(), a.getCustomerName(), Money.format(a.getBalance()), a.getStatus().toString(), a.isLocked() ? "LOCKED" : (a.hasPin() ? "PIN set" : "no PIN"), a.getCreatedAt() == null ? "" : a.getCreatedAt().format(DATE_TIME)));
        }
        io.table(List.of("ID", "Type", "Customer", "Balance", "Status", "Security", "Created"), rows);
    }

    // ===================================================== TRANSACTIONS

    private String readReference() {
        String suggestion = paymentService.newReferenceId();
        String reference = io.readLine("Reference ID [Enter = " + suggestion + "]: ");
        return reference.isEmpty() ? suggestion : reference;
    }

    private void deposit() {
        Long accountId = io.readId("Account ID: ");
        BigDecimal amount = io.readAmount("Amount (FRW): ");
        String reference = readReference();
        List<Transaction> rows = paymentService.deposit(reference, accountId, amount);
        printResult("Deposit", reference, rows, accountId);
    }

    private void withdraw() {
        Long accountId = io.readId("Account ID: ");
        BigDecimal amount = io.readAmount("Amount (FRW): ");
        String reference = readReference();
        String pin = io.readSecret("Account PIN: ");
        List<Transaction> rows = paymentService.withdraw(reference, accountId, amount, pin);
        printResult("Withdrawal", reference, rows, accountId);
    }

    private void transfer() {
        Long fromId = io.readId("From account ID: ");
        Long toId = io.readId("To account ID: ");
        BigDecimal amount = io.readAmount("Amount (FRW): ");
        String reference = readReference();
        String pin = io.readSecret("PIN of account #" + fromId + ": ");
        List<Transaction> rows = paymentService.transfer(reference, fromId, toId, amount, pin);
        printResult("Transfer", reference, rows, fromId);
    }

    private void printResult(String operation, String reference, List<Transaction> rows, Long accountId) {
        io.success(operation + " processed. Reference: " + reference.trim());
        printTransactions(rows);
        Account account = accountService.getAccount(accountId);
        io.println("New balance of account #" + accountId + ": " + Money.format(account.getBalance()) + " FRW");
    }

    private void history() {
        Long accountId = io.readId("Account ID: ");
        Account account = accountService.getAccount(accountId);
        io.println("Account #" + accountId + " (" + account.getAccountType() + ", " + account.getCustomerName() + ") - balance " + Money.format(account.getBalance()));
        printTransactions(paymentService.getTransactionHistory(accountId));
    }

    private void searchTransactions() {
        io.println("Leave a field empty to ignore it.");
        TransactionFilter filter = readFilter();
        printTransactions(paymentService.searchTransactions(filter));
    }

    private TransactionFilter readFilter() {
        TransactionFilter filter = new TransactionFilter();
        String account = io.readLine("Account ID: ");
        if (!account.isEmpty()) {
            filter.setAccountId(parseId(account));
        }
        String customer = io.readLine("Customer ID: ");
        if (!customer.isEmpty()) {
            filter.setCustomerId(parseId(customer));
        }
        String type = io.readLine("Type (DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT, FEE): ");
        if (!type.isEmpty()) {
            try {
                filter.setTransactionType(TransactionType.valueOf(type.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Unknown transaction type '" + type + "'.");
            }
        }
        String reference = io.readLine("Reference contains: ");
        filter.setReferenceContains(reference.isEmpty() ? null : reference);
        filter.setFromDate(io.readDate("From date (yyyy-MM-dd)", null));
        filter.setToDate(io.readDate("To date (yyyy-MM-dd)", null));
        return filter;
    }

    private void checkReference() {
        String reference = io.readRequired("Reference ID: ");
        io.println(paymentService.isProcessed(reference) ? "Reference '" + reference + "' WAS already processed. Re-sending it will be rejected." : "Reference '" + reference + "' has NOT been processed yet.");
    }

    private void printTransactions(List<Transaction> transactions) {
        List<List<String>> rows = new ArrayList<>();
        for (Transaction t : transactions) {
            rows.add(List.of(String.valueOf(t.getId()), t.getTimestamp() == null ? "" : t.getTimestamp().format(DATE_TIME), t.getReferenceId(), t.getAccountId() == null ? "(deleted)" : "#" + t.getAccountId(), t.getTransactionType().toString(), (t.getTransactionType().isCredit() ? "+" : "-") + Money.format(t.getAmount()), t.getDescription() == null ? "" : t.getDescription()));
        }
        io.table(List.of("ID", "Time", "Reference", "Account", "Type", "Amount", "Description"), rows);
    }

    // ===================================================== REPORTS

    private void exportCsv() {
        io.println("Choose which transactions to export (empty = all).");
        TransactionFilter filter = readFilter();
        String fileName = io.readLine("File name [transactions.csv]: ");
        Path file = Path.of(fileName.isEmpty() ? "transactions.csv" : fileName);
        int count = reportService.exportTransactionsToCsv(filter, file);
        io.success(count + " transaction(s) exported to " + file.toAbsolutePath());
    }

    private void dailySummary() {
        LocalDate date = io.readDate("Date (yyyy-MM-dd)", LocalDate.now());
        List<DailySummary> lines = reportService.getDailySummary(date);
        io.println("Daily summary for " + date + ":");
        List<List<String>> rows = new ArrayList<>();
        BigDecimal credits = BigDecimal.ZERO;
        BigDecimal debits = BigDecimal.ZERO;
        long count = 0;
        for (DailySummary line : lines) {
            rows.add(List.of(line.getTransactionType().toString(), String.valueOf(line.getCount()), Money.format(line.getTotalAmount())));
            count += line.getCount();
            if (line.getTransactionType().isCredit()) {
                credits = credits.add(line.getTotalAmount());
            } else {
                debits = debits.add(line.getTotalAmount());
            }
        }
        io.table(List.of("Type", "Count", "Total (FRW)"), rows);
        io.println("Transactions: " + count + " | money in: " + Money.format(credits) + " | money out: " + Money.format(debits) + " | net: " + Money.format(credits.subtract(debits)));
    }

    private void customerStatement() {
        Long customerId = io.readId("Customer ID: ");
        LocalDate to = io.readDate("To date (yyyy-MM-dd)", LocalDate.now());
        LocalDate from = io.readDate("From date (yyyy-MM-dd)", to.withDayOfMonth(1));
        CustomerStatement statement = reportService.getCustomerStatement(customerId, from, to);
        io.header("STATEMENT - " + statement.getCustomer().getFullName());
        io.println("Customer : #" + statement.getCustomer().getId() + " " + statement.getCustomer().getFullName() + " | " + statement.getCustomer().getEmail() + " | " + statement.getCustomer().getPhoneNumber());
        io.println("Period   : " + statement.getFromDate() + " to " + statement.getToDate());
        io.println("Accounts :");
        printAccounts(statement.getAccounts());
        io.println("Transactions:");
        printTransactions(statement.getTransactions());
        io.println("Total credits: " + Money.format(statement.getTotalCredits()) + " | total debits: " + Money.format(statement.getTotalDebits()) + " | net: " + Money.format(statement.getNetMovement()) + " | current total balance: " + Money.format(statement.getTotalBalance()));
    }

    private void failedLog() {
        List<List<String>> rows = new ArrayList<>();
        for (FailedTransaction f : paymentService.getFailedTransactions(50)) {
            rows.add(List.of(f.getFailedAt() == null ? "" : f.getFailedAt().format(DATE_TIME), f.getReferenceId() == null ? "" : f.getReferenceId(), f.getAccountId() == null ? "" : "#" + f.getAccountId(), f.getTransactionType() == null ? "" : f.getTransactionType().toString(), f.getAmount() == null ? "" : Money.format(f.getAmount()), f.getReason()));
        }
        io.table(List.of("Time", "Reference", "Account", "Type", "Amount", "Reason"), rows);
    }

    private void inMemoryRecords() {
        io.println("Processed reference IDs in the in-memory Set: " + context.getLedger().getProcessedReferenceIds().size());
        io.println("Transactions processed in this session (List): " + context.getLedger().getTransactionHistory().size());
        io.println("Failed requests in this session (List): " + context.getLedger().getFailedTransactionLog().size());
        context.getLedger().getFailedTransactionLog().forEach(f -> io.println("  - " + f.getReferenceId() + ": " + f.getReason()));
        io.println("Customers with accounts loaded in the Map: " + context.getLedger().getCustomerAccounts().keySet());
    }

    // ===================================================== USERS

//    private void changePassword() {
//        String current = io.readSecret("Current password: ");
//        String newPassword = io.readSecret("New password (min 8, letters + digits): ");
//        String repeat = io.readSecret("Repeat new password: ");
//        if (!newPassword.equals(repeat)) {
//            throw new ValidationException("The passwords do not match.");
//        }
//        authService.changePassword(current, newPassword);
//        io.success("Password changed.");
//    }

//    private void listUsers() {
//        List<List<String>> rows = new ArrayList<>();
//        for (AppUser u : authService.getUsers()) {
//            rows.add(List.of(String.valueOf(u.getId()), u.getUsername(), u.getRole().toString(), u.isActive() ? "active" : "inactive"));
//        }
//        io.table(List.of("ID", "Username", "Role", "Status"), rows);
//    }

//    private void createUser() {
//        SessionContext.requireAdmin();
//        String username = io.readRequired("Username: ");
//        String password = io.readSecret("Password (min 8, letters + digits): ");
//        String roleText = io.readLine("Role (ADMIN/USER) [USER]: ");
//        Role role;
//        try {
//            role = roleText.isEmpty() ? Role.USER : Role.valueOf(roleText.toUpperCase());
//        } catch (IllegalArgumentException e) {
//            throw new ValidationException("Role must be ADMIN or USER.");
//        }
//        AppUser user = authService.createUser(username, password, role);
//        io.success("User '" + user.getUsername() + "' created with role " + user.getRole() + ".");
//    }

//    private void toggleUser() {
//        SessionContext.requireAdmin();
//        Long userId = io.readId("User ID: ");
//        boolean activate = io.confirm("Activate the user? (n = deactivate)");
//        authService.setUserActive(userId, activate);
//        io.success("User #" + userId + (activate ? " activated." : " deactivated."));
//    }

    private Long parseId(String text) {
        try {
            return Long.valueOf(text.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("'" + text + "' is not a valid ID.");
        }
    }
}
