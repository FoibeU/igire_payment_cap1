package LAB2;

import LAB1.Customer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * DAO for the "customers" table. Every method takes the Connection from the LAB3.service layer,
 * so several DAO calls can share one database transaction.
 */
public interface CustomerDao {

    Customer create(Connection connection, Customer customer) throws SQLException;

    Optional<Customer> findById(Connection connection, Long id) throws SQLException;

    List<Customer> findAll(Connection connection) throws SQLException;

    boolean update(Connection connection, Customer customer) throws SQLException;

    boolean delete(Connection connection, Long id) throws SQLException;

    Optional<Customer> findByEmail(Connection connection, String email) throws SQLException;

    List<Customer> search(Connection connection, String text) throws SQLException;
}
