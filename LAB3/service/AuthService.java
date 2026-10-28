//package LAB3.service;
//
//import LAB1.AppUser;
////import LAB1.Role;
//
//import java.util.List;
//
///**
// * Staff login and user management (bonus: role-based access).
// */
//public interface AuthService {
//
//    boolean hasUsers();
//
//    /** Creates the first administrator. Only allowed while there are no users at all. */
//    AppUser createFirstAdmin(String username, String password);
//
//    /** Checks the credentials and stores the user in SessionContext. */
//    AppUser login(String username, String password);
//
//    void logout();
//
//    /** Admin only. */
////    AppUser createUser(String username, String password, Role role);
//
//    /** Admin only. */
//    List<AppUser> getUsers();
//
//    /** Admin only: activate or deactivate a user (an admin cannot deactivate himself). */
//    void setUserActive(Long userId, boolean active);
//
//    void changePassword(String currentPassword, String newPassword);
//}
