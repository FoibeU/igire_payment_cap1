package LAB2;

import LAB1.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;


public interface AccountDao {
    Account create(Connection connection, Account account) throws SQLException;

    Optional<Account> findById(Connection connection, Long id) throws SQLException;

    List<Account> findAll(Connection connection) throws SQLException;

    boolean update(Connection connection, Account account) throws SQLException;

    boolean updateBalance(Connection connection, Long id, BigDecimal balance) throws SQLException;
    boolean deleteInactive(Connection connection, Long id) throws SQLException;

    Optional<Account> findByIdForUpdate(Connection connection, Long id) throws SQLException;

    List<Account> findByCustomerId(Connection connection, Long customerId) throws SQLException;

    int countByCustomerId(Connection connection, Long customerId) throws SQLException;
}
