package LAB1;

import java.util.Objects;
public class Customer {

    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;

    public Customer() {
    }

    public Customer(String fullName, String email, String phoneNumber) {
        this(null, fullName, email, phoneNumber);
    }

    public Customer(Long id, String fullName, String email, String phoneNumber) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Customer other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Customer{id=" + id + ", fullName='"
                + fullName + "', email='"
                + email + "', phoneNumber='"
                + phoneNumber + "'}";
    }
}
