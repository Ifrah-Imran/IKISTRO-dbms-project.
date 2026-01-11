package backend;

import models.Review;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for handling Review-related operations.
 * This class interacts with both Oracle DB (for basic rating data) and MongoDB
 * (for rich review content).
 */
public class ReviewService {

    /**
     * Checks if a user has purchased a specific product and the order is
     * completed/delivered.
     * This is used to verify "Verified Purchase" status.
     * 
     * @param userId    The ID of the user.
     * @param productId The ID of the product.
     * @return true if the user has purchased the product, false otherwise.
     */
    public boolean hasUserPurchasedProduct(int userId, int productId) {
        // Join ORDERS and ORDER_ITEMS to find a matching record
        String query = "SELECT COUNT(*) FROM ORDER_ITEMS oi " +
                "JOIN ORDERS o ON oi.ORDER_ID = o.ORDER_ID " +
                "WHERE o.USER_ID = ? AND oi.PRODUCT_ID = ? AND o.ORDER_STATUS IN ('COMPLETED', 'DELIVERED')";

        try (Connection conn = OracleDB.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, productId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // If count > 0, return true
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves all reviews for a specific product from MongoDB.
     * 
     * @param productId The ID of the product.
     * @return A list of Review objects.
     */
    public List<Review> getReviewsForProduct(int productId) {
        List<Review> reviews = new ArrayList<>();
        // Get the MongoDB collection
        MongoCollection<Document> collection = MongoDB.getReviewsCollection();

        // Find documents with matching productId
        for (Document doc : collection.find(Filters.eq("productId", productId))) {
            Review r = new Review();
            r.setProductId(doc.getInteger("productId"));
            r.setUserId(doc.getInteger("userId"));
            r.setRating(doc.getInteger("rating"));
            r.setTitle(doc.getString("title"));
            r.setText(doc.getString("text"));

            // Handle lists (tags, replies) safely
            List<String> tags = doc.getList("tags", String.class);
            if (tags != null)
                r.setTags(tags);

            reviews.add(r);
        }
        return reviews;
    }

    /**
     * Adds a new review to the system.
     * Saves basic info to Oracle DB and full info to MongoDB.
     * 
     * @param review The Review object to add.
     * @return true if successful, false otherwise.
     */
    public boolean addReview(Review review) {
        // 1. Add to SQL (Rating only) - Useful for calculating average ratings quickly
        String query = "INSERT INTO REVIEWS_SQL (USER_ID, PRODUCT_ID, RATING) VALUES (?, ?, ?)";
        try (Connection conn = OracleDB.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, review.getUserId());
            pstmt.setInt(2, review.getProductId());
            pstmt.setInt(3, review.getRating());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                // 2. Add to MongoDB (Full details including text, images, etc.)
                saveReviewToMongo(review);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Helper method to save a review to MongoDB.
     * 
     * @param review The Review object to save.
     */
    private void saveReviewToMongo(Review review) {
        MongoCollection<Document> collection = MongoDB.getReviewsCollection();

        Document doc = new Document("productId", review.getProductId())
                .append("userId", review.getUserId()) // Changed from customerId
                .append("rating", review.getRating())
                .append("title", review.getTitle())
                .append("text", review.getText())
                .append("tags", review.getTags());

        collection.insertOne(doc);
    }
}
