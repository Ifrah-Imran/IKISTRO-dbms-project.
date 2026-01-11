package models;

/**
 * Represents a User in the system.
 * This class maps to the 'USERS' table in the database.
 * It holds information about the user such as their ID, email, password, name,
 * and role.
 */
public class User {
    // Unique identifier for the user (Primary Key in database)
    private int userId;

    // User's email address, used for login
    private String email;

    // User's password (should be hashed in a real application, but plain text for
    // this demo)
    private String password;

    // User's full name
    private String name;

    // User's role (e.g., "CUSTOMER", "ADMIN")
    private String role;

    /**
     * Default constructor.
     * Creates an empty User object.
     */
    public User() {
    }

    /**
     * Parameterized constructor.
     * Used to create a User object with specific details.
     * 
     * @param userId   The unique ID of the user.
     * @param email    The user's email address.
     * @param password The user's password.
     * @param name     The user's name.
     * @param role     The user's role (e.g., "CUSTOMER", "ADMIN").
     */
    public User(int userId, String email, String password, String name, String role) {
        this.userId = userId;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    // --- Getters and Setters ---
    // These methods allow other parts of the program to access and modify the
    // private fields.

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
