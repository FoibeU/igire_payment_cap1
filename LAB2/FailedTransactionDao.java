package LAB2;

import LAB1.FailedTransaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface FailedTransactionDao {

    FailedTransaction create(Connection connection, FailedTransaction failure) throws SQLException;

    List<FailedTransaction> findRecent(Connection connection, int limit) throws SQLException;

    boolean updateReason(Connection connection, Long id, String reason) throws SQLException;

    boolean delete(Connection connection, Long id) throws SQLException;

    List<FailedTransaction> findByReferenceId(Connection connection, String referenceId) throws SQLException;

    int deleteOlderThan(Connection connection, int days) throws SQLException;
}
