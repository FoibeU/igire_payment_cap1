//package LAB1;
//
//import java.time.LocalDateTime;
//import java.util.Objects;
//
///**
// * A staff member who logs in to IgirePay (maps to the "app_users" table). Bonus: role-based access.
// */
//public class AppUser {
//
//    private Long id;
//    private String username;
//    private String passwordHash;
//    private String passwordSalt;
////    private Role role;
//    private boolean active = true;
//    private LocalDateTime createdAt;
//
//    public AppUser() {
//    }
//
////    public AppUser(String username, Role role) {
////        this.username = username;
////        this.role = role;
////    }
//
////    public AppUser(Long id, String username, String passwordHash, String passwordSalt, Role role, boolean active, LocalDateTime createdAt) {
////        this.id = id;
////        this.username = username;
////        this.passwordHash = passwordHash;
////        this.passwordSalt = passwordSalt;
////        this.role = role;
////        this.active = active;
////        this.createdAt = createdAt;
////    }
//
////    public boolean isAdmin() {
////        return role == Role.ADMIN;
////    }
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getUsername() {
//        return username;
//    }
//
//    public void setUsername(String username) {
//        this.username = username;
//    }
//
//    public String getPasswordHash() {
//        return passwordHash;
//    }
//
//    public void setPasswordHash(String passwordHash) {
//        this.passwordHash = passwordHash;
//    }
//
//    public String getPasswordSalt() {
//        return passwordSalt;
//    }
//
//    public void setPasswordSalt(String passwordSalt) {
//        this.passwordSalt = passwordSalt;
//    }
//
//    public Role getRole() {
//        return role;
//    }
//
//    public void setRole(Role role) {
//        this.role = role;
//    }
//
//    public boolean isActive() {
//        return active;
//    }
//
//    public void setActive(boolean active) {
//        this.active = active;
//    }
//
//    public LocalDateTime getCreatedAt() {
//        return createdAt;
//    }
//
//    public void setCreatedAt(LocalDateTime createdAt) {
//        this.createdAt = createdAt;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        if (!(o instanceof AppUser other)) {
//            return false;
//        }
//        return id != null && id.equals(other.id);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hashCode(id);
//    }
//
//    /** Never includes the password hash or salt. */
//    @Override
//    public String toString() {
//        return "AppUser{id=" + id + ", username='" + username + "', role=" + role + ", active=" + active + "}";
//    }
//}
