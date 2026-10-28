package LAB2;

import LAB1.FailedTransaction;
import LAB1.TransactionType;
import util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC implementation of FailedTransactionDao.
 */
public class FailedTransactionDaoImpl implements FailedTransactionDao {

    private static final String COLUMNS = "SELECT id, reference_id, account_id, transaction_type, amount, reason, failed_at FROM failed_transactions";
    private static final String INSERT_SQL = "INSERT INTO failed_transactions (reference_id, account_id, transaction_type, amount, reason) VALUES (?, ?, ?, ?, ?)";
    private static final String FIND_RECENT_SQL = COLUMNS + " ORDER BY failed_at DESC, id DESC LIMIT ?";
    private static final String UPDATE_REASON_SQL = "UPDATE failed_transactions SET reason = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM failed_transactions WHERE id = ?";
    private static final String FIND_BY_REFERENCE_SQL = COLUMNS + " WHERE reference_id = ? ORDER BY failed_at DESC";
    private static final String DELETE_OLDER_THAN_SQL = "DELETE FROM failed_transactions WHERE failed_at < CURRENT_TIMESTAMP - make_interval(days => ?)";

    @Override
    public FailedTransaction create(Connection connection, FailedTransaction failure) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, truncate(failure.getReferenceId(), 100));
            JdbcUtil.setNullableLong(statement, 2, failure.getAccountId());
            statement.setString(3, failure.getTransactionType() == null ? null : failure.getTransactionType().name());
            statement.setBigDecimal(4, failure.getAmount());
            statement.setString(5, truncate(failure.getReason() == null ? "Unknown error" : failure.getReason(), 500));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    failure.setId(keys.getLong("id"));
                    failure.setFailedAt(JdbcUtil.toLocalDateTime(keys.getTimestamp("failed_at")));
                }
            }
            return failure;
        }
    }

    @Override
    public List<FailedTransaction> findRecent(Connection connection, int limit) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_RECENT_SQL)) {
            statement.setInt(1, Math.max(1, limit));
            return findMany(statement);
        }
    }

    @Override
    public boolean updateReason(Connection connection, Long id, String reason) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_REASON_SQL)) {
            statement.setString(1, truncate(reason, 500));
            statement.setLong(2, id);
            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(Connection connection, Long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public List<FailedTransaction> findByReferenceId(Connection connection, String referenceId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_REFERENCE_SQL)) {
            statement.setString(1, referenceId);
            return findMany(statement);
        }
    }

    @Override
    public int deleteOlderThan(Connection connection, int days) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_OLDER_THAN_SQL)) {
            statement.setInt(1, days);
            return statement.executeUpdate();
        }
    }

    private List<FailedTransaction> findMany(PreparedStatement statement) throws SQLException {
        List<FailedTransaction> rows = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                String type = rs.getString("transaction_type");
                rows.add(new FailedTransaction(rs.getLong("id"), rs.getString("reference_id"), JdbcUtil.getNullableLong(rs, "account_id"), type == null ? null : TransactionType.valueOf(type), rs.getBigDecimal("amount"), rs.getString("reason"), JdbcUtil.toLocalDateTime(rs.getTimestamp("failed_at"))));
            }
        }
        return rows;
    }

    private String truncate(String text, int max) {
        return text == null || text.length() <= max ? text : text.substring(0, max);
    }
}
