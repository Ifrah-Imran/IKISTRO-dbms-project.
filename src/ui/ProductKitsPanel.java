package ui;

import backend.ProductKitService;
import models.ProductKit;
import models.Product;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ProductKitsPanel extends JPanel {
    private MainFrame mainFrame;
    private ProductKitService kitService;
    private JPanel kitsContainer;

    public ProductKitsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.kitService = new ProductKitService();
        setLayout(new BorderLayout());
        setBackground(new Color(30, 30, 30)); // Dark background

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 30, 30));
        header.setBorder(new EmptyBorder(30, 50, 30, 50));

        JLabel title = new JLabel("Exclusive Product Kits");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.WEST);

        JLabel subtitle = new JLabel("Curated bundles for your home");
        subtitle.setFont(new Font("SansSerif", Font.ITALIC, 18));
        subtitle.setForeground(new Color(200, 200, 200));
        header.add(subtitle, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // Grid
        // Use FlowLayout for better control over card size, or GridBagLayout.
        // FlowLayout allows cards to keep their preferred size.
        kitsContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 30));
        kitsContainer.setBackground(new Color(30, 30, 30));
        kitsContainer.setBorder(new EmptyBorder(20, 40, 40, 40));

        JScrollPane scroll = new JScrollPane(kitsContainer);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(new Color(30, 30, 30));
        add(scroll, BorderLayout.CENTER);

        loadKits();
    }

    private void loadKits() {
        kitsContainer.removeAll();
        List<ProductKit> kits = kitService.getAllKits();

        if (kits.isEmpty()) {
            JLabel empty = new JLabel("No product kits available at the moment.", SwingConstants.CENTER);
            empty.setFont(new Font("SansSerif", Font.PLAIN, 16));
            empty.setForeground(Color.WHITE);
            kitsContainer.add(empty);
        } else {
            for (ProductKit kit : kits) {
                kitsContainer.add(createKitCard(kit));
            }
        }
        kitsContainer.revalidate();
        kitsContainer.repaint();
    }

    private JPanel createKitCard(ProductKit kit) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 100), 1),
                new EmptyBorder(0, 0, 0, 0)));
        card.setBackground(new Color(45, 45, 45)); // Slightly lighter dark
        card.setPreferredSize(new Dimension(300, 420)); // Smaller width, taller for content

        // Image Area (Placeholder or First Product Image)
        JLabel imgLabel = new JLabel();
        imgLabel.setPreferredSize(new Dimension(300, 180));
        imgLabel.setOpaque(true);
        imgLabel.setBackground(Color.WHITE); // White bg for image to pop
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);

        if (kit.getImage() != null && !kit.getImage().isEmpty()) {
            ImageIcon icon = new ImageIcon(kit.getImage());
            Image img = icon.getImage().getScaledInstance(300, 180, Image.SCALE_SMOOTH);
            imgLabel.setIcon(new ImageIcon(img));
            imgLabel.setText("");
        } else {
            // Fallback: Try first product image or text
            if (!kit.getProducts().isEmpty() && kit.getProducts().get(0).getImage() != null
                    && !kit.getProducts().get(0).getImage().isEmpty()) {
                ImageIcon icon = new ImageIcon(kit.getProducts().get(0).getImage());
                Image img = icon.getImage().getScaledInstance(300, 180, Image.SCALE_SMOOTH); // Match preferred size
                imgLabel.setIcon(new ImageIcon(img));
                imgLabel.setText("");
            } else {
                imgLabel.setText("No Image");
                imgLabel.setIcon(null);
            }
        }
        imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(imgLabel);

        // Content Container
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(new Color(45, 45, 45));
        content.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Name
        JLabel nameLabel = new JLabel(kit.getKitName());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(nameLabel);
        content.add(Box.createVerticalStrut(5));

        // Description (Simulated)
        JTextArea desc = new JTextArea(
                "Experience the perfect combination of style and functionality with this curated set.");
        desc.setWrapStyleWord(true);
        desc.setLineWrap(true);
        desc.setEditable(false);
        desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
        desc.setForeground(new Color(200, 200, 200));
        desc.setBackground(new Color(45, 45, 45));
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(desc);
        content.add(Box.createVerticalStrut(10));

        // Includes List (Truncated)
        JLabel incLabel = new JLabel("Includes: " + kit.getProducts().size() + " items");
        incLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        incLabel.setForeground(new Color(150, 150, 150));
        incLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(incLabel);

        content.add(Box.createVerticalGlue()); // Push bottom elements down

        // Price Section
        double totalPrice = kit.getProducts().stream().mapToDouble(Product::getProductPrice).sum();
        double discountedPrice = totalPrice * (1 - kit.getDiscountPercentage() / 100.0);

        JPanel pricePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pricePanel.setBackground(new Color(45, 45, 45));
        pricePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel oldPrice = new JLabel("$" + String.format("%.0f", totalPrice));
        Font baseFont = new Font("SansSerif", Font.PLAIN, 14);
        java.util.Map<java.awt.font.TextAttribute, Object> attributes = new java.util.HashMap<>(
                baseFont.getAttributes());
        attributes.put(java.awt.font.TextAttribute.STRIKETHROUGH, java.awt.font.TextAttribute.STRIKETHROUGH_ON);
        oldPrice.setFont(baseFont.deriveFont(attributes));
        oldPrice.setForeground(Color.GRAY);

        JLabel newPrice = new JLabel("$" + String.format("%.2f", discountedPrice));
        newPrice.setFont(new Font("SansSerif", Font.BOLD, 20));
        newPrice.setForeground(new Color(100, 255, 100)); // Bright Green

        pricePanel.add(newPrice);
        pricePanel.add(oldPrice);
        content.add(pricePanel);
        content.add(Box.createVerticalStrut(10));

        // Add Button
        JButton addToCartBtn = new JButton("Add Bundle");
        addToCartBtn.setBackground(Color.WHITE);
        addToCartBtn.setForeground(Color.BLACK);
        addToCartBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        addToCartBtn.setFocusPainted(false);
        addToCartBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addToCartBtn.setMaximumSize(new Dimension(300, 40));
        addToCartBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addToCartBtn.addActionListener(e -> {
            for (Product p : kit.getProducts()) {
                mainFrame.addToCart(p, 1);
            }
            JOptionPane.showMessageDialog(mainFrame, "All items in '" + kit.getKitName() + "' added to cart!");
        });
        content.add(addToCartBtn);

        card.add(content);
        return card;
    }
}
