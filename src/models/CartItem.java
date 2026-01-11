package models;

public class CartItem {
    private Product product;
    private int quantity;
    private String selectedSize;
    private String selectedColor;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.selectedSize = "";
        this.selectedColor = "";
    }

    public CartItem(Product product, int quantity, String selectedSize, String selectedColor) {
        this.product = product;
        this.quantity = quantity;
        this.selectedSize = selectedSize != null ? selectedSize : "";
        this.selectedColor = selectedColor != null ? selectedColor : "";
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSelectedSize() {
        return selectedSize;
    }

    public void setSelectedSize(String selectedSize) {
        this.selectedSize = selectedSize;
    }

    public String getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(String selectedColor) {
        this.selectedColor = selectedColor;
    }

    public double getTotalPrice() {
        return product.getProductPrice() * quantity;
    }
}
