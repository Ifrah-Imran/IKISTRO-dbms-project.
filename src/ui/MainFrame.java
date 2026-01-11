
package ui;

import backend.WishlistService;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import models.CartItem;
import models.Product;
import models.User;

/**
 * The MainFrame class is the main window of the application.
 * It holds all the different panels (Home, Cart, Wishlist, etc.) and manages
 * navigation.
 */
public class MainFrame extends JFrame {

    private User user; // The currently logged-in user
    private JPanel mainPanel; // The panel that holds different views (CardLayout)
    private CardLayout cardLayout; // Layout manager to switch between views

    // Panels
    private HomePagePanel homePagePanel;
    private ProductPagePanel productPagePanel;
    private AllProductsPanel allProductsPanel;

    // Data
    private List<CartItem> cart = new ArrayList<>(); // Shopping cart items
    private List<Product> wishlist = new ArrayList<>(); // Wishlist items
    private WishlistService wishlistService = new WishlistService();

    // Header components
    private JLabel cartCountLabel;

    /**
     * Constructor to create the Main Application Window.
     * 
     * @param user The user who logged in.
     */
    public MainFrame(User user) {
        Theme.setupUI(); // Apply global font defaults
        this.user = user;

        setTitle("iKistro - " + user.getName());
        setSize(1200, 800); // Larger default size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen

        // Main Layout
        setLayout(new BorderLayout());

        // 0. Free Shipping Banner
        JPanel topBanner = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topBanner.setBackground(Theme.DARK_BROWN);
        JLabel shippingLabel = new JLabel("FREE SHIPPING on orders over $500! Shop Now.");
        shippingLabel.setForeground(Theme.CREAM);
        shippingLabel.setFont(Theme.getBodyFont(Font.BOLD, 12));
        topBanner.add(shippingLabel);

        // 1. Custom Header (Center)
        // We wrap banner and header in a single NORTH panel.
        JPanel northContainer = new JPanel(new BorderLayout());
        northContainer.add(topBanner, BorderLayout.NORTH);
        northContainer.add(createCustomHeader(), BorderLayout.CENTER);
        add(northContainer, BorderLayout.NORTH);

        // 2. Main Content Panel
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(Theme.CREAM);

        // Initialize and add Panels
        homePagePanel = new HomePagePanel(this);
        mainPanel.add(homePagePanel, "HOME");
        mainPanel.add(new CartPanel(this), "CART");
        mainPanel.add(new WishlistPanel(this), "WISHLIST");

        productPagePanel = new ProductPagePanel(this);
        mainPanel.add(productPagePanel, "PRODUCT_DETAILS");
        mainPanel.add(new HelpPanel(), "HELP");
        mainPanel.add(new ProductKitsPanel(this), "KITS");

        allProductsPanel = new AllProductsPanel(this);
        mainPanel.add(allProductsPanel, "ALL_PRODUCTS");

        // Admin panel only for admins
        if (isAdmin()) {
            mainPanel.add(new AdminPanel(this), "ADMIN");
        }

        add(mainPanel, BorderLayout.CENTER);

        // Show panel based on role
        if (isAdmin()) {
            showPanel("ADMIN");
        } else {
            // Load persisted wishlist for the user
            try {
                wishlist = wishlistService.getWishlistForUser(user.getUserId());
            } catch (Exception ex) {
                wishlist = new ArrayList<>();
            }
            showPanel("HOME");
        }
    }

    /**
     * Creates the custom header with Logo, Menu, and Icons.
     */
    private JPanel createCustomHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.BEIGE);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Left: Hamburger Menu & Logo
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setBackground(Theme.BEIGE);

        if (!isAdmin()) {
            JButton menuBtn = createIconButton("\u2630", "Menu"); // Hamburger icon
            menuBtn.setForeground(Theme.CHARCOAL);
            menuBtn.addActionListener(e -> showSideMenu(menuBtn));
            leftPanel.add(menuBtn);
        }

        JLabel logoLabel = new JLabel("iKistro");
        logoLabel.setFont(Theme.getHeaderFont(Font.BOLD, 32));
        logoLabel.setForeground(Theme.DARK_BROWN);
        logoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isAdmin())
                    showPanel("HOME");
            }
        });
        leftPanel.add(logoLabel);

        headerPanel.add(leftPanel, BorderLayout.WEST);

        // Right: Icons & User Menu
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        rightPanel.setBackground(Theme.BEIGE);

        if (!isAdmin()) {
            // Help Icon
            JButton helpBtn = createIconButton("?", "Help");
            helpBtn.setForeground(Theme.CHARCOAL);
            helpBtn.addActionListener(e -> showPanel("HELP"));
            rightPanel.add(helpBtn);

            // Wishlist Icon
            JButton wishlistBtn = createIconButton("♥", "Wishlist");
            wishlistBtn.setForeground(Theme.CHARCOAL);
            wishlistBtn.addActionListener(e -> showPanel("WISHLIST"));
            rightPanel.add(wishlistBtn);

            // Cart Icon with Badge
            JLayeredPane cartPane = new JLayeredPane();
            cartPane.setPreferredSize(new Dimension(40, 40));

            JButton cartBtn = createIconButton("\uD83D\uDED2", "Cart"); // Shopping cart unicode
            cartBtn.setForeground(Theme.CHARCOAL);
            cartBtn.setBounds(0, 5, 30, 30);
            cartBtn.addActionListener(e -> showPanel("CART"));

            cartCountLabel = new JLabel("0", SwingConstants.CENTER);
            cartCountLabel.setOpaque(true);
            cartCountLabel.setBackground(Theme.GOLD);
            cartCountLabel.setForeground(Color.WHITE);
            cartCountLabel.setFont(Theme.getBodyFont(Font.BOLD, 10));
            cartCountLabel.setBounds(20, 0, 18, 18);
            // Make round (simple approach)
            cartCountLabel.setBorder(BorderFactory.createLineBorder(Theme.BEIGE, 1));

            cartPane.add(cartBtn, JLayeredPane.DEFAULT_LAYER);
            cartPane.add(cartCountLabel, JLayeredPane.POPUP_LAYER);

            rightPanel.add(cartPane);
        }

        // User/Logout
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBackground(Theme.TAN);
        logoutBtn.setForeground(Theme.CHARCOAL);
        logoutBtn.setFont(Theme.getBodyFont(Font.BOLD, 12));
        logoutBtn.addActionListener(e -> {
            new RoleSelectionFrame().setVisible(true);
            dispose();
        });
        rightPanel.add(logoutBtn);

        headerPanel.add(rightPanel, BorderLayout.EAST);

        // Bottom border line
        JPanel container = new JPanel(new BorderLayout());
        container.add(headerPanel, BorderLayout.CENTER);
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.TAN);
        container.add(sep, BorderLayout.SOUTH);

        return container;
    }

    /**
     * Shows a popup side menu.
     */
    private void showSideMenu(Component invoker) {
        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(Color.WHITE);

        JMenuItem homeItem = new JMenuItem("Home");
        homeItem.addActionListener(e -> showPanel("HOME"));
        menu.add(homeItem);

        JMenuItem productsItem = new JMenuItem("All Products");
        productsItem.addActionListener(e -> {
            allProductsPanel.resetFilter();
            showPanel("ALL_PRODUCTS");
        });
        menu.add(productsItem);

        JMenuItem bundlesItem = new JMenuItem("Bundles / Kits");
        bundlesItem.addActionListener(e -> showPanel("KITS"));
        menu.add(bundlesItem);

        menu.addSeparator();

        JMenu categoriesMenu = new JMenu("Categories");
        JMenuItem shopCatItem = new JMenuItem("Shop by Category");
        shopCatItem.addActionListener(e -> showPanel("HOME"));
        categoriesMenu.add(shopCatItem);
        menu.add(categoriesMenu);

        JMenu sortMenu = new JMenu("Sort Products by Price");
        JMenuItem lowToHigh = new JMenuItem("Low to High");
        lowToHigh.addActionListener(e -> {
            showPanel("ALL_PRODUCTS");
            allProductsPanel.sortProducts(true);
        });
        sortMenu.add(lowToHigh);

        JMenuItem highToLow = new JMenuItem("High to Low");
        highToLow.addActionListener(e -> {
            showPanel("ALL_PRODUCTS");
            allProductsPanel.sortProducts(false);
        });
        sortMenu.add(highToLow);

        menu.add(sortMenu);

        menu.show(invoker, 0, invoker.getHeight());
    }

    /**
     * Helper to create a simple icon button.
     */
    private JButton createIconButton(String text, String tooltip) {
        JButton btn = new JButton(text);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(Theme.getBodyFont(Font.PLAIN, 20));
        btn.setToolTipText(tooltip);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Switches the main view to the specified panel name.
     */
    public void showPanel(String name) {
        cardLayout.show(mainPanel, name);
    }

    /**
     * Shows the details page for a specific product.
     */
    public void showProductDetails(Product product) {
        productPagePanel.setProduct(product);
        showPanel("PRODUCT_DETAILS");
    }

    // --- Cart Management ---

    public void addToCart(Product product, int quantity) {
        boolean found = false;
        for (CartItem item : cart) {
            if (item.getProduct().getProductId() == product.getProductId() &&
                    item.getSelectedSize().isEmpty() && item.getSelectedColor().isEmpty()) {
                item.setQuantity(item.getQuantity() + quantity);
                found = true;
                break;
            }
        }
        if (!found) {
            cart.add(new CartItem(product, quantity));
        }
        updateCartCount();
        JOptionPane.showMessageDialog(this, "Added to cart!");
    }

    // Overload for product page with options
    public void addToCart(CartItem newItem) {
        boolean found = false;
        for (CartItem item : cart) {
            if (item.getProduct().getProductId() == newItem.getProduct().getProductId() &&
                    item.getSelectedSize().equals(newItem.getSelectedSize()) &&
                    item.getSelectedColor().equals(newItem.getSelectedColor())) {
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                found = true;
                break;
            }
        }
        if (!found) {
            cart.add(newItem);
        }
        updateCartCount();
    }

    public void removeFromCart(CartItem item) {
        cart.remove(item);
        updateCartCount();
    }

    public List<CartItem> getCart() {
        return cart;
    }

    public void clearCart() {
        cart.clear();
        updateCartCount();
    }

    private void updateCartCount() {
        if (cartCountLabel != null) {
            int count = cart.stream().mapToInt(CartItem::getQuantity).sum();
            cartCountLabel.setText(String.valueOf(count));
            cartCountLabel.setVisible(count > 0);
        }
    }

    // --- Wishlist Management ---

    public void addToWishlist(Product product) {
        // Persist to DB and update local list
        boolean ok = wishlistService.addToWishlist(user.getUserId(), product.getProductId());
        if (ok) {
            // avoid duplicates in-memory
            boolean exists = false;
            for (Product p : wishlist)
                if (p.getProductId() == product.getProductId()) {
                    exists = true;
                    break;
                }
            if (!exists)
                wishlist.add(product);
            JOptionPane.showMessageDialog(this, "Added to wishlist!");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add to wishlist.");
        }
    }

    public void removeFromWishlist(Product product) {
        boolean ok = wishlistService.removeFromWishlist(user.getUserId(), product.getProductId());
        if (ok) {
            wishlist.removeIf(p -> p.getProductId() == product.getProductId());
            JOptionPane.showMessageDialog(this, "Removed from wishlist.");
        } else {
            // still remove from UI to avoid confusion
            wishlist.removeIf(p -> p.getProductId() == product.getProductId());
            JOptionPane.showMessageDialog(this, "Removed from wishlist.");
        }
    }

    public List<Product> getWishlist() {
        return wishlist;
    }

    public User getUser() {
        return user;
    }

    private boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(user.getRole());
    }
}
