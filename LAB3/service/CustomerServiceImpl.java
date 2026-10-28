package LAB3.service;

import LAB2.CustomerDao;
import exception.DataAccessException;
import exception.ValidationException;
import LAB1.Customer;
import util.TransactionManager;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
public class CustomerServiceImpl implements CustomerService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,15}$");
    private static final String FOREIGN_KEY_VIOLATION = "23503";
    private static final String UNIQUE_VIOLATION = "23505";

    private final CustomerDao customerDao;
    private final TransactionManager transactionManager;

    public CustomerServiceImpl(CustomerDao customerDao, TransactionManager transactionManager) {
        this.customerDao = customerDao;
        this.transactionManager = transactionManager;
    }

    @Override
    public Customer registerCustomer(Customer customer) {
        normalize(customer);
        validate(customer);
        try {
            return transactionManager.inTransaction(connection -> {
                if (customerDao.findByEmail(connection, customer.getEmail()).isPresent()) {
                    throw new ValidationException("A customer with email '" + customer.getEmail() + "' already exists.");
                }
                return customerDao.create(connection, customer);
            });
        } catch (DataAccessException e) {
            throw translate(e, customer);
        }
    }

    @Override
    public Customer updateCustomer(Customer customer) {
        if (customer.getId() == null) {
            throw new ValidationException("Please select a customer to update.");
        }
        normalize(customer);
        validate(customer);
        try {
            return transactionManager.inTransaction(connection -> {
                Optional<Customer> sameEmail = customerDao.findByEmail(connection, customer.getEmail());
                if (sameEmail.isPresent() && !sameEmail.get().getId().equals(customer.getId())) {
                    throw new ValidationException("Another customer already uses email '" + customer.getEmail() + "'.");
                }
                if (!customerDao.update(connection, customer)) {
                    throw new ValidationException("Customer #" + customer.getId() + " does not exist.");
                }
                return customer;
            });
        } catch (DataAccessException e) {
            throw translate(e, customer);
        }
    }

    @Override
    public void deleteCustomer(Long customerId) {
        if (customerId == null) {
            throw new ValidationException("Please select a customer to delete.");
        }
        try {
            boolean deleted = transactionManager.inTransaction(connection -> customerDao.delete(connection, customerId));
            if (!deleted) {
                throw new ValidationException("Customer #" + customerId + " does not exist.");
            }
        } catch (DataAccessException e) {
            if (FOREIGN_KEY_VIOLATION.equals(e.getSqlState())) {
                throw new ValidationException("Customer #" + customerId + " still has accounts. Close and delete the accounts first.");
            }
            throw e;
        }
    }

    @Override
    public Customer getCustomer(Long customerId) {
        if (customerId == null) {
            throw new ValidationException("Customer ID is required.");
        }
        return transactionManager.inTransaction(connection -> customerDao.findById(connection, customerId)).orElseThrow(() -> new ValidationException("Customer #" + customerId + " does not exist."));
    }

    @Override
    public List<Customer> getAllCustomers() {
        return transactionManager.inTransaction(customerDao::findAll);
    }

    @Override
    public List<Customer> searchCustomers(String text) {
        if (text == null || text.isBlank()) {
            return getAllCustomers();
        }
        return transactionManager.inTransaction(connection -> customerDao.search(connection, text));
    }

    private RuntimeException translate(DataAccessException e, Customer customer) {
        if (UNIQUE_VIOLATION.equals(e.getSqlState())) {
            return new ValidationException("A customer with email '" + customer.getEmail() + "' already exists.");
        }
        return e;
    }

    private void normalize(Customer customer) {
        customer.setFullName(trim(customer.getFullName()).replaceAll("\\s+", " "));
        customer.setEmail(trim(customer.getEmail()).toLowerCase());
        customer.setPhoneNumber(trim(customer.getPhoneNumber()).replaceAll("[\\s-]", ""));
    }

    private void validate(Customer customer) {
        if (customer.getFullName().isEmpty()) {
            throw new ValidationException("Full name is required.");
        }
        if (customer.getFullName().length() > 150) {
            throw new ValidationException("Full name must not exceed 150 characters.");
        }
        if (customer.getEmail().isEmpty()) {
            throw new ValidationException("Email is required.");
        }
        if (customer.getEmail().length() > 150 || !EMAIL_PATTERN.matcher(customer.getEmail()).matches()) {
            throw new ValidationException("Email format is invalid (example: john@example.com).");
        }
        if (customer.getPhoneNumber().isEmpty()) {
            throw new ValidationException("Phone number is required.");
        }
        if (!PHONE_PATTERN.matcher(customer.getPhoneNumber()).matches()) {
            throw new ValidationException("Phone number must be 10 to 15 digits, optionally starting with + (example: 0788123456 or +250788123456).");
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
