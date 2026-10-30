package LAB2;

import LAB1.DailySummary;
import LAB1.Transaction;
import LAB1.TransactionFilter;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionDao {

    Transaction create(Connection connection, Transaction transaction) throws SQLException;

    Optional<Transaction> findById(Connection connection, Long id) throws SQLException;

    boolean updateDescription(Connection connection, Long id, String description) throws SQLException;

    boolean delete(Connection connection, Long id) throws SQLException;

    List<Transaction> findByAccountId(Connection connection, Long accountId) throws SQLException;

    List<Transaction> findByReferenceId(Connection connection, String referenceId) throws SQLException;

    List<Transaction> search(Connection connection, TransactionFilter filter) throws SQLException;

    List<DailySummary> dailySummary(Connection connection, LocalDate date) throws SQLException;
}
