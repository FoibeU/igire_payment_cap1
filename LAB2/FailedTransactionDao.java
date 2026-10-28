package LAB2;

import LAB1.FailedTransaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO for the "failed_transactions" table (failed transaction log).
 */
public interface FailedTransactionDao {

    // ---- Create
    FailedTransaction create(Connection connection, FailedTransaction failure) throws SQLException;

    // ---- Read
    List<FailedTransaction> findRecent(Connection connection, int limit) throws SQLException;

    // ---- Update
    boolean updateReason(Connection connection, Long id, String reason) throws SQLException;

    // ---- Delete
    boolean delete(Connection connection, Long id) throws SQLException;

    // ---- Custom queries
    List<FailedTransaction> findByReferenceId(Connection connection, String referenceId) throws SQLException;

    int deleteOlderThan(Connection connection, int days) throws SQLException;
}
