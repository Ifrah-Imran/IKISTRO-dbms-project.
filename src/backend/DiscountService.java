package backend;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import models.DiscountCode;

public class DiscountService {

    // Validate and get discount code details
    public DiscountCode validateDiscountCode(String code) {
        String query = "SELECT * FROM DISCOUNT_CODES WHERE UPPER(CODE) = UPPER(?) AND IS_ACTIVE = 1";

        try (Connection conn = OracleDB.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, code);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    DiscountCode discount = new DiscountCode();
                    discount.setDiscountId(rs.getInt("DISCOUNT_ID"));
                    discount.setCode(rs.getString("CODE"));
                    discount.setDiscountPercentage(rs.getInt("DISCOUNT_PERCENTAGE"));
                    discount.setExpiryDate(new Date(rs.getTimestamp("EXPIRY_DATE").getTime()));
                    discount.setUsageLimit(rs.getInt("USAGE_LIMIT"));
                    discount.setTimesUsed(rs.getInt("TIMES_USED"));
                    discount.setActive(rs.getBoolean("IS_ACTIVE"));

                    // Check if code is expired
                    if (discount.getExpiryDate().before(new Date())) {
                        return null; // Code expired
                    }

                    // Check if usage limit exceeded
                    if (discount.getUsageLimit() > 0 && discount.getTimesUsed() >= discount.getUsageLimit()) {
                        return null; // Usage limit exceeded
                    }

                    return discount;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Apply discount code (increment usage count)
    public boolean applyDiscountCode(String code) {
        String query = "UPDATE DISCOUNT_CODES SET TIMES_USED = TIMES_USED + 1 WHERE UPPER(CODE) = UPPER(?)";

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = OracleDB.getConnection();
            conn.setAutoCommit(false); // Start transaction

            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, code);
            int rows = pstmt.executeUpdate();

            conn.commit(); // Commit transaction
            return rows > 0;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // Create a new discount code (Admin function)
    public boolean createDiscountCode(String code, int discountPercentage, Date expiryDate, int usageLimit) {
        String query = "INSERT INTO DISCOUNT_CODES (CODE, DISCOUNT_PERCENTAGE, EXPIRY_DATE, USAGE_LIMIT, TIMES_USED, IS_ACTIVE) VALUES (?, ?, ?, ?, 0, 1)";

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = OracleDB.getConnection();
            conn.setAutoCommit(false); // Start transaction

            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, code);
            pstmt.setInt(2, discountPercentage);
            pstmt.setTimestamp(3, new Timestamp(expiryDate.getTime()));
            pstmt.setInt(4, usageLimit);

            int rows = pstmt.executeUpdate();
            conn.commit(); // Commit transaction
            return rows > 0;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // Get all discount codes (active and inactive) for admin view
    public List<DiscountCode> getAllActiveCodes() {
        List<DiscountCode> codes = new ArrayList<>();
        // Query updated to fetch ALL codes so admin can manage them (delete/view
        // inactive)
        String query = "SELECT * FROM DISCOUNT_CODES ORDER BY IS_ACTIVE DESC, EXPIRY_DATE DESC";

        try (Connection conn = OracleDB.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                DiscountCode discount = new DiscountCode();
                discount.setDiscountId(rs.getInt("DISCOUNT_ID"));
                discount.setCode(rs.getString("CODE"));
                discount.setDiscountPercentage(rs.getInt("DISCOUNT_PERCENTAGE"));
                discount.setExpiryDate(new Date(rs.getTimestamp("EXPIRY_DATE").getTime()));
                discount.setUsageLimit(rs.getInt("USAGE_LIMIT"));
                discount.setTimesUsed(rs.getInt("TIMES_USED"));
                discount.setActive(rs.getBoolean("IS_ACTIVE"));
                codes.add(discount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return codes;
    }

    // Delete a discount code
    public boolean deleteDiscountCode(int discountId) {
        String query = "DELETE FROM DISCOUNT_CODES WHERE DISCOUNT_ID = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = OracleDB.getConnection();
            conn.setAutoCommit(false); // Start transaction

            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, discountId);
            int rows = pstmt.executeUpdate();

            conn.commit(); // Commit transaction
            return rows > 0;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
