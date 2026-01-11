package ui;

import backend.ProductService;
import models.Category;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CategoryManagementPanel extends JPanel {
    private ProductService productService;
    private JTable categoryTable;
    private DefaultTableModel tableModel;

    public CategoryManagementPanel() {
        productService = new ProductService();
        setLayout(new BorderLayout());

        // Toolbar
        JToolBar toolBar = new JToolBar();
        JButton addBtn = new JButton("Add Category");
        addBtn.addActionListener(e -> showAddCategoryDialog());
        toolBar.add(addBtn);

        JButton deleteBtn = new JButton("Delete Category");
        deleteBtn.addActionListener(e -> deleteSelectedCategory());
        toolBar.add(deleteBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadCategories());
        toolBar.add(refreshBtn);

        add(toolBar, BorderLayout.NORTH);

        // Table
        String[] columns = { "ID", "Name" };
        tableModel = new DefaultTableModel(columns, 0);
        categoryTable = new JTable(tableModel);
        add(new JScrollPane(categoryTable), BorderLayout.CENTER);

        loadCategories();
    }

    private void loadCategories() {
        tableModel.setRowCount(0);
        List<Category> categories = productService.getAllCategories();
        for (Category c : categories) {
            tableModel.addRow(new Object[] { c.getCategoryId(), c.getCategoryName() });
        }
    }

    private void showAddCategoryDialog() {
        String name = JOptionPane.showInputDialog(this, "Enter Category Name:");
        if (name != null && !name.trim().isEmpty()) {
            if (productService.addCategory(name.trim())) {
                JOptionPane.showMessageDialog(this, "Category added!");
                loadCategories();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add category.");
            }
        }
    }

    private void deleteSelectedCategory() {
        int row = categoryTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a category to delete.");
            return;
        }

        int id = (Integer) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this category?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (productService.deleteCategory(id)) {
                JOptionPane.showMessageDialog(this, "Category deleted!");
                loadCategories();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete category. It might be in use.");
            }
        }
    }
}
