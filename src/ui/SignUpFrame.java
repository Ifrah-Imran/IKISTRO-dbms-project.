package ui;

import backend.UserService;
import models.User;

import javax.swing.*;
import java.awt.*;

/**
 * The SignUpFrame class handles new user registration.
 * It collects user details and creates a new account in the system.
 */
public class SignUpFrame extends JFrame {
    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private UserService userService;

    /**
     * Constructor to create the Sign Up screen.
     */
    public SignUpFrame() {
        userService = new UserService();

        setTitle("IKEA Store - Sign Up");
        setSize(400, 400); // Increased height slightly
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Form Panel
        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        panel.setBackground(Theme.CREAM);

        // Name
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(Theme.getBodyFont(Font.BOLD, 14));
        nameLabel.setForeground(Theme.CHARCOAL);
        panel.add(nameLabel);

        nameField = new JTextField();
        nameField.setFont(Theme.getBodyFont(Font.PLAIN, 14));
        panel.add(nameField);

        // Email
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(Theme.getBodyFont(Font.BOLD, 14));
        emailLabel.setForeground(Theme.CHARCOAL);
        panel.add(emailLabel);

        emailField = new JTextField();
        emailField.setFont(Theme.getBodyFont(Font.PLAIN, 14));
        panel.add(emailField);

        // Password
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(Theme.getBodyFont(Font.BOLD, 14));
        passLabel.setForeground(Theme.CHARCOAL);
        panel.add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setFont(Theme.getBodyFont(Font.PLAIN, 14));
        panel.add(passwordField);

        // Register Button
        JButton registerButton = new JButton("Register");
        registerButton.setFont(Theme.getBodyFont(Font.BOLD, 14));
        registerButton.setBackground(Theme.DARK_BROWN);
        registerButton.setForeground(Theme.CREAM);
        registerButton.setFocusPainted(false);
        registerButton.addActionListener(e -> register());
        panel.add(registerButton);

        // Back Button
        JButton backButton = new JButton("Back to Login");
        backButton.setFont(Theme.getBodyFont(Font.PLAIN, 14));
        backButton.setBackground(Theme.BEIGE); // Lighter button for secondary action
        backButton.setForeground(Theme.CHARCOAL);
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> {
            new LoginFrame("CUSTOMER").setVisible(true);
            dispose();
        });
        panel.add(backButton);

        add(panel, BorderLayout.CENTER);
    }

    /**
     * Handles the registration logic.
     */
    private void register() {
        String name = nameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        // Basic validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create a new User object
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole("CUSTOMER"); // Default role is always CUSTOMER

        // Attempt to sign up via UserService
        if (userService.signUp(user)) {
            JOptionPane.showMessageDialog(this, "Registration Successful! Please Login.");
            new LoginFrame("CUSTOMER").setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Registration Failed. Email might be taken.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
