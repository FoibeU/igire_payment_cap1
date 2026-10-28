package LAB2;

import LAB1.Customer;
import util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of CustomerDao. Only PreparedStatements with ? parameters are used,
 * so user input is never concatenated into SQL (no SQL injection).
 */
public class CustomerDaoImpl implements CustomerDao {

    private static final String COLUMNS = "SELECT id, full_name, email, phone_number FROM customers";
    private static final String INSERT = "INSERT INTO customers (full_name, email, phone_number) VALUES (?, ?, ?)";
    private static final String FIND_BY_ID = COLUMNS + " WHERE id = ?";
    private static final String FIND_ALL = COLUMNS + " ORDER BY id DESC";
    private static final String UPDATE = "UPDATE customers SET full_name = ?, email = ?, phone_number = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM customers WHERE id = ?";
    private static final String FIND_BY_EMAIL = COLUMNS + " WHERE LOWER(email) = LOWER(?)";
    private static final String SEARCH = COLUMNS + " WHERE full_name ILIKE ? OR email ILIKE ? OR phone_number ILIKE ? ORDER BY full_name";

    @Override
    public Customer create(Connection connection, Customer customer) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, customer.getFullName());
            statement.setString(2, customer.getEmail());
            statement.setString(3, customer.getPhoneNumber());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    customer.setId(keys.getLong("id"));
                }
            }
            return customer;
        }
    }

    @Override
    public Optional<Customer> findById(Connection connection, Long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {
            statement.setLong(1, id);
            return findOne(statement);
        }
    }

    @Override
    public List<Customer> findAll(Connection connection) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL)) {
            return findMany(statement);
        }
    }

    @Override
    public boolean update(Connection connection, Customer customer) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE)) {
            statement.setString(1, customer.getFullName());
            statement.setString(2, customer.getEmail());
            statement.setString(3, customer.getPhoneNumber());
            statement.setLong(4, customer.getId());
            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(Connection connection, Long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(DELETE)) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public Optional<Customer> findByEmail(Connection connection, String email) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_EMAIL)) {
            statement.setString(1, email);
            return findOne(statement);
        }
    }

    @Override
    public List<Customer> search(Connection connection, String text) throws SQLException {
        String pattern = JdbcUtil.likeContains(text == null ? "" : text.trim());
        try (PreparedStatement statement = connection.prepareStatement(SEARCH)) {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            return findMany(statement);
        }
    }

    private Optional<Customer> findOne(PreparedStatement statement) throws SQLException {
        try (ResultSet rs = statement.executeQuery()) {
            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
        }
    }

    private List<Customer> findMany(PreparedStatement statement) throws SQLException {
        List<Customer> customers = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                customers.add(mapRow(rs));
            }
        }
        return customers;
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(rs.getLong("id"), rs.getString("full_name"), rs.getString("email"), rs.getString("phone_number"));
    }
}
