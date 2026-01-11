package ui;

import backend.UserService;
import java.awt.*;
import javax.swing.*;
import models.User;

/**
 * The LoginFrame class handles user authentication.
 * It allows users to log in as either a "CUSTOMER" or "ADMIN".
 */
public class LoginFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private UserService userService;
    private String targetRole; // The role the user is trying to log in as

   
    public LoginFrame(String targetRole) {
        this.targetRole = targetRole;
        this.userService = new UserService();

        // Set up the window
        setTitle("iKistro - " + (targetRole.equals("ADMIN") ? "Admin" : "Customer") + " Login");
        setSize(900, 600); // Larger window for better UI
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Exit app when closed
                setLocationRelativeTo(null); // Center on screen
        setLayout(new BorderLayout());

        // Split Layout: Left Image/Branding, Right Login Form
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));

        // --- Left Side: Branding ---
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Theme.BLACK_FOREST); // Dark background

        JLabel brandLabel = new JLabel("iKistro", SwingConstants.CENTER);
        brandLabel.setFont(Theme.getHeaderFont(Font.BOLD, 48));
        brandLabel.setForeground(Theme.CORNSILK);
        leftPanel.add(brandLabel, BorderLayout.CENTER);

        JLabel subLabel = new JLabel("Premium Furniture Store", SwingConstants.CENTER);
        subLabel.setFont(Theme.getBodyFont(Font.PLAIN, 18));
        subLabel.setForeground(Theme.LIGHT_CARAMEL);
        subLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 200, 0));
        leftPanel.add(subLabel, BorderLayout.SOUTH);

        mainPanel.add(leftPanel);

        // --- Right Side: Login Form ---
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Theme.CORNSILK);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(100, 50, 100, 50)); // Padding

        // Welcome Text
        JLabel titleLabel = new JLabel("Welcome Back");
        titleLabel.setFont(Theme.getHeaderFont(Font.BOLD, 32));
        titleLabel.setForeground(Theme.BLACK_FOREST);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(titleLabel);

        JLabel roleLabel = new JLabel("Login as " + (targetRole.equals("ADMIN") ? "Administrator" : "Customer"));
        roleLabel.setFont(Theme.getBodyFont(Font.PLAIN, 14));
        roleLabel.setForeground(Theme.OLIVE_LEAF);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(roleLabel);

        rightPanel.add(Box.createVerticalStrut(40)); // Spacer

        // Email Input
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(Theme.getBodyFont(Font.BOLD, 12));
        emailLabel.setForeground(Theme.BLACK_FOREST);
        emailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(emailLabel);
        rightPanel.add(Box.createVerticalStrut(5));

        emailField = new JTextField();
        emailField.setMaximumSize(new Dimension(400, 40));
        emailField.setPreferredSize(new Dimension(400, 40));
        emailField.setFont(Theme.getBodyFont(Font.PLAIN, 14));
        emailField.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(emailField);

        rightPanel.add(Box.createVerticalStrut(20));

        // Password Input
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(Theme.getBodyFont(Font.BOLD, 12));
        passLabel.setForeground(Theme.BLACK_FOREST);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(passLabel);
        rightPanel.add(Box.createVerticalStrut(5));

        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(400, 40));
        passwordField.setPreferredSize(new Dimension(400, 40));
        passwordField.setFont(Theme.getBodyFont(Font.PLAIN, 14));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(passwordField);

        rightPanel.add(Box.createVerticalStrut(30));

        // Login Button
        JButton loginButton = new JButton("Login");
        loginButton.setMaximumSize(new Dimension(400, 45));
        loginButton.setBackground(Theme.BLACK_FOREST);
        loginButton.setForeground(Theme.CORNSILK);
        loginButton.setFont(Theme.getBodyFont(Font.BOLD, 14));
        loginButton.setFocusPainted(false);
        loginButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.addActionListener(e -> login()); // Call login() method when clicked
        rightPanel.add(loginButton);

        rightPanel.add(Box.createVerticalStrut(10));

        // Sign Up Button (Only for Customers)
        if ("CUSTOMER".equals(targetRole)) {
            JButton signUpButton = new JButton("Create Account");
            signUpButton.setMaximumSize(new Dimension(400, 45));
            signUpButton.setBackground(Theme.CORNSILK);
            signUpButton.setForeground(Theme.BLACK_FOREST);
            signUpButton.setFont(Theme.getBodyFont(Font.BOLD, 14));
            signUpButton.setFocusPainted(false);
            signUpButton.setBorder(BorderFactory.createLineBorder(Theme.BLACK_FOREST));
            signUpButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            signUpButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            signUpButton.addActionListener(e -> {
                new SignUpFrame().setVisible(true); // Open Sign Up screen
                dispose(); // Close Login screen
            });
            rightPanel.add(signUpButton);
        }

        rightPanel.add(Box.createVerticalGlue()); // Push content up

        // Back Button
        JButton backButton = new JButton("← Back");
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setForeground(Theme.OLIVE_LEAF);
        backButton.setFont(Theme.getBodyFont(Font.PLAIN, 12));
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        backButton.addActionListener(e -> {
            new RoleSelectionFrame().setVisible(true); // Go back to Role Selection
            dispose();
        });
        rightPanel.add(backButton);

        mainPanel.add(rightPanel);
        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Handles the login logic.
     */
    private void login() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        // Basic validation
        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both email and password", "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Attempt login via UserService
        User user = userService.login(email, password);

        if (user != null) {
            // Check if the user has the correct role (e.g., preventing a Customer from
            // logging in as Admin)
            if ("ADMIN".equals(targetRole) && !"ADMIN".equalsIgnoreCase(user.getRole())) {
                JOptionPane.showMessageDialog(this, "Access Denied: You are not an Admin", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Login successful
            JOptionPane.showMessageDialog(this, "Welcome " + user.getName() + "!");
            new MainFrame(user).setVisible(true); // Open Main App
            dispose(); // Close Login screen
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        // Seed data for testing purposes
        new backend.DataSeeder().seed();
        // Start the application
        SwingUtilities.invokeLater(() -> new RoleSelectionFrame().setVisible(true));
    }
}
