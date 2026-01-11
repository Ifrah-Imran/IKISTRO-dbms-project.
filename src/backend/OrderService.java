package backend;

import models.CartItem;
import models.Order;
import models.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for handling Order-related database operations.
 * This includes creating orders, retrieving orders, and updating order status.
 */
public class OrderService {

    /**
     * Creates a new order in the database.
     * This method involves multiple steps (inserting order, inserting items,
     * updating stock)
     * and uses a transaction to ensure all steps complete successfully or none at
     * all.
     * 
     * @param order The Order object containing all order details.
     * @return true if the order was created successfully, false otherwise.
     */
    public boolean createOrder(Order order) {
        Connection conn = null;
        try {
            conn = OracleDB.getConnection();
            conn.setAutoCommit(false); // Start transaction (disable auto-commit)

            // 1. Insert into ORDERS table
            // We use USER_ID instead of CUSTOMER_ID now.
            String orderQuery = "INSERT INTO ORDERS (USER_ID, ADDRESS_ID, DISCOUNT_CODE, ORDER_STATUS, TOTAL_AMOUNT, CONTACT_NAME, CONTACT_PHONE, CONTACT_EMAIL, PAYMENT_METHOD) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            int orderId = -1;

            // We request the generated keys (ORDER_ID) back from the database
            try (PreparedStatement pstmt = conn.prepareStatement(orderQuery, new String[] { "ORDER_ID" })) {
                pstmt.setInt(1, order.getUserId());

                // Handle optional Address ID
                if (order.getAddressId() > 0)
                    pstmt.setInt(2, order.getAddressId());
                else
                    pstmt.setNull(2, Types.INTEGER);

                pstmt.setString(3, order.getDiscountCode());
                pstmt.setString(4, "PENDING"); // Initial status
                pstmt.setDouble(5, order.getTotalAmount());

                // Contact and Payment details
                pstmt.setString(6, order.getContactName());
                pstmt.setString(7, order.getContactPhone());
                pstmt.setString(8, order.getContactEmail());
                pstmt.setString(9, order.getPaymentMethod());

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    // Retrieve the generated ORDER_ID
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next())
                            orderId = rs.getInt(1);
                    }
                }
            }

            // If order insertion failed, rollback and return false
            if (orderId == -1) {
                conn.rollback();
                return false;
            }

            // 2. Insert into ORDER_ITEMS and Update INVENTORY
            String itemQuery = "INSERT INTO ORDER_ITEMS (ORDER_ID, PRODUCT_ID, QUANTITY, PRICE_AT_PURCHASE) VALUES (?, ?, ?, ?)";
            // Note: This assumes a single warehouse for simplicity
            String updateStockQuery = "UPDATE INVENTORY SET STOCK_QUANTITY = STOCK_QUANTITY - ? WHERE PRODUCT_ID = ? AND WAREHOUSE_ID = (SELECT WAREHOUSE_ID FROM WAREHOUSES WHERE ROWNUM = 1)";

            try (PreparedStatement itemStmt = conn.prepareStatement(itemQuery);
                    PreparedStatement stockStmt = conn.prepareStatement(updateStockQuery)) {

                for (CartItem item : order.getItems()) {
                    // Prepare batch for Order Items
                    itemStmt.setInt(1, orderId);
                    itemStmt.setInt(2, item.getProduct().getProductId());
                    itemStmt.setInt(3, item.getQuantity());
                    itemStmt.setDouble(4, item.getProduct().getProductPrice());
                    itemStmt.addBatch();

                    // Prepare batch for Stock Update
                    stockStmt.setInt(1, item.getQuantity());
                    stockStmt.setInt(2, item.getProduct().getProductId());
                    stockStmt.addBatch();
                }

                // Execute all batched statements
                itemStmt.executeBatch();
                stockStmt.executeBatch();
            }

            // Commit the transaction (save all changes)
            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            // If any error occurs, rollback the transaction (undo changes)
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            // Restore auto-commit and close connection
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Retrieves all orders for a specific user.
     * 
     * @param userId The ID of the user.
     * @return A list of Order objects.
     */
    public List<Order> getOrdersForUser(int userId) {
        List<Order> orders = new ArrayList<>();
        // Query using USER_ID
        String query = "SELECT * FROM ORDERS WHERE USER_ID = ? ORDER BY ORDER_DATE DESC";

        try (Connection conn = OracleDB.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Order o = new Order();
                    o.setOrderId(rs.getInt("ORDER_ID"));
                    o.setUserId(rs.getInt("USER_ID")); // Changed from CUSTOMER_ID
                    o.setAddressId(rs.getInt("ADDRESS_ID"));
                    o.setDiscountCode(rs.getString("DISCOUNT_CODE"));
                    o.setOrderDate(rs.getTimestamp("ORDER_DATE"));
                    o.setOrderStatus(rs.getString("ORDER_STATUS"));
                    o.setTotalAmount(rs.getDouble("TOTAL_AMOUNT"));

                    // Try to get optional columns (handle potential missing columns in older
                    // schemas)
                    try {
                        o.setContactName(rs.getString("CONTACT_NAME"));
                        o.setContactPhone(rs.getString("CONTACT_PHONE"));
                        o.setContactEmail(rs.getString("CONTACT_EMAIL"));
                        o.setPaymentMethod(rs.getString("PAYMENT_METHOD"));
                    } catch (SQLException ignore) {
                        // Ignore if columns don't exist
                    }
                    orders.add(o);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * Retrieves all orders in the system (for Admin).
     * 
     * @return A list of all Order objects.
     */
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String query = "SELECT * FROM ORDERS ORDER BY ORDER_DATE DESC";

        try (Connection conn = OracleDB.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Order o = new Order();
                o.setOrderId(rs.getInt("ORDER_ID"));
                o.setUserId(rs.getInt("USER_ID")); // Changed from CUSTOMER_ID
                o.setAddressId(rs.getInt("ADDRESS_ID"));
                o.setDiscountCode(rs.getString("DISCOUNT_CODE"));
                o.setOrderDate(rs.getTimestamp("ORDER_DATE"));
                o.setOrderStatus(rs.getString("ORDER_STATUS"));
                o.setTotalAmount(rs.getDouble("TOTAL_AMOUNT"));
                try {
                    o.setContactName(rs.getString("CONTACT_NAME"));
                    o.setContactPhone(rs.getString("CONTACT_PHONE"));
                    o.setContactEmail(rs.getString("CONTACT_EMAIL"));
                    o.setPaymentMethod(rs.getString("PAYMENT_METHOD"));
                } catch (SQLException ignore) {
                }
                orders.add(o);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    /**
     * Updates the status of an order.
     * 
     * @param orderId The ID of the order to update.
     * @param status  The new status (e.g., "Shipped").
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateOrderStatus(int orderId, String status) {
        String query = "UPDATE ORDERS SET ORDER_STATUS = ? WHERE ORDER_ID = ?";
        try (Connection conn = OracleDB.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, orderId);

            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
