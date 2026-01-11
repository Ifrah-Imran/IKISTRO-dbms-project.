package models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents an Order placed by a User.
 * This class maps to the 'ORDERS' table in the database.
 */
public class Order {
    // Unique identifier for the order
    private int orderId;

    // The ID of the user who placed the order
    private int userId;

    // The ID of the address where the order should be shipped
    private int addressId;

    // The discount code applied to the order (if any)
    private String discountCode;

    // The date and time when the order was placed
    private Date orderDate;

    // The current status of the order (e.g., "Pending", "Shipped")
    private String orderStatus;

    // The total cost of the order
    private double totalAmount;

    // The list of items included in the order
    private List<CartItem> items;

    // Additional contact/shipping/payment details
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String paymentMethod;

    /**
     * Default constructor.
     * Initializes the list of items.
     */
    public Order() {
        this.items = new ArrayList<>();
    }

    // --- Getters and Setters ---

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getAddressId() {
        return addressId;
    }

    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }

    public String getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
