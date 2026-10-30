package LAB2;

import LAB1.DailySummary;
import LAB1.Transaction;
import LAB1.TransactionFilter;
import LAB1.TransactionType;
import util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TransactionDaoImpl implements TransactionDao {

    private static final String COLUMNS = "SELECT t.id, t.account_id, t.reference_id, t.transaction_type, t.amount, t.description, t.created_at FROM transactions t";
    private static final String INSERT_SQL = "INSERT INTO transactions (account_id, reference_id, transaction_type, amount, description) VALUES (?, ?, ?, ?, ?)";
    private static final String FIND_BY_ID_SQL = COLUMNS + " WHERE t.id = ?";
    private static final String UPDATE_DESCRIPTION_SQL = "UPDATE transactions SET description = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM transactions WHERE id = ?";
    private static final String FIND_BY_ACCOUNT_SQL = COLUMNS + " WHERE t.account_id = ? ORDER BY t.created_at DESC, t.id DESC";
    private static final String FIND_BY_REFERENCE_SQL = COLUMNS + " WHERE t.reference_id = ? ORDER BY t.id";
    private static final String DAILY_SUMMARY_SQL = "SELECT transaction_type, COUNT(*) AS cnt, COALESCE(SUM(amount), 0) AS total FROM transactions WHERE created_at >= ? AND created_at < ? GROUP BY transaction_type ORDER BY transaction_type";

    @Override
    public Transaction create(Connection connection, Transaction transaction) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            JdbcUtil.setNullableLong(statement, 1, transaction.getAccountId());
            statement.setString(2, transaction.getReferenceId());
            statement.setString(3, transaction.getTransactionType().name());
            statement.setBigDecimal(4, transaction.getAmount());
            statement.setString(5, truncate(transaction.getDescription(), 255));
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    transaction.setId(keys.getLong("id"));
                    transaction.setTimestamp(JdbcUtil.toLocalDateTime(keys.getTimestamp("created_at")));
                }
            }
            return transaction;
        }
    }

    @Override
    public Optional<Transaction> findById(Connection connection, Long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
            statement.setLong(1, id);
            List<Transaction> rows = findMany(statement);
            return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
        }
    }

    @Override
    public boolean updateDescription(Connection connection, Long id, String description) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_DESCRIPTION_SQL)) {
            statement.setString(1, truncate(description, 255));
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
    public List<Transaction> findByAccountId(Connection connection, Long accountId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ACCOUNT_SQL)) {
            statement.setLong(1, accountId);
            return findMany(statement);
        }
    }

    @Override
    public List<Transaction> findByReferenceId(Connection connection, String referenceId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_REFERENCE_SQL)) {
            statement.setString(1, referenceId);
            return findMany(statement);
        }
    }

    @Override
    public List<Transaction> search(Connection connection, TransactionFilter filter) throws SQLException {
        StringBuilder sql = new StringBuilder(COLUMNS);
        List<Object> parameters = new ArrayList<>();
        if (filter.getCustomerId() != null) {
            sql.append(" JOIN accounts a ON a.id = t.account_id");
        }
        sql.append(" WHERE 1 = 1");
        if (filter.getCustomerId() != null) {
            sql.append(" AND a.customer_id = ?");
            parameters.add(filter.getCustomerId());
        }
        if (filter.getAccountId() != null) {
            sql.append(" AND t.account_id = ?");
            parameters.add(filter.getAccountId());
        }
        if (filter.getTransactionType() != null) {
            sql.append(" AND t.transaction_type = ?");
            parameters.add(filter.getTransactionType().name());
        }
        if (filter.getReferenceContains() != null && !filter.getReferenceContains().isBlank()) {
            sql.append(" AND t.reference_id ILIKE ?");
            parameters.add(JdbcUtil.likeContains(filter.getReferenceContains().trim()));
        }
        if (filter.getFromDate() != null) {
            sql.append(" AND t.created_at >= ?");
            parameters.add(Timestamp.valueOf(filter.getFromDate().atStartOfDay()));
        }
        if (filter.getToDate() != null) {
            sql.append(" AND t.created_at < ?");
            parameters.add(Timestamp.valueOf(filter.getToDate().plusDays(1).atStartOfDay()));
        }
        sql.append(" ORDER BY t.created_at DESC, t.id DESC LIMIT ?");
        parameters.add(Math.max(1, filter.getLimit()));

        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }
            return findMany(statement);
        }
    }

    @Override
    public List<DailySummary> dailySummary(Connection connection, LocalDate date) throws SQLException {
        List<DailySummary> lines = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(DAILY_SUMMARY_SQL)) {
            statement.setTimestamp(1, Timestamp.valueOf(date.atStartOfDay()));
            statement.setTimestamp(2, Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    lines.add(new DailySummary(date, TransactionType.valueOf(rs.getString("transaction_type")), rs.getLong("cnt"), rs.getBigDecimal("total")));
                }
            }
        }
        return lines;
    }

    private List<Transaction> findMany(PreparedStatement statement) throws SQLException {
        List<Transaction> rows = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                rows.add(new Transaction(rs.getLong("id"), JdbcUtil.getNullableLong(rs, "account_id"), rs.getString("reference_id"), rs.getBigDecimal("amount"), TransactionType.valueOf(rs.getString("transaction_type")), JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")), rs.getString("description")));
            }
        }
        return rows;
    }

    private String truncate(String text, int max) {
        return text == null || text.length() <= max ? text : text.substring(0, max);
    }
}
