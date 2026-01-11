package ui;

import backend.ProductService;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import models.Category;
import models.Product;

public class HomePagePanel extends JPanel {
    private MainFrame mainFrame;
    private ProductService productService;
    private JPanel contentPanel;

    // PATHS FOR BACKGROUND IMAGES - User can update these paths
    public static final String HERO_IMAGE_PATH ="C:\\Users\\ifrah\\Desktop\\ikea-store-v16.java\\ikea-store-v8.java\\src\\ui\\monochromatic-urban-minimal-landscape.png";
    public static final String CATEGORY_BG_BASE_PATH = "src/ui/category_"; // e.g., category_Furniture.jpg

    public HomePagePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.productService = new ProductService();

        setLayout(new BorderLayout());
        setLayout(new BorderLayout());
        setBackground(Theme.CREAM);

        // Scrollable Main Content
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Theme.CREAM);

        JScrollPane mainScroll = new JScrollPane(contentPanel);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScroll, BorderLayout.CENTER);

        loadContent();
    }

    public void loadContent() {
        contentPanel.removeAll();

        // 1. Hero Section
        contentPanel.add(createHeroSection());
        contentPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        // 2. Categories Section
        contentPanel.add(createSectionTitle("Shop by Category"));
        contentPanel.add(createCategoriesSection());
        contentPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        // 3. Bundles / Kits Call to Action
        contentPanel.add(createBundlesSection());
        contentPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        // 4. Best Sellers (Simulated)
        contentPanel.add(createSectionTitle("Best Sellers"));
        contentPanel.add(createBestSellersSection());
        contentPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // Helper for external calls (compatibility)
    public void loadProducts() {
        loadContent();
    }

    public void loadProducts(int categoryId) {
        contentPanel.removeAll();

        JButton backBtn = new JButton("← Back to Home");
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setForeground(Theme.DARK_BROWN);
        backBtn.setFont(Theme.getBodyFont(Font.BOLD, 14));
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.addActionListener(e -> loadContent());
        contentPanel.add(backBtn);

        JPanel grid = new JPanel(new GridLayout(0, 4, 20, 20));
        grid.setBackground(Theme.CREAM);
        grid.setBorder(new EmptyBorder(20, 40, 40, 40));

        List<Product> products = productService.getProductsByCategory(categoryId);
        for (Product p : products) {
            grid.add(createProductCard(p));
        }

        while (grid.getComponentCount() < 4 && !products.isEmpty()) {
            JPanel filler = new JPanel();
            filler.setOpaque(false);
            grid.add(filler);
        }

        contentPanel.add(grid);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createHeroSection() {
        // Use a layered pane or custom painting for background image
        JPanel hero = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Try to load image from path
                File imgFile = new File(HERO_IMAGE_PATH);
                if (imgFile.exists()) {
                    ImageIcon icon = new ImageIcon(HERO_IMAGE_PATH);
                    if (icon.getImage() != null) {
                        g.drawImage(icon.getImage(), 0, 0, getWidth(), getHeight(), this);
                    }
                } else {
                    // Fallback color if image not found
                    g.setColor(new Color(26, 56, 46)); // Fallback Lighter Forest
                    g.fillRect(0, 0, getWidth(), getHeight());
                }

                // Semi-transparent overlay for text readability
                g.setColor(new Color(0, 0, 0, 80)); // Slightly lighter overlay
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        hero.setLayout(new BorderLayout());
        hero.setPreferredSize(new Dimension(1000, 350));
        hero.setMaximumSize(new Dimension(2000, 350));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.setBorder(new EmptyBorder(0, 80, 0, 0));

        JLabel welcome = new JLabel("Welcome to iKistro");
        welcome.setFont(Theme.getHeaderFont(Font.BOLD, 56));
        welcome.setForeground(Theme.CREAM); // Keep light as it is on image overlay

        JLabel sub = new JLabel("Modern Furniture for Modern Living");
        sub.setFont(Theme.getHeaderFont(Font.PLAIN, 28));
        sub.setForeground(new Color(220, 220, 220));

        textPanel.add(Box.createVerticalGlue());
        textPanel.add(welcome);
        textPanel.add(Box.createVerticalStrut(15));
        textPanel.add(sub);
        textPanel.add(Box.createVerticalGlue());

        hero.add(textPanel, BorderLayout.WEST);

        return hero;
    }

    private JPanel createSectionTitle(String title) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Theme.CREAM);
        panel.setBorder(new EmptyBorder(0, 40, 10, 0));

        JLabel label = new JLabel(title);
        label.setFont(Theme.getHeaderFont(Font.BOLD, 28));
        label.setForeground(Theme.DARK_BROWN);
        panel.add(label);

        panel.setMaximumSize(new Dimension(2000, 60));
        return panel;
    }

    private JPanel createCategoriesSection() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
        panel.setBackground(Theme.CREAM);

        List<Category> categories = productService.getAllCategories();
        for (Category c : categories) {
            // Styled Category Button
            JButton btn = new JButton(c.getCategoryName());
            btn.setPreferredSize(new Dimension(180, 80));
            btn.setBackground(Theme.PRIMARY_ACCENT); // Forest Green
            btn.setForeground(Color.WHITE);
            btn.setFont(Theme.getBodyFont(Font.BOLD, 18));
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createLineBorder(Theme.BRONZE, 2)); // Bronze border
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    btn.setBackground(Theme.BRONZE); // Bronze on hover
                    btn.setBorder(BorderFactory.createLineBorder(Theme.PRIMARY_ACCENT, 2));
                }

                public void mouseExited(MouseEvent e) {
                    btn.setBackground(Theme.PRIMARY_ACCENT);
                    btn.setBorder(BorderFactory.createLineBorder(Theme.BRONZE, 2));
                }
            });
            btn.addActionListener(e -> loadProducts(c.getCategoryId()));
            panel.add(btn);
        }

        return panel;
    }

    private JPanel createBundlesSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BEIGE);
        panel.setBorder(new EmptyBorder(40, 60, 40, 60));
        panel.setMaximumSize(new Dimension(1200, 200));

        // Use DARK_BROWN hex #654321 for compatibility with Theme
        JLabel label = new JLabel(
                "<html><div style='color: #654321;'><h2>Bundle & Save</h2><p>Exclusive deals on curated sets.</p></div></html>");

        JButton btn = new JButton("View Bundles");
        btn.setFont(Theme.getBodyFont(Font.BOLD, 16));
        btn.setBackground(Theme.TAN);
        btn.setForeground(Theme.CHARCOAL);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(150, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> mainFrame.showPanel("KITS"));

        panel.add(label, BorderLayout.WEST);
        panel.add(btn, BorderLayout.EAST);

        return panel;
    }

    private JPanel createBestSellersSection() {
        JPanel container = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        container.setBackground(Theme.CREAM);

        List<Product> products = productService.getAllProducts().stream().limit(5).collect(Collectors.toList());

        for (Product p : products) {
            container.add(createProductCard(p));
        }

        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(null);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setPreferredSize(new Dimension(1000, 350));
        scroll.setMaximumSize(new Dimension(2000, 350));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.CREAM);
        wrapper.add(scroll, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel createProductCard(Product p) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(220, 320));
        card.setBorder(BorderFactory.createLineBorder(Theme.TAN, 1));
        card.setBackground(Theme.BEIGE);

        // Image Placeholder
        JLabel imgLabel = new JLabel();
        imgLabel.setPreferredSize(new Dimension(220, 160));
        imgLabel.setOpaque(true);
        imgLabel.setBackground(new Color(240, 240, 240)); // Keep image bg light for contrast
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        if (p.getImage() != null && !p.getImage().isEmpty()) {
            ImageIcon icon = new ImageIcon(p.getImage());
            Image img = icon.getImage().getScaledInstance(180, 130, Image.SCALE_SMOOTH);
            imgLabel.setIcon(new ImageIcon(img));
        } else {
            imgLabel.setText("No Image");
            imgLabel.setForeground(Color.GRAY);
        }
        imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(imgLabel);

        card.add(Box.createVerticalStrut(15));

        // Name
        JLabel name = new JLabel(p.getProductName());
        name.setFont(Theme.getBodyFont(Font.BOLD, 15));
        name.setForeground(Theme.DARK_BROWN);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(name);

        // Price
        JLabel price = new JLabel("$" + String.format("%.2f", p.getProductPrice()));
        price.setFont(Theme.getBodyFont(Font.PLAIN, 15));
        price.setForeground(Theme.SUCCESS_GREEN);
        price.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(price);

        card.add(Box.createVerticalGlue());

        // Button
        JButton viewBtn = new JButton("View Details");
        viewBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        viewBtn.setBackground(Theme.TAN);
        viewBtn.setForeground(Theme.CHARCOAL);
        viewBtn.setFocusPainted(false);
        viewBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewBtn.addActionListener(e -> mainFrame.showProductDetails(p));
        card.add(viewBtn);

        card.add(Box.createVerticalStrut(15));

        // Hover effect
        card.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createLineBorder(Theme.GOLD, 2)); // Bronze border on hover
            }

            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createLineBorder(Theme.TAN, 1));
            }
        });

        return card;
    }
}
