package ui;

import backend.ProductService;
import models.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InventoryManagementPanel extends JPanel {
    private ProductService productService;
    private JTable inventoryTable;
    private DefaultTableModel tableModel;

    public InventoryManagementPanel() {
        productService = new ProductService();
        setLayout(new BorderLayout());

        // Toolbar
        JToolBar toolBar = new JToolBar();
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadInventory());
        toolBar.add(refreshBtn);

        JButton updateBtn = new JButton("Update Stock");
        updateBtn.addActionListener(e -> updateStock());
        toolBar.add(updateBtn);

        add(toolBar, BorderLayout.NORTH);

        // Table
        String[] columns = { "ID", "Product Name", "Current Stock" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Stock update via button
            }
        };
        inventoryTable = new JTable(tableModel);
        add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        loadInventory();
    }

    private void loadInventory() {
        tableModel.setRowCount(0);
        List<Product> products = productService.getAllProducts();
        for (Product p : products) {
            tableModel.addRow(new Object[] {
                    p.getProductId(),
                    p.getProductName(),
                    p.getStockQuantity()
            });
        }
    }

    private void updateStock() {
        int row = inventoryTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to update stock.");
            return;
        }

        int productId = (int) tableModel.getValueAt(row, 0);
        String currentStockStr = tableModel.getValueAt(row, 2).toString();

        String newStockStr = JOptionPane.showInputDialog(this, "Enter new stock quantity:", currentStockStr);
        if (newStockStr != null) {
            try {
                int newStock = Integer.parseInt(newStockStr);
                if (newStock < 0) {
                    JOptionPane.showMessageDialog(this, "Stock cannot be negative.");
                    return;
                }

                Product product = productService.getProductById(productId);
                if (product != null) {
                    product.setStockQuantity(newStock);
                    if (productService.updateProduct(product)) {
                        JOptionPane.showMessageDialog(this, "Stock updated!");
                        loadInventory();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update stock.");
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid number format.");
            }
        }
    }
}
