package backend;

import java.sql.*;

/**
 * Service class for handling Address-related operations.
 * Allows creating new addresses for users.
 */
public class AddressService {

    /**
     * Creates a new address for a user in the database.
     * 
     * @param userId      The ID of the user.
     * @param addressLine The street address.
     * @param city        The city.
     * @param postalCode  The postal/zip code.
     * @return The ID of the newly created address, or -1 if creation failed.
     */
    public int createAddress(int userId, String addressLine, String city, String postalCode) {
        // Insert query using USER_ID and USER_ADDRESSES table
        String query = "INSERT INTO USER_ADDRESSES (USER_ID, ADDRESS_LINE, CITY, POSTAL_CODE) VALUES (?, ?, ?, ?)";

        try (Connection conn = OracleDB.getConnection();
                // We request the generated key (ADDRESS_ID) back
                PreparedStatement pstmt = conn.prepareStatement(query, new String[] { "ADDRESS_ID" })) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, addressLine);
            pstmt.setString(3, city);
            pstmt.setString(4, postalCode);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                // Retrieve the generated ADDRESS_ID
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
