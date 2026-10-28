package util;

import exception.DataAccessException;
import exception.DatabaseConnectionException;

import java.sql.Connection;
import java.sql.SQLException;
public class TransactionManager {


    @FunctionalInterface
    public interface SqlWork<T> {
        T execute(Connection connection) throws SQLException;
    }

    public <T> T inTransaction(SqlWork<T> work) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                T result = work.execute(connection);
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException e) {
                rollbackQuietly(connection);
                throw e;
            }
        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().startsWith("08")) {
                throw new DatabaseConnectionException("Lost the connection to PostgreSQL: " + e.getMessage(), e);
            }
            throw new DataAccessException("Database error: " + e.getMessage(), e);
        }
    }

    private void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ignored) {
            // the original error is more useful than a rollback failure
        }
    }
}
