package LAB3.service;

import LAB2.AccountDao;
import LAB2.CustomerDao;
import LAB2.TransactionDao;
import exception.IgirePayException;
import exception.ValidationException;
import LAB1.Account;
import LAB1.Customer;
import LAB1.CustomerStatement;
import LAB1.DailySummary;
import LAB1.Transaction;
import LAB1.TransactionFilter;
import util.TransactionManager;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Reports: CSV export, daily summary and customer statements.
 */
public class ReportServiceImpl implements ReportService {

    private static final DateTimeFormatter CSV_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int MAX_EXPORT_ROWS = 100_000;

    private final CustomerDao customerDao;
    private final AccountDao accountDao;
    private final TransactionDao transactionDao;
    private final TransactionManager transactionManager;

    public ReportServiceImpl(CustomerDao customerDao, AccountDao accountDao, TransactionDao transactionDao, TransactionManager transactionManager) {
        this.customerDao = customerDao;
        this.accountDao = accountDao;
        this.transactionDao = transactionDao;
        this.transactionManager = transactionManager;
    }

    @Override
    public int exportTransactionsToCsv(TransactionFilter filter, Path file) {
        if (file == null) {
            throw new ValidationException("Please choose a file to export to.");
        }
        TransactionFilter criteria = filter == null ? new TransactionFilter() : filter;
        criteria.setLimit(MAX_EXPORT_ROWS);
        List<Transaction> rows = transactionManager.inTransaction(connection -> transactionDao.search(connection, criteria));
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                writer.write('﻿'); // BOM, so Excel opens UTF-8 correctly
                writer.write("transaction_id,timestamp,reference_id,account_id,transaction_type,direction,amount,description");
                writer.newLine();
                for (Transaction t : rows) {
                    writer.write(String.join(",",
                            String.valueOf(t.getId()),
                            t.getTimestamp() == null ? "" : t.getTimestamp().format(CSV_DATE_TIME),
                            csvText(t.getReferenceId()),
                            t.getAccountId() == null ? "" : String.valueOf(t.getAccountId()),
                            t.getTransactionType().name(),
                            t.getTransactionType().isCredit() ? "CREDIT" : "DEBIT",
                            t.getAmount().toPlainString(),
                            csvText(t.getDescription())));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new IgirePayException("Could not write the CSV file " + file + ": " + e.getMessage(), e);
        }
        return rows.size();
    }

    @Override
    public List<DailySummary> getDailySummary(LocalDate date) {
        LocalDate day = date == null ? LocalDate.now() : date;
        return transactionManager.inTransaction(connection -> transactionDao.dailySummary(connection, day));
    }

    @Override
    public CustomerStatement getCustomerStatement(Long customerId, LocalDate fromDate, LocalDate toDate) {
        if (customerId == null) {
            throw new ValidationException("Please select a customer.");
        }
        LocalDate to = toDate == null ? LocalDate.now() : toDate;
        LocalDate from = fromDate == null ? to.withDayOfMonth(1) : fromDate;
        if (from.isAfter(to)) {
            throw new ValidationException("'From' date must be before or equal to 'To' date.");
        }
        return transactionManager.inTransaction(connection -> {
            Customer customer = customerDao.findById(connection, customerId).orElseThrow(() -> new ValidationException("Customer #" + customerId + " does not exist."));
            List<Account> accounts = accountDao.findByCustomerId(connection, customerId);
            List<Transaction> transactions = transactionDao.search(connection, TransactionFilter.forCustomer(customerId, from, to));
            return new CustomerStatement(customer, from, to, accounts, transactions);
        });
    }

    /** Quotes a CSV text cell and neutralises spreadsheet formulas (CSV injection). */
    private String csvText(String value) {
        if (value == null) {
            return "";
        }
        String safe = value;
        if (!safe.isEmpty() && "=+-@\t\r".indexOf(safe.charAt(0)) >= 0) {
            safe = "'" + safe;
        }
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
}
