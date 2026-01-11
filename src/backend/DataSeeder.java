package backend;

import java.util.Date;
import java.util.List;
import models.Product;

public class DataSeeder {
    private ProductService productService;
    private DiscountService discountService;

    public DataSeeder() {
        productService = new ProductService();
        discountService = new DiscountService();
    }

    public void seed() {
        seedCategories();
        seedProducts();
        seedDiscounts();
    }

    private void seedCategories() {
        if (productService.getAllCategories().isEmpty()) {
            productService.addCategory("Tables");
            productService.addCategory("Chairs");
            productService.addCategory("Sofas");
            productService.addCategory("Beds");
            productService.addCategory("Lighting");
            System.out.println("Seeded Categories");
        }
    }

    private void seedProducts() {
        if (productService.getAllProducts().isEmpty()) {
            // Get Category IDs (assuming they are assigned sequentially or we can fetch
            // them)
            // For simplicity, we'll fetch them first
            int tableId = getCategoryId("Tables");
            int chairId = getCategoryId("Chairs");
            int sofaId = getCategoryId("Sofas");

            if (tableId != -1) {
                productService.addProduct(new Product(0, "LACK Side Table", 12.99, tableId, 50));
                productService.addProduct(new Product(0, "LISABO Coffee Table", 149.00, tableId, 20));
            }
            if (chairId != -1) {
                productService.addProduct(new Product(0, "MARKUS Office Chair", 199.00, chairId, 30));
                productService.addProduct(new Product(0, "STEFAN Chair", 25.00, chairId, 100));
            }
            if (sofaId != -1) {
                productService.addProduct(new Product(0, "EKTORP 3-seat Sofa", 499.00, sofaId, 10));
            }
            System.out.println("Seeded Products");
        }
    }

    private void seedDiscounts() {
        if (discountService.getAllActiveCodes().isEmpty()) {
            // Expire in 30 days
            Date expiry = new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000);
            discountService.createDiscountCode("WELCOME10", 10, expiry, 100);
            discountService.createDiscountCode("SAVE20", 20, expiry, 50);
            System.out.println("Seeded Discounts");
        }
    }

    private int getCategoryId(String name) {
        List<models.Category> categories = productService.getAllCategories();
        for (models.Category c : categories) {
            if (c.getCategoryName().equalsIgnoreCase(name)) {
                return c.getCategoryId();
            }
        }
        return -1;
    }
}
