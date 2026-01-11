package ui;

import backend.ReviewService;
import models.Product;
import models.Review;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.List;

public class ProductPagePanel extends JPanel {
    private MainFrame mainFrame;
    private Product product;
    private ReviewService reviewService;

    private JLabel nameLabel;
    private JLabel priceLabel;
    private JLabel productCodeLabel;
    private StarRatingPanel ratingPanel; // Custom star panel
    private JLabel ratingTextLabel;
    private JLabel imageLabel;

    // Tags Panel - promoted to field to access in setProduct
    private JPanel tagsPanel;

    private JTextArea descriptionArea;
    private JComboBox<String> sizeComboBox;
    private JComboBox<String> colorComboBox;
    private JPanel reviewsPanel;
    private JButton addReviewBtn;

    private backend.ProductService productService;

    public ProductPagePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.reviewService = new ReviewService();
        this.productService = new backend.ProductService();

        setLayout(new BorderLayout());
        setBackground(Theme.CREAM);

        // Top: Back Button
        JButton backButton = new JButton("Back to Home");
        backButton.addActionListener(e -> mainFrame.showPanel("HOME"));
        add(backButton, BorderLayout.NORTH);

        // Center: Product Info
        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        centerPanel.setBackground(Theme.CREAM);

        // Left: Image
        imageLabel = new JLabel("Image", SwingConstants.CENTER);
        centerPanel.add(imageLabel);

        // Right: Details
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        detailsPanel.setBackground(Theme.CREAM);

        nameLabel = new JLabel("Product Name");
        nameLabel.setFont(Theme.getHeaderFont(Font.BOLD, 24));
        detailsPanel.add(nameLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        priceLabel = new JLabel("$0.00");
        priceLabel.setFont(Theme.getBodyFont(Font.PLAIN, 18));
        priceLabel.setForeground(Theme.SUCCESS_GREEN);
        detailsPanel.add(priceLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Tags Panel
        tagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        tagsPanel.setBackground(Theme.CREAM);
        detailsPanel.add(tagsPanel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        productCodeLabel = new JLabel("Product Code: N/A");
        productCodeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        productCodeLabel.setForeground(Color.GRAY);
        detailsPanel.add(productCodeLabel);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Rating Section
        JPanel ratingContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        ratingContainer.setBackground(Theme.CREAM);
        ratingPanel = new StarRatingPanel();
        ratingTextLabel = new JLabel("(0 reviews)");
        ratingTextLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        ratingContainer.add(ratingPanel);
        ratingContainer.add(ratingTextLabel);
        detailsPanel.add(ratingContainer);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Description
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(new Font("Arial", Font.BOLD, 14));
        detailsPanel.add(descLabel);
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(Theme.BEIGE);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setMaximumSize(new Dimension(400, 80));
        detailsPanel.add(descScroll);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Size Selection
        JLabel sizeLabel = new JLabel("Size:");
        sizeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        detailsPanel.add(sizeLabel);
        sizeComboBox = new JComboBox<>();
        sizeComboBox.setMaximumSize(new Dimension(200, 30));
        detailsPanel.add(sizeComboBox);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Color Selection
        JLabel colorLabel = new JLabel("Color:");
        colorLabel.setFont(new Font("Arial", Font.BOLD, 12));
        detailsPanel.add(colorLabel);
        colorComboBox = new JComboBox<>();
        colorComboBox.setMaximumSize(new Dimension(200, 30));
        detailsPanel.add(colorComboBox);
        detailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JButton addToCartBtn = new JButton("Add to Cart");
        addToCartBtn.setBackground(Theme.DARK_BROWN);
        addToCartBtn.setForeground(Theme.CREAM);
        addToCartBtn.setFont(Theme.getBodyFont(Font.BOLD, 14));
        addToCartBtn.addActionListener(e -> {
            if (product != null) {
                String size = (String) sizeComboBox.getSelectedItem();
                String color = (String) colorComboBox.getSelectedItem();
                addToCartWithOptions(size, color);
            }
        });
        detailsPanel.add(addToCartBtn);

        JButton wishlistBtn = new JButton("Add to Wishlist");
        wishlistBtn.addActionListener(e -> {
            if (product != null)
                mainFrame.addToWishlist(product);
        });
        detailsPanel.add(wishlistBtn);

        centerPanel.add(detailsPanel);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom: Reviews
        reviewsPanel = new JPanel();
        reviewsPanel.setLayout(new BoxLayout(reviewsPanel, BoxLayout.Y_AXIS));
        JScrollPane reviewScroll = new JScrollPane(reviewsPanel);
        reviewScroll.setPreferredSize(new Dimension(800, 200));
        reviewScroll.setBorder(BorderFactory.createTitledBorder("Reviews"));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(reviewScroll, BorderLayout.CENTER);

        addReviewBtn = new JButton("Write a Review");
        addReviewBtn.addActionListener(e -> showAddReviewDialog());
        addReviewBtn.setEnabled(false); // Initially disabled
        bottomPanel.add(addReviewBtn, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void addToCartWithOptions(String size, String color) {
        // Find existing item with same product, size, and color
        boolean found = false;
        for (models.CartItem item : mainFrame.getCart()) {
            if (item.getProduct().getProductId() == product.getProductId() &&
                    item.getSelectedSize().equals(size != null ? size : "") &&
                    item.getSelectedColor().equals(color != null ? color : "")) {
                item.setQuantity(item.getQuantity() + 1);
                found = true;
                break;
            }
        }
        if (!found) {
            mainFrame.getCart().add(new models.CartItem(product, 1, size, color));
        }

        String msg = "Added to cart!";
        if (size != null && !size.isEmpty())
            msg += "\nSize: " + size;
        if (color != null && !color.isEmpty())
            msg += "\nColor: " + color;
        JOptionPane.showMessageDialog(this, msg);
    }

    public void setProduct(Product product) {
        this.product = product;
        nameLabel.setText(product.getProductName());
        priceLabel.setText("$" + String.format("%.2f", product.getProductPrice()));

        // Product Code
        String code = product.getProductCode();
        productCodeLabel.setText("Product Code: " + (code != null && !code.isEmpty() ? code : "N/A"));

        // Description
        String desc = product.getDescription();
        descriptionArea.setText(desc != null && !desc.isEmpty() ? desc : "No description available.");

        // Sizes
        sizeComboBox.removeAllItems();
        if (product.getSizes() != null && !product.getSizes().isEmpty()) {
            for (String size : product.getSizes()) {
                sizeComboBox.addItem(size);
            }
            sizeComboBox.setEnabled(true);
        } else {
            sizeComboBox.addItem("N/A");
            sizeComboBox.setEnabled(false);
        }

        // Colors
        colorComboBox.removeAllItems();
        if (product.getColors() != null && !product.getColors().isEmpty()) {
            for (String color : product.getColors()) {
                colorComboBox.addItem(color);
            }
            colorComboBox.setEnabled(true);
        } else {
            colorComboBox.addItem("N/A");
            colorComboBox.setEnabled(false);
        }

        if (product.getImage() != null && !product.getImage().isEmpty()) {
            ImageIcon icon = new ImageIcon(product.getImage());
            Image img = icon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(img));
            imageLabel.setText("");
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText("No Image");
        }

        // Populate Tags
        tagsPanel.removeAll();
        if (product.getTags() != null && !product.getTags().isEmpty()) {
            for (String tag : product.getTags()) {
                JLabel tagLabel = new JLabel(" " + tag + " ");
                tagLabel.setOpaque(true);
                tagLabel.setBackground(Theme.PRIMARY_ACCENT);
                tagLabel.setForeground(Color.WHITE);
                tagLabel.setFont(new Font("Arial", Font.BOLD, 12));
                tagLabel.setBorder(BorderFactory.createLineBorder(Theme.PRIMARY_ACCENT, 1, true));
                tagsPanel.add(tagLabel);
            }
        }
        tagsPanel.revalidate();
        tagsPanel.repaint();

        // Check if user has purchased this product
        boolean hasPurchased = reviewService.hasUserPurchasedProduct(
                mainFrame.getUser().getUserId(),
                product.getProductId());

        // Enable review button only if user has purchased this product
        addReviewBtn.setEnabled(hasPurchased);
        if (hasPurchased) {
            addReviewBtn.setToolTipText("Share your experience with this product");
        } else {
            addReviewBtn.setToolTipText("You can only review products you have purchased");
        }

        loadReviews();
        updateRatingDisplay();
    }

    private void updateRatingDisplay() {
        List<Review> reviews = reviewService.getReviewsForProduct(product.getProductId());
        if (reviews.isEmpty()) {
            ratingPanel.setRating(0);
            ratingTextLabel.setText("(0 reviews)");
        } else {
            double avgRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
            ratingPanel.setRating(avgRating);
            ratingTextLabel.setText(String.format("%.1f (%d reviews)", avgRating, reviews.size()));
        }
    }

    private void loadReviews() {
        reviewsPanel.removeAll();
        List<Review> reviews = reviewService.getReviewsForProduct(product.getProductId());

        if (reviews.isEmpty()) {
            JLabel noReviews = new JLabel("No reviews yet. Be the first to review!");
            noReviews.setForeground(Color.GRAY);
            reviewsPanel.add(noReviews);
        } else {
            for (Review r : reviews) {
                JPanel reviewCard = new JPanel(new BorderLayout());
                reviewCard.setBorder(BorderFactory.createEtchedBorder());
                reviewCard.setBackground(Theme.CREAM);

                // Header with stars
                JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
                header.setBackground(Theme.CREAM);
                StarRatingPanel stars = new StarRatingPanel();
                stars.setRating(r.getRating());
                stars.setPreferredSize(new Dimension(80, 15)); // Smaller stars
                header.add(stars);

                JLabel titleLabel = new JLabel(r.getTitle());
                titleLabel.setFont(Theme.getBodyFont(Font.BOLD, 12));
                header.add(titleLabel);

                reviewCard.add(header, BorderLayout.NORTH);

                JTextArea text = new JTextArea(r.getText());
                text.setEditable(false);
                text.setLineWrap(true);
                text.setWrapStyleWord(true);
                text.setBackground(Theme.CREAM);
                text.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                reviewCard.add(text, BorderLayout.CENTER);

                reviewsPanel.add(reviewCard);
                reviewsPanel.add(Box.createVerticalStrut(5));
            }
        }
        reviewsPanel.revalidate();
        reviewsPanel.repaint();
    }

    private void showAddReviewDialog() {
        // Double-check that user has purchased the product
        if (!reviewService.hasUserPurchasedProduct(
                mainFrame.getUser().getUserId(),
                product.getProductId())) {
            JOptionPane.showMessageDialog(this,
                    "You can only review products you have purchased.",
                    "Access Denied",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(mainFrame, "Write Review", true);
        dialog.setSize(300, 300);
        dialog.setLayout(new GridLayout(5, 2));

        JTextField titleField = new JTextField();
        JTextArea textArea = new JTextArea();
        JComboBox<Integer> ratingBox = new JComboBox<>(new Integer[] { 1, 2, 3, 4, 5 });

        dialog.add(new JLabel("Title:"));
        dialog.add(titleField);
        dialog.add(new JLabel("Rating:"));
        dialog.add(ratingBox);
        dialog.add(new JLabel("Review:"));
        dialog.add(new JScrollPane(textArea));

        JButton submitBtn = new JButton("Submit");
        submitBtn.addActionListener(e -> {
            Review review = new Review();
            review.setProductId(product.getProductId());
            review.setUserId(mainFrame.getUser().getUserId());
            review.setTitle(titleField.getText());
            review.setText(textArea.getText());
            review.setRating((Integer) ratingBox.getSelectedItem());

            if (reviewService.addReview(review)) {
                JOptionPane.showMessageDialog(dialog, "Review added!");
                dialog.dispose();
                loadReviews();
                updateRatingDisplay();
            } else {
                JOptionPane.showMessageDialog(dialog, "Failed to add review.");
            }
        });
        dialog.add(submitBtn);

        dialog.setVisible(true);
    }

    // Inner class for drawing stars
    private class StarRatingPanel extends JPanel {
        private double rating = 0;

        public StarRatingPanel() {
            setPreferredSize(new Dimension(100, 20));
            setBackground(Theme.CREAM);
        }

        public void setRating(double rating) {
            this.rating = rating;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int starSize = getHeight();
            int gap = 2;

            for (int i = 0; i < 5; i++) {
                int x = i * (starSize + gap);
                drawStar(g2d, x, 0, starSize, i < rating);
            }
        }

        private void drawStar(Graphics2D g2d, int x, int y, int size, boolean filled) {
            Path2D star = new Path2D.Double();
            double cx = x + size / 2.0;
            double cy = y + size / 2.0;
            double outerRadius = size / 2.0;
            double innerRadius = outerRadius / 2.5;

            for (int i = 0; i < 10; i++) {
                double angle = Math.PI / 2 + i * Math.PI / 5; // Start at top
                double r = (i % 2 == 0) ? outerRadius : innerRadius;
                double px = cx + Math.cos(angle) * r; // Note: cos/sin swapped for rotation if needed, but standard is
                                                      // fine
                // Adjusting angle to point up: -PI/2 start
                angle = -Math.PI / 2 + i * Math.PI / 5;
                px = cx + Math.cos(angle) * r;
                double py = cy + Math.sin(angle) * r;

                if (i == 0)
                    star.moveTo(px, py);
                else
                    star.lineTo(px, py);
            }
            star.closePath();

            if (filled) {
                g2d.setColor(Theme.GOLD); // Gold
                g2d.fill(star);
            }
            g2d.setColor(Theme.MUTE_GOLD); // GoldenRod outline
            g2d.draw(star);
        }
    }
}
