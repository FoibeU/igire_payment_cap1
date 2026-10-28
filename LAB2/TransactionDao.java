package LAB2;

import LAB1.DailySummary;
import LAB1.Transaction;
import LAB1.TransactionFilter;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * DAO for the "transactions" table (the ledger).
 * Ledger rows are immutable in the business flow: services never update amounts or delete rows.
 * update/delete exist for completeness of the DAO (description correction, data clean-up by an admin).
 */
public interface TransactionDao {

    // ---- Create
    Transaction create(Connection connection, Transaction transaction) throws SQLException;

    // ---- Read
    Optional<Transaction> findById(Connection connection, Long id) throws SQLException;

    // ---- Update
    boolean updateDescription(Connection connection, Long id, String description) throws SQLException;

    // ---- Delete
    boolean delete(Connection connection, Long id) throws SQLException;

    // ---- Custom queries
    List<Transaction> findByAccountId(Connection connection, Long accountId) throws SQLException;

    List<Transaction> findByReferenceId(Connection connection, String referenceId) throws SQLException;

    /** Search/filter by account, customer, type, reference text and date range. */
    List<Transaction> search(Connection connection, TransactionFilter filter) throws SQLException;

    /** Number and total amount per transaction type for one day. */
    List<DailySummary> dailySummary(Connection connection, LocalDate date) throws SQLException;
}
