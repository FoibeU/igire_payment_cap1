package LAB3.service;

import LAB2.AccountDao;
import LAB2.AccountDaoImpl;
//import LAB2.AppUserDao;
//import LAB2.AppUserDaoImpl;
import LAB2.CustomerDao;
import LAB2.CustomerDaoImpl;
import LAB2.FailedTransactionDao;
import LAB2.FailedTransactionDaoImpl;
import LAB2.ProcessedRequestDao;
import LAB2.ProcessedRequestDaoImpl;
import LAB2.TransactionDao;
import LAB2.TransactionDaoImpl;
import util.TransactionManager;



public class AppContext {

    private final TransactionLedger ledger = new TransactionLedger();
    private final CustomerService customerService;
    private final AccountService accountService;
    private final PaymentService paymentService;
    private final ReportService reportService;
//    private final AuthService authService;


    public AppContext() {
        this(new CustomerDaoImpl(), new AccountDaoImpl(), new TransactionDaoImpl(), new ProcessedRequestDaoImpl(), new FailedTransactionDaoImpl(), new TransactionManager());
    }

    public AppContext(CustomerDao customerDao, AccountDao accountDao, TransactionDao transactionDao, ProcessedRequestDao processedRequestDao, FailedTransactionDao failedTransactionDao, TransactionManager transactionManager) {
        customerService = new CustomerServiceImpl(customerDao, transactionManager);
        accountService = new AccountServiceImpl(accountDao, customerDao, transactionManager, ledger);
        paymentService = new PaymentServiceImpl(accountDao, transactionDao, processedRequestDao, failedTransactionDao, accountService, transactionManager, ledger);
        reportService = new ReportServiceImpl(customerDao, accountDao, transactionDao, transactionManager);
//        authService = new AuthServiceImpl(appUserDao, transactionManager);
    }

    public void start() {
        paymentService.loadProcessedReferences();
    }

    public TransactionLedger getLedger() {
        return ledger;
    }

    public CustomerService getCustomerService() {
        return customerService;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public PaymentService getPaymentService() {
        return paymentService;
    }

    public ReportService getReportService() {
        return reportService;
    }

//    public AuthService getAuthService() {
//        return authService;
//    }
}
