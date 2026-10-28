//package LAB2;
//
//import LAB1.AppUser;
//
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.List;
//import java.util.Optional;
//
///**
// * DAO for the "app_users" table (staff login, bonus role-based access).
// */
//public interface AppUserDao {
//
//    // ---- Create
//    AppUser create(Connection connection, AppUser user) throws SQLException;
//
//    // ---- Read
//    Optional<AppUser> findById(Connection connection, Long id) throws SQLException;
//
//    List<AppUser> findAll(Connection connection) throws SQLException;
//
//    // ---- Update
//    boolean update(Connection connection, AppUser user) throws SQLException;
//
//    // ---- Delete
//    boolean delete(Connection connection, Long id) throws SQLException;
//
//    // ---- Custom queries
//    Optional<AppUser> findByUsername(Connection connection, String username) throws SQLException;
//
//    long count(Connection connection) throws SQLException;
//}
