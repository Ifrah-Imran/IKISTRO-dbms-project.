
package ui;

import backend.ProductService;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import models.Product;

public class AdminPanel extends JPanel {
    private MainFrame mainFrame;
    private JTabbedPane tabbedPane;

    public AdminPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 30));

        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(new Color(30, 30, 30));
        tabbedPane.setForeground(Color.WHITE);

        tabbedPane.addTab("Products", new ProductsManagementPanel());
        tabbedPane.addTab("Categories", new CategoryManagementPanel());
        tabbedPane.addTab("Orders / Progress", new ProductsProgressPanel(mainFrame));
        tabbedPane.addTab("Inventory", new InventoryManagementPanel());
        tabbedPane.addTab("Discounts", new DiscountManagementPanel());
        tabbedPane.addTab("Product Kits", new ProductKitsManagementPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    class ProductsManagementPanel extends JPanel {
        private ProductService productService;
        private JTable productsTable;
        private DefaultTableModel tableModel;

        public ProductsManagementPanel() {
            productService = new ProductService();
            setLayout(new BorderLayout());
            setBackground(new Color(30, 30, 30));

            // Toolbar
            JToolBar toolBar = new JToolBar();
            toolBar.setBackground(new Color(45, 45, 45));
            toolBar.setFloatable(false);

            JButton addBtn = createStyledButton("Add Product");
            addBtn.addActionListener(e -> showProductDialog(null));
            toolBar.add(addBtn);

            JButton updateBtn = createStyledButton("Update Product");
            updateBtn.addActionListener(e -> {
                int selectedRow = productsTable.getSelectedRow();
                if (selectedRow != -1) {
                    int productId = (int) tableModel.getValueAt(selectedRow, 0);
                    Product p = productService.getProductById(productId);
                    if (p != null) {
                        showProductDialog(p);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Please select a product to update.");
                }
            });
            toolBar.add(updateBtn);

            JButton refreshBtn = createStyledButton("Refresh");
            refreshBtn.addActionListener(e -> loadProducts());
            toolBar.add(refreshBtn);

            JButton deleteBtn = createStyledButton("Delete Product");
            deleteBtn.setForeground(new Color(255, 100, 100));
            deleteBtn.addActionListener(e -> {
                int selectedRow = productsTable.getSelectedRow();
                if (selectedRow != -1) {
                    int productId = (int) tableModel.getValueAt(selectedRow, 0);
                    String productName = (String) tableModel.getValueAt(selectedRow, 1);

                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to delete '" + productName + "'?",
                            "Confirm Delete",
                            JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        if (productService.deleteProduct(productId)) {
                            JOptionPane.showMessageDialog(this, "Product deleted.");
                            loadProducts();
                        } else {
                            JOptionPane.showMessageDialog(this, "Failed to delete product.");
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Please select a product to delete.");
                }
            });
            toolBar.add(deleteBtn);
            add(toolBar, BorderLayout.NORTH);

            // Table
            String[] columns = { "ID", "Name", "Price", "Category" };
            tableModel = new DefaultTableModel(columns, 0);
            productsTable = new JTable(tableModel);
            productsTable.setRowHeight(25);
            add(new JScrollPane(productsTable), BorderLayout.CENTER);

            loadProducts();
        }

        private JButton createStyledButton(String text) {
            JButton btn = new JButton(text);
            btn.setBackground(new Color(60, 60, 60));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            return btn;
        }

        private void loadProducts() {
            tableModel.setRowCount(0);
            List<Product> products = productService.getAllProducts();
            for (Product p : products) {
                tableModel.addRow(new Object[] {
                        p.getProductId(),
                        p.getProductName(),
                        p.getProductPrice(),
                        p.getCategoryId()
                });
            }
        }

        private void showProductDialog(Product product) {
            JDialog dialog = new JDialog(mainFrame, product == null ? "Add Product" : "Edit Product", true);
            dialog.setSize(500, 600);
            dialog.setLayout(new BorderLayout());

            JPanel formPanel = new JPanel();
            formPanel.setLayout(new GridLayout(0, 2, 10, 10));
            formPanel.setBorder(new javax.swing.border.EmptyBorder(20, 20, 20, 20));

            JTextField idField = new JTextField();
            idField.setEditable(false);
            if (product != null) {
                idField.setText(String.valueOf(product.getProductId()));
            } else {
                idField.setText("Auto-generated");
            }

            JTextField nameField = new JTextField();
            JTextField priceField = new JTextField();
            JTextField categoryField = new JTextField();
            JTextField stockField = new JTextField("0");
            JTextArea descArea = new JTextArea(3, 20);
            JTextField sizesField = new JTextField();
            JTextField colorsField = new JTextField();
            JTextField tagsField = new JTextField();

            JButton imageBtn = new JButton("Select Image");
            JLabel imagePathLabel = new JLabel("No image selected");
            // Use a final array to hold the mutable string, or just a class field.
            // Since we are in a method, we can use a final wrapper or just a
            // StringBuilder/Container.
            // Actually, we can just check the label text or use a hidden field.
            // Let's use a StringBuilder to hold the path.
            StringBuilder selectedImage = new StringBuilder();

            if (product != null) {
                nameField.setText(product.getProductName());
                priceField.setText(String.valueOf(product.getProductPrice()));
                categoryField.setText(String.valueOf(product.getCategoryId()));
                stockField.setText(String.valueOf(product.getStockQuantity()));
                descArea.setText(product.getDescription());
                sizesField.setText(String.join(",", product.getSizes()));
                colorsField.setText(String.join(",", product.getColors()));
                if (product.getTags() != null) {
                    tagsField.setText(String.join(",", product.getTags()));
                }
                if (product.getImage() != null && !product.getImage().isEmpty()) {
                    selectedImage.append(product.getImage());
                    imagePathLabel.setText(new File(product.getImage()).getName());
                }
            }

            imageBtn.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                if (fileChooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    selectedImage.setLength(0); // Clear previous
                    selectedImage.append(file.getAbsolutePath());
                    imagePathLabel.setText(file.getName());
                }
            });

            formPanel.add(new JLabel("Product ID (Auto):"));
            formPanel.add(idField);
            formPanel.add(new JLabel("Name:"));
            formPanel.add(nameField);
            formPanel.add(new JLabel("Price:"));
            formPanel.add(priceField);
            formPanel.add(new JLabel("Category ID:"));
            formPanel.add(categoryField);
            formPanel.add(new JLabel("Stock Quantity:"));
            formPanel.add(stockField);
            formPanel.add(new JLabel("Description:"));
            formPanel.add(new JScrollPane(descArea));
            formPanel.add(new JLabel("Sizes (comma sep):"));
            formPanel.add(sizesField);
            formPanel.add(new JLabel("Colors (comma sep):"));
            formPanel.add(colorsField);
            formPanel.add(new JLabel("Tags (comma sep):"));
            formPanel.add(tagsField);
            formPanel.add(imageBtn);
            formPanel.add(imagePathLabel);

            dialog.add(formPanel, BorderLayout.CENTER);

            JButton saveBtn = new JButton("Save");
            saveBtn.addActionListener(e -> {
                try {
                    Product p = (product != null) ? product : new Product();

                    if (product == null) {
                        // Generate Random ID (6 digits)
                        int randomId = 100000 + (int) (Math.random() * 900000);
                        p.setProductId(randomId);
                    }

                    p.setProductName(nameField.getText());
                    p.setProductPrice(Double.parseDouble(priceField.getText()));
                    p.setCategoryId(Integer.parseInt(categoryField.getText()));
                    p.setStockQuantity(Integer.parseInt(stockField.getText()));
                    p.setDescription(descArea.getText());

                    List<String> sizes = new ArrayList<>();
                    for (String s : sizesField.getText().split(",")) {
                        if (!s.trim().isEmpty())
                            sizes.add(s.trim());
                    }
                    p.setSizes(sizes);

                    List<String> colors = new ArrayList<>();
                    for (String c : colorsField.getText().split(",")) {
                        if (!c.trim().isEmpty())
                            colors.add(c.trim());
                    }
                    p.setColors(colors);

                    List<String> tags = new ArrayList<>();
                    for (String t : tagsField.getText().split(",")) {
                        if (!t.trim().isEmpty())
                            tags.add(t.trim());
                    }
                    p.setTags(tags);

                    if (selectedImage.length() > 0) {
                        p.setImage(selectedImage.toString());
                    }

                    boolean success;
                    if (product == null) {
                        success = productService.addProduct(p);
                    } else {
                        success = productService.updateProduct(p);
                    }

                    if (success) {
                        JOptionPane.showMessageDialog(dialog, "Product saved! ID: " + p.getProductId());
                        dialog.dispose();
                        loadProducts();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Failed to save product.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Invalid number format.");
                }
            });

            JPanel bottomPanel = new JPanel();
            bottomPanel.add(saveBtn);
            dialog.add(bottomPanel, BorderLayout.SOUTH);

            dialog.setLocationRelativeTo(mainFrame);
            dialog.setVisible(true);
        }
    }
}
