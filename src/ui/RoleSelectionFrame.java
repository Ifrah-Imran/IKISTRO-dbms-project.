package ui;

import javax.swing.*;
import java.awt.*;

public class RoleSelectionFrame extends JFrame {

    public RoleSelectionFrame() {
        setTitle("IKEA Store - Select Role");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Theme.CREAM);

        JLabel label = new JLabel("Welcome to IKEA Store", SwingConstants.CENTER);
        label.setFont(Theme.getHeaderFont(Font.BOLD, 24));
        label.setForeground(Theme.CHARCOAL);
        panel.add(label);

        JButton customerBtn = createStyledButton("Customer");
        customerBtn.addActionListener(e -> {
            new LoginFrame("CUSTOMER").setVisible(true);
            dispose();
        });
        panel.add(customerBtn);

        JButton adminBtn = createStyledButton("Admin");
        adminBtn.addActionListener(e -> {
            new LoginFrame("ADMIN").setVisible(true);
            dispose();
        });
        panel.add(adminBtn);

        add(panel, BorderLayout.CENTER);
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.getBodyFont(Font.BOLD, 16));
        btn.setBackground(Theme.DARK_BROWN);
        btn.setForeground(Theme.CREAM);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RoleSelectionFrame().setVisible(true));
    }
}
