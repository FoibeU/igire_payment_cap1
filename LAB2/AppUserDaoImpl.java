//package LAB2;
//
//import LAB1.AppUser;
//import LAB1.Role;
//import util.JdbcUtil;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
///**
// * JDBC implementation of AppUserDao.
// */
//public class AppUserDaoImpl implements AppUserDao {
//
//    private static final String COLUMNS = "SELECT id, username, password_hash, password_salt, role, active, created_at FROM app_users";
//    private static final String INSERT_SQL = "INSERT INTO app_users (username, password_hash, password_salt, role, active) VALUES (?, ?, ?, ?, ?)";
//    private static final String FIND_BY_ID_SQL = COLUMNS + " WHERE id = ?";
//    private static final String FIND_ALL_SQL = COLUMNS + " ORDER BY username";
//    private static final String UPDATE_SQL = "UPDATE app_users SET password_hash = ?, password_salt = ?, role = ?, active = ? WHERE id = ?";
//    private static final String DELETE_SQL = "DELETE FROM app_users WHERE id = ?";
//    private static final String FIND_BY_USERNAME_SQL = COLUMNS + " WHERE LOWER(username) = LOWER(?)";
//    private static final String COUNT_SQL = "SELECT COUNT(*) FROM app_users";
//
//    @Override
//    public AppUser create(Connection connection, AppUser user) throws SQLException {
//        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
//            statement.setString(1, user.getUsername());
//            statement.setString(2, user.getPasswordHash());
//            statement.setString(3, user.getPasswordSalt());
//            statement.setString(4, user.getRole().name());
//            statement.setBoolean(5, user.isActive());
//            statement.executeUpdate();
//            try (ResultSet keys = statement.getGeneratedKeys()) {
//                if (keys.next()) {
//                    user.setId(keys.getLong("id"));
//                    user.setCreatedAt(JdbcUtil.toLocalDateTime(keys.getTimestamp("created_at")));
//                }
//            }
//            return user;
//        }
//    }
//
//    @Override
//    public Optional<AppUser> findById(Connection connection, Long id) throws SQLException {
//        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {
//            statement.setLong(1, id);
//            return findOne(statement);
//        }
//    }
//
//    @Override
//    public List<AppUser> findAll(Connection connection) throws SQLException {
//        List<AppUser> users = new ArrayList<>();
//        try (PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL);
//             ResultSet rs = statement.executeQuery()) {
//            while (rs.next()) {
//                users.add(mapRow(rs));
//            }
//        }
//        return users;
//    }
//
//    @Override
//    public boolean update(Connection connection, AppUser user) throws SQLException {
//        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
//            statement.setString(1, user.getPasswordHash());
//            statement.setString(2, user.getPasswordSalt());
//            statement.setString(3, user.getRole().name());
//            statement.setBoolean(4, user.isActive());
//            statement.setLong(5, user.getId());
//            return statement.executeUpdate() == 1;
//        }
//    }
//
//    @Override
//    public boolean delete(Connection connection, Long id) throws SQLException {
//        try (PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {
//            statement.setLong(1, id);
//            return statement.executeUpdate() == 1;
//        }
//    }
//
//    @Override
//    public Optional<AppUser> findByUsername(Connection connection, String username) throws SQLException {
//        try (PreparedStatement statement = connection.prepareStatement(FIND_BY_USERNAME_SQL)) {
//            statement.setString(1, username);
//            return findOne(statement);
//        }
//    }
//
//    @Override
//    public long count(Connection connection) throws SQLException {
//        try (PreparedStatement statement = connection.prepareStatement(COUNT_SQL);
//             ResultSet rs = statement.executeQuery()) {
//            return rs.next() ? rs.getLong(1) : 0;
//        }
//    }
//
//    private Optional<AppUser> findOne(PreparedStatement statement) throws SQLException {
//        try (ResultSet rs = statement.executeQuery()) {
//            return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
//        }
//    }
//
//    private AppUser mapRow(ResultSet rs) throws SQLException {
//        return new AppUser(rs.getLong("id"), rs.getString("username"), rs.getString("password_hash"), rs.getString("password_salt"), Role.valueOf(rs.getString("role")), rs.getBoolean("active"), JdbcUtil.toLocalDateTime(rs.getTimestamp("created_at")));
//    }
//}
