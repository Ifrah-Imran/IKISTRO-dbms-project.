package ui;

import backend.ProductKitService;
import backend.ProductService;
import models.Product;
import models.ProductKit;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductKitsManagementPanel extends JPanel {
    private ProductKitService kitService;
    private ProductService productService;
    private JTable kitsTable;
    private DefaultTableModel tableModel;

    public ProductKitsManagementPanel() {
        kitService = new ProductKitService();
        productService = new ProductService();
        setLayout(new BorderLayout());

        // Toolbar
        JToolBar toolBar = new JToolBar();
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadKits());
        toolBar.add(refreshBtn);

        JButton addBtn = new JButton("Create Kit");
        addBtn.addActionListener(e -> showKitDialog(null));
        toolBar.add(addBtn);

        JButton updateBtn = new JButton("Update Kit");
        updateBtn.addActionListener(e -> {
            int selectedRow = kitsTable.getSelectedRow();
            if (selectedRow != -1) {
                int kitId = (int) tableModel.getValueAt(selectedRow, 0);
                // We need to fetch the full kit details including products
                ProductKit kit = new ProductKit();
                kit.setKitId(kitId);
                kit.setKitName((String) tableModel.getValueAt(selectedRow, 1));
                String discountStr = (String) tableModel.getValueAt(selectedRow, 2);
                kit.setDiscountPercentage(Double.parseDouble(discountStr.replace("%", "")));
                // Fetch products for this kit
                kit.setProducts(kitService.getProductsForKit(kitId));

                showKitDialog(kit);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a kit to update.");
            }
        });
        toolBar.add(updateBtn);

        JButton deleteBtn = new JButton("Delete Kit");
        deleteBtn.setForeground(Color.RED);
        deleteBtn.addActionListener(e -> {
            int selectedRow = kitsTable.getSelectedRow();
            if (selectedRow != -1) {
                int kitId = (int) tableModel.getValueAt(selectedRow, 0);
                String kitName = (String) tableModel.getValueAt(selectedRow, 1);

                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete kit '" + kitName + "'?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    if (kitService.deleteKit(kitId)) {
                        JOptionPane.showMessageDialog(this, "Kit deleted successfully.");
                        loadKits();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to delete kit.");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select a kit to delete.");
            }
        });
        toolBar.add(deleteBtn);

        add(toolBar, BorderLayout.NORTH);

        // Table
        String[] columns = { "ID", "Kit Name", "Discount %", "Products" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        kitsTable = new JTable(tableModel);
        // Adjust column widths
        kitsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        kitsTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        kitsTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        kitsTable.getColumnModel().getColumn(3).setPreferredWidth(400);

        add(new JScrollPane(kitsTable), BorderLayout.CENTER);

        loadKits();
    }

    private void loadKits() {
        tableModel.setRowCount(0);
        List<ProductKit> kits = kitService.getAllKits();
        for (ProductKit k : kits) {
            // Fetch products if not already fetched (getAllKits in service might need
            // update or we fetch here)
            // The service getAllKits seems to fetch products now based on my previous read,
            // but let's be safe and ensure we have them.
            if (k.getProducts() == null || k.getProducts().isEmpty()) {
                k.setProducts(kitService.getProductsForKit(k.getKitId()));
            }

            String productNames = k.getProducts().stream()
                    .map(Product::getProductName)
                    .collect(Collectors.joining(", "));

            tableModel.addRow(new Object[] {
                    k.getKitId(),
                    k.getKitName(),
                    k.getDiscountPercentage() + "%",
                    productNames
            });
        }
    }

    private void showKitDialog(ProductKit existingKit) {
        String title = (existingKit == null) ? "Create Product Kit" : "Update Product Kit";
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), title, true);
        dialog.setSize(600, 600);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(15, 15, 5, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Kit Name:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        JTextField nameField = new JTextField();
        formPanel.add(nameField, gbc);

        // Discount
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Discount (%):"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        JTextField discountField = new JTextField();
        formPanel.add(discountField, gbc);

        // Image Selection
        JButton imageBtn = new JButton("Select Image");
        JLabel imageLabel = new JLabel("No image selected");
        StringBuilder selectedImage = new StringBuilder();

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        formPanel.add(imageBtn, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        formPanel.add(imageLabel, gbc);

        imageBtn.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                selectedImage.setLength(0);
                selectedImage.append(file.getAbsolutePath());
                imageLabel.setText(file.getName());
            }
        });

        dialog.add(formPanel, BorderLayout.NORTH);

        // Product Selection
        DefaultListModel<Product> listModel = new DefaultListModel<>();
        List<Product> allProducts = productService.getAllProducts();
        for (Product p : allProducts) {
            listModel.addElement(p);
        }
        JList<Product> productList = new JList<>(listModel);
        productList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        productList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Product) {
                    Product p = (Product) value;
                    setText(p.getProductName() + " ($" + p.getProductPrice() + ")");
                }
                return this;
            }
        });

        dialog.add(new JScrollPane(productList), BorderLayout.CENTER);

        JLabel helpLabel = new JLabel(
                "<html>Select products to include (Hold Ctrl/Cmd to select multiple).<br/>Deselect to remove from kit.</html>");
        helpLabel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // Populate if updating
        if (existingKit != null) {
            nameField.setText(existingKit.getKitName());
            discountField.setText(String.valueOf(existingKit.getDiscountPercentage()));

            if (existingKit.getImage() != null && !existingKit.getImage().isEmpty()) {
                selectedImage.append(existingKit.getImage());
                imageLabel.setText(new java.io.File(existingKit.getImage()).getName());
            }

            List<Integer> existingProductIds = new ArrayList<>();
            for (Product p : existingKit.getProducts())
                existingProductIds.add(p.getProductId());

            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < listModel.size(); i++) {
                if (existingProductIds.contains(listModel.get(i).getProductId())) {
                    indices.add(i);
                }
            }
            int[] selectedIndices = indices.stream().mapToInt(i -> i).toArray();
            productList.setSelectedIndices(selectedIndices);
        }

        JButton saveBtn = new JButton("Save Kit");
        saveBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String discStr = discountField.getText().trim();

                if (name.isEmpty() || discStr.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please fill all fields.");
                    return;
                }

                double discount = Double.parseDouble(discStr);

                if (discount < 0 || discount > 100) {
                    JOptionPane.showMessageDialog(dialog, "Discount must be between 0% and 100%.");
                    return;
                }

                List<Product> selectedProducts = productList.getSelectedValuesList();
                if (selectedProducts.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please select at least one product.");
                    return;
                }

                ProductKit kit = (existingKit != null) ? existingKit : new ProductKit();
                kit.setKitName(name);
                // Schema expects 0.0 to 1.0, user enters 0-100
                kit.setDiscountPercentage(discount / 100.0);
                kit.setProducts(selectedProducts);
                if (selectedImage.length() > 0) {
                    kit.setImage(selectedImage.toString());
                }

                boolean success;
                if (existingKit == null) {
                    success = kitService.createKit(kit);
                } else {
                    success = kitService.updateKit(kit);
                }

                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Kit saved successfully!");
                    dialog.dispose();
                    loadKits();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to save kit. Database error.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid number format for discount.");
            }
        });

        JPanel btnPanel = new JPanel();
        btnPanel.add(saveBtn);

        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.add(helpLabel, BorderLayout.NORTH);
        bottomContainer.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(bottomContainer, BorderLayout.SOUTH);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
