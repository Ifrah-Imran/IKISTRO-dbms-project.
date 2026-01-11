package ui;

import backend.ProductService;
import models.Product;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AllProductsPanel extends JPanel {
    private MainFrame mainFrame;
    private ProductService productService;
    private JPanel listPanel;
    private List<Product> allProducts;

    public AllProductsPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.productService = new ProductService();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(20, 40, 20, 40));
        JLabel title = new JLabel("All Products");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setForeground(new Color(30, 30, 30));
        header.add(title);
        add(header, BorderLayout.NORTH);

        // Content
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        loadProducts();
    }

    private void loadProducts() {
        allProducts = productService.getAllProducts();
        renderProducts();
    }

    private void renderProducts() {
        listPanel.removeAll();

        for (Product p : allProducts) {
            listPanel.add(createProductRow(p));
            listPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(230, 230, 230));
            sep.setMaximumSize(new Dimension(2000, 1));
            listPanel.add(sep);
            listPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    public void sortProducts(boolean ascending) {
        if (allProducts == null)
            return;

        if (ascending) {
            allProducts.sort(Comparator.comparingDouble(Product::getProductPrice));
        } else {
            allProducts.sort((p1, p2) -> Double.compare(p2.getProductPrice(), p1.getProductPrice()));
        }
        renderProducts();
    }

    public void filterByCategory(int categoryId) {
        allProducts = productService.getProductsByCategory(categoryId);
        renderProducts();
    }

    public void resetFilter() {
        loadProducts();
    }

    private JPanel createProductRow(Product p) {
        JPanel row = new JPanel(new BorderLayout(30, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(0, 40, 0, 40));
        row.setMaximumSize(new Dimension(2000, 180));
        row.setPreferredSize(new Dimension(800, 180));

        // Image
        JLabel imgLabel = new JLabel();
        imgLabel.setPreferredSize(new Dimension(180, 180));
        imgLabel.setOpaque(true);
        imgLabel.setBackground(new Color(250, 250, 250));
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        if (p.getImage() != null && !p.getImage().isEmpty()) {
            ImageIcon icon = new ImageIcon(p.getImage());
            Image img = icon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH);
            imgLabel.setIcon(new ImageIcon(img));
        } else {
            imgLabel.setText("No Image");
            imgLabel.setForeground(Color.GRAY);
        }
        row.add(imgLabel, BorderLayout.WEST);

        // Info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel name = new JLabel(p.getProductName());
        name.setFont(new Font("SansSerif", Font.BOLD, 20));
        name.setForeground(new Color(30, 30, 30));
        infoPanel.add(name);
        infoPanel.add(Box.createVerticalStrut(8));

        JLabel price = new JLabel("$" + String.format("%.2f", p.getProductPrice()));
        price.setFont(new Font("SansSerif", Font.PLAIN, 18));
        price.setForeground(new Color(0, 100, 0));
        infoPanel.add(price);
        infoPanel.add(Box.createVerticalStrut(12));

        JTextArea desc = new JTextArea(p.getDescription());
        desc.setWrapStyleWord(true);
        desc.setLineWrap(true);
        desc.setEditable(false);
        desc.setBackground(Color.WHITE);
        desc.setForeground(Color.GRAY);
        desc.setFont(new Font("SansSerif", Font.PLAIN, 14));
        desc.setMaximumSize(new Dimension(800, 80));
        infoPanel.add(desc);

        row.add(infoPanel, BorderLayout.CENTER);

        // Action
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(Color.WHITE);

        JButton viewBtn = new JButton("View Details");
        viewBtn.setBackground(new Color(30, 30, 30));
        viewBtn.setForeground(Color.WHITE);
        viewBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        viewBtn.setFocusPainted(false);
        viewBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewBtn.addActionListener(e -> mainFrame.showProductDetails(p));

        actionPanel.add(viewBtn);
        row.add(actionPanel, BorderLayout.EAST);

        // Hover
        row.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                row.setBackground(new Color(250, 250, 250));
                infoPanel.setBackground(new Color(250, 250, 250));
                actionPanel.setBackground(new Color(250, 250, 250));
                desc.setBackground(new Color(250, 250, 250));
            }

            public void mouseExited(MouseEvent e) {
                row.setBackground(Color.WHITE);
                infoPanel.setBackground(Color.WHITE);
                actionPanel.setBackground(Color.WHITE);
                desc.setBackground(Color.WHITE);
            }
        });

        return row;
    }
}
