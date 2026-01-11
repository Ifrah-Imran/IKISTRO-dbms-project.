package models;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private int productId;
    private String productName;
    private double productPrice;
    private int categoryId;
    private int stockQuantity;
    private String productCode;
    private double averageRating;

    // Fields from MongoDB
    private String image;
    private List<String> tags;
    private String description;
    private List<String> sizes;
    private List<String> colors;

    public Product() {
        this.image = "";
        this.tags = new ArrayList<>();
        this.sizes = new ArrayList<>();
        this.colors = new ArrayList<>();
        this.productCode = "";
        this.averageRating = 0.0;
        this.description = "";
    }

    public Product(int productId, String productName, double productPrice, int categoryId, int stockQuantity) {
        this();
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.categoryId = categoryId;
        this.stockQuantity = stockQuantity;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getSizes() {
        return sizes;
    }

    public void setSizes(List<String> sizes) {
        this.sizes = sizes;
    }

    public List<String> getColors() {
        return colors;
    }

    public void setColors(List<String> colors) {
        this.colors = colors;
    }

    @Override
    public String toString() {
        return productName + " ($" + productPrice + ")";
    }
}
