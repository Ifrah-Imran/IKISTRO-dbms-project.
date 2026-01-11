package models;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Review for a Product, written by a User.
 * This class maps to the 'REVIEWS' table in the database.
 */
public class Review {
    // The ID of the product being reviewed
    private int productId;

    // The ID of the user who wrote the review
    private int userId;

    // The rating given (e.g., 1 to 5 stars)
    private int rating;

    // The title or summary of the review
    private String title;

    // The main content/body of the review
    private String text;

    // List of tags associated with the review (e.g., "Quality", "Value")
    private List<String> tags;

    /**
     * Default constructor.
     * Initializes the lists.
     */
    public Review() {
        this.tags = new ArrayList<>();
    }

    /**
     * Parameterized constructor.
     * 
     * @param productId The ID of the product.
     * @param userId    The ID of the user.
     * @param rating    The rating score.
     * @param title     The review title.
     * @param text      The review text.
     */
    public Review(int productId, int userId, int rating, String title, String text) {
        this();
        this.productId = productId;
        this.userId = userId;
        this.rating = rating;
        this.title = title;
        this.text = text;
    }

    // --- Getters and Setters ---

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

}
