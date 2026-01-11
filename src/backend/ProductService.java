package backend;

import models.Product;
import org.bson.Document;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import models.Category;

public class ProductService {

    public ProductService() {
        ensureProductCodeColumn();
    }

    private void ensureProductCodeColumn() {
        try (Connection conn = OracleDB.getConnection();
                Statement stmt = conn.createStatement()) {
            try {
                stmt.executeQuery("SELECT PRODUCT_CODE FROM PRODUCTS WHERE 1=0");
            } catch (SQLException e) {
                // Column likely missing, add it
                stmt.executeUpdate("ALTER TABLE PRODUCTS ADD PRODUCT_CODE VARCHAR2(50)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String query = "SELECT p.PRODUCT_ID, p.PRODUCT_NAME, p.PRODUCT_PRICE, p.CATEGORY_ID, p.PRODUCT_CODE, " +
                "COALESCE(SUM(i.STOCK_QUANTITY), 0) as STOCK_QUANTITY " +
                "FROM PRODUCTS p " +
                "LEFT JOIN INVENTORY i ON p.PRODUCT_ID = i.PRODUCT_ID " +
                "GROUP BY p.PRODUCT_ID, p.PRODUCT_NAME, p.PRODUCT_PRICE, p.CATEGORY_ID, p.PRODUCT_CODE " +
                "ORDER BY p.PRODUCT_ID";

        try (Connection conn = OracleDB.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Product p = new Product(
                        rs.getInt("PRODUCT_ID"),
                        rs.getString("PRODUCT_NAME"),
                        rs.getDouble("PRODUCT_PRICE"),
                        rs.getInt("CATEGORY_ID"),
                        rs.getInt("STOCK_QUANTITY"));
                p.setProductCode(rs.getString("PRODUCT_CODE"));
                enrichProductFromMongo(p);
                products.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public Product getProductById(int productId) {
        String query = "SELECT p.PRODUCT_ID, p.PRODUCT_NAME, p.PRODUCT_PRICE, p.CATEGORY_ID, p.PRODUCT_CODE, " +
                "COALESCE(SUM(i.STOCK_QUANTITY), 0) as STOCK_QUANTITY " +
                "FROM PRODUCTS p " +
                "LEFT JOIN INVENTORY i ON p.PRODUCT_ID = i.PRODUCT_ID " +
                "WHERE p.PRODUCT_ID = ? " +
                "GROUP BY p.PRODUCT_ID, p.PRODUCT_NAME, p.PRODUCT_PRICE, p.CATEGORY_ID, p.PRODUCT_CODE";
        try (Connection conn = OracleDB.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, productId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Product p = new Product(
                            rs.getInt("PRODUCT_ID"),
                            rs.getString("PRODUCT_NAME"),
                            rs.getDouble("PRODUCT_PRICE"),
                            rs.getInt("CATEGORY_ID"),
                            rs.getInt("STOCK_QUANTITY"));
                    p.setProductCode(rs.getString("PRODUCT_CODE"));
                    enrichProductFromMongo(p);
                    return p;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addProduct(Product product) {
        String query = "INSERT INTO PRODUCTS (PRODUCT_NAME, PRODUCT_PRICE, CATEGORY_ID, PRODUCT_CODE) VALUES (?, ?, ?, ?)";
        try (Connection conn = OracleDB.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query, new String[] { "PRODUCT_ID" })) {

            if (product.getProductCode() == null || product.getProductCode().isEmpty()) {
                product.setProductCode(
                        "IK-" + System.currentTimeMillis() % 100000 + "-" + (char) ('A' + Math.random() * 26));
            }

            pstmt.setString(1, product.getProductName());
            pstmt.setDouble(2, product.getProductPrice());
            if (product.getCategoryId() > 0) {
                pstmt.setInt(3, product.getCategoryId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.setString(4, product.getProductCode());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        product.setProductId(newId);

                        // Add to inventory
                        addInventory(newId, product.getStockQuantity());

                        saveProductToMongo(product);
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void addInventory(int productId, int quantity) {
        String query = "INSERT INTO INVENTORY (PRODUCT_ID, WAREHOUSE_ID, STOCK_QUANTITY) VALUES (?, ?, ?)";
        int warehouseId = getDefaultWarehouseId();
        try (Connection conn = OracleDB.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, productId);
            pstmt.setInt(2, warehouseId);
            pstmt.setInt(3, quantity);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getDefaultWarehouseId() {
        String query = "SELECT WAREHOUSE_ID FROM WAREHOUSES WHERE ROWNUM = 1";
        try (Connection conn = OracleDB.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1; // Default fallback
    }

    public boolean updateProduct(Product product) {
        String query = "UPDATE PRODUCTS SET PRODUCT_NAME = ?, PRODUCT_PRICE = ?, CATEGORY_ID = ? WHERE PRODUCT_ID = ?";
        try (Connection conn = OracleDB.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, product.getProductName());
            pstmt.setDouble(2, product.getProductPrice());
            pstmt.setInt(3, product.getCategoryId());
            pstmt.setInt(4, product.getProductId());

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                updateInventory(product.getProductId(), product.getStockQuantity());
                updateProductInMongo(product);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void updateInventory(int productId, int quantity) {
        int warehouseId = getDefaultWarehouseId();
        // Merge approach to handle both insert (if missing) and update
        String query = "MERGE INTO INVENTORY target " +
                "USING (SELECT ? as pid, ? as wid, ? as qty FROM dual) source " +
                "ON (target.PRODUCT_ID = source.pid AND target.WAREHOUSE_ID = source.wid) " +
                "WHEN MATCHED THEN UPDATE SET target.STOCK_QUANTITY = source.qty " +
                "WHEN NOT MATCHED THEN INSERT (PRODUCT_ID, WAREHOUSE_ID, STOCK_QUANTITY) VALUES (source.pid, source.wid, source.qty)";

        try (Connection conn = OracleDB.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, productId);
            pstmt.setInt(2, warehouseId);
            pstmt.setInt(3, quantity);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteProduct(int productId) {
        Connection conn = null;
        try {
            conn = OracleDB.getConnection();
            conn.setAutoCommit(false);

            // Delete dependencies to avoid FK constraints/orphaned records
            // 1. Inventory
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM INVENTORY WHERE PRODUCT_ID = ?")) {
                ps.setInt(1, productId);
                ps.executeUpdate();
            }
            // 2. Wishlists (Fixed table name)
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM WISHLISTS WHERE PRODUCT_ID = ?")) {
                ps.setInt(1, productId);
                ps.executeUpdate();
            }
            // 4. Kit Items (if this product is part of a kit)
            // Note: This might make kits incomplete, but essential for deleting product.
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM KIT_ITEMS WHERE PRODUCT_ID = ?")) {
                ps.setInt(1, productId);
                ps.executeUpdate();
            }
            // 5. Order Items - WARNING: This modifies order history.
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM ORDER_ITEMS WHERE PRODUCT_ID = ?")) {
                ps.setInt(1, productId);
                ps.executeUpdate();
            }
            // 6. Reviews (Oracle side if exists, though primarily Mongo now)
            // (Skipping Oracle review delete as it's implied Mongo-driven, but good
            // practice if table exists)

            // Finally delete the product
            String query = "DELETE FROM PRODUCTS WHERE PRODUCT_ID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, productId);
                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    deleteProductFromMongo(productId);
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
        return false;
    }

    private void enrichProductFromMongo(Product product) {
        MongoCollection<Document> collection = MongoDB.getProductsCollection();
        Document doc = collection.find(Filters.eq("productId", product.getProductId())).first();

        if (doc != null) {
            String image = doc.getString("image");
            List<String> tags = doc.getList("tags", String.class);
            String description = doc.getString("description");
            List<String> sizes = doc.getList("sizes", String.class);
            List<String> colors = doc.getList("colors", String.class);

            if (image != null)
                product.setImage(image);
            if (tags != null)
                product.setTags(tags);
            if (description != null)
                product.setDescription(description);
            if (sizes != null)
                product.setSizes(sizes);
            if (colors != null)
                product.setColors(colors);
        }
    }

    private void saveProductToMongo(Product product) {
        MongoCollection<Document> collection = MongoDB.getProductsCollection();
        Document doc = new Document("productId", product.getProductId())
                .append("image", product.getImage())
                .append("tags", product.getTags() != null ? product.getTags() : new ArrayList<>())
                .append("description", product.getDescription())
                .append("sizes", product.getSizes())
                .append("colors", product.getColors());
        collection.insertOne(doc);
    }

    private void updateProductInMongo(Product product) {
        MongoCollection<Document> collection = MongoDB.getProductsCollection();
        collection.updateOne(Filters.eq("productId", product.getProductId()),
                Updates.combine(
                        Updates.set("image", product.getImage()),
                        Updates.set("tags", product.getTags() != null ? product.getTags() : new ArrayList<>()),
                        Updates.set("description", product.getDescription()),
                        Updates.set("sizes", product.getSizes()),
                        Updates.set("colors", product.getColors())));
    }

    private void deleteProductFromMongo(int productId) {
        MongoCollection<Document> collection = MongoDB.getProductsCollection();
        collection.deleteOne(Filters.eq("productId", productId));
    }

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT * FROM CATEGORIES";
        try (Connection conn = OracleDB.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                categories.add(new Category(
                        rs.getInt("CATEGORY_ID"),
                        rs.getString("CATEGORY_NAME")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public boolean addCategory(String categoryName) {
        String query = "INSERT INTO CATEGORIES (CATEGORY_NAME) VALUES (?)";
        try (Connection conn = OracleDB.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, categoryName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteCategory(int categoryId) {
        String query = "DELETE FROM CATEGORIES WHERE CATEGORY_ID = ?";
        try (Connection conn = OracleDB.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, categoryId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Product> getProductsByCategory(int categoryId) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT p.PRODUCT_ID, p.PRODUCT_NAME, p.PRODUCT_PRICE, p.CATEGORY_ID, p.PRODUCT_CODE, " +
                "COALESCE(SUM(i.STOCK_QUANTITY), 0) as STOCK_QUANTITY " +
                "FROM PRODUCTS p " +
                "LEFT JOIN INVENTORY i ON p.PRODUCT_ID = i.PRODUCT_ID " +
                "WHERE p.CATEGORY_ID = ? " +
                "GROUP BY p.PRODUCT_ID, p.PRODUCT_NAME, p.PRODUCT_PRICE, p.CATEGORY_ID, p.PRODUCT_CODE";
        try (Connection conn = OracleDB.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product(
                            rs.getInt("PRODUCT_ID"),
                            rs.getString("PRODUCT_NAME"),
                            rs.getDouble("PRODUCT_PRICE"),
                            rs.getInt("CATEGORY_ID"),
                            rs.getInt("STOCK_QUANTITY"));
                    p.setProductCode(rs.getString("PRODUCT_CODE"));
                    enrichProductFromMongo(p);
                    products.add(p);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }
}
