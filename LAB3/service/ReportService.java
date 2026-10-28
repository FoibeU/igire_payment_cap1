package LAB3.service;

import LAB1.CustomerStatement;
import LAB1.DailySummary;
import LAB1.TransactionFilter;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/**
 * Transaction reports (Lab 3.3).
 */
public interface ReportService {

    /** Exports the transactions matching the filter to a CSV file and returns the number of rows written. */
    int exportTransactionsToCsv(TransactionFilter filter, Path file);

    List<DailySummary> getDailySummary(LocalDate date);

    CustomerStatement getCustomerStatement(Long customerId, LocalDate fromDate, LocalDate toDate);
}
