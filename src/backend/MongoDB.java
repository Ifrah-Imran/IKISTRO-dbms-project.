package backend;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class MongoDB {
    // Update connection string as needed
    private static final String URI = "mongodb://localhost:27017";
    private static final String DB_NAME = "ikea_store";

    private static MongoClient mongoClient;
    private static MongoDatabase database;

    static {
        try {
            mongoClient = MongoClients.create(URI);
            database = mongoClient.getDatabase(DB_NAME);
        } catch (Exception e) {
            System.err.println("Failed to connect to MongoDB. Please ensure MongoDB is running and driver is added.");
            e.printStackTrace();
        }
    }

    public static MongoDatabase getDatabase() {
        return database;
    }

    public static MongoCollection<Document> getProductsCollection() {
        return database.getCollection("products");
    }

    public static MongoCollection<Document> getReviewsCollection() {
        return database.getCollection("reviews");
    }

    public static MongoCollection<Document> getProductDescriptionsCollection() {
        return database.getCollection("product_descriptions");
    }

    public static MongoCollection<Document> getProductKitsCollection() {
        return database.getCollection("product_kits");
    }

    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
