package LAB2;

import LAB1.Account;
import LAB1.AccountStatus;
import LAB1.AccountType;
import LAB1.SavingsAccount;
import util.JdbcUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountDaoImpl implements AccountDao {

    private static final String SELECT = "SELECT a.id, a.customer_id, c.full_name AS customer_name, a.account_type, a.balance, a.status, a.pin_hash, a.pin_salt, a.failed_pin_attempts, a.locked, a.created_at, "
            + "(SELECT COUNT(*) FROM transactions t WHERE t.account_id = a.id AND t.transaction_type IN ('WITHDRAWAL', 'TRANSFER_OUT') AND t.created_at >= date_trunc('month', CURRENT_TIMESTAMP)) AS withdrawals_this_month "
            + "FROM accounts a JOIN customers c ON c.id = a.customer_id";
    private static final String INSERT_SQL = "INSERT INTO accounts (customer_id, account_type, balance, status, pin_hash, pin_salt, failed_pin_attempts, locked) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String FIND_BY_ID_SQL = SELECT + " WHERE a.id = ?";
    private static final String FIND_BY_ID_FOR_UPDATE_SQL = SELECT + " WHERE a.id = ? FOR UPDATE OF a";
    private static final String FIND_ALL_SQL = SELECT + " ORDER BY a.id DESC";
    private static final String FIND_BY_CUSTOMER_SQL = SELECT + " WHERE a.customer_id = ? ORDER BY a.id DESC";
    private static final String UPDATE_SQL = "UPDATE accounts SET balance = ?, status = ?, pin_hash = ?, pin_salt = ?, failed_pin_attempts = ?, locked = ? WHERE id = ?";
    private static final String UPDATE_BALANCE_SQL = "UPDATE accounts SET balance = ? WHERE id = ?";
    private static final String DELETE_INACTIVE_SQL = "DELETE FROM accounts WHERE id = ? AND status = 'INACTIVE' AND balance = 0";
    private static final String COUNT_BY_CUSTOMER_SQL = "SELECT COUNT(*) FROM accounts WHERE customer_id = ?";

    @Override
    public Account create(Connection connection, Account account) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, account.getCustomerId());
            statement.setString(2, account.getAccountType().name());
            statement.setBigDecimal(3, account.getBalance());
            statement.setString(4, account.getStatus().name());
            statement.setString(5, account.getPinHash());
            statement.setString(6, account.getPinSalt());
            statement.setInt(7, account.getFailedPinAttempts());
            statement.setBoolean(8, account.isLocked());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    account.setId(keys.getLong("id"));
                    account.setCreatedAt(JdbcUtil.toLocalDateTime(keys.getTimestamp("created_at")));
                }
            }
            return account;
        }
    }

    @Override
    public Optional<Account> findById(Connection connection, Long id) throws SQLException {
        return findOne(connection, FIND_BY_ID_SQL, id);
    }

    @Override
    public List<Account> findAll(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL)) {
            return findMany(statement);
        }
    }

    @Override
    public boolean update(Connection connection, Account account) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setBigDecimal(1, account.getBalance());
            statement.setString(2, account.getStatus().name());
            statement.setString(3, account.getPinHash());
            statement.setString(4, account.getPinSalt());
            statement.setInt(5, account.getFailedPinAttempts());
            statement.setBoolean(6, account.isLocked());
            statement.setLong(7, account.getId());
            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public boolean updateBalance(Connection connection, Long id, BigDecimal balance) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_BALANCE_SQL)) {
            statement.setBigDecimal(1, balance);
            statement.setLong(2, id);
            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public boolean deleteInactive(Connection connection, Long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE_INACTIVE_SQL)) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public Optional<Account> findByIdForUpdate(Connection connection, Long id) throws SQLException {
        return findOne(connection, FIND_BY_ID_FOR_UPDATE_SQL, id);
    }

    @Override
    public List<Account> findByCustomerId(Connection connection, Long customerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_CUSTOMER_SQL)) {
            statement.setLong(1, customerId);
            return findMany(statement);
        }
    }

    @Override
    public int countByCustomerId(Connection connection, Long customerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(COUNT_BY_CUSTOMER_SQL)) {
            statement.setLong(1, customerId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Optional<Account> findOne(Connection connection, String sql, Long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    private List<Account> findMany(PreparedStatement statement) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                accounts.add(mapRow(rs));
            }
        }
        return accounts;
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        Account account = Account.of(AccountType.valueOf(rs.getString("account_type")));
        account.setId(rs.getLong("id"));
        account.setCustomerId(rs.getLong("customer_id"));
        account.setCustomerName(rs.getString("customer_name"));
        account.setBalance(rs.getBigDecimal("balance"));
        account.setStatus(AccountStatus.valueOf(rs.getString("status")));
        account.setPinHash(rs.getString("pin_hash"));
        account.setPinSalt(rs.getString("pin_salt"));
        account.setFailedPinAttempts(rs.getInt("failed_pin_attempts"));
        account.setLocked(rs.getBoolean("locked"));
        account.setCreatedAt(JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
        if (account instanceof SavingsAccount savings) {
            savings.setWithdrawalsThisMonth(rs.getInt("withdrawals_this_month"));
        }
        return account;
    }
}
