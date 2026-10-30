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


    boolean create(Connection connection, String referenceId) throws SQLException;

    Optional<ProcessedRequest> findByReferenceId(Connection connection, String referenceId) throws SQLException;

    List<ProcessedRequest> findAll(Connection connection, int limit) throws SQLException;

    boolean touch(Connection connection, String referenceId) throws SQLException;

    boolean delete(Connection connection, String referenceId) throws SQLException;

    boolean exists(Connection connection, String referenceId) throws SQLException;

    List<String> findAllReferenceIds(Connection connection) throws SQLException;

    int deleteOlderThan(Connection connection, int days) throws SQLException;
}
