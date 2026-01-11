package ui;

import backend.OrderService;
import models.Order;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProductsProgressPanel extends JPanel {
    private MainFrame mainFrame;
    private OrderService orderService;
    private JTable ordersTable;
    private DefaultTableModel tableModel;

    public ProductsProgressPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.orderService = new OrderService();

        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.add(new JLabel("Order Management"));
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadOrders());
        header.add(refreshBtn);
        add(header, BorderLayout.NORTH);

        // Table
        String[] columns = { "Order ID", "User ID", "Date", "Total", "Status" };
        tableModel = new DefaultTableModel(columns, 0);
        ordersTable = new JTable(tableModel);
        add(new JScrollPane(ordersTable), BorderLayout.CENTER);

        // Footer (Update Status)
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footer.add(new JLabel("Update Status:"));

        JComboBox<String> statusBox = new JComboBox<>(new String[] { "PENDING", "SHIPPED", "DELIVERED", "CANCELLED" });
        footer.add(statusBox);

        JButton updateBtn = new JButton("Update");
        updateBtn.addActionListener(e -> {
            int row = ordersTable.getSelectedRow();
            if (row != -1) {
                int orderId = (Integer) tableModel.getValueAt(row, 0);
                String status = (String) statusBox.getSelectedItem();
                if (orderService.updateOrderStatus(orderId, status)) {
                    JOptionPane.showMessageDialog(this, "Status updated!");
                    loadOrders();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update status.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please select an order.");
            }
        });
        footer.add(updateBtn);

        add(footer, BorderLayout.SOUTH);

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent e) {
                loadOrders();
            }
        });
    }

    private void loadOrders() {
        tableModel.setRowCount(0);
        List<Order> orders = orderService.getAllOrders();

        for (Order o : orders) {
            tableModel.addRow(new Object[] {
                    o.getOrderId(),
                    o.getUserId(),
                    o.getOrderDate(),
                    o.getTotalAmount(),
                    o.getOrderStatus()
            });
        }
    }
}
