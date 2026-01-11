package ui;

import java.awt.*;
import javax.swing.*;

public class HelpPanel extends JPanel {
    public HelpPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel title = new JLabel("Need Help?");
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel message = new JLabel("<html><center>We are here to assist you with your shopping experience.<br>" +
                "If you have any questions or issues, please reach out to us.</center></html>");
        message.setFont(new Font("Arial", Font.PLAIN, 18));
        message.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel contact = new JLabel("<html><center><b>Contact Us:</b><br>" +
                "Phone: +1-800-IKISTRO<br>" +
                "Email: support@ikistro.com<br>" +
                "Address: 123 Furniture Lane, Design City</center></html>");
        contact.setFont(new Font("Arial", Font.PLAIN, 16));
        contact.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentPanel.add(title);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        contentPanel.add(message);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        contentPanel.add(contact);

        add(contentPanel, BorderLayout.CENTER);
    }
}
