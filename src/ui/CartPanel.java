package ui;

import backend.OrderService;
import backend.DiscountService;
import backend.AddressService;
import models.CartItem;
import models.Order;
import models.DiscountCode;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CartPanel extends JPanel {
    private MainFrame mainFrame;
    private OrderService orderService;
    private DiscountService discountService;
    private JPanel cartItemsPanel;
    private JLabel totalLabel;
    private JLabel subtotalLabel;
    private JLabel discountLabel;
    private JLabel shippingLabel;
    private JLabel taxLabel;
    private JTextField discountField;
    private JButton applyDiscountBtn;
    private double subtotal = 0;
    private double discountAmount = 0;
    private double shippingFee = 0;
    private double taxAmount = 0;
    private String appliedDiscountCode = "";

    // Constants
    private static final double STANDARD_SHIPPING = 9.99;
    private static final double FREE_SHIPPING_THRESHOLD = 100.0;
    private static final double STANDARD_TAX_RATE = 0.05; // 5%
    private static final double KIT_TAX_RATE = 0.03; // 3% for kit items

    public CartPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.orderService = new OrderService();
        this.discountService = new DiscountService();

        setLayout(new BorderLayout());
        setBackground(Theme.CREAM);

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(Theme.CREAM);
        JLabel title = new JLabel("Shopping Cart");
        title.setFont(Theme.getHeaderFont(Font.BOLD, 20));
        header.add(title);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadCartItems());
        header.add(refreshBtn);
        add(header, BorderLayout.NORTH);

        // Cart Items
        cartItemsPanel = new JPanel();
        cartItemsPanel.setBackground(Theme.CREAM);
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));
        add(new JScrollPane(cartItemsPanel), BorderLayout.CENTER);

        // Footer (Checkout)
        JPanel footer = new JPanel(new GridLayout(7, 1));
        footer.setBackground(Theme.CREAM);

        // Subtotal
        JPanel subtotalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        subtotalPanel.setBackground(Theme.CREAM);
        subtotalPanel.add(new JLabel("Subtotal: "));
        subtotalLabel = new JLabel("$0.00");
        subtotalLabel.setFont(Theme.getBodyFont(Font.PLAIN, 14));
        subtotalPanel.add(subtotalLabel);
        footer.add(subtotalPanel);

        // Discount Code Section
        JPanel discountPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        discountPanel.setBackground(Theme.CREAM);
        discountPanel.add(new JLabel("Discount Code:"));
        discountField = new JTextField(10);
        discountPanel.add(discountField);
        applyDiscountBtn = new JButton("Apply");
        applyDiscountBtn.setBackground(Theme.TAN);
        applyDiscountBtn.setForeground(Theme.CHARCOAL);
        applyDiscountBtn.addActionListener(e -> applyDiscount());
        discountPanel.add(applyDiscountBtn);
        discountLabel = new JLabel("Discount: $0.00");
        discountLabel.setFont(Theme.getBodyFont(Font.PLAIN, 12));
        discountLabel.setForeground(Theme.SUCCESS_GREEN);
        discountPanel.add(discountLabel);
        footer.add(discountPanel);

        // Total
        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.setBackground(Theme.CREAM);
        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(Theme.getBodyFont(Font.BOLD, 16));
        totalPanel.add(totalLabel);
        footer.add(totalPanel);

        // Shipping
        JPanel shippingPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        shippingPanel.setBackground(Theme.CREAM);
        shippingLabel = new JLabel("Shipping: $0.00");
        shippingLabel.setFont(Theme.getBodyFont(Font.PLAIN, 12));
        shippingPanel.add(shippingLabel);
        footer.add(shippingPanel);

        // Tax
        JPanel taxPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        taxPanel.setBackground(Theme.CREAM);
        taxLabel = new JLabel("Tax: $0.00");
        taxLabel.setFont(Theme.getBodyFont(Font.PLAIN, 12));
        taxPanel.add(taxLabel);
        footer.add(taxPanel);

        // Checkout Button
        JPanel checkoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        checkoutPanel.setBackground(Theme.CREAM);
        JButton checkoutBtn = new JButton("Checkout");
        checkoutBtn.setBackground(Theme.DARK_BROWN);
        checkoutBtn.setForeground(Theme.CREAM);
        checkoutBtn.setFont(Theme.getBodyFont(Font.BOLD, 14));
        checkoutBtn.addActionListener(e -> checkout());
        checkoutPanel.add(checkoutBtn);
        footer.add(checkoutPanel);

        add(footer, BorderLayout.SOUTH);

        // Add listener to refresh when shown
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent e) {
                loadCartItems();
            }
        });
    }

    private void loadCartItems() {
        cartItemsPanel.removeAll();
        List<CartItem> cart = mainFrame.getCart();
        subtotal = 0;

        for (CartItem item : cart) {
            JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            itemPanel.setBorder(BorderFactory.createEtchedBorder());
            itemPanel.setBackground(Theme.BEIGE);

            JLabel nameLabel = new JLabel(item.getProduct().getProductName());
            nameLabel.setFont(Theme.getBodyFont(Font.BOLD, 12));
            nameLabel.setPreferredSize(new Dimension(200, 30));
            itemPanel.add(nameLabel);

            JLabel priceLabel = new JLabel("$" + item.getProduct().getProductPrice());
            priceLabel.setFont(Theme.getBodyFont(Font.PLAIN, 12));
            priceLabel.setPreferredSize(new Dimension(80, 30));
            itemPanel.add(priceLabel);

            JSpinner quantitySpinner = new JSpinner(new SpinnerNumberModel(item.getQuantity(), 1, 100, 1));
            quantitySpinner.addChangeListener(e -> {
                item.setQuantity((Integer) quantitySpinner.getValue());
                updateTotal();
            });
            itemPanel.add(quantitySpinner);

            JButton removeBtn = new JButton("Remove");
            removeBtn.setBackground(Theme.ERROR_RED);
            removeBtn.setForeground(Color.WHITE);
            removeBtn.addActionListener(e -> {
                mainFrame.removeFromCart(item);
                loadCartItems();
            });
            itemPanel.add(removeBtn);

            // Show size and color if selected
            if (item.getSelectedSize() != null && !item.getSelectedSize().isEmpty()) {
                JLabel sizeLabel = new JLabel("Size: " + item.getSelectedSize());
                sizeLabel.setFont(new Font("Arial", Font.ITALIC, 11));
                itemPanel.add(sizeLabel);
            }
            if (item.getSelectedColor() != null && !item.getSelectedColor().isEmpty()) {
                JLabel colorLabel = new JLabel("Color: " + item.getSelectedColor());
                colorLabel.setFont(new Font("Arial", Font.ITALIC, 11));
                itemPanel.add(colorLabel);
            }

            cartItemsPanel.add(itemPanel);
            subtotal += item.getTotalPrice();
        }

        updateTotal();
        cartItemsPanel.revalidate();
        cartItemsPanel.repaint();
    }

    private boolean isProductFromKit(models.Product product) {
        // Check if this product is part of any kit by checking if it was added via
        // ProductKitsPanel
        // For simplicity, we'll check product tags or a flag (this is a simplified
        // approach)
        // In a real implementation, you'd track this in CartItem or check against kit
        // products
        return false; // Simplified - you can enhance this logic
    }

    private void updateTotal() {
        subtotal = 0;
        for (CartItem item : mainFrame.getCart()) {
            subtotal += item.getTotalPrice();
        }

        // Calculate shipping
        if (subtotal >= FREE_SHIPPING_THRESHOLD) {
            shippingFee = 0;
        } else if (subtotal > 0) {
            shippingFee = STANDARD_SHIPPING;
        } else {
            shippingFee = 0;
        }

        // Calculate tax (simplified - apply standard rate to all)
        // In a real implementation, you'd check each item to see if it's from a kit
        taxAmount = subtotal * STANDARD_TAX_RATE;

        double finalTotal = subtotal - discountAmount + shippingFee + taxAmount;

        subtotalLabel.setText(String.format("$%.2f", subtotal));
        shippingLabel.setText(String.format("Shipping: $%.2f%s", shippingFee,
                subtotal >= FREE_SHIPPING_THRESHOLD && subtotal > 0 ? " (FREE!)" : ""));
        taxLabel.setText(String.format("Tax (5%%): $%.2f", taxAmount));
        totalLabel.setText(String.format("Total: $%.2f", finalTotal));
    }

    private void applyDiscount() {
        String code = discountField.getText().trim();

        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a discount code", "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate the discount code
        DiscountCode discount = discountService.validateDiscountCode(code);

        if (discount == null) {
            JOptionPane.showMessageDialog(this,
                    "Invalid or expired discount code",
                    "Code Error",
                    JOptionPane.ERROR_MESSAGE);
            discountField.setText("");
            discountAmount = 0;
            appliedDiscountCode = "";
            updateTotal();
            return;
        }

        // Calculate discount
        discountAmount = (subtotal * discount.getDiscountPercentage()) / 100.0;
        appliedDiscountCode = code;

        JOptionPane.showMessageDialog(this,
                "Discount code '" + code + "' applied!\n" +
                        discount.getDiscountPercentage() + "% discount = $" + String.format("%.2f", discountAmount),
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

        discountLabel
                .setText(String.format("Discount: $%.2f (%d%%)", discountAmount, discount.getDiscountPercentage()));
        discountField.setEnabled(false);
        applyDiscountBtn.setEnabled(false);
        updateTotal();
    }

    private void checkout() {
        List<CartItem> cart = mainFrame.getCart();
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty!");
            return;
        }

        // Re-validate discount code before proceeding
        if (!appliedDiscountCode.isEmpty()) {
            DiscountCode validCode = discountService.validateDiscountCode(appliedDiscountCode);
            if (validCode == null) {
                JOptionPane.showMessageDialog(this,
                        "The discount code '" + appliedDiscountCode
                                + "' is no longer valid.\nIt has been removed from your total.",
                        "Invalid Discount",
                        JOptionPane.WARNING_MESSAGE);

                // Remove invalid discount
                discountAmount = 0;
                appliedDiscountCode = "";
                discountField.setText("");
                discountField.setEnabled(true);
                applyDiscountBtn.setEnabled(true);
                discountLabel.setText("Discount: $0.00");
                updateTotal();
                return; // Stop checkout so user can review the new total
            }
            // Fix: Use the canonical code from DB to ensure case matches for FK constraint
            appliedDiscountCode = validCode.getCode();
        }

        // Collect shipping/contact/payment details before creating order
        // Collect shipping/contact/payment details before creating order
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        JTextField firstName = new JTextField();
        JTextField lastName = new JTextField();
        JTextField cityField = new JTextField();
        JTextField postalCodeField = new JTextField();
        JTextField addressField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField(mainFrame.getUser().getEmail());
        JComboBox<String> paymentBox = new JComboBox<>(
                new String[] { "Credit Card", "Debit Card", "PayPal", "Cash on Delivery" });

        // Helper method to add field with label
        addFormField(form, "First Name:", firstName);
        addFormField(form, "Last Name:", lastName);
        addFormField(form, "City:", cityField);
        addFormField(form, "Postal Code:", postalCodeField);
        addFormField(form, "Full Address:", addressField);
        addFormField(form, "Contact Number:", phoneField);
        addFormField(form, "Email:", emailField);
        addFormField(form, "Payment Method:", paymentBox);

        int res = JOptionPane.showConfirmDialog(this, form, "Shipping & Payment Details", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) {
            return; // user cancelled
        }

        String fullName = (firstName.getText().trim() + " " + lastName.getText().trim()).trim();
        if (fullName.isEmpty() || addressField.getText().trim().isEmpty() ||
                phoneField.getText().trim().isEmpty() || postalCodeField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please provide name, address, postal code and contact number.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String paymentMethod = (String) paymentBox.getSelectedItem();
        if ("Credit Card".equals(paymentMethod) || "Debit Card".equals(paymentMethod)
                || "PayPal".equals(paymentMethod)) {
            if (!showPaymentDetailsDialog(paymentMethod)) {
                return; // Payment cancelled or failed validation
            }
        }

        // Create address record
        AddressService addrSvc = new AddressService();
        int addressId = addrSvc.createAddress(mainFrame.getUser().getUserId(), addressField.getText().trim(),
                cityField.getText().trim(), postalCodeField.getText().trim());

        Order order = new Order();
        order.setUserId(mainFrame.getUser().getUserId());
        order.setAddressId(addressId);
        order.setDiscountCode(appliedDiscountCode);

        double finalTotal = subtotal - discountAmount + shippingFee + taxAmount;
        order.setTotalAmount(finalTotal);
        order.setItems(cart);
        order.setContactName(fullName);
        order.setContactPhone(phoneField.getText().trim());
        order.setContactEmail(emailField.getText().trim());
        order.setPaymentMethod(paymentMethod);

        if (orderService.createOrder(order)) {
            // Increment usage count if discount code was applied
            if (!appliedDiscountCode.isEmpty()) {
                discountService.applyDiscountCode(appliedDiscountCode);
            }

            // Show invoice dialog
            showInvoiceDialog(order, finalTotal);

            mainFrame.clearCart();
            loadCartItems();
            discountField.setText("");
            discountField.setEnabled(true);
            applyDiscountBtn.setEnabled(true);
            discountLabel.setText("Discount: $0.00");
            shippingLabel.setText("Shipping: $0.00");
            taxLabel.setText("Tax: $0.00");
            discountAmount = 0;
            shippingFee = 0;
            taxAmount = 0;
            appliedDiscountCode = "";
        } else {
            JOptionPane.showMessageDialog(this, "Failed to place order. Check inventory or connection.");
        }
    }

    private void showInvoiceDialog(Order order, double finalTotal) {
        JDialog invoiceDialog = new JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this),
                "Order Confirmation", true);
        invoiceDialog.setSize(500, 600);
        invoiceDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Theme.CREAM);

        // Thank you message
        JLabel thankYouLabel = new JLabel("Thank You for Your Order!");
        thankYouLabel.setFont(new Font("Arial", Font.BOLD, 24));
        thankYouLabel.setForeground(new Color(0, 128, 0));
        thankYouLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(thankYouLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Order ID
        JLabel orderIdLabel = new JLabel("Order ID: #" + order.getOrderId());
        orderIdLabel.setFont(new Font("Arial", Font.BOLD, 16));
        orderIdLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(orderIdLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Delivery estimate
        JLabel deliveryLabel = new JLabel("Estimated Delivery: 5-7 Business Days");
        deliveryLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        deliveryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(deliveryLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Separator
        JSeparator separator1 = new JSeparator();
        separator1.setMaximumSize(new Dimension(450, 1));
        contentPanel.add(separator1);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Order summary
        JLabel summaryLabel = new JLabel("Order Summary:");
        summaryLabel.setFont(new Font("Arial", Font.BOLD, 14));
        summaryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(summaryLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setMaximumSize(new Dimension(400, 200));

        detailsPanel.add(new JLabel("Subtotal:"));
        detailsPanel.add(new JLabel(String.format("$%.2f", subtotal)));

        if (discountAmount > 0) {
            detailsPanel.add(new JLabel("Discount:"));
            JLabel discLabel = new JLabel(String.format("-$%.2f", discountAmount));
            discLabel.setForeground(new Color(220, 20, 60));
            detailsPanel.add(discLabel);
        }

        detailsPanel.add(new JLabel("Shipping:"));
        detailsPanel.add(new JLabel(shippingFee == 0 ? "FREE" : String.format("$%.2f", shippingFee)));

        detailsPanel.add(new JLabel("Tax (5%):"));
        detailsPanel.add(new JLabel(String.format("$%.2f", taxAmount)));

        contentPanel.add(detailsPanel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Separator
        JSeparator separator2 = new JSeparator();
        separator2.setMaximumSize(new Dimension(450, 1));
        contentPanel.add(separator2);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Final total
        JLabel totalLabel = new JLabel(String.format("Total: $%.2f", finalTotal));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 20));
        totalLabel.setForeground(new Color(0, 51, 153));
        totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(totalLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Contact info
        JLabel contactLabel = new JLabel("<html><center>Questions? Contact us:<br>" +
                "Phone: +1-800-IKISTRO<br>" +
                "Email: support@ikistro.com</center></html>");
        contactLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        contactLabel.setForeground(Color.GRAY);
        contactLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(contactLabel);

        invoiceDialog.add(new JScrollPane(contentPanel), BorderLayout.CENTER);

        // Close button
        JButton closeBtn = new JButton("Close");
        closeBtn.setBackground(new Color(0, 51, 153));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFont(new Font("Arial", Font.BOLD, 14));
        closeBtn.addActionListener(e -> invoiceDialog.dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.add(closeBtn);
        invoiceDialog.add(btnPanel, BorderLayout.SOUTH);

        invoiceDialog.setLocationRelativeTo(this);
        invoiceDialog.setVisible(true);
    }

    private boolean showPaymentDetailsDialog(String method) {
        // Common Phone Number Field
        JTextField phoneField = new JTextField();

        if ("PayPal".equals(method)) {
            JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
            JTextField emailField = new JTextField();

            panel.add(new JLabel("PayPal Email:"));
            panel.add(emailField);
            panel.add(new JLabel("Phone Number:"));
            panel.add(phoneField);

            while (true) {
                int result = JOptionPane.showConfirmDialog(this, panel, "PayPal Checkout",
                        JOptionPane.OK_CANCEL_OPTION);
                if (result != JOptionPane.OK_OPTION)
                    return false;

                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();

                if (email.isEmpty() || !email.contains("@")) {
                    JOptionPane.showMessageDialog(this, "Invalid Email.", "Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                if (!phone.matches("\\d{10,15}")) { // Simple regex for 10-15 digits
                    JOptionPane.showMessageDialog(this, "Invalid Phone Number. Must be 10-15 digits.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                return true;
            }
        } else {
            // Credit/Debit Card
            JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
            JTextField cardNumField = new JTextField();
            JTextField expiryField = new JTextField();
            JTextField cvvField = new JTextField();

            panel.add(new JLabel("Card Number (16 digits):"));
            panel.add(cardNumField);
            panel.add(new JLabel("Expiry Date (MM/YY):"));
            panel.add(expiryField);
            panel.add(new JLabel("CVV (3 digits):"));
            panel.add(cvvField);
            panel.add(new JLabel("Phone Number:"));
            panel.add(phoneField);

            while (true) {
                int result = JOptionPane.showConfirmDialog(this, panel, "Enter " + method + " Details",
                        JOptionPane.OK_CANCEL_OPTION);
                if (result != JOptionPane.OK_OPTION) {
                    return false;
                }

                String cardNum = cardNumField.getText().trim();
                String expiry = expiryField.getText().trim();
                String cvv = cvvField.getText().trim();
                String phone = phoneField.getText().trim();

                if (!cardNum.matches("\\d{16}")) {
                    JOptionPane.showMessageDialog(this, "Invalid Card Number. Must be 16 digits.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                if (!expiry.matches("(0[1-9]|1[0-2])/\\d{2}")) {
                    JOptionPane.showMessageDialog(this, "Invalid Expiry Date. Format: MM/YY", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                if (!cvv.matches("\\d{3}")) {
                    JOptionPane.showMessageDialog(this, "Invalid CVV. Must be 3 digits.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                if (!phone.matches("\\d{10,15}")) {
                    JOptionPane.showMessageDialog(this, "Invalid Phone Number. Must be 10-15 digits.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                return true;
            }
        }
    }

    private void addFormField(JPanel panel, String labelText, JComponent field) {
        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.setBorder(new javax.swing.border.EmptyBorder(5, 5, 5, 5));
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        fieldPanel.add(label, BorderLayout.NORTH);
        fieldPanel.add(field, BorderLayout.CENTER);
        panel.add(fieldPanel);
    }
}
