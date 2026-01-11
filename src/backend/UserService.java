package backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import models.User;

/**
 * Service class for handling User-related database operations.
 * This includes logging in, signing up, and checking user roles.
 */
public class UserService {

    /**
     * Attempts to log in a user with the given email and password.
     * 
     * @param email    The email provided by the user.
     * @param password The password provided by the user.
     * @return A User object if login is successful, or null if it fails.
     */
    public User login(String email, String password) {
        // SQL query to select a user with the matching email and password
        // We use the 'USERS' table now.
        String query = "SELECT * FROM USERS WHERE EMAIL = ? AND PASSWORD = ?";

        // Try-with-resources to automatically close Connection and PreparedStatement
        try (Connection conn = OracleDB.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Set the parameters for the query (replace ? with actual values)
            pstmt.setString(1, email);
            pstmt.setString(2, password);

            // Execute the query and get the result set
            try (ResultSet rs = pstmt.executeQuery()) {
                // If a row is returned, it means the user exists and credentials are correct
                if (rs.next()) {
                    // Create and return a new User object with data from the database
                    return new User(
                            rs.getInt("USER_ID"), // Get integer from USER_ID column
                            rs.getString("EMAIL"), // Get string from EMAIL column
                            rs.getString("PASSWORD"), // Get string from PASSWORD column
                            rs.getString("NAME"), // Get string from NAME column
                            rs.getString("ROLE")); // Get string from ROLE column
                }
            }
        } catch (SQLException e) {
            // Print any database errors to the console
            e.printStackTrace();
        }
        // Return null if login failed (user not found or error occurred)
        return null;
    }

    /**
     * Registers a new user in the database.
     * 
     * @param user The User object containing the new user's details.
     * @return true if registration was successful, false otherwise.
     */
    public boolean signUp(User user) {
        // SQL query to insert a new row into the USERS table
        String query = "INSERT INTO USERS (EMAIL, PASSWORD, NAME, ROLE) VALUES (?, ?, ?, ?)";

        try (Connection conn = OracleDB.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            // Set the values for the new user
            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getName());
            pstmt.setString(4, "CUSTOMER"); // Default role is CUSTOMER

            // Execute the update (INSERT) and check how many rows were affected
            int rows = pstmt.executeUpdate();

            // If rows > 0, the insertion was successful
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if a user has the "ADMIN" role.
     * 
     * @param user The User object to check.
     * @return true if the user is an admin, false otherwise.
     */
    public boolean isAdmin(User user) {
        // Check if user is not null and their role is "ADMIN" (ignoring case)
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }
}
