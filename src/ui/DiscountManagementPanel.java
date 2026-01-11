package ui;

import backend.DiscountService;
import models.DiscountCode;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DiscountManagementPanel extends JPanel {
    private DiscountService discountService;
    private JTable discountTable;
    private DefaultTableModel tableModel;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public DiscountManagementPanel() {
        discountService = new DiscountService();
        setLayout(new BorderLayout());

        // Toolbar
        JToolBar toolBar = new JToolBar();
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadDiscounts());
        toolBar.add(refreshBtn);

        JButton addBtn = new JButton("Add Discount");
        addBtn.addActionListener(e -> showAddDiscountDialog());
        toolBar.add(addBtn);

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(e -> deleteSelectedDiscount());
        toolBar.add(deleteBtn);

        add(toolBar, BorderLayout.NORTH);

        // Table
        String[] columns = { "ID", "Code", "Percentage", "Expiry", "Usage Limit", "Used", "Active" };
        tableModel = new DefaultTableModel(columns, 0);
        discountTable = new JTable(tableModel);
        add(new JScrollPane(discountTable), BorderLayout.CENTER);

        loadDiscounts();
    }

    private void loadDiscounts() {
        tableModel.setRowCount(0);
        List<DiscountCode> codes = discountService.getAllActiveCodes();
        for (DiscountCode d : codes) {
            tableModel.addRow(new Object[] {
                    d.getDiscountId(),
                    d.getCode(),
                    d.getDiscountPercentage() + "%",
                    dateFormat.format(d.getExpiryDate()),
                    d.getUsageLimit(),
                    d.getTimesUsed(),
                    d.isActive()
            });
        }
    }

    private void showAddDiscountDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Discount Code", true);
        dialog.setSize(300, 250);
        dialog.setLayout(new GridLayout(5, 2, 5, 5));

        JTextField codeField = new JTextField();
        JTextField percentField = new JTextField();
        JTextField expiryField = new JTextField("yyyy-MM-dd");
        JTextField limitField = new JTextField("0"); // 0 means unlimited

        dialog.add(new JLabel("Code:"));
        dialog.add(codeField);
        dialog.add(new JLabel("Percentage (1-100):"));
        dialog.add(percentField);
        dialog.add(new JLabel("Expiry (yyyy-MM-dd):"));
        dialog.add(expiryField);
        dialog.add(new JLabel("Usage Limit (0=Unlim):"));
        dialog.add(limitField);

        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> {
            try {
                String code = codeField.getText().trim();
                int percent = Integer.parseInt(percentField.getText().trim());
                Date expiry = dateFormat.parse(expiryField.getText().trim());
                int limit = Integer.parseInt(limitField.getText().trim());

                if (code.isEmpty() || percent <= 0 || percent > 100) {
                    JOptionPane.showMessageDialog(dialog, "Invalid input.");
                    return;
                }

                if (discountService.createDiscountCode(code, percent, expiry, limit)) {
                    JOptionPane.showMessageDialog(dialog, "Discount code created!");
                    dialog.dispose();
                    loadDiscounts();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to create code.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid format: " + ex.getMessage());
            }
        });
        dialog.add(saveBtn);

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void deleteSelectedDiscount() {
        int row = discountTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a discount code.");
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String codeName = (String) tableModel.getValueAt(row, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to PERMANENTLY DELETE code '" + codeName + "'?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (discountService.deleteDiscountCode(id)) {
                JOptionPane.showMessageDialog(this, "Discount code deleted!");
                loadDiscounts();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete code.");
            }
        }
    }
}
