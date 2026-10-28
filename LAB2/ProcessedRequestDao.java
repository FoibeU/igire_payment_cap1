package LAB2;

import LAB1.ProcessedRequest;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * DAO for the "processed_requests" table (idempotency of payment requests).
 */
public interface ProcessedRequestDao {

    // ---- Create
    /**
     * Records the reference as processed, atomically.
     *
     * @return true if it was recorded now, false if this reference already exists (duplicate)
     */
    boolean create(Connection connection, String referenceId) throws SQLException;

    // ---- Read
    Optional<ProcessedRequest> findByReferenceId(Connection connection, String referenceId) throws SQLException;

    List<ProcessedRequest> findAll(Connection connection, int limit) throws SQLException;

    // ---- Update
    /** Refreshes processed_at (e.g. after a manual reconciliation). */
    boolean touch(Connection connection, String referenceId) throws SQLException;

    // ---- Delete
    boolean delete(Connection connection, String referenceId) throws SQLException;

    // ---- Custom queries
    boolean exists(Connection connection, String referenceId) throws SQLException;

    List<String> findAllReferenceIds(Connection connection) throws SQLException;

    /** Housekeeping: removes references older than the given number of days. */
    int deleteOlderThan(Connection connection, int days) throws SQLException;
}
