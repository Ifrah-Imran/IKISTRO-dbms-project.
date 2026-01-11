package ui.admin;

import backend.DiscountService;
import models.DiscountCode;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;

/**
 * Admin panel for managing discount codes
 * This can be integrated into the AdminPanel for full discount management
 */
public class DiscountManagementPanel extends JPanel {
    private DiscountService discountService;
    private JPanel codesPanel;
    private JTextArea detailsArea;

    public DiscountManagementPanel() {
        this.discountService = new DiscountService();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top: Create New Code Section
        JPanel createPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        createPanel.setBorder(BorderFactory.createTitledBorder("Create New Discount Code"));

        JTextField codeField = new JTextField();
        createPanel.add(new JLabel("Code:"));
        createPanel.add(codeField);

        JSpinner percentageSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        createPanel.add(new JLabel("Discount %:"));
        createPanel.add(percentageSpinner);

        JSpinner daysSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 365, 1));
        createPanel.add(new JLabel("Valid Days:"));
        createPanel.add(daysSpinner);

        JSpinner limitSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 10000, 1));
        createPanel.add(new JLabel("Usage Limit (0=unlimited):"));
        createPanel.add(limitSpinner);

        JButton createBtn = new JButton("Create Code");
        createBtn.addActionListener(e -> {
            String code = codeField.getText().trim();
            if (code.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a code", "Validation Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int percentage = (Integer) percentageSpinner.getValue();
            int days = (Integer) daysSpinner.getValue();
            int limit = (Integer) limitSpinner.getValue();

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, days);
            Date expiryDate = cal.getTime();

            if (discountService.createDiscountCode(code, percentage, expiryDate, limit)) {
                JOptionPane.showMessageDialog(this,
                        "Discount code '" + code + "' created successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                codeField.setText("");
                loadCodes();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to create code. It may already exist.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        createPanel.add(createBtn);
        add(createPanel, BorderLayout.NORTH);

        // Middle: List of Active Codes
        JPanel middlePanel = new JPanel(new BorderLayout());

        // Header for list with Refresh button
        JPanel listHeader = new JPanel(new BorderLayout());
        listHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        JLabel listTitle = new JLabel("Active Discount Codes");
        listTitle.setFont(new Font("Arial", Font.BOLD, 12));
        listHeader.add(listTitle, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh List");
        refreshBtn.addActionListener(e -> loadCodes());
        listHeader.add(refreshBtn, BorderLayout.EAST);

        middlePanel.add(listHeader, BorderLayout.NORTH);
        middlePanel.setBorder(BorderFactory.createEtchedBorder()); // Removed titled border to use custom header

        codesPanel = new JPanel();
        codesPanel.setLayout(new BoxLayout(codesPanel, BoxLayout.Y_AXIS));
        JScrollPane codesScroll = new JScrollPane(codesPanel);
        middlePanel.add(codesScroll, BorderLayout.CENTER);

        add(middlePanel, BorderLayout.CENTER);

        // Bottom: Details and Actions
        detailsArea = new JTextArea(5, 40);
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setBorder(BorderFactory.createTitledBorder("Code Details"));

        add(detailsScroll, BorderLayout.SOUTH);

        loadCodes();
    }

    private void loadCodes() {
        codesPanel.removeAll();
        java.util.List<DiscountCode> codes = discountService.getAllActiveCodes();

        if (codes.isEmpty()) {
            codesPanel.add(new JLabel("No active discount codes"));
        } else {
            for (DiscountCode code : codes) {
                JPanel codeCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
                codeCard.setBorder(BorderFactory.createEtchedBorder());

                JLabel codeLabel = new JLabel(code.getCode());
                codeLabel.setFont(new Font("Arial", Font.BOLD, 14));
                codeLabel.setPreferredSize(new Dimension(120, 25));
                codeCard.add(codeLabel);

                JLabel percentageLabel = new JLabel(code.getDiscountPercentage() + "% OFF");
                percentageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
                codeCard.add(percentageLabel);

                JLabel statusLabel = new JLabel(code.isActive() ? "[ACTIVE]" : "[INACTIVE]");
                statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
                statusLabel.setForeground(code.isActive() ? new Color(0, 150, 0) : Color.GRAY);
                codeCard.add(statusLabel);

                JLabel usageLabel = new JLabel("Uses: " + code.getTimesUsed() + "/"
                        + (code.getUsageLimit() == 0 ? "∞" : code.getUsageLimit()));
                codeCard.add(usageLabel);

                JButton detailsBtn = new JButton("Details");
                detailsBtn.addActionListener(e -> showCodeDetails(code));
                codeCard.add(detailsBtn);

                JButton deleteBtn = new JButton("Delete");
                deleteBtn.setBackground(new Color(255, 200, 200));
                deleteBtn.addActionListener(e -> {
                    int confirm = JOptionPane.showConfirmDialog(DiscountManagementPanel.this,
                            "Are you sure you want to PERMANENTLY DELETE discount code '" + code.getCode()
                                    + "'?\nThis cannot be undone.",
                            "Confirm Deletion",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);

                    if (confirm == JOptionPane.YES_OPTION) {
                        if (discountService.deleteDiscountCode(code.getDiscountId())) {
                            JOptionPane.showMessageDialog(DiscountManagementPanel.this,
                                    "Code deleted successfully",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE);
                            loadCodes();
                            detailsArea.setText(""); // Clear details
                        } else {
                            JOptionPane.showMessageDialog(DiscountManagementPanel.this,
                                    "Failed to delete code.",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                codeCard.add(deleteBtn);

                codesPanel.add(codeCard);
            }
        }

        codesPanel.revalidate();
        codesPanel.repaint();
    }

    private void showCodeDetails(DiscountCode code) {
        long daysRemaining = (code.getExpiryDate().getTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
        String limitInfo = code.getUsageLimit() == 0 ? "Unlimited" : code.getUsageLimit() + " uses";
        String usedPercentage = code.getUsageLimit() == 0 ? "N/A"
                : String.format("%.1f%%", (code.getTimesUsed() * 100.0 / code.getUsageLimit()));

        String details = String.format(
                "Code: %s\n" +
                        "Discount: %d%%\n" +
                        "Status: %s\n" +
                        "Expires: %d days remaining\n" +
                        "Usage Limit: %s\n" +
                        "Times Used: %d\n" +
                        "Usage: %s\n" +
                        "Created: %s",
                code.getCode(),
                code.getDiscountPercentage(),
                code.isActive() ? "Active" : "Inactive",
                daysRemaining,
                limitInfo,
                code.getTimesUsed(),
                usedPercentage,
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(code.getExpiryDate()));

        detailsArea.setText(details);
    }
}
