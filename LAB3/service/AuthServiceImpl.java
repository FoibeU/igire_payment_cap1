//package LAB3.service;
//
//import LAB2.AppUserDao;
//import exception.AuthenticationException;
//import exception.DataAccessException;
//import exception.ValidationException;
//import LAB1.AppUser;
//import LAB1.Role;
//import util.PasswordHasher;
//import util.TransactionManager;
//
//import java.util.List;
//import java.util.regex.Pattern;
//
///**
// * Staff authentication. Passwords are stored as PBKDF2 hashes with a random salt.
// */
//public class AuthServiceImpl implements AuthService {
//
//    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{3,50}$");
//    private static final String INVALID_LOGIN = "Invalid username or password.";
//
//    private final AppUserDao appUserDao;
//    private final TransactionManager transactionManager;
//
//    public AuthServiceImpl(AppUserDao appUserDao, TransactionManager transactionManager) {
//        this.appUserDao = appUserDao;
//        this.transactionManager = transactionManager;
//    }
//
//    @Override
//    public boolean hasUsers() {
//        return transactionManager.inTransaction(appUserDao::count) > 0;
//    }
//
//    @Override
//    public AppUser createFirstAdmin(String username, String password) {
//        AppUser user = newUser(username, password, Role.ADMIN);
//        AppUser created = transactionManager.inTransaction(connection -> {
//            if (appUserDao.count(connection) > 0) {
//                throw new AuthenticationException("An administrator already exists. Please log in.");
//            }
//            return appUserDao.create(connection, user);
//        });
//        SessionContext.login(created);
//        return created;
//    }
//
//    @Override
//    public AppUser login(String username, String password) {
//        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
//            throw new AuthenticationException(INVALID_LOGIN);
//        }
//        AppUser user = transactionManager.inTransaction(connection -> appUserDao.findByUsername(connection, username.trim())).orElseThrow(() -> new AuthenticationException(INVALID_LOGIN));
//        if (!PasswordHasher.matches(password, user.getPasswordSalt(), user.getPasswordHash())) {
//            throw new AuthenticationException(INVALID_LOGIN);
//        }
//        if (!user.isActive()) {
//            throw new AuthenticationException("User '" + user.getUsername() + "' is deactivated. Contact an administrator.");
//        }
//        SessionContext.login(user);
//        return user;
//    }
//
//    @Override
//    public void logout() {
//        SessionContext.logout();
//    }
//
//    @Override
//    public AppUser createUser(String username, String password, Role role) {
//        SessionContext.requireAdmin();
//        if (role == null) {
//            throw new ValidationException("Please select a role.");
//        }
//        AppUser user = newUser(username, password, role);
//        try {
//            return transactionManager.inTransaction(connection -> {
//                if (appUserDao.findByUsername(connection, user.getUsername()).isPresent()) {
//                    throw new ValidationException("Username '" + user.getUsername() + "' is already taken.");
//                }
//                return appUserDao.create(connection, user);
//            });
//        } catch (DataAccessException e) {
//            if ("23505".equals(e.getSqlState())) {
//                throw new ValidationException("Username '" + user.getUsername() + "' is already taken.");
//            }
//            throw e;
//        }
//    }
//
//    @Override
//    public List<AppUser> getUsers() {
//        SessionContext.requireAdmin();
//        return transactionManager.inTransaction(appUserDao::findAll);
//    }
//
//    @Override
//    public void setUserActive(Long userId, boolean active) {
//        AppUser admin = SessionContext.requireAdmin();
//        if (userId == null) {
//            throw new ValidationException("Please select a user.");
//        }
//        if (!active && userId.equals(admin.getId())) {
//            throw new ValidationException("You cannot deactivate your own user.");
//        }
//        transactionManager.inTransaction(connection -> {
//            AppUser user = appUserDao.findById(connection, userId).orElseThrow(() -> new ValidationException("User #" + userId + " does not exist."));
//            user.setActive(active);
//            return appUserDao.update(connection, user);
//        });
//    }
//
//    @Override
//    public void changePassword(String currentPassword, String newPassword) {
//        AppUser current = SessionContext.requireLoggedIn();
//        if (!PasswordHasher.matches(currentPassword, current.getPasswordSalt(), current.getPasswordHash())) {
//            throw new AuthenticationException("The current password is wrong.");
//        }
//        validatePassword(newPassword);
//        String salt = PasswordHasher.newSalt();
//        current.setPasswordSalt(salt);
//        current.setPasswordHash(PasswordHasher.hash(newPassword, salt));
//        transactionManager.inTransaction(connection -> appUserDao.update(connection, current));
//    }
//
//    private AppUser newUser(String username, String password, Role role) {
//        String name = username == null ? "" : username.trim();
//        if (!USERNAME_PATTERN.matcher(name).matches()) {
//            throw new ValidationException("Username must be 3 to 50 characters: letters, digits, '.', '_' or '-'.");
//        }
//        validatePassword(password);
//        AppUser user = new AppUser(name, role);
//        String salt = PasswordHasher.newSalt();
//        user.setPasswordSalt(salt);
//        user.setPasswordHash(PasswordHasher.hash(password, salt));
//        return user;
//    }
//
//    private void validatePassword(String password) {
//        if (password == null || password.length() < 8 || !password.matches(".*[A-Za-z].*") || !password.matches(".*[0-9].*")) {
//            throw new ValidationException("Password must be at least 8 characters and contain letters and digits.");
//        }
//    }
//}
