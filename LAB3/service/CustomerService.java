package LAB3.service;

import LAB1.Customer;

import java.util.List;
public interface CustomerService {

    Customer registerCustomer(Customer customer);

    Customer updateCustomer(Customer customer);

    void deleteCustomer(Long customerId);

    Customer getCustomer(Long customerId);

    List<Customer> getAllCustomers();

    List<Customer> searchCustomers(String text);
}
