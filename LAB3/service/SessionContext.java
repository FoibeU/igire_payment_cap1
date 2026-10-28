//package LAB3.service;
//
//import exception.AuthenticationException;
//import LAB1.AppUser;
//
///**
// * Holds the logged-in staff user (desktop/console app: one user per running application).
// * Services call requireAdmin() before admin-only operations (bonus: role-based access).
// */
//public final class SessionContext {
//
//    private static volatile AppUser currentUser;
//
//    private SessionContext() {
//    }
//
//    public static void login(AppUser user) {
//        currentUser = user;
//    }
//
//    public static void logout() {
//        currentUser = null;
//    }
//
//    public static AppUser getCurrentUser() {
//        return currentUser;
//    }
//
//    public static boolean isAdmin() {
//        return currentUser != null && currentUser.isAdmin();
//    }
//
//    public static AppUser requireLoggedIn() {
//        if (currentUser == null) {
//            throw new AuthenticationException("Please log in first.");
//        }
//        return currentUser;
//    }
//
//    public static AppUser requireAdmin() {
//        AppUser user = requireLoggedIn();
//        if (!user.isAdmin()) {
//            throw new AuthenticationException("Only an administrator can do this. You are logged in as '" + user.getUsername() + "' (" + user.getRole() + ").");
//        }
//        return user;
//    }
//}
