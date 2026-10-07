public class Session {
    private static int userId;
    private static String name;
    private static String email;
    private static String role;

    public static void start(int id, String userName, String userEmail, String userRole) {
        userId = id;
        name = userName;
        email = userEmail;
        role = userRole;
    }

    public static int getUserId() {
        return userId;
    }

    public static String getName() {
        return name;
    }

    public static String getEmail() {
        return email;
    }

    public static String getRole() {
        return role;
    }

    public static boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public static void clear() {
        userId = 0;
        name = null;
        email = null;
        role = null;
    }
}
