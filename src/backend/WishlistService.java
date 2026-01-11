package backend;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Product;

/**
 * Service class for handling Wishlist operations.
 * Allows users to add, remove, and view products in their wishlist.
 */
public class WishlistService {

    /**
     * Retrieves the list of products in a user's wishlist.
     * 
     * @param userId The ID of the user.
     * @return A list of Product objects.
     */
    public List<Product> getWishlistForUser(int userId) {
        List<Product> wishlist = new ArrayList<>();
        // Query to get all product IDs in the wishlist for this user
        String query = "SELECT PRODUCT_ID FROM WISHLISTS WHERE USER_ID = ?";

        try (Connection conn = OracleDB.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                ProductService ps = new ProductService();
                while (rs.next()) {
                    // For each product ID, fetch the full Product details
                    int pid = rs.getInt("PRODUCT_ID");
                    Product p = ps.getProductById(pid);
                    if (p != null)
                        wishlist.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wishlist;
    }

    /**
     * Adds a product to the user's wishlist.
     * Checks if the product is already in the wishlist before adding.
     * 
     * @param userId    The ID of the user.
     * @param productId The ID of the product.
     * @return true if added successfully (or already exists), false otherwise.
     */
    public boolean addToWishlist(int userId, int productId) {
        // Check if already exists
        String check = "SELECT COUNT(*) FROM WISHLISTS WHERE USER_ID = ? AND PRODUCT_ID = ?";
        // Insert query
        String insert = "INSERT INTO WISHLISTS (USER_ID, PRODUCT_ID) VALUES (?, ?)";

        try (Connection conn = OracleDB.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(check)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true; // Already exists, so we consider it "added"
                }
            }

            // If not exists, insert it
            try (PreparedStatement ins = conn.prepareStatement(insert)) {
                ins.setInt(1, userId);
                ins.setInt(2, productId);
                int rows = ins.executeUpdate();
                return rows > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Removes a product from the user's wishlist.
     * 
     * @param userId    The ID of the user.
     * @param productId The ID of the product to remove.
     * @return true if removed successfully, false otherwise.
     */
    public boolean removeFromWishlist(int userId, int productId) {
        String delete = "DELETE FROM WISHLISTS WHERE USER_ID = ? AND PRODUCT_ID = ?";
        try (Connection conn = OracleDB.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(delete)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, productId);

            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
