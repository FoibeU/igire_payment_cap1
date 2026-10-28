package LAB3.service;

import LAB1.CustomerStatement;
import LAB1.DailySummary;
import LAB1.TransactionFilter;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public interface ReportService {
    int exportTransactionsToCsv(TransactionFilter filter, Path file);

    List<DailySummary> getDailySummary(LocalDate date);

    CustomerStatement getCustomerStatement(Long customerId, LocalDate fromDate, LocalDate toDate);
}
