package backend;

import models.Product;
import models.ProductKit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

public class ProductKitService {

    public boolean createKit(ProductKit kit) {
        Connection conn = null;
        try {
            conn = OracleDB.getConnection();
            conn.setAutoCommit(false);

            String kitQuery = "INSERT INTO PRODUCT_KITS (KIT_NAME, DISCOUNT_PERCENTAGE) VALUES (?, ?)";
            int kitId = -1;

            try (PreparedStatement pstmt = conn.prepareStatement(kitQuery, new String[] { "KIT_ID" })) {
                pstmt.setString(1, kit.getKitName());
                pstmt.setDouble(2, kit.getDiscountPercentage());

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next())
                            kitId = rs.getInt(1);
                    }
                }
            }

            if (kitId == -1) {
                conn.rollback();
                return false;
            }
            kit.setKitId(kitId); // Ensure ID is set

            String itemQuery = "INSERT INTO KIT_ITEMS (KIT_ID, PRODUCT_ID) VALUES (?, ?)";
            try (PreparedStatement itemStmt = conn.prepareStatement(itemQuery)) {
                for (Product p : kit.getProducts()) {
                    itemStmt.setInt(1, kitId);
                    itemStmt.setInt(2, p.getProductId());
                    itemStmt.addBatch();
                }
                itemStmt.executeBatch();
            }

            // Save Image to Mongo
            saveKitImageToMongo(kit);

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
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

    public boolean updateKit(ProductKit kit) {
        Connection conn = null;
        try {
            conn = OracleDB.getConnection();
            conn.setAutoCommit(false);

            // 1. Update Kit Details
            String updateQuery = "UPDATE PRODUCT_KITS SET KIT_NAME = ?, DISCOUNT_PERCENTAGE = ? WHERE KIT_ID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                pstmt.setString(1, kit.getKitName());
                pstmt.setDouble(2, kit.getDiscountPercentage());
                pstmt.setInt(3, kit.getKitId());
                pstmt.executeUpdate();
            }

            // 2. Update Items (Delete all and re-insert)
            String deleteItems = "DELETE FROM KIT_ITEMS WHERE KIT_ID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteItems)) {
                pstmt.setInt(1, kit.getKitId());
                pstmt.executeUpdate();
            }

            String itemQuery = "INSERT INTO KIT_ITEMS (KIT_ID, PRODUCT_ID) VALUES (?, ?)";
            try (PreparedStatement itemStmt = conn.prepareStatement(itemQuery)) {
                for (Product p : kit.getProducts()) {
                    itemStmt.setInt(1, kit.getKitId());
                    itemStmt.setInt(2, p.getProductId());
                    itemStmt.addBatch();
                }
                itemStmt.executeBatch();
            }

            // Update Image in Mongo
            updateKitImageInMongo(kit);

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
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

    public List<ProductKit> getAllKits() {
        List<ProductKit> kits = new ArrayList<>();
        String query = "SELECT * FROM PRODUCT_KITS";

        try (Connection conn = OracleDB.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                ProductKit kit = new ProductKit();
                kit.setKitId(rs.getInt("KIT_ID"));
                kit.setKitName(rs.getString("KIT_NAME"));
                kit.setDiscountPercentage(rs.getDouble("DISCOUNT_PERCENTAGE"));
                kit.setProducts(getProductsForKit(kit.getKitId()));
                enrichKitFromMongo(kit);
                kits.add(kit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return kits;
    }

    public List<Product> getProductsForKit(int kitId) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT p.* FROM PRODUCTS p JOIN KIT_ITEMS k ON p.PRODUCT_ID = k.PRODUCT_ID WHERE k.KIT_ID = ?";
        try (Connection conn = OracleDB.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, kitId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product(
                            rs.getInt("PRODUCT_ID"),
                            rs.getString("PRODUCT_NAME"),
                            rs.getDouble("PRODUCT_PRICE"),
                            rs.getInt("CATEGORY_ID"),
                            0 // Stock not needed for kit display usually, or fetch it
                    );
                    // Try to get code if column exists, else ignore
                    try {
                        p.setProductCode(rs.getString("PRODUCT_CODE"));
                    } catch (SQLException e) {
                    }
                    products.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public boolean deleteKit(int kitId) {
        Connection conn = null;
        try {
            conn = OracleDB.getConnection();
            conn.setAutoCommit(false);

            // 1. Delete Kit Items first (Foreign Key constraint)
            String deleteItems = "DELETE FROM KIT_ITEMS WHERE KIT_ID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteItems)) {
                pstmt.setInt(1, kitId);
                pstmt.executeUpdate();
            }

            // 2. Delete Kit
            String deleteKit = "DELETE FROM PRODUCT_KITS WHERE KIT_ID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteKit)) {
                pstmt.setInt(1, kitId);
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    deleteKitImageFromMongo(kitId);
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
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

    private void saveKitImageToMongo(ProductKit kit) {
        MongoCollection<Document> collection = MongoDB.getProductKitsCollection();
        Document doc = new Document("kitId", kit.getKitId())
                .append("image", kit.getImage());
        collection.insertOne(doc);
    }

    private void updateKitImageInMongo(ProductKit kit) {
        MongoCollection<Document> collection = MongoDB.getProductKitsCollection();
        // Check if exists
        if (collection.countDocuments(Filters.eq("kitId", kit.getKitId())) == 0) {
            saveKitImageToMongo(kit);
        } else {
            collection.updateOne(Filters.eq("kitId", kit.getKitId()),
                    Updates.set("image", kit.getImage()));
        }
    }

    private void deleteKitImageFromMongo(int kitId) {
        MongoCollection<Document> collection = MongoDB.getProductKitsCollection();
        collection.deleteOne(Filters.eq("kitId", kitId));
    }

    private void enrichKitFromMongo(ProductKit kit) {
        MongoCollection<Document> collection = MongoDB.getProductKitsCollection();
        Document doc = collection.find(Filters.eq("kitId", kit.getKitId())).first();
        if (doc != null) {
            String image = doc.getString("image");
            if (image != null) {
                kit.setImage(image);
            }
        }
    }
}
